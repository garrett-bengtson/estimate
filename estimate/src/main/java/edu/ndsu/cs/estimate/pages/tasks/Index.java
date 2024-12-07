package edu.ndsu.cs.estimate.pages.tasks;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
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

import edu.ndsu.cs.estimate.cayenne.persistent.Hours;
import edu.ndsu.cs.estimate.cayenne.persistent.Task;
import edu.ndsu.cs.estimate.entities.interfaces.UserAccount;
import edu.ndsu.cs.estimate.services.database.interfaces.UserAccountDatabaseService;
import edu.ndsu.cs.estimate.services.tasks.CayenneTaskFactory;
import edu.ndsu.cs.estimate.services.tasks.TaskDatabaseService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Index {

    @Inject
    private AlertManager alertManager;

    @Property
    @Persist
    private String dateRange;

    @InjectComponent
    private Form dateForm;

    @Component(id = "dateRange", parameters = {"value=dateRange"})
    private TextField dateRangeField;

    @Persist
    @Property
    private int hours;

    @Inject
    private TaskDatabaseService taskDBS;

    @Persist
    @Property
    private List<? extends Task> tasks;

    @Property
    private Task task;

    @InjectComponent
    private Form addHourForm;

    @InjectComponent
    private Form add1HourForm;

    @InjectComponent
    private Form add2HourForm;

    @InjectComponent
    private Form add3HourForm;

    @InjectComponent
    private Form add5HourForm;

    @InjectComponent
    private Form add10HourForm;

    @Inject
    private SecurityService securityService;

    @Inject
    private UserAccountDatabaseService userAccountDatabaseService;

    @Property
    @Persist
    private UserAccount userAccount;

    @SessionAttribute
    @Property
    private Boolean noTasks = null;

    @SessionAttribute
    @Property
    private Boolean makeExamples = null;

    @SetupRender
    void setupRender() {
        if (noTasks == null) {
            noTasks = true;
        }
        if (makeExamples == null) {
            makeExamples = false;
        }
        if (securityService.getSubject().getPrincipal() == null) {
            return; // User needs to log in
        }
        // Alerts if tasks are due within the next week
        List<String> alerts = taskDBS.getDeadlineNotifications();
        if (!alerts.isEmpty()) {
            StringBuilder alertString = new StringBuilder();
            for (String alert : alerts) {
                alertString.append(alert).append("\n");
            }
            alertManager.alert(Duration.SINGLE, Severity.INFO, alertString.toString());
        }

        String principal = securityService.getSubject().getPrincipal().toString();
        userAccount = userAccountDatabaseService.getUserAccount(principal);
        getTasks();
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
        if (makeExamples) {
            makeExampleTasks();
        }
        tasks = taskDBS.listAllTasks(start, end, userAccount);
        noTasks = tasks.isEmpty();
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

    void onSuccessFromDateForm() {
        getTasks();
    }

    void onDelete(int pk) {
        taskDBS.deleteTask(pk);
        getTasks();
    }

    boolean isLoggedHoursSumLessThanOneMillion(Task task, int input) {
        return task.getTimeTaken() + input <= 1_000_000;
    }

    void onSubmitFromAddHourForm(int pk, int hoursToAdd) {
        Task tempTask = taskDBS.getTask(pk);

        if (isLoggedHoursSumLessThanOneMillion(tempTask, hoursToAdd)) {
            int newTotalTime = tempTask.getTimeTaken() + hoursToAdd;
            tempTask.setTimeTaken(newTotalTime);
            taskDBS.updateTask(tempTask);

            Hours newHours = tempTask.getObjectContext().newObject(Hours.class);
            newHours.setTimestamp(new Date());
            newHours.setHoursLogged(hoursToAdd);

            tempTask.addToHours(newHours);
            newHours.getObjectContext().commitChanges();

            getTasks();
        } else {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Total hours logged can't be more than 1,000,000");
        }
    }

    @OnEvent(component = "addHourForm")
    void addHourForm(int pk) {
        // Retrieve the task by its primary key (pk)
        Task tempTask = taskDBS.getTask(pk);

        // Ensure the input hours are valid
        if (hours < -1_000_000 || hours > 1_000_000) {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Hours must be between -1,000,000 and 1,000,000.");
            return;
        }

        // Delegate to the existing `onSubmitFromAddHourForm` method
        onSubmitFromAddHourForm(pk, hours);
    }



    void onSubmitFromAdd1HourForm(int pk) {
        onSubmitFromAddHourForm(pk, 1);
    }

    void onSubmitFromAdd2HourForm(int pk) {
        onSubmitFromAddHourForm(pk, 2);
    }

    void onSubmitFromAdd3HourForm(int pk) {
        onSubmitFromAddHourForm(pk, 3);
    }

    void onSubmitFromAdd5HourForm(int pk) {
        onSubmitFromAddHourForm(pk, 5);
    }

    void onSubmitFromAdd10HourForm(int pk) {
        onSubmitFromAddHourForm(pk, 10);
    }

    void makeExampleTasks() {
        makeExamples = false;
        for (int i = 0; i < 5; i++) {
            CayenneTaskFactory.generateInstance(taskDBS.getCayenneService().newContext(), userAccount);
        }
        getTasks();
    }

    @OnEvent(component = "complete")
    Object onClickCloseComplete(int pk) {
        Task tempTask = taskDBS.getTask(pk);
        tempTask.setCompleted(true);
        taskDBS.updateTask(tempTask);
        return Index.class;
    }

    @OnEvent(component = "drop")
    Object onClickCloseDropped(int pk) {
        Task tempTask = taskDBS.getTask(pk);
        tempTask.setDropped(true);
        taskDBS.updateTask(tempTask);
        return Index.class;
    }

    @OnEvent(component = "willNotComplete")
    Object onClickCloseWillNotComplete(int pk) {
        Task tempTask = taskDBS.getTask(pk);
        tempTask.setWillNotComplete(true);
        taskDBS.updateTask(tempTask);
        return Index.class;
    }

    @OnEvent(component = "cannotComplete")
    Object onClickCloseCannotComplete(int pk) {
        Task tempTask = taskDBS.getTask(pk);
        tempTask.setCannotComplete(true);
        taskDBS.updateTask(tempTask);
        return Index.class;
    }
}
