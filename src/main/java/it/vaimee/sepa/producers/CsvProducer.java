package it.vaimee.sepa.producers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.unibo.arces.wot.sepa.pattern.Producer;

public class CsvProducer extends Producer{
	BufferedReader in;
	
	public CsvProducer(JSAP appProfile, String updateID)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		super(appProfile, updateID);
		
		 try {
			in = new BufferedReader(new FileReader("foo.in"));
		} catch (FileNotFoundException e) {
			throw new SEPAPropertiesException(e.getMessage());
		}
		
	}

}
