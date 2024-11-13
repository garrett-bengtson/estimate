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
    private Date timestampDate;
    
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
        Date timestamp = timestampDate;
        if (timestamp == null) {
            hoursForm.recordError("Invalid date format. Please use MM/dd/yyyy HH:mm.");
        } else {
        	task = taskDatabase.getTask(taskPK);
        	task.setTimeTaken(task.getTimeTaken() + hour.getHoursLogged());
        	taskDatabase.updateTask(task); // Update task in database
            hour.setTimestamp(timestamp);  // Set the parsed timeStamp on the `hour` object
        }
        
        List<String> errors = hour.validate();
        for (String error : errors) {
            hoursForm.recordError(error);
        }
        
       
    }
    
    private Date parseDate(String timestampStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        dateFormat.setLenient(false);  // Enforce strict parsing
        try {
            return dateFormat.parse(timestampStr);
        } catch (ParseException e) {
            return null;  // Return null if parsing fails
        }
    }

    Object onSuccessFromHoursForm() {
        alertManager.alert(Duration.TRANSIENT, Severity.SUCCESS, "Hours added successfully.");
        return Index.class;
    }
}
