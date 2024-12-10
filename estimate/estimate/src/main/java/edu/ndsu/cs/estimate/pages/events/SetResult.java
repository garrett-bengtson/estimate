package edu.ndsu.cs.estimate.pages.events;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.tynamo.security.services.SecurityService;

import java.io.IOException;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;

import edu.ndsu.cs.estimate.cayenne.persistent.Event;
import edu.ndsu.cs.estimate.entities.interfaces.UserAccount;
import edu.ndsu.cs.estimate.services.database.interfaces.EventDatabaseService;
import edu.ndsu.cs.estimate.services.database.interfaces.UserAccountDatabaseService;

public class SetResult {

    @Inject
    private EventDatabaseService eventDatabaseService;
    
    @Inject
    private SecurityService securityService;
    
    @Inject
    private UserAccountDatabaseService userAccountDatabaseService;

    @Property
    @Persist
    private UserAccount userAccount;
    
    @InjectComponent
    private Form resultForm;

    @Inject
    private AlertManager alertManager;

    @Property
    private Event event;

    @Property
    @PageActivationContext
    private int eventId;

    @Property
    private Integer result;

    @Component(id = "resultSlider")
    private TextField resultField;
    
    @Inject
    private Response response;

    @Inject
    private PageRenderLinkSource pageRenderLinkSource;
    
    void setupRender() {
    	//Check if user is logged in
    	if(securityService.getSubject().getPrincipal() != null) {
    		String principal = securityService.getSubject().getPrincipal().toString();
        	userAccount = userAccountDatabaseService.getUserAccount(principal);
    	}
    	//Redirect user to the login page
    	else {
    		Link link = pageRenderLinkSource.createPageRenderLink("Login");
            try {
                response.sendRedirect(link.toURI());
            } catch (IOException e) {
                throw new RuntimeException("Redirection failed", e);
            }
    	}
    	
    	// Check if the user is an admin. Note that the alertManager
    	// line is meant to display an error message to the user after
    	// the redirect. It is not working for some reason. t:alerts
    	// display on the index page, so it has something to do with
    	// the redirect.
        if (!userAccount.isAdmin()) {
        	// Store alert message.
            alertManager.error("Access denied. Only admins can set the result of events.");
            // Redirect non-admin users to the Index page
            Link link = pageRenderLinkSource.createPageRenderLink(Index.class);
            try {
                response.sendRedirect(link.toURI());
            } catch (IOException e) {
                throw new RuntimeException("Redirection failed", e);
            }
        }
    }
    
    void onActivate(int eventId) {
        this.eventId = eventId;
        event = eventDatabaseService.findEventById(eventId);
        if (event != null) {
            result = event.getResult();
        }
    }

    void onValidateFromResultForm() {
        if (result < 0 || result > 100) {
            resultForm.recordError(resultField, "Result must be between 0 and 100.");
            return;
        }

        eventDatabaseService.updateEventResult(eventId, result);
    }

    Object onSuccessFromResultForm() {
        alertManager.success("Result has been updated successfully.");
        return Index.class; 
    }

    Integer onPassivate() {
        return eventId;
    }
}
