package it.vaimee.tools;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.pattern.JSAP;

public abstract class ITool {
	protected static final Logger logger = LogManager.getLogger();
	protected JSAP appProfile;
		
	public ITool(JSAP appProfile) throws SEPAProtocolException, SEPASecurityException {
		this.appProfile = appProfile;
	};
	
	public abstract void start() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, SEPABindingsException;
	
	public abstract void stop() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, IOException;
}
