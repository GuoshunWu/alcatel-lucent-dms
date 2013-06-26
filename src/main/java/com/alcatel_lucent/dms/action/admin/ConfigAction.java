package com.alcatel_lucent.dms.action.admin;

import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.action.ProgressAction;
import com.alcatel_lucent.dms.action.ProgressQueue;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.LanguageService;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.hibernate.search.impl.SimpleIndexingProgressMonitor;
import org.hibernate.search.stat.Statistics;

import java.util.concurrent.TimeUnit;

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
