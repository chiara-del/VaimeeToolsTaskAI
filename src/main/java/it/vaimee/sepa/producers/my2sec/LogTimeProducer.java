package it.vaimee.sepa.producers.my2sec;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.unibo.arces.wot.sepa.pattern.Producer;

public class LogTimeProducer extends Producer {

	public LogTimeProducer(JSAP appProfile)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		super(appProfile, "LOG_TIME");
	}
}
