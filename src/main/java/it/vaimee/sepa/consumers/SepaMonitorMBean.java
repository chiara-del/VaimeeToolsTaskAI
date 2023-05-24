package it.vaimee.sepa.consumers;

import it.vaimee.tools.ConsumerMBean;

public interface SepaMonitorMBean extends ConsumerMBean {
	public String getBrokenConnections();
}
