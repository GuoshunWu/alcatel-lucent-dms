package com.alcatel_lucent.dms.service;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Task;

public interface TaskService {
	
	/**
	 * Create a translation task.
	 * @param productId product id
	 * @param appId application id, null if the task is created on product level
	 * @param name task name
	 * @param dictIds list of dictionary id
	 * @param languageIds list language id
	 * @return Task object created
	 */
	Task createTask(Long productId, Long appId, String name, Collection<Long> dictIds, Collection<Long> languageIds);
	
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
	
	/**
	 * Receive translation result from an extracted file folder
	 * @param taskId task id
	 * @param taskDir translation task files folder
	 * @return Task object
	 * @throws BusinessException
	 */
	Task receiveTaskFiles(Long taskId, String taskDir) throws BusinessException;
	
	/**
	 * Apply translation results in the task to context dictionaries.
	 * The translation result will be ignored in following cases:
	 *   the translation value is empty, ignore it.
	 *   the translation value equals to reference and markAllTranslated parameter is set to false
	 * @param taskId task id
	 * @param markAllTranslated indicate whether to mark all strings as translated, even if it's same as reference
	 * @return Task object
	 * @throws BusinessException
	 */
	Task applyTask(Long taskId, boolean markAllTranslated) throws BusinessException;
	
	/**
	 * Calculate task summary
	 * @param taskId task id
	 * @return first level key is app base id, second level key is language id, value is [translated, not translated]
	 */
	Map<Long, Map<Long, int[]>> getTaskSummary(Long taskId);

}
