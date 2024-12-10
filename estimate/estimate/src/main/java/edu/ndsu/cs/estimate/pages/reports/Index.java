package edu.ndsu.cs.estimate.pages.reports;

import java.util.Date;
import java.util.List;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.tynamo.security.services.SecurityService;

import edu.ndsu.cs.estimate.cayenne.persistent.Report;
import edu.ndsu.cs.estimate.cayenne.persistent.Task;
import edu.ndsu.cs.estimate.entities.interfaces.UserAccount;
import edu.ndsu.cs.estimate.services.database.interfaces.CayenneService;
import edu.ndsu.cs.estimate.services.database.interfaces.ReportDatabaseService;
import edu.ndsu.cs.estimate.services.database.interfaces.UserAccountDatabaseService;
import edu.ndsu.cs.estimate.services.tasks.TaskDatabaseService;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.exp.ExpressionFactory;
import org.slf4j.Logger;

public class Index {

    @Inject
    private TaskDatabaseService taskDBS;

    @Inject
    private ReportDatabaseService reportDBS;

    @Inject
    private CayenneService cayenneService;

    @Inject
    private Logger logger;

    @Property
    private List<? extends Task> tasks;

    @Property
    private List<Report> reports;

    @Property
    private Report report;

    @InjectComponent
    private Form reportForm;

    @Property
    private Task task;

    @Inject
    private SecurityService securityService;

    @Inject
    private UserAccountDatabaseService userAccountDatabaseService;

    @Property
    private UserAccount userAccount;

    @SetupRender
    void setupRender() {
        if (securityService.getSubject().getPrincipal() == null) {
            return;  // User needs to log in
        }
        String principal = securityService.getSubject().getPrincipal().toString();
        userAccount = userAccountDatabaseService.getUserAccount(principal);
        getTasks();
        reports = reportDBS.findAllReports();  // Fetch reports from the database

        // Debug logging
        logger.info("Fetched reports: " + reports.size());
        for (Report r : reports) {
            logger.info("Report category: " + r.getCategory());
        }
    }

    private void getTasks() {
        tasks = taskDBS.listAllTasks(new Date(0), new Date(2145916800000L), userAccount);  // Fetch all tasks
    }

    void onSuccessFromReportForm() {
        // Get the current ObjectContext
        ObjectContext context = cayenneService.newContext();

        // Create a new report within the same ObjectContext
        Report newReport = context.newObject(Report.class);
        newReport.setCategory("Generated Report");  // Set other necessary fields as appropriate
        getTasks();

        // Ensure tasks are within the same DataContext
        for (Task t : tasks) {
            Task localTask = ObjectSelect.query(Task.class)
                             .where(ExpressionFactory.matchDbExp(Task.TASK_ID_PK_COLUMN, t.getObjectId()))
                             .selectOne(context);
            newReport.addToTasks(localTask);
        }

        // Save the new report to the database
        context.commitChanges();

        // Debug logging
        logger.info("New report created with category: " + newReport.getCategory());
        logger.info("Tasks associated with report: " + newReport.getTasks().size());

        // Refresh the list of reports
        reports = reportDBS.findAllReports();
    }
}
