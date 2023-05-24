package it.vaimee.tools.logging;

import java.io.IOException;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.pattern.Consumer;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.vaimee.sepa.aggregators.HistoricalData;
import it.vaimee.sepa.consumers.MySqlObservationConsumer;
import it.vaimee.tools.ITool;

public class ObservationsLogger extends ITool {
	private Consumer history;
	
	public ObservationsLogger(JSAP appProfile) throws SEPAProtocolException, SEPASecurityException, SEPABindingsException, SEPAPropertiesException {
		super(appProfile);
		
		if (appProfile.getExtendedData().get("target").getAsString().equals("sepa")) history = new HistoricalData(appProfile);
		else if (appProfile.getExtendedData().get("target").getAsString().equals("mysql")) history = new MySqlObservationConsumer(appProfile);
		else throw new SEPAPropertiesException("target not found or not recognized");
	}

	@Override
	public void start() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, SEPABindingsException {
		history.subscribe();
	}
	
	@Override
	public void stop() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, IOException {
		history.unsubscribe();
		history.close();		
	}
}