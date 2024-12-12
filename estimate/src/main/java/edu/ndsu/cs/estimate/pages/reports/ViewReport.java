package edu.ndsu.cs.estimate.pages.reports;
import java.util.List;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.alerts.AlertManager;
import edu.ndsu.cs.estimate.cayenne.persistent.Report;
import edu.ndsu.cs.estimate.cayenne.persistent.Task;
import edu.ndsu.cs.estimate.services.database.interfaces.ReportDatabaseService;
import org.apache.tapestry5.annotations.PageActivationContext;

public class ViewReport {
    @Property
    private Report report;

    @Property
    private List<Task> tasks;

    @Property
    private Task task;

    @Property
    private int totalTasks;

    @Property
    private double averageDaysToCompletion;

    @Inject
    private ReportDatabaseService reportDBS;

    @Inject
    private AlertManager alertManager;

    @PageActivationContext
    private int reportId;

    @SetupRender
    void setupRender() {
        // Fetch the report using reportId
        report = reportDBS.findReportById(reportId);

        if (report != null) {
            tasks = report.getTasks();
            totalTasks = tasks.size();
        } else {
            alertManager.error("Report not found.");
        }
    }

    



}

