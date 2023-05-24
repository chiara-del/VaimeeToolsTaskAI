package it.vaimee.tools;

public interface AggregatorMBean extends ConsumerMBean {
	
	public String getUpdateID();	
	
	public String getUpdateHost();
	
	public long getFailedUpdates();
	
	public long getUpdates();
	
	public long getAverageUpdateTime();
}
