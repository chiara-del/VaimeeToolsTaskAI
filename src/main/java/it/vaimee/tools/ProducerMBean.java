package it.vaimee.tools;

public interface ProducerMBean {
	
	public String getUpdateID();	
	
	public String getUpdateHost();
	
	public long getFailedUpdates();
	
	public long getUpdates();
	
	public long getAverageUpdateTime();
	public long getMaxUpdateTime();
	public long getMinUpdateTime();
}
