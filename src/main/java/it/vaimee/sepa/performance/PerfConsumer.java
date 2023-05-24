package it.vaimee.sepa.performance;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.pattern.Consumer;
import it.unibo.arces.wot.sepa.pattern.JSAP;

public class PerfConsumer extends Consumer {

	public PerfConsumer(JSAP jsap,String id)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		super(jsap, id);
	}
}
