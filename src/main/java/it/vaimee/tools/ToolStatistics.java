package it.vaimee.tools;

import java.util.Date;

public class ToolStatistics {
	private Date lastNotify;
	private Date firstNotify = null;

	private long notifications = 0;
	private long updateFailed = 0;
	private long updates = 0;
	private float notificationsPerMinute = 0;
	
	private float averageUpdateTime = 0;
	private float maxUpdateTime = -1;
	private float minUpdateTime = -1;
	
	Date start;

	public void notification() {
		// Statistics
		lastNotify = new Date();
		notifications++;

		if (firstNotify == null)
			firstNotify = new Date();
		else {
			float delta = (lastNotify.getTime() - firstNotify.getTime()) / 60000;
			if (delta != 0)
				notificationsPerMinute = notifications / delta;
		}
	}

	public void updateStart() {
		updates++;
		start = new Date();
	}

	public void updateStop() {
		Date stop = new Date();
		long deltaT = stop.getTime() - start.getTime();
		
		averageUpdateTime = (averageUpdateTime * (updates - 1) + deltaT) / updates;
		
		if (maxUpdateTime == -1) maxUpdateTime = deltaT;
		else if (deltaT > maxUpdateTime) maxUpdateTime = deltaT;
		
		if (minUpdateTime == -1) minUpdateTime = deltaT;
		else if (deltaT < minUpdateTime) minUpdateTime = deltaT;
	}

	public void updateFailed() {
		updateFailed++;
	}

	public long getNotifications() {
		return notifications;
	}

	public long getNotificationsperMinute() {
		return Math.round(notificationsPerMinute);
	}

	public long getFailedUpdates() {
		return updateFailed;
	}

	public long getUpdates() {
		return updates;
	}

	public long getUpdateAverageTime() {
		return Math.round(averageUpdateTime);
	}
	
	public long getUpdateMaxTime() {
		return Math.round(maxUpdateTime);
	}
	
	public long getUpdateMinTime() {
		return Math.round(minUpdateTime);
	}

	public Date getLastNotificationDate() {
		return lastNotify;
	}
}
