package edu.ndsu.cs.estimate.pages.hours;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import edu.ndsu.cs.estimate.cayenne.persistent.Hours;
import edu.ndsu.cs.estimate.cayenne.persistent.Task;
import edu.ndsu.cs.estimate.services.tasks.TaskDatabaseService;
import java.util.List;

public class LogHours {
	
	@Property
	private Task task;
		
	@Property
	private List<Hours> taskHours;
	
	@Property
	private Hours loggedHours;
	
	@Property
    private Integer taskPK;
	
	@Inject
    private TaskDatabaseService taskDatabase;
	
	void onActivate(Integer taskPK) {
        this.taskPK = taskPK;
        if (taskPK != null) {
            task = taskDatabase.getTask(taskPK);

        }
    }
	
    // SetupRender initializes the page with the session data
    void setupRender() {
    	System.out.println("\n\n\n" + task);
    	if (task != null) {
            task = taskDatabase.getTask(taskPK);
            taskHours = task.getHours();
        }                   
      }
    }