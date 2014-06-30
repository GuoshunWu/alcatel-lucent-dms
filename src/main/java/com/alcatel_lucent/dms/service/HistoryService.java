package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryHistory;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.Task;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.model.TranslationHistory;

public interface HistoryService {
	
	// DictionaryHistory methods
	DictionaryHistory logDelivery(Dictionary dictionary, String tempPath);
	void logCreateTask(Task task);
	void logReceiveTask(Task task, String tempPath);
	void logImportTask(Task task);
	void logCloseTask(Task task);

	// TranslationHistory methods
	
	/**
	 * Add a translation history object whenever a translation is changed
	 * The TranslationHistory objects are queued, necessary to call flushHistoryQueue() to save them
	 * @param trans translation object
	 * @param refLabel reference label if any
	 * @param oper operation type
	 * @param memo operation memo
	 * @return translation history object
	 */
	TranslationHistory addTranslationHistory(Translation trans, Label refLabel, int oper, String memo);
	
	/**
	 * Flush TranslationHistory objects in queue
	 */
	void flushHistoryQueue();
}
