package com.alcatel_lucent.dms.model;

import javax.persistence.Transient;
import java.sql.Timestamp;

@SuppressWarnings("serial")
public class TranslationHistory extends BaseEntity {
	
	public static final int TRANS_OPER_DELIVER = 1;	// delivered from dictionary, with refLabel
	public static final int TRANS_OPER_RECEIVE = 2;	// received from task, with refLabel
	public static final int TRANS_OPER_NEW = 3;	// new label/language created in DMS, or change label reference/context, with refLabel
	public static final int TRANS_OPER_INPUT = 4;	// manually inputted or imported from Excel into DMS, with or without refLabel
	public static final int TRANS_OPER_CAPITALIZE = 5;	// changed by capitalization, with refLabel
	public static final int TRANS_OPER_GLOSSARY = 6;	// changed by glossary matching, without refLabel
	public static final int TRANS_OPER_SUGGEST = 7; // suggested by DMS, with or without refLabel
	public static final int TRANS_OPER_STATUS = 8;	// manually change translation status
	
	private Translation parent;
	private Long refLabelId;
	private String translation;
	private Integer status;
	
	private int operationType;
	private Timestamp operationTime;
	private User operator;
	private String memo;

    private Label historyLabel;

    @Transient
    public Label getHistoryLabel() {
        return historyLabel;
    }

    public void setHistoryLabel(Label historyLabel) {
        this.historyLabel = historyLabel;
    }

    public Translation getParent() {
		return parent;
	}
	public void setParent(Translation parent) {
		this.parent = parent;
	}
	public String getTranslation() {
		return translation;
	}
	public void setTranslation(String translation) {
		this.translation = translation;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public int getOperationType() {
		return operationType;
	}
	public void setOperationType(int operationType) {
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
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public Long getRefLabelId() {
		return refLabelId;
	}
	public void setRefLabelId(Long refLabelId) {
		this.refLabelId = refLabelId;
	}

}
