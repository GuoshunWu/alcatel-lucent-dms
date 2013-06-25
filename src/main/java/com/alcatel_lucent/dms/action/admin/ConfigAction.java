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
//public class ConfigAction extends ProgressAction {
public class ConfigAction extends JSONAction {

    public DaoService getDaoService() {
        return daoService;
    }

    public void setDaoService(DaoService daoService) {
        this.daoService = daoService;
    }

    private DaoService daoService;

    @Override
    // TODO: printStatusMessage in event

    protected String performAction() throws Exception {
        log.info("ConfigAction...");
        FullTextSession fullTextSession = Search.getFullTextSession(daoService.getSession());

        fullTextSession.createIndexer().progressMonitor(new SimpleIndexingProgressMonitor() {
            @Override
            protected void printStatusMessage(long starttime, long totalTodoCount, long doneCount) {
                long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - starttime);
                log.info("{} documents indexed in {} ms", doneCount, elapsedMs);
                float estimateSpeed = doneCount * 1000f / elapsedMs;
                float estimatePercentileComplete = doneCount * 100f / totalTodoCount;
                log.info("Indexing speed: {} documents/second; progress: {}%", estimateSpeed, estimatePercentileComplete);
                ProgressQueue.setProgress(
                        String.format("{} documents indexed in {} ms. Indexing speed: {} documents/second", doneCount, elapsedMs, estimateSpeed),
                        (int) estimatePercentileComplete);

            }

            @Override
            public void indexingCompleted() {
                super.indexingCompleted();
                ProgressQueue.setProgress("Reindexed entities complete.", 100);
            }

            @Override
            public void addToTotalCount(long count) {
                super.addToTotalCount(count);
            }
        }).startAndWait();
        setMessage("Index create successful.");
        return SUCCESS;
    }

}
