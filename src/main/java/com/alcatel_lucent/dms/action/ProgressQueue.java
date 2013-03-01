package com.alcatel_lucent.dms.action;

import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("serial")
public class ProgressQueue extends LinkedBlockingQueue<ProgressEvent> {
	private String id;
	private static ThreadLocal<ProgressQueue> instance = new ThreadLocal<ProgressQueue>();
	
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
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
