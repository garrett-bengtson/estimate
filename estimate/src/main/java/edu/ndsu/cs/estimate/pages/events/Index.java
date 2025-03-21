package edu.ndsu.cs.estimate.pages.events;

import edu.ndsu.cs.estimate.cayenne.persistent.Event;
import org.tynamo.security.services.SecurityService;
import edu.ndsu.cs.estimate.entities.interfaces.UserAccount;
import edu.ndsu.cs.estimate.services.database.interfaces.EventDatabaseService;
import edu.ndsu.cs.estimate.services.database.interfaces.UserAccountDatabaseService;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Select;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Index {

    @Inject
    private EventDatabaseService db;

    @Inject
    private AlertManager alertManager;
    
    @Property
    @Persist
    private UserAccount userAccount;
    
    @Property
    private List<Event> events;
    
    @Property
    private boolean noEvents;
    @Property
    private boolean noEventsWithResults;
    @Property
    private boolean noEventsWithoutResults;
    
    @Property
    private Event currentEvent;

    @Property
    @Persist
    private String dateRange;

    @InjectComponent
    private Form dateForm;

    @Component(id = "dateRange", parameters = {"value=dateRange"})
    private TextField dateRangeField;
    
    @Property
    private List<String> categories = new ArrayList<>();

    @Component(id = "category", parameters = {"value=category", "model=categoryModel", "blankOption=never"})
    private Select categorySelect;

    @Persist
    @Property
    private String category;
    
    @Component(id = "hiddenCategory", parameters = {"value=hiddenCategory"})
    private TextField hiddenCategoryField;

    @Persist
    @Property
    private String hiddenCategory;
    
    @Inject
    private UserAccountDatabaseService userAccountDatabaseService;

    @Inject
    private SecurityService securityService;

    @Property
    private List<Event> eventsWithResults;

    @Property
    private List<Event> eventsWithoutResults;
    
    void setupRender() {
    	//Check if user is logged in
    	if(securityService.getSubject().getPrincipal() != null) {
    		String principal = securityService.getSubject().getPrincipal().toString();
        	userAccount = userAccountDatabaseService.getUserAccount(principal);
    	}	
        getEvents();
        //No events is true if there are no events OR
        //if none of the events are approved for users.
        noEvents = (events == null) || events.isEmpty() ||
        		events.stream()
        		.filter(event -> event.isApproved() == true || userAccount.isAdmin())
        		.collect(Collectors.toList()).isEmpty();
        
        eventsWithResults = events.stream()
                .filter(event -> event.getResult() != null)
                .filter(event -> event.isApproved() == true || userAccount.isAdmin())
                .collect(Collectors.toList());
        noEventsWithResults = (eventsWithResults == null) || eventsWithResults.isEmpty();

        eventsWithoutResults = events.stream()
                   .filter(event -> event.getResult() == null)
                   .filter(event -> event.isApproved() == true || userAccount.isAdmin())
                   .collect(Collectors.toList());
        noEventsWithoutResults = (eventsWithoutResults == null) || eventsWithoutResults.isEmpty();
    }
    
    //Change event approval from true to false and vice versa
    @OnEvent(component = "changeApprovalStatusUncompleted", value = "action")
    void handleChangeApprovalStatusUncompleted(int eventid) {
    	db.changeApprovalStatus(eventid);
    }

    @OnEvent(component = "changeApprovalStatusCompleted", value = "action")
    void handleChangeApprovalStatusCompleted(int eventid) {
    	db.changeApprovalStatus(eventid);
    }
    
    
    
    
    
    //Check if the event is approved or if the user is an admin.
    //if either are true, return true. This will cause the event to
    //display.
    public Boolean isPublicEvent(Boolean currentEventApproval) {
    	if(currentEventApproval || userAccount.isAdmin()) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    private void getEvents() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        dateFormat.setLenient(false);
        Date start = new Date(0); // 1/1/1970
        // 0 ms after 1/1/1970
        Date end = new Date(2145916800000L); // 1/1/2038
        // 2145916800000 ms after 1/1/1970, L for long input, instead of int

        if (dateRange != null && !dateRange.isEmpty()) {
            String[] dates = dateRange.split(" - ");
            try {
                start = dateFormat.parse(dates[0]);
                end = (dates.length == 2) ? dateFormat.parse(dates[1]) : start;
            } catch (Exception e) {
                dateForm.recordError("The date range format is invalid.");
                return;
            }
        }
        
        events = db.findEventsInRange(start, end, category);
        categories = db.findAllCategoriesInRange(start, end);
    }
    
    public SelectModel getCategoryModel() {
        List<OptionModel> options = new ArrayList<>();

        options.add(new OptionModelImpl("All Categories", ""));

        if (categories == null) {
            System.err.println("Categories list is null in getCategoryModel.");
            return new SelectModelImpl(null, options);
        }

        boolean categoryIncluded = false;
        for (String category : categories) {
            options.add(new OptionModelImpl(category, category));
            if (category.equals(category)) {
                categoryIncluded = true;
            }
        }

        if (!categoryIncluded && category != null && !category.isEmpty()) {
            options.add(new OptionModelImpl(category, category));
        }

        System.err.println("Options in getCategoryModel: " + options.stream().map(OptionModel::getLabel).collect(Collectors.toList()));
        System.err.println("Selected Category: " + category);

        return new SelectModelImpl(null, options);
    }


    void onValidateFromDateForm() {
        if (dateRange != null && !dateRange.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            dateFormat.setLenient(false);
            String[] dates = dateRange.split(" - ");
            if (dates.length == 2) {
                try {
                    dateFormat.parse(dates[0]);
                    dateFormat.parse(dates[1]);
                } catch (ParseException e) {
                    dateForm.recordError("The date range format is invalid. Please use MM/dd/yyyy - MM/dd/yyyy format.");
                }
            } else {
                dateForm.recordError("The date range format is invalid. Please use MM/dd/yyyy - MM/dd/yyyy format.");
            }
        }
    }

    @OnEvent(value = "submit", component = "dateForm")
    void onSuccessFromDateForm() {
        System.err.println("read category: " + hiddenCategory);
        category = hiddenCategory;
    }
    
    @OnEvent(component = "deleteUncompleted", value = "action")
    void handleDeleteUncompleted(int eventId) {
    	db.deleteEvent(eventId);
    }

    @OnEvent(component = "deleteCompleted", value = "action")
    void handleDeleteCompleted(int eventId) {
    	db.deleteEvent(eventId);
    }
    
}
