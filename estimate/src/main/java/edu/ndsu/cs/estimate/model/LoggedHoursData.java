package edu.ndsu.cs.estimate.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.ndsu.cs.estimate.cayenne.persistent.Hours;

public class LoggedHoursData {
	
	private ArrayList<Date> dateList = new ArrayList<Date>();
	private ArrayList<Integer> hourList = new ArrayList<Integer>();
	
	public LoggedHoursData(ArrayList<Date> dateList, ArrayList<Integer> hourList) {
		this.dateList = dateList;
		this.hourList = hourList;
	}
	public ArrayList<Date> returnDateList()
	{
		return dateList;
	}
	public ArrayList<Integer> returnHourList()
	{
		return hourList;
	}
}
