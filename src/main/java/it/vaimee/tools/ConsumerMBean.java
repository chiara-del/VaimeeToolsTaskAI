package it.vaimee.tools;

public interface ConsumerMBean {
	
	public String getSubscribeID();
	
	public String getSubscribeHost();
	
	public long getNotifications();
	
	public long getNotificationsPerMinute();
	
	public String getLastNotificationDateTime();
}
