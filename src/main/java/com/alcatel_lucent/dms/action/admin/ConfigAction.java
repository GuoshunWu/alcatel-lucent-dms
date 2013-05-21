package com.alcatel_lucent.dms.action.admin;

import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.LanguageService;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.impl.SimpleIndexingProgressMonitor;
import org.hibernate.search.stat.Statistics;

@SuppressWarnings("serial")
public class ConfigAction extends JSONAction {

    public DaoService getDaoService() {
        return daoService;
    }

    public void setDaoService(DaoService daoService) {
        this.daoService = daoService;
    }

    private DaoService daoService;
	


	@Override
	protected String performAction() throws Exception {
		log.info("ConfigAction...");
        FullTextSession fullTextSession = Search.getFullTextSession(daoService.getSession());
        fullTextSession.createIndexer().startAndWait();
        Statistics statistics=fullTextSession.getSearchFactory().getStatistics();


        setMessage("Index create successful.");
		return SUCCESS;
	}

}
