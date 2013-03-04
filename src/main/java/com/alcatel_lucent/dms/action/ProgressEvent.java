package com.alcatel_lucent.dms.action;

public class ProgressEvent {
	private String cmd;
	private String msg;
	private int percent;
	
	public ProgressEvent(String cmd, String msg, int percent) {
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
	public int getPercent() {
		return percent;
	}
	public void setPercent(int percent) {
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
