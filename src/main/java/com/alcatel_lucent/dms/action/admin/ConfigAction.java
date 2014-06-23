package com.alcatel_lucent.dms.action.admin;

import com.alcatel_lucent.dms.action.ProgressAction;
import com.alcatel_lucent.dms.action.ProgressQueue;
import com.alcatel_lucent.dms.service.DaoService;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;

@SuppressWarnings("serial")
public class ConfigAction extends ProgressAction {

    public DaoService getDaoService() {
        return daoService;
    }

    public void setDaoService(DaoService daoService) {
        this.daoService = daoService;
    }

    private DaoService daoService;

    @Override
    protected String performAction() throws Exception {
        FullTextSession fullTextSession = Search.getFullTextSession(daoService.getSession());
        final ProgressQueue queue = ProgressQueue.getInstance();
        AjaxIndexProgressMonitor monitor = new AjaxIndexProgressMonitor(queue);
        fullTextSession.createIndexer().progressMonitor(monitor).startAndWait();
        setMessage(String.format("Reindexed %d entities successful.", monitor.getTotalCounter().get()));
        return SUCCESS;
    }

}
