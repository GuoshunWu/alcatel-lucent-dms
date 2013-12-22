package com.alcatel_lucent.dms.action.admin;

import com.alcatel_lucent.dms.action.ProgressAction;
import com.alcatel_lucent.dms.service.GlossaryService;

/**
 * Created by Administrator on 13-12-22.
 */
public class ApplyGlossariesAction extends ProgressAction {

    private String oper;

    public String getOper() {
        return oper;
    }

    public void setOper(String oper) {
        this.oper = oper;
    }

    private GlossaryService glossaryService;

    public void setGlossaryService(GlossaryService glossaryService) {
        this.glossaryService = glossaryService;
    }

    @Override
    protected String performAction() throws Exception {
        glossaryService.consistentGlossaries();

        setStatus(0);
        setMessage(getText("message.success"));
        return SUCCESS;
    }
}
