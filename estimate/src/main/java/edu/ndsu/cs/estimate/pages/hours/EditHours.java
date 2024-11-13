package edu.ndsu.cs.estimate.pages.hours;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.annotations.Inject;

import edu.ndsu.cs.estimate.cayenne.persistent.Hours;
import edu.ndsu.cs.estimate.cayenne.persistent.Task;
import edu.ndsu.cs.estimate.pages.Index;
import edu.ndsu.cs.estimate.services.hours.HoursDatabaseService;
import edu.ndsu.cs.estimate.services.tasks.TaskDatabaseService;

public class EditHours {

    @Inject
    private TaskDatabaseService taskDatabase;
    
    @Inject
    private HoursDatabaseService hoursDatabase;
    
    @Inject
    private AlertManager alertManager; 
    
    @InjectComponent
    private Form editHoursForm;
    
    @Property
    @Persist
    private Task task;
    
    @Property
    private List<? extends Hours> hours;
    
    @Property
    private Hours hour;
    
    @Property
    private String timeStampStr;  // Holds user input for timestamp
    
    @Property
    private Integer taskPK;
    
    void onActivate(Integer taskPK) {
        this.taskPK = taskPK;
    }
    
    Integer onPassivate() {
        return taskPK; 
    }
    
    void setupRender() {
        if (taskPK != null) {
            task = taskDatabase.getTask(taskPK);
            hours = hoursDatabase.listAllHoursByTask(task);
        }
        }
        private Date parseDate(String timeStampStr) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            dateFormat.setLenient(false);  // Enforce strict parsing
            try {
                return dateFormat.parse(timeStampStr);
            } catch (ParseException e) {
                return null;  // Return null if parsing fails
            }
    }

    void onValidateFromEditHoursForm() {
        // Parse the timeStamp string to a Date object
        Date timeStamp = parseDate(timeStampStr);
        if (timeStamp == null) {
            editHoursForm.recordError("Invalid date format. Please use MM/dd/yyyy HH:mm.");
        } else {
            hour.setTimestamp(timeStamp);  // Set the parsed timestamp on the `hour` object
        }
        
        // Validate other properties of `hour` if necessary
        List<String> errors = hour.validate();
        for (String error : errors) {
            editHoursForm.recordError(error);
        }
        
        if (!editHoursForm.getHasErrors()) {
            hoursDatabase.updateHours(hour);
        }
    
    
   
    }

    Object onSuccessFromEditHoursForm() {
        alertManager.alert(Duration.TRANSIENT, Severity.SUCCESS, "Hours updated successfully.");
        return Index.class;
    }
}
