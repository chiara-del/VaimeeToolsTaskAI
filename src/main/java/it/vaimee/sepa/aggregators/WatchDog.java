package it.vaimee.sepa.aggregators;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import it.unibo.arces.wot.sepa.logging.Logging;
import it.unibo.arces.wot.sepa.pattern.Aggregator;
import it.unibo.arces.wot.sepa.pattern.JSAP;

public class WatchDog extends Aggregator {
	private AtomicBoolean running = new AtomicBoolean(true);
	private AtomicBoolean warning = new AtomicBoolean(false);
	
	private Date lastNotification = null;
	private Thread thread;
	
	public WatchDog(JSAP jsap, String subID,String updateID)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		super(jsap, subID, updateID);
		
		thread = new Thread() {
			public void run() {
				while(running.get()) {
					warning.set(true);
					try {
						synchronized(warning) {
							warning.wait(appProfile.getExtendedData().get("interval").getAsLong()*1000);
						}
					} catch (InterruptedException e) {
						
					}
					if (warning.get()) {
						try {
							update();
						} catch (SEPASecurityException | SEPAProtocolException | SEPAPropertiesException
								| SEPABindingsException e) {
							Logging.logger.error(e.getMessage());
						}
					}
				}
			}
		};
	}
	
	public void start() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, SEPABindingsException {
		subscribe();
		thread.start();
	}
	
	public void stop() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException {
		unsubscribe();
		running.set(false);
		warning.set(false);
		thread.interrupt();
	}

	@Override
	public void onAddedResults(BindingsResults results) {
		lastNotification = new Date();
		synchronized(warning) {
			warning.set(false);
			warning.notify();
		}
	}

	public Date getLastNotification() {
		return lastNotification;
	}
}
