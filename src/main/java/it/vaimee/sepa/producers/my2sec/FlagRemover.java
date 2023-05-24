package it.vaimee.sepa.producers.my2sec;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.unibo.arces.wot.sepa.pattern.Producer;

public class FlagRemover extends Producer {
	
	public FlagRemover(JSAP appProfile)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		super(appProfile, "RESET_SYNCHRONIZATION_FLAG");
	}
	
	public boolean resetFlag(RDFTermURI flag) throws SEPABindingsException, SEPASecurityException, SEPAProtocolException, SEPAPropertiesException {
		setUpdateBindingValue("flag", flag);
		return !update().isError();
	}
	
}
