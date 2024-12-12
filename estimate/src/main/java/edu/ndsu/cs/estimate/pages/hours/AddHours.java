package edu.ndsu.cs.estimate.pages.hours;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.annotations.Inject;

import edu.ndsu.cs.estimate.cayenne.persistent.Hours;
import edu.ndsu.cs.estimate.cayenne.persistent.Task;
import edu.ndsu.cs.estimate.pages.tasks.Index;
import edu.ndsu.cs.estimate.services.hours.HoursDatabaseService;
import edu.ndsu.cs.estimate.services.tasks.TaskDatabaseService;

public class AddHours {

    @Inject
    private TaskDatabaseService taskDatabase;
    
    @Inject
    private HoursDatabaseService hoursDatabase;
    
    @Inject
    private AlertManager alertManager;
    
    @InjectComponent
    private Form hoursForm;
    
    @Property
    @Persist
    private Task task;
    
    @Property
    private List<? extends Hours> hours;
    
    @Property
    @Persist
    private Hours hour;
    
    @Property
    @Persist
    private String timestampStr;
    
    @Component(id = "timeStamp", parameters = {"value=timestampStr", "type=text"})
    private TextField timeStampField;
    
    @Property
    @Persist
    private Integer taskPK;
    
    void onActivate(Integer taskPK) {
        this.taskPK = taskPK;
    }
    
    Integer onPassivate() {
        return taskPK; 
    }
    
    void setupRender() {
        hour = hoursDatabase.getNewHours();
        if (taskPK != null) {
            task = taskDatabase.getTask(taskPK);
            hours = hoursDatabase.listAllHoursByTask(task);
        }
    }
    
    void onValidateFromHoursForm() {
        if (timestampStr == null) {
            hoursForm.recordError("Invalid date format. Please use MM/dd/yyyy.");
        }
        else {
        	Date timestampDate = parseDate(timestampStr);
        	
        	//Check if the inputted date is in the future
        	Date today = new Date();
        	if(timestampDate.after(today)) {
        		hoursForm.recordError("Date cannot be in the future.");
        		return;
        	}
    		task = taskDatabase.getTask(taskPK);
    		if(task.getTimeTaken() + hour.getHoursLogged() > 1000000) {
      			hoursForm.recordError("Total hours logged would be over one million.");
      			return;
      		}
    		else if(task.getTimeTaken() + hour.getHoursLogged() < 0) {
    			hoursForm.recordError("Total hours logged would be negative.");
    			return;
    		}
    		else if(hour.getHoursLogged() == 0) {
    			hoursForm.recordError("There is no reason to log 0 hours!");
    			return;
    		}
    		else {
    			task.setTimeTaken(task.getTimeTaken() + hour.getHoursLogged());
    			taskDatabase.updateTask(task); // Update task in database
                hour.setTimestamp(timestampDate);  // Set the parsed timeStamp on the `hour` object
    		
                //Add an instance of the added hours to the Hours table
                //for logging purposes
                Hours addedHours = task.getObjectContext().newObject(Hours.class);
                addedHours.setTimestamp(hour.getTimeStamp());
                addedHours.setHoursLogged(hour.getHoursLogged());
                
                task.addToHours(addedHours);
                addedHours.getObjectContext().commitChanges();
    		}
        }
//        List<String> errors = hour.validate();
//        for (String error : errors) {
//            hoursForm.recordError(error);
//        }
    }
     
    private Date parseDate(String timestampStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        dateFormat.setLenient(false);  // Enforce strict parsing
        try {
        	Date parsedDate = dateFormat.parse(timestampStr);
            return parsedDate;
        } catch (ParseException e) {
        	hoursForm.recordError("Can't parse a date.");
            return null;  // Return null if parsing fails
        }
    }

    Object onSuccessFromHoursForm() {
        alertManager.alert(Duration.TRANSIENT, Severity.SUCCESS, "Hours added successfully.");
        return Index.class;
    }
}
