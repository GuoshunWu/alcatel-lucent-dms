package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryHistory;
import com.alcatel_lucent.dms.model.Task;

public interface HistoryService {
	
	DictionaryHistory logDelivery(Dictionary dictionary, String tempPath);
	void logCreateTask(Task task);
	void logReceiveTask(Task task, String tempPath);
	void logImportTask(Task task);
	void logCloseTask(Task task);

}
