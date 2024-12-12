package edu.ndsu.cs.estimate.services.database.implementations;

import edu.ndsu.cs.estimate.cayenne.persistent.Event;
import edu.ndsu.cs.estimate.cayenne.persistent.Report;
import edu.ndsu.cs.estimate.cayenne.persistent.Task;
import edu.ndsu.cs.estimate.services.database.interfaces.ReportDatabaseService;
import edu.ndsu.cs.estimate.services.database.interfaces.CayenneService;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.property.ListProperty;
import org.apache.cayenne.exp.property.NumericProperty;
import org.apache.cayenne.exp.property.StringProperty;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.SelectById;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.ObjectSelect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CayenneReportDatabaseService implements ReportDatabaseService {

    private final CayenneService cayenneService;

    public CayenneReportDatabaseService(CayenneService cayenneService) {
        this.cayenneService = cayenneService;
    }

    @Override
    public void saveReport(String category, Double daysSinceTaskStart, Double estimatedDaysToCompletion, Double netLossGain, String taskStatus, List<? extends Task> tasks) {
        ObjectContext context = cayenneService.newContext();

        // Create a new report within the context
        Report newReport = context.newObject(Report.class);
        newReport.setCategory(category);
        newReport.setDaysSinceTaskStart(daysSinceTaskStart);
        newReport.setEstimatedDaysToCompletion(estimatedDaysToCompletion);
        newReport.setNetLossGain(netLossGain);
        newReport.setTaskStatus(taskStatus);

     // Add all tasks to the new report 
        for(Task task : tasks) {
        	Task localTask = ObjectSelect.query(Task.class)
        			.where(ExpressionFactory.matchDbExp(Task.TASK_ID_PK_COLUMN, task.getObjectId().getIdSnapshot().get(Task.TASK_ID_PK_COLUMN))) 
        			.selectOne(context);
        newReport.addToTasks(localTask); }
        
        // Commit changes
        context.commitChanges();
    }


    @Override
    public List<Report> findAllReports() {
        ObjectContext context = cayenneService.newContext();
        return ObjectSelect.query(Report.class).select(context);
    }

    @Override
    public Report findReportById(int id) {
        ObjectContext context = cayenneService.newContext();
        return SelectById.query(Report.class, id).selectOne(context);
    }

    @Override
    public void updateReport(Report report) {
        ObjectContext context = cayenneService.newContext();
        context.commitChanges(); // Commit the changes to the context
    }

    @Override
    public void deleteReport(int reportPK) {
        ObjectContext context = cayenneService.newContext();
        Report reportToDelete = ObjectSelect.query(Report.class)
                                            .where(ExpressionFactory.matchDbExp("reportPK", reportPK))
                                            .selectOne(context);

        if (reportToDelete != null) {
            // Create a list of tasks to delete
            List<Task> tasksToDelete = new ArrayList<>(reportToDelete.getTasks());
            
            for (Task task : tasksToDelete) {
                task.removeFromReports(reportToDelete);  // Remove the relationship from task side
                reportToDelete.removeFromTasks(task);    // Remove the relationship from report side
                context.deleteObject(task);
            }

            // Commit changes after removing tasks
            context.commitChanges();

            // Delete the Report object
            context.deleteObject(reportToDelete);
            context.commitChanges();
        } else {
            throw new IllegalArgumentException("Report not found with PK: " + reportPK);
        }
    }









}
