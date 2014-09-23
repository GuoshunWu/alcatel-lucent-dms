package com.alcatel_lucent.dms.action;

import com.alcatel_lucent.dms.UserContext;
import com.opensymphony.xwork2.ActionContext;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.SessionAware;

import java.util.Map;

/**
 * Base class of actions which need a progress bar
 * Input:
 *   pqCmd - 	must be "start" or "process"
 *   			if cmd="start", a working thread is created and executes performAction()
 *   			if cmd="process", the action wait for and return a ProgressEvent
 *   pqId - 	the unique id of ProgressQueue, must be specified when cmd="process"
 * Output:
 *   queueId - 	the unique id of ProgressQueue
 *   event - 	last event in the queue
 *   status - 	final result of the action (only available when done)
 *   message - 	final message of the action (only available when done)
 * @author allany
 *
 */
@ParentPackage("dms-json")
@Result(type="json", params={"noCache","true","ignoreHierarchy","false","includeProperties","pqId,event.*"})
@SuppressWarnings("serial")
abstract public class ProgressAction extends JSONAction implements SessionAware {
	public static final String CMD_START = "start";
	public static final String CMD_PROCESS = "process";
	public static final String CMD_DONE = "done";
	public static final String CMD_ERROR = "error";
	
	
	private Map<String, Object> session;

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public Map<String, Object> getSession() {
        return session;
    }

    // input
	private String pqCmd;
	
	// input & output
	private String pqId;
	
	// output
	private ProgressEvent event;
	
	private Thread thread;



	public String execute() {
		if (getPqCmd().equals(CMD_START)) {	// initialize progress queue and start job
			setPqId("pq_" + System.currentTimeMillis());
            final ProgressQueue queue = new ProgressQueue(getPqId());
            session.put(getPqId(), queue);
            final UserContext uc=UserContext.getInstance();
            final ActionContext ctx= ActionContext.getContext();
            thread = new Thread(new Runnable() {
				@Override
				public void run() {
                    ActionContext.setContext(ctx);
                    try {
						ProgressQueue.setInstance(queue);
                        UserContext.setUserContext(uc);
						performAction();
						if (status == 0) {
							queue.put(new ProgressEvent(CMD_DONE, getMessage(), 100f));
						} else {
							queue.put(new ProgressEvent(CMD_ERROR, getMessage(), -1f));
						}
                    }
                    catch (Exception e) {
						e.printStackTrace();
						try {
                            String msg = StringUtils.defaultIfEmpty(e.getMessage(), getText("message.generalErrorMsg"));
							queue.put(new ProgressEvent(CMD_ERROR, msg, -1f));
						} catch (InterruptedException e1) {
							// dummy
						}
					} finally {
						ProgressQueue.removeInstance();
						UserContext.removeUserContext();
					}
				}
			});
			thread.start();
            thread.setName("Event_"+ getPqId());
			event = new ProgressEvent(CMD_PROCESS, "Please wait...", -1f);
		} else if (getPqCmd().equals(CMD_PROCESS)) {	// wait for an event in the queue
			ProgressQueue queue = (ProgressQueue) session.get(getPqId());
			if (queue != null) {
				do {
					try {
						event = queue.take();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} while (!queue.isEmpty());
				// destruct queue if finished
				if (event.getCmd().equals(CMD_DONE) || event.getCmd().equals(CMD_ERROR)) {
					session.remove(getPqId());
				}
			} else {
				event = new ProgressEvent(CMD_DONE, "", 100f);
			}
            log.info("event="+event);
		} else {
			event = new ProgressEvent(CMD_ERROR, "Unknown progress action cmd " + getPqCmd() + ".", -1f);
			setStatus(-1);
			setMessage("Unknown progress action cmd: " + getPqCmd());
		}
		return SUCCESS;
	}


    public ProgressEvent getEvent() {
		return event;
	}

	public void setEvent(ProgressEvent event) {
		this.event = event;
	}

    public String getPqCmd() {
        return pqCmd;
    }

    public void setPqCmd(String pqCmd) {
        this.pqCmd = pqCmd;
    }

    public String getPqId() {
        return pqId;
    }

    public void setPqId(String pqId) {
        this.pqId = pqId;
    }
}
