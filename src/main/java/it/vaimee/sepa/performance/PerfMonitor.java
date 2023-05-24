package it.vaimee.sepa.performance;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.vaimee.tools.ITool;

public class PerfMonitor extends ITool {
	PerfConsumer consumer;
	PerfProducer producer;
	Thread run;
	
	AtomicInteger n = new AtomicInteger();

	public PerfMonitor(JSAP appProfile) throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		super(appProfile);
		producer = new PerfProducer(appProfile, "ONE_TRIPLE");
		consumer = new PerfConsumer(appProfile, "ALL_TRIPLE");
	}

	@Override
	public void start()
			throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, SEPABindingsException {
		//consumer.subscribe();
		n.set(0);
		new Thread() {
			public void run() {
				while(true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println(String.format("%d", n.get()));
					n.set(0);
				}
			}
		}.start();
		run = new Thread() {
			public void run() {
				while (true) {
					try {
						producer.update();
						n.getAndIncrement();
					} catch (SEPASecurityException | SEPAProtocolException | SEPAPropertiesException
							| SEPABindingsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		run.start();
	}

	@Override
	public void stop() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, IOException {
		//consumer.unsubscribe();
		run.interrupt();
	}

}
