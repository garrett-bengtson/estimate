package edu.ndsu.cs.estimate.services.database.interfaces;

import edu.ndsu.cs.estimate.cayenne.persistent.Report;
import edu.ndsu.cs.estimate.cayenne.persistent.Task;

import java.util.List;

public interface ReportDatabaseService {

    void saveReport(String category, Double daysSinceTaskStart, Double estimatedDaysToCompletion, Double netLossGain, String taskStatus, List<? extends Task> task);

    List<Report> findAllReports();

    Report findReportById(int id);

    void updateReport(Report report);

    void deleteReport(int reportPK);
}
