package com.alcatel_lucent.dms.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@SuppressWarnings("serial")

@Entity
@Table(name = "TASK_DETAIL")
public class TaskDetail extends BaseEntity {

    @Id
    @GeneratedValue(generator = "HILO_GEN")
    @TableGenerator(name = "HILO_GEN", table = "ID_TASK_DETAIL")
    @Column(name = "ID")
    @Override
    public Long getId() {
        return super.getId();
    }

    private Task task;
    private Text text;
    private Language language;
    private String origTranslation;
    private String newTranslation;
    private Label label;
    private String labelKey;
    private String maxLength;
    private String description;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "TASK_ID", nullable = false)
    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TEXT_ID", nullable = false)
    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    @ManyToOne
    @JoinColumn(name = "LANGUAGE_ID", nullable = false)
    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Column(name = "ORIG_TRANSLATION", length = 1024)
    public String getOrigTranslation() {
        return origTranslation;
    }

    public void setOrigTranslation(String origTranslation) {
        this.origTranslation = origTranslation;
    }

    @Column(name = "NEW_TRANSLATION", length = 1024)
    public String getNewTranslation() {
        return newTranslation;
    }

    public void setNewTranslation(String newTranslation) {
        this.newTranslation = newTranslation;
    }

    @Column(name = "LABEL_KEY")
    public String getLabelKey() {
        return labelKey;
    }


    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    @Column(name = "MAX_LENGTH")
    public String getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(String maxLength) {
        this.maxLength = maxLength;
    }

    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne
    @JoinColumn(name = "LABEL_ID", nullable = false)
    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }
}
