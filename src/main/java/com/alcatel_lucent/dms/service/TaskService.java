package com.alcatel_lucent.dms.service;

import java.io.File;
import java.util.Collection;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.Task;

public interface TaskService {
	
	/**
	 * Create a translation task.
	 * @param productId product id
	 * @param name task name
	 * @param dictIds list of dictionary id
	 * @param languageIds list language id
	 * @return Task object created
	 */
	Task createTask(Long productId, String name, Collection<Long> dictIds, Collection<Long> languageIds);
	
	/**
	 * Close a task, all "In progress" translation will be replaced by "Not translated"
	 * @param taskId task id
	 */
	void closeTask(Long taskId) throws BusinessException;
	
	/**
	 * Generate translation task files in target directory.
	 * @param targetDir target directory
	 * @param taskId task id
	 */
	void generateTaskFiles(String targetDir, Long taskId);

}
