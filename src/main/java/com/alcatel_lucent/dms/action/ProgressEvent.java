package com.alcatel_lucent.dms.action;

public class ProgressEvent {
	private String cmd;
	private String msg;
	private float percent;
	
	public ProgressEvent(String cmd, String msg, float percent) {
		this.cmd = cmd;
		this.msg = msg;
		this.percent = percent;
	}
	
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public float getPercent() {
		return percent;
	}
	public void setPercent(float percent) {
		this.percent = percent;
	}

    @Override
    public String toString() {
        return "ProgressEvent{" +
                "cmd='" + cmd + '\'' +
                ", msg='" + msg + '\'' +
                ", percent=" + percent +
                '}';
    }
}
