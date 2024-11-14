package edu.ndsu.cs.estimate.pages.events;

import edu.ndsu.cs.estimate.cayenne.persistent.Event;
import edu.ndsu.cs.estimate.entities.interfaces.UserAccount;
import edu.ndsu.cs.estimate.services.database.interfaces.EventDatabaseService;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Select;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.tynamo.security.services.SecurityService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import edu.ndsu.cs.estimate.services.database.interfaces.UserAccountDatabaseService;

public class Add {

    @Inject
    private SecurityService securityService;
    
    @Inject
    private EventDatabaseService eventDatabaseService;

    @Inject
    private UserAccountDatabaseService userAccountDatabaseService;
    
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

    @Property
    private List<String> uniqueCategories;

    @Component
    private Form eventForm;
    
    @Component(parameters = {"value=eventDateString", "type=text"})
    private TextField eventDate;

    @Component(parameters = {"value=category", "model=uniqueCategories"})
    private Select categorySelect;

    @Inject
    private AlertManager alertManager;

    void setupRender() {
        String principal = securityService.getSubject().getPrincipal().toString();
        userAccount = userAccountDatabaseService.getUserAccount(principal);

        uniqueCategories = eventDatabaseService.findAllCategories();
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
            }
            else {
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
