package it.vaimee.sepa.aggregators;

import it.vaimee.tools.AggregatorMBean;

public interface HistoricalDataMBean extends AggregatorMBean {
	public String getLiveGraph();
	public String getHistoryGraph();
}
