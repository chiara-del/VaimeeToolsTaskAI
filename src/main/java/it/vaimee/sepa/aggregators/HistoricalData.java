package it.vaimee.sepa.aggregators;

import java.text.SimpleDateFormat;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.response.Response;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.logging.Logging;
import it.unibo.arces.wot.sepa.pattern.Aggregator;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.vaimee.tools.SEPABeans;
import it.vaimee.tools.ToolStatistics;

public class HistoricalData extends Aggregator implements HistoricalDataMBean {	
	private String liveGraph;
	private String historyGraph;
	
	private ToolStatistics statistics = new ToolStatistics();
	
	public HistoricalData(JSAP appProfile)
			throws SEPAProtocolException, SEPASecurityException, SEPABindingsException, SEPAPropertiesException {
		super(appProfile, "Logging.logger", "Logging.logger");
		
		liveGraph = appProfile.getExtendedData().getAsJsonObject("logging").get("liveGraph").getAsString();
		historyGraph = appProfile.getExtendedData().getAsJsonObject("logging").getAsJsonObject("sepa").get("historyGraph").getAsString();
		
		setUpdateBindingValue("graph", new RDFTermURI(historyGraph));
		
		setSubscribeBindingValue("graph", new RDFTermURI(liveGraph));
		
		SEPABeans.registerMBean("SEPA:type=" + this.getClass().getSimpleName(), this);
	}

	@Override
	public void onFirstResults(BindingsResults results) {
		onAddedResults(results);
	}
	
	@Override
	public void onAddedResults(BindingsResults results) {
		statistics.notification();
		
		Logging.logger.info(appProfile.getSubscribeHost("Logging.logger") + " graph: "+liveGraph+" --> "+appProfile.getUpdateHost("Logging.logger")+" graph: "+historyGraph);
							
		for (Bindings res : results.getBindings()) {
			
			Logging.logger.info(res);
			
			try {
				setUpdateBindings(res);
			} catch (SEPABindingsException e) {
				Logging.logger.error(e.getMessage());
				if (Logging.logger.isTraceEnabled())
					e.printStackTrace();
				continue;
			}
			try {
				statistics.updateStart();				
				Response ret = update();
				statistics.updateStop();
								
				if (ret.isError()) {
					Logging.logger.error(ret);
					statistics.updateFailed();
				}
			} catch (SEPASecurityException | SEPAProtocolException | SEPAPropertiesException
					| SEPABindingsException e) {
				Logging.logger.error(e.getMessage());
				if (Logging.logger.isTraceEnabled())
					e.printStackTrace();
			}
		}
	}

	private void setUpdateBindings(Bindings b) throws SEPABindingsException {
		for (String var : b.getVariables()) {
			setUpdateBindingValue(var, b.getRDFTerm(var));
		}
	}
	
	@Override
	public String getUpdateID() {
		return updateId;
	}

	@Override
	public String getSubscribeID() {
		return subID;
	}

	@Override
	public long getNotifications() {
		return statistics.getNotifications();
	}

	@Override
	public long getNotificationsPerMinute() {
		return statistics.getNotificationsperMinute();
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
	public String getLastNotificationDateTime() {
		SimpleDateFormat iso = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		return iso.format(statistics.getLastNotificationDate());
	}

	@Override
	public String getLiveGraph() {
		return liveGraph;
	}

	@Override
	public String getHistoryGraph() {
		return historyGraph;
	}

	@Override
	public String getUpdateHost() {
		return appProfile.getUpdateHost(updateId);
	}

	@Override
	public String getSubscribeHost() {
		return appProfile.getSubscribeHost(subID);
	}
}
