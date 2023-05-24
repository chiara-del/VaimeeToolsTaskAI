package it.vaimee.sepa.performance;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.unibo.arces.wot.sepa.pattern.Producer;

public class PerfProducer extends Producer {

	public PerfProducer(JSAP jsap,String id)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		super(jsap, id);
	}

}
