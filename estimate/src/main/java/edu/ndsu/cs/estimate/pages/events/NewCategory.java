package edu.ndsu.cs.estimate.pages.events;

import edu.ndsu.cs.estimate.cayenne.persistent.Event;
import edu.ndsu.cs.estimate.services.database.interfaces.EventDatabaseService;
import edu.ndsu.cs.estimate.services.database.interfaces.UserAccountDatabaseService;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.tynamo.security.services.SecurityService;
import edu.ndsu.cs.estimate.entities.interfaces.UserAccount;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.services.PageRenderLinkSource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewCategory {

    @Inject
    private UserAccountDatabaseService userAccountDatabaseService;

    @Inject
    private SecurityService securityService;

    @Inject
    private EventDatabaseService eventDatabaseService;

    @Property
    @Persist
    private UserAccount userAccount;

    @Property
    private String name;

    @Property
    private String description;

    @Property
    private String category;

    @Property
    private String eventDateString;

    @Component
    private Form eventForm;

    @Component(parameters = {"value=eventDateString", "type=text"})
    private TextField eventDate;

    @Component(parameters = {"value=category"})
    private TextField categoryTextField;

    @Inject
    private AlertManager alertManager;

    @Inject
    private Response response;

    @Inject
    private PageRenderLinkSource pageRenderLinkSource;

    void setupRender() {
        String principal = securityService.getSubject().getPrincipal().toString();
        userAccount = userAccountDatabaseService.getUserAccount(principal);

        // Check if the user is an admin
        if (!userAccount.isAdmin()) {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Access denied. Only admins can create new categories.");
            // Redirect non-admin users to the Index page
            Link link = pageRenderLinkSource.createPageRenderLink(Index.class);
            try {
                response.sendRedirect(link.toURI());
            } catch (IOException e) {
                throw new RuntimeException("Redirection failed", e);
            }
        }
    }

    void onValidateFromEventForm() {
        if (name == null) {
            eventForm.recordError("Name must be included in event creation.");
        }
        if (description == null) {
            eventForm.recordError("Description must be included in event creation.");
        }
        if (category == null || category.length() == 0) {
            eventForm.recordError("Category must be included in event creation.");
        }
        if (eventDateString == null) {
            eventForm.recordError("Date must be included in event creation.");
        }
        if(!eventForm.getHasErrors()){
            Date eventDate = parseDate(eventDateString);
            if (eventDate == null) {
                eventForm.recordError("Invalid date format for event date. Please use MM/dd/yyyy.");
            } else {
                Event event = eventDatabaseService.createEvent(name, description, category, eventDate);
                event.setEventDate(eventDate);
            }
        }
    }

    Object onSuccessFromEventForm() {
        alertManager.alert(Duration.SINGLE, Severity.SUCCESS, "Event added successfully.");
        return Index.class;
    }

    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }
}
