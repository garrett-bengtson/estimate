package edu.ndsu.cs.estimate.pages.reports;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.annotations.SetupRender;

import edu.ndsu.cs.estimate.cayenne.persistent.Report;
import edu.ndsu.cs.estimate.services.database.interfaces.ReportDatabaseService;

public class ViewReport {

    @Property
    @ActivationRequestParameter
    private int reportId;

    @Property
    private Report report;

    @Inject
    private ReportDatabaseService reportDBS;

    @Inject
    private Request request;

    @SetupRender
    void setupRender() {
        if (request.getParameter("report") != null) {
            reportId = Integer.parseInt(request.getParameter("report"));
        }
        report = reportDBS.findReportById(reportId);
    }
}
