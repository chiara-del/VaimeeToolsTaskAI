package it.vaimee.sepa.consumers;

import it.vaimee.tools.ConsumerMBean;

public interface MySqlObservationConsumerMBean extends ConsumerMBean {
	public String getLiveGraph();
}
