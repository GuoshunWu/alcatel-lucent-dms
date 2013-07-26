package com.alcatel_lucent.dms.action.admin;

import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.GlossaryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

@SuppressWarnings("serial")
public class GlossaryAction extends JSONAction {

    private String oper;
    private String text;
    private String newText;

    public String getNewText() {
        return newText;
    }

    public void setNewText(String newText) {
        this.newText = newText;
    }

    @Autowired
    private GlossaryService glossaryService;


    @Override
    protected String performAction() throws Exception {
        log.info(this.getClass().getSimpleName() + ": oper=" + oper + ", text=" + text);
        if (oper.equals("add")) {
            glossaryService.createGlossary(text);
        } else if (oper.equals("edit")) {
            glossaryService.updateGlossary(text, newText);
        } else if (oper.equals("del")) {
            glossaryService.deleteGlossaries(Arrays.asList(text.split(",")));
        } else {
            throw new SystemError("Unknown oper: " + oper);
        }
        return SUCCESS;
    }


    public String getOper() {
        return oper;
    }

    public void setOper(String oper) {
        this.oper = oper;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
