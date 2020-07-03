package org.openmrs.module.eptsreports;

import java.util.List;

import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;

public class EPTSGlobalPropertyListener  implements GlobalPropertyListener{

	@Override
	public void globalPropertyChanged(GlobalProperty arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void globalPropertyDeleted(String arg0) {
		AdministrationService administrationService = Context.getAdministrationService();
		List<GlobalProperty> globalProperties =
				administrationService.getAllGlobalProperties();	
		
		for(GlobalProperty gp:globalProperties) {
			if(gp.getProperty().contains("eptsreports")) {
				administrationService.purgeGlobalProperty(gp);
			}
		}
		
	}

	@Override
	public boolean supportsPropertyName(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
