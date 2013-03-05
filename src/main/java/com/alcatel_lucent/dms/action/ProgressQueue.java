package com.alcatel_lucent.dms.action;

import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("serial")
public class ProgressQueue extends LinkedBlockingQueue<ProgressEvent> {
	private String id;
	private static ThreadLocal<ProgressQueue> instance = new ThreadLocal<ProgressQueue>();
	private static ThreadLocal<String> currentMsg = new ThreadLocal<String>();
	
	public ProgressQueue(String queueId) {
		id = queueId;
	};
	
	public static void setInstance(ProgressQueue queue) {
		instance.set(queue);
	}
	
	public static void setProgress(String message, int percent) {
		ProgressQueue queue = instance.get();
		if (queue != null) {
			try {
				queue.put(new ProgressEvent(ProgressAction.CMD_PROCESS, message, percent));
				currentMsg.set(message);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void setProgress(int percent) {
		setProgress(currentMsg.get(), percent); 
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
