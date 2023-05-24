package it.vaimee.tools.monitoring;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.vaimee.tools.ITool;

public class WatchDog extends ITool {

	private it.vaimee.sepa.aggregators.WatchDog wd;
	
	public WatchDog(JSAP appProfile) throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		super(appProfile);
		
		wd = new it.vaimee.sepa.aggregators.WatchDog(appProfile, "WD_SUB","WD_UPD");
	}

	@Override
	public void start() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, SEPABindingsException {
		wd.start();
	}

	@Override
	public void stop() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException {
		wd.stop();
	}
}
