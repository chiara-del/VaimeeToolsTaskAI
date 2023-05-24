package it.vaimee.sepa.producers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.response.Response;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermLiteral;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.unibo.arces.wot.sepa.pattern.Producer;
import it.vaimee.tools.SEPABeans;
import it.vaimee.tools.ToolStatistics;

public class TSX1SimSingleUpdate extends Producer implements TSX1SimSingleUpdateMBean {
protected static final Logger logger = LogManager.getLogger();
	
	private String valueA = "0", valueB = "0", valueC = "0", valueD = "0";
	private int i = 0;
	private AtomicBoolean runningAtomicBoolean = new AtomicBoolean(true);
	private Thread thread;

	private ToolStatistics statistics = new ToolStatistics();
	
	protected void updateValues() {
		if (i == 364)
			i = 0;
		valueA = String.format("%.2f", Math.sin(Math.toRadians(i)) * 50).replace(",", ".");
		valueB = String.format("%.2f", 50 + Math.sin(Math.toRadians(i + 90)) * 100).replace(",", ".");
		valueC = String.format("%.2f", 150 + Math.sin(Math.toRadians(i + 180))  * 200).replace(",", ".");
		valueD = String.format("%.2f", 200 + Math.sin(Math.toRadians(i + 270))  * 250).replace(",", ".");
		i = i + 1;
	}

	public void close() throws IOException {
		runningAtomicBoolean.set(false);
		thread.interrupt();
		super.close();
	}
	
	public TSX1SimSingleUpdate(JSAP jsap, ArrayList<String> urn, String foi, long sleep, String graph)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, SEPABindingsException {
		super(jsap, "TSX1_OBSERVATION");
		
		SEPABeans.registerMBean("SEPA:type=" + this.getClass().getSimpleName(), this);
		
		setUpdateBindingValue("graph", new RDFTermURI(graph));
		setUpdateBindingValue("foi", new RDFTermURI(foi));
		
		setUpdateBindingValue("probe1", new RDFTermURI(urn.get(0)));
		setUpdateBindingValue("probe2", new RDFTermURI(urn.get(1)));
		setUpdateBindingValue("probe3", new RDFTermURI(urn.get(2)));
		setUpdateBindingValue("probe4", new RDFTermURI(urn.get(3)));
		
		thread = new Thread() {
			public void run() {

				while (runningAtomicBoolean.get()) {
					try {
						logger.info("TSX1 simulator FOI: "+foi+" GRAPH: "+graph+" sleep: "+sleep+" ms");
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
						return;
					}

					updateValues();
					
					logger.info("ProbeA: "+urn.get(0)+" Value: "+String.valueOf(valueA));
					logger.info("ProbeB: "+urn.get(1)+" Value: "+String.valueOf(valueB));
					logger.info("ProbeC: "+urn.get(2)+" Value: "+String.valueOf(valueC));
					logger.info("ProbeD: "+urn.get(3)+" Value: "+String.valueOf(valueD));
					
					try {	
						
						setUpdateBindingValue("value1", new RDFTermLiteral(String.valueOf(valueA)));
						setUpdateBindingValue("value2", new RDFTermLiteral(String.valueOf(valueB)));
						setUpdateBindingValue("value3", new RDFTermLiteral(String.valueOf(valueC)));
						setUpdateBindingValue("value4", new RDFTermLiteral(String.valueOf(valueD)));
						
						statistics.updateStart();
						Response ret = update(5000,3);
						statistics.updateStop();
						
						if (ret.isError()) {
							statistics.updateFailed();
							logger.error(ret);
						}
					} catch (SEPABindingsException | SEPASecurityException | SEPAProtocolException
							| SEPAPropertiesException e) {
						logger.error(e.getMessage());
						continue;
					}
				}
			}
		};
		thread.setName(foi);
		thread.start();
		
	}

	@Override
	public String getUpdateID() {
		return SPARQL_ID;
	}


	@Override
	public String getUpdateHost() {
		return appProfile.getUpdateHost(SPARQL_ID);
	}

	@Override
	public long getFailedUpdates() {
		return statistics.getFailedUpdates();
	}

	@Override
	public long getUpdates() {
		return statistics.getUpdates();
	}

	@Override
	public long getAverageUpdateTime() {
		return statistics.getUpdateAverageTime();
	}

	@Override
	public long getMaxUpdateTime() {
		return statistics.getUpdateMaxTime();
	}

	@Override
	public long getMinUpdateTime() {
		return statistics.getUpdateMinTime();
	}

}
