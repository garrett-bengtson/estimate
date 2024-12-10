package edu.ndsu.cs.estimate.services.database.implementations;

import edu.ndsu.cs.estimate.cayenne.persistent.Report;
import edu.ndsu.cs.estimate.services.database.interfaces.ReportDatabaseService;
import edu.ndsu.cs.estimate.services.database.interfaces.CayenneService;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.SelectById;

import java.util.List;

public class CayenneReportDatabaseService implements ReportDatabaseService {

    private final CayenneService cayenneService;

    public CayenneReportDatabaseService(CayenneService cayenneService) {
        this.cayenneService = cayenneService;
    }

    @Override
    public void saveReport(Report report) {
        ObjectContext context = cayenneService.newContext();
        context.registerNewObject(report);
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
    public void deleteReport(Report report) {
        ObjectContext context = cayenneService.newContext();
        context.deleteObject(report);
        context.commitChanges();
    }
}
