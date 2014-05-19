package com.alcatel_lucent.dms.model;

import java.sql.Timestamp;

@SuppressWarnings("serial")
public class DictionaryHistory extends BaseEntity {
	
	public static final String OP_DELIVER = "Deliver";
	public static final String OP_CREATE_TASK = "CreateTask";
	public static final String OP_RECEIVE_TASK = "ReceiveTask";
	public static final String OP_IMPORT_TASK = "ImportTask";
	public static final String OP_CLOSE_TASK = "CloseTask";
	
	private Dictionary dictionary;
	private String operationType;
	private Timestamp operationTime;
	private User operator;
	private String tempPath;
	private Task task;
	
    public Long getId() {
        return super.getId();
    }
    
	public Dictionary getDictionary() {
		return dictionary;
	}
	public void setDictionary(Dictionary dictionary) {
		this.dictionary = dictionary;
	}
	
	public String getOperationType() {
		return operationType;
	}
	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}
	
	public Timestamp getOperationTime() {
		return operationTime;
	}
	public void setOperationTime(Timestamp operationTime) {
		this.operationTime = operationTime;
	}
	
	public User getOperator() {
		return operator;
	}
	public void setOperator(User operator) {
		this.operator = operator;
	}
	
	public String getTempPath() {
		return tempPath;
	}
	public void setTempPath(String tempPath) {
		this.tempPath = tempPath;
	}
	
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}

}
