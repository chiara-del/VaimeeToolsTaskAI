package it.vaimee.sepa.init;

import java.io.IOException;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.vaimee.sepa.aggregators.SepaSyncAggregator;
import it.vaimee.tools.ITool;

public class SepaSync extends ITool {

	private SepaSyncAggregator agent;
	
	public SepaSync(JSAP appProfile) throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		super(appProfile);
		agent = new SepaSyncAggregator(appProfile, "FROM_SEPA", "TO_SEPA");
	}

	@Override
	public void start()
			throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, SEPABindingsException {
		agent.subscribe();
		
	}

	@Override
	public void stop() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, IOException {
		agent.unsubscribe();
		
	}

}
