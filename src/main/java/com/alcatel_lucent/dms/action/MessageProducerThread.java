package com.alcatel_lucent.dms.action;

import com.alcatel_lucent.dms.UserContext;
import com.opensymphony.xwork2.ActionContext;
import org.apache.commons.lang.StringUtils;

import static com.alcatel_lucent.dms.action.ProgressAction.CMD_DONE;
import static com.alcatel_lucent.dms.action.ProgressAction.CMD_ERROR;

/**
 * Created by Administrator on 13-12-16.
 */
public class MessageProducerThread extends Thread {

    private ProgressQueue queue;
    private UserContext uc;

    private int status;

    private ActionContext actionContext;

    private ProgressAction progressAction;

    public MessageProducerThread(ProgressQueue queue, UserContext uc, ProgressAction progressAction, ActionContext actionContext) {
        this.queue = queue;
        this.uc = uc;
        this.progressAction = progressAction;
        this.actionContext = actionContext;
    }

    @Override
    public void run() {
        try {
            ProgressQueue.setInstance(queue);
            UserContext.setUserContext(uc);
            progressAction.performAction();
            if (status == 0) {
                queue.put(new ProgressEvent(CMD_DONE, progressAction.getMessage(), 100f));
            } else {
                queue.put(new ProgressEvent(CMD_ERROR, progressAction.getMessage(), -1f));
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
//                progressAction.getText("message.success");
                String msg = StringUtils.defaultIfEmpty(e.getMessage(), "System error, please contact system administrator!");
                queue.put(new ProgressEvent(CMD_ERROR, msg, -1f));
            } catch (InterruptedException e1) {
                // dummy
            }
        } finally {
            ProgressQueue.removeInstance();
            UserContext.removeUserContext();
        }
    }
}
