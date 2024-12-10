package edu.ndsu.cs.estimate.pages.hours;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresRoles;
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

@RequiresRoles("manager")
public class ViewHours {

    @Inject
    private TaskDatabaseService taskDatabase;
    
    @Inject
    private HoursDatabaseService hoursDatabase;
    
    @Inject
    private AlertManager alertManager; 

    @Property
    @Persist
    private Task task;
    
    @Property
    private List<? extends Hours> hours;
    
    @Property
    private Hours hour;
    
    @Property
    private Integer taskPK;

    // New property to hold the timeStamp input as a string
    @Property
    private String timestampStr;

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

    // Method to parse timeStamp string into Date object
    @SuppressWarnings("unused")
	private Date parseDate(String timeStampStr) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        try {
            return format.parse(timeStampStr);
        } catch (ParseException e) {
            alertManager.alert(Duration.TRANSIENT, Severity.ERROR, "Invalid date format. Use MM/dd/yyyy HH:mm.");
            return null;
        }
    }
}
