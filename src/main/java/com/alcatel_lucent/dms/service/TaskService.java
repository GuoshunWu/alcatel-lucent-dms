package com.alcatel_lucent.dms.service;

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
	 * Cancel a task.
	 * @param taskId task id
	 */
	void cancelTask(Long taskId) throws BusinessException;

}
