package edu.ndsu.cs.estimate.pages.events;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.tynamo.security.services.SecurityService;

import edu.ndsu.cs.estimate.cayenne.persistent.Event;
import edu.ndsu.cs.estimate.services.database.interfaces.EventDatabaseService;

public class Edit {

    @Inject
    private EventDatabaseService eventDatabaseService;

    @Property
    private Event event;

    @Property
    @PageActivationContext
    private int eventId;

    @Component
    private Form eventForm;

    @Component(id = "name")
    private TextField eventName;

    @Component(id = "category")
    private TextField eventCategory;

    @Component(id = "description")
    private TextField eventDescription;

    @Component(id = "eventDate")
    private TextField eventDateField;

    @Property
    private String eventDateString;

    void onActivate(int eventId) {
        this.eventId = eventId;
        event = eventDatabaseService.findEventById(eventId);
        if (event != null) {
            eventDateString = new SimpleDateFormat("MM/dd/yyyy").format(event.getEventDate());
        }
    }

    void onValidateFromEventForm() {
        if (eventForm.isValid()) {
            Date eventDate = parseDate(eventDateString);
            if (eventDate == null) {
                eventForm.recordError(eventDateField, "Invalid date format for event date. Please use MM/dd/yyyy.");
            } else {
                event.setEventDate(eventDate);
                eventDatabaseService.updateEvent(eventId, event.getName(), event.getDescription(), event.getCategory(), eventDate);
            }
        }
    }

    Object onSuccessFromEventForm() {
        return Index.class;
    }

    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            dateFormat.setLenient(false);
            return dateFormat.parse(dateString);
        } catch (Exception e) {
            return null;
        }
        
    }
    

    
    @Property
    private boolean isAdmin;

    @Inject
    private SecurityService securityService;

    void setupRender() {
        // Check if the current user has the "admin" role
        isAdmin = securityService.hasRole("admin");
    }
    

    	
    
}
