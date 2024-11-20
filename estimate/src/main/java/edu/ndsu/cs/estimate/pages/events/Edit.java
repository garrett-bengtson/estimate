package edu.ndsu.cs.estimate.pages.events;

import edu.ndsu.cs.estimate.cayenne.persistent.Event;
import edu.ndsu.cs.estimate.entities.interfaces.UserAccount;
import edu.ndsu.cs.estimate.services.database.interfaces.EventDatabaseService;
import edu.ndsu.cs.estimate.services.database.interfaces.UserAccountDatabaseService;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.tynamo.security.services.SecurityService;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.PageLoaded;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Select;
import org.apache.tapestry5.corelib.components.TextField;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.annotations.Persist;
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
    
    @Inject
    private UserAccountDatabaseService userAccountDatabaseService;
    
    @Property
    @Persist
    private UserAccount userAccount;

    @Property
    private Event event;
    
    @Property
    private Boolean approved;
    
    @Component
    private Form eventForm;

    @Property
    @PageActivationContext
    private int eventId;

    @Property
    private String name;

    @Property
    private String category;

    @Property
    private String description;

    @Component(parameters = {"value=eventDateString", "type=text"})
    private TextField eventDate;

    @Property
    private String eventDateString;
    
    @Property
    private List<String> uniqueCategories;
    
    @Component(parameters = {"value=category", "model=uniqueCategories"})
    private Select categorySelect;
    
    @Property
    private boolean isAdmin;

    @Inject
    private SecurityService securityService;

    void setupRender() {
        // Check if the current user has the "admin" role
        isAdmin = securityService.hasRole("admin");
        
        String principal = securityService.getSubject().getPrincipal().toString();
        userAccount = userAccountDatabaseService.getUserAccount(principal);

        uniqueCategories = eventDatabaseService.findAllCategories();
    }


    void onActivate(int eventId) {
        this.eventId = eventId;
        event = eventDatabaseService.findEventById(eventId);
        if (event != null) {
            eventDateString = new SimpleDateFormat("MM/dd/yyyy").format(event.getEventDate());
        }
    }

    void onValidateFromEventForm() {
    	if (eventDateString == null) {
    		eventForm.recordError("Date must be included in event creation.");
    	}
    	else {
    		Date eventDate = parseDate(eventDateString);
        	if (eventDate == null) {
        		eventForm.recordError("Invalid date format for event data. Please use MM/dd/yyyy.");
        	}
    		if (!eventForm.getHasErrors())	 {
    			//If the user leaves a field blank, assume
    			//they want to leave that field as it was.
    			if (name == null) {
    	    		name = event.getName();
    	    	}
    	    	if (description == null) {
    	    		description = event.getDescription();
    	    	}
    	    	if (category == null || category.length() == 0) {
    	    		category = event.getCategory();
    	    	}
                eventDatabaseService.updateEvent(eventId, name, description, category, eventDate, approved);

    		}
    	}

    }

    Object onSuccessFromEventForm() {
        return Index.class;
    }

    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            return dateFormat.parse(dateString);
        } catch (Exception e) {
            return null;
        }
        
    }	
}
