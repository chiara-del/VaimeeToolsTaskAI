package it.vaimee.tools.logging;

import java.io.IOException;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.vaimee.sepa.consumers.SepaBridgeConsumer;
import it.vaimee.tools.ITool;

public class SepaBridge extends ITool {
	private SepaBridgeConsumer consumer;
	
	public SepaBridge(JSAP appProfile) throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		super(appProfile);
		consumer = new SepaBridgeConsumer(appProfile);
	}

	@Override
	public void start()
			throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, SEPABindingsException {
		consumer.subscribe();
		
	}

	@Override
	public void stop() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, IOException {
		consumer.unsubscribe();
		
	}

}
