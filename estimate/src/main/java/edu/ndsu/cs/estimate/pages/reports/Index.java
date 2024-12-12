package edu.ndsu.cs.estimate.pages.reports;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.tynamo.security.services.SecurityService;

import edu.ndsu.cs.estimate.cayenne.persistent.Report;
import edu.ndsu.cs.estimate.cayenne.persistent.Task;
import edu.ndsu.cs.estimate.entities.interfaces.UserAccount;
import edu.ndsu.cs.estimate.services.database.interfaces.ReportDatabaseService;
import edu.ndsu.cs.estimate.services.database.interfaces.UserAccountDatabaseService;
import edu.ndsu.cs.estimate.services.tasks.TaskDatabaseService;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.alerts.AlertManager;

public class Index {

    @Inject 
    private AlertManager alertManager;
    
    @SessionAttribute
    @Property
    private Boolean noTasks = null;
    
    @Property
    @Persist
    private String dateRange;

    @Component(id = "dateRange", parameters = {"value=dateRange"})
    private TextField dateRangeField;

    @Component(id = "newCategoryField", parameters = {"value=newCategory"})
    private TextField newCategoryField;

    @InjectComponent
    private Form dateForm;

    @InjectComponent
    private Form categoryForm;

    @Inject
    private TaskDatabaseService taskDBS;

    @Inject
    private ReportDatabaseService reportDBS;

    @Persist
    @Property
    private List<? extends Task> tasks;

    @Property
    private List<Report> reports;

    @Property
    private Report report;

    @Property
    private Task task;

    @Inject
    private SecurityService securityService;

    @Inject
    private UserAccountDatabaseService userAccountDatabaseService;
    
    @Property
    private UserAccount userAccount;

    @Property
    private List<String> categories = new ArrayList<>();

    @Component(id = "categorySelect", parameters = {"value=category", "model=categoryModel", "blankOption=never"})
    private org.apache.tapestry5.corelib.components.Select categorySelect;

    @Persist
    @Property
    private String category;

    @Persist
    @Property
    private String newCategory;

    @Persist
    @Property
    private String hiddenCategory; // Ensure this property is declared

    @Property
    private SelectModel categoryModel;

    @SetupRender
    void setupRender() {
        if (securityService.getSubject().getPrincipal() == null) {
            return;  // User needs to log in
        }
        String principal = securityService.getSubject().getPrincipal().toString();
        userAccount = userAccountDatabaseService.getUserAccount(principal);
        getTasks();
        reports = reportDBS.findAllReports();  // Fetch reports from the database
        categories = reports.stream()
                            .map(Report::getCategory)
                            .distinct()
                            .collect(Collectors.toList());
        categories.add("All Categories");
        categoryModel = createCategoryModel();
    }

    private SelectModel createCategoryModel() {
        List<OptionModel> options = new ArrayList<>();
        for (String cat : categories) {
            options.add(new OptionModelImpl(cat, cat));
        }
        return new SelectModelImpl(null, options);
    }

    void onValidateFromDateForm() {
        if (dateRange != null && !dateRange.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            dateFormat.setLenient(false);
            String[] dates = dateRange.split(" - ");
            if (dates.length == 2) {
                try {
                    dateFormat.parse(dates[0]);
                    dateFormat.parse(dates[1]);
                } catch (ParseException e) {
                    dateForm.recordError("The date range format is invalid. Please use MM/dd/yyyy - MM/dd/yyyy format.");
                }
            } else {
                dateForm.recordError("The date range format is invalid. Please use MM/dd/yyyy - MM/dd/yyyy format.");
            }
        }
    }

    private void getTasks() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        dateFormat.setLenient(false);
        Date start = new Date(0); // 1/1/1970
        Date end = new Date(2145916800000L); // 1/1/2038

        if (dateRange != null && !dateRange.isEmpty()) {
            String[] dates = dateRange.split(" - ");
            try {
                start = dateFormat.parse(dates[0]);
                end = (dates.length == 2) ? dateFormat.parse(dates[1]) : start;
            } catch (ParseException e) {
                dateForm.recordError("The date range format is invalid.");
                return;
            }
        }

        tasks = taskDBS.listAllTasksAll(start, end, userAccount);
        noTasks = tasks.isEmpty();
    }

    @OnEvent(value = "action", component = "deleteLink")
    void deleteReport(Integer reportPK) {
        try {
            reportDBS.deleteReport(reportPK);
            alertManager.success("Report deleted successfully.");
        } catch (IllegalArgumentException e) {
            alertManager.error(e.getMessage());
        }

        // Refresh the list of reports
        reports = reportDBS.findAllReports();
    }

    @OnEvent(value = "submit", component = "categoryForm")
    void onCategoryFormSubmit() {
        if (category == null || category.isEmpty()) {
            categoryForm.recordError("Category cannot be empty.");
        } else {
            generateReport();
        }
    }

    void generateReport() {
        if (tasks == null || tasks.isEmpty()) {
            alertManager.error("Tasks are null or empty. Cannot save report.");
            return;
        }

        // Ensure category is selected
        if (category == null || category.isEmpty()) {
            alertManager.error("Category cannot be empty.");
            return;
        }

        // Set actual values for the report
        Double daysSinceTaskStart = calculateDaysSinceTaskStart(tasks.get(0));
        Double estimatedDaysToCompletion = calculateEstimatedDaysToCompletion(tasks);
        Double netLossGain = calculateNetLossGain(tasks);
        String taskStatus = tasks.get(0).getName();

        // Save report with all tasks
        reportDBS.saveReport(category, daysSinceTaskStart, estimatedDaysToCompletion, netLossGain, taskStatus, tasks);
        alertManager.success("Report saved successfully.");

        // Refresh the list of reports
        reports = reportDBS.findAllReports();
    }
      
    
    private Double calculateDaysSinceTaskStart(Task task) {
        Date startDate = task.getStartDate();
        Date currentDate = new Date();
        long differenceInMillies = Math.abs(currentDate.getTime() - startDate.getTime());
        long differenceInDays = differenceInMillies / (24 * 60 * 60 * 1000);
        return (double) differenceInDays;
    }

    private Double calculateEstimatedDaysToCompletion(List<? extends Task> tasks) {
        return tasks.stream()
            .filter(task -> task.getEstEndDate() != null)
            .mapToDouble(task -> {
                long differenceInMillies = task.getEstEndDate().getTime() - new Date().getTime();
                long differenceInDays = differenceInMillies / (24 * 60 * 60 * 1000);
                return (double) differenceInDays;
            })
            .average()
            .orElse(0.0);
    }

    private Double calculateNetLossGain(List<? extends Task> tasks) {
        return tasks.stream()
            .mapToDouble(Task::getTimeTaken) // This assumes 'timeTaken' represents a net value
            .sum();
    }
}
