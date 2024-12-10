package edu.ndsu.cs.estimate.services.database.interfaces;

import edu.ndsu.cs.estimate.cayenne.persistent.Report;
import java.util.List;

public interface ReportDatabaseService {

    void saveReport(Report report);

    List<Report> findAllReports();

    Report findReportById(int id);

    void updateReport(Report report);

    void deleteReport(Report report);
}
