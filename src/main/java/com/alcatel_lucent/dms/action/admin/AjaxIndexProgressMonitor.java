package com.alcatel_lucent.dms.action.admin;

import com.alcatel_lucent.dms.action.ProgressQueue;
import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.hibernate.search.util.LoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: guoshunw
 * Date: 13-6-25
 * Time: 下午6:11
 */
public class AjaxIndexProgressMonitor implements MassIndexerProgressMonitor {

    private static final Logger log = LoggerFactory.make();

    private final AtomicLong documentsDoneCounter = new AtomicLong();
    private final AtomicLong totalCounter = new AtomicLong();
    private volatile long startTime;
    private final int loggingPeriod;

    private ProgressQueue queue;

    /**
     * Logs progress of indexing job every 50 documents written.
     */
    public AjaxIndexProgressMonitor(ProgressQueue queue) {
        this(50, queue);
    }

    /**
     * Logs progress of indexing job every <code>loggingPeriod</code>
     * documents written.
     *
     * @param loggingPeriod the logging period
     */
    public AjaxIndexProgressMonitor(int loggingPeriod, ProgressQueue queue) {
        this.loggingPeriod = loggingPeriod;
        this.queue = queue;
    }

    @Override
    public void entitiesLoaded(int size) {
        //not used
    }

    @Override
    public void documentsAdded(long increment) {
        long current = documentsDoneCounter.addAndGet(increment);
        if (current == increment) {
            startTime = System.nanoTime();
        }
        if (current % getStatusMessagePeriod() == 0) {
            printStatusMessage(startTime, totalCounter.get(), current);
        }
    }

    @Override
    public void documentsBuilt(int number) {
        //not used
    }

    @Override
    public void addToTotalCount(long count) {
        totalCounter.addAndGet(count);
        //  set current thread related queue
        ProgressQueue.setInstance(queue);
        log.info("Going to reindex {} entities", count);
        ProgressQueue.setProgress(String.format("Going to reindex %d entities", count), 0);
    }

    @Override
    public void indexingCompleted() {
        ProgressQueue.setProgress(String.format("Reindexed %d entities", totalCounter.get()), 100);
        log.info("Reindexed {} entities", totalCounter.get());
        this.queue = null;
    }

    public AtomicLong getTotalCounter() {
        return totalCounter;
    }

    protected int getStatusMessagePeriod() {
        return loggingPeriod;
    }

    protected void printStatusMessage(long starttime, long totalTodoCount, long doneCount) {
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - starttime);
        log.info("{} documents indexed in {} ms", doneCount, elapsedMs);
        float estimateSpeed = doneCount * 1000f / elapsedMs;
        float estimatePercentileComplete = doneCount * 100f / totalTodoCount;
        log.info("Indexing speed: {} documents/second; progress: {}%", estimateSpeed, estimatePercentileComplete);
        ProgressQueue.setInstance(queue);
        ProgressQueue.setProgress(String.format("%d documents indexed in %d ms. Indexing speed: %.2f documents/second", doneCount, elapsedMs, estimateSpeed), (int) estimatePercentileComplete);
    }

}
