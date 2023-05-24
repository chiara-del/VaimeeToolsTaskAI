package it.vaimee.tools.simulating;

import java.io.IOException;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermLiteral;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.unibo.arces.wot.sepa.pattern.Producer;
import it.vaimee.tools.ITool;

public class WeatherAgentSimulator extends ITool {
	Producer producer ;
	
	public WeatherAgentSimulator(JSAP appProfile) throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, SEPABindingsException {
		super(appProfile);
		producer = new Producer(appProfile, "uploadForecast");
		producer.setUpdateBindingValue("feature", new RDFTermURI("meter:Place"));
		producer.setUpdateBindingValue("property", new RDFTermURI("weather:temperature"));
		producer.setUpdateBindingValue("unit", new RDFTermURI("unit:DegreeCelsius"));
		producer.setUpdateBindingValue("time", new RDFTermLiteral("2021-10-15T03:03:00Z"));
		producer.setUpdateBindingValue("ptime", new RDFTermLiteral("2021-10-13T03:03:00Z"));
		producer.setUpdateBindingValue("value", new RDFTermLiteral("0"));
	}
	
	@Override
	public void start()
			throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, SEPABindingsException {
		for (int i=0; i < appProfile.getExtendedData().get("n").getAsInt(); i++) {
			producer.setUpdateBindingValue("value", new RDFTermLiteral(""+i));
			producer.update();
		}
		
	}

	@Override
	public void stop() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, IOException {
		// TODO Auto-generated method stub
		
	}

}
