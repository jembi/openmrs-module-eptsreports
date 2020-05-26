package org.openmrs.module.eptsreports.reporting.calculation.ltfu;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Test {

	public static void main(String[] args) {
		
	 
 		Date calendar1 = new GregorianCalendar(2019,7,20).getTime();
 		Date calendar2 =  new GregorianCalendar(2019,3,20).getTime();
 		Date calendar3 =  new GregorianCalendar(2019,11,20).getTime();
 		
 		
 		List<Date> dates = Arrays.asList(null,null,null);
		 
 		 System.out.println(getTheMaxDate(dates));
		
	 
	}
	
	private static  Date getTheMaxDate(List<Date> dates) {
		
		 
		Date max  = new GregorianCalendar(1980,5,1).getTime();
		for(Date current:dates) {
			if(current!=null) {
				if(current.compareTo(max)>0) {
					max = current;
				}
			}
		}
		
		if(max.equals(new GregorianCalendar(1980,5,1).getTime())) {
			return null;
		}
		
		return  max;
		
	}

}
