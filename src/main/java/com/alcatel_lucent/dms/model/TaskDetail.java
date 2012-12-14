package com.alcatel_lucent.dms.model;

@SuppressWarnings("serial")
public class TaskDetail extends BaseEntity {
	private Task task;
	private Text text;
	private Language language;
	private String origTranslation;
	private String newTranslation;
	private Label label;
	private String labelKey;
	private String maxLength;
	private String description;
	
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}
	public Text getText() {
		return text;
	}
	public void setText(Text text) {
		this.text = text;
	}
	public Language getLanguage() {
		return language;
	}
	public void setLanguage(Language language) {
		this.language = language;
	}
	public String getOrigTranslation() {
		return origTranslation;
	}
	public void setOrigTranslation(String origTranslation) {
		this.origTranslation = origTranslation;
	}
	public String getNewTranslation() {
		return newTranslation;
	}
	public void setNewTranslation(String newTranslation) {
		this.newTranslation = newTranslation;
	}
	public String getLabelKey() {
		return labelKey;
	}
	public void setLabelKey(String labelKey) {
		this.labelKey = labelKey;
	}
	public String getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(String maxLength) {
		this.maxLength = maxLength;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Label getLabel() {
		return label;
	}
	public void setLabel(Label label) {
		this.label = label;
	}
}
