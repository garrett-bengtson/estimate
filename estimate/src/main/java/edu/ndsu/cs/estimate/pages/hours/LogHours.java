package edu.ndsu.cs.estimate.pages.hours;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;

import edu.ndsu.cs.estimate.cayenne.persistent.Hours;
import edu.ndsu.cs.estimate.cayenne.persistent.Task;
import edu.ndsu.cs.estimate.model.LoggedHoursData;
import edu.ndsu.cs.estimate.services.tasks.TaskDatabaseService;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class LogHours {
	
    @Property
    private int index;
	
//	@Property
//	private Task task;
//	
//	@Property 
//	private Hours hour;
	
	@Property
	@SessionState
	private LoggedHoursData returnHours;
		
	@Property
	private List<Hours> taskHours;
	
	@Property
	private ArrayList<Date> loggedHoursDates;
		
	@Property
	private ArrayList<Integer> loggedHoursList;
	
	@Property
	private Date loggedDate;
	
	@Property
	private Integer loggedHours;
	
//	@Property
//    @Persist
//    private Integer taskPK;
	
//	@Inject
//    private TaskDatabaseService taskDatabase;
	
//	void onPrepare() {
//	    if (returnHours == null) {
//	        returnHours = new LoggedHoursData(loggedHoursDates, loggedHoursList);
//	    }
//	}

	
	
    // SetupRender initializes the page with the session data
    void setupRender() {
    	if (returnHours != null) {
    		loggedHoursDates = returnHours.returnDateList();
    		loggedHoursList = returnHours.returnHourList();
    	} else {
    		
    	}
//    	if (taskPK != null) {
//            task = taskDatabase.getTask(taskPK);
//            taskHours = task.getHours();
//            
//            loggedHoursDates = new ArrayList<>();
//            loggedHoursList = new ArrayList<>();
//            for (Hours h : taskHours) {
//                loggedHoursDates.add(h.getTimestamp());
//                loggedHoursList.add(h.getHoursLogged());
//            
//            }
//        }                   
      }
    }
