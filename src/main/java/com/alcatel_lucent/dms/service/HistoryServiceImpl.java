package com.alcatel_lucent.dms.service;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryHistory;
import com.alcatel_lucent.dms.model.Task;
import com.alcatel_lucent.dms.model.User;

@Service("historyService")
public class HistoryServiceImpl extends BaseServiceImpl implements HistoryService {

	@Override
	public DictionaryHistory logDelivery(Dictionary dictionary, String tempPath) {
		User operator = UserContext.getInstance().getUser();
		DictionaryHistory history = new DictionaryHistory();
		history.setDictionary(dictionary);
		history.setOperationType(DictionaryHistory.OP_DELIVER);
		history.setOperationTime(new Timestamp(System.currentTimeMillis()));
		history.setOperator((User) dao.retrieve(User.class, operator.getLoginName()));
		history.setTempPath(tempPath);
		history = (DictionaryHistory) dao.create(history);
		return history;
	}
	
	private void logTask(Task task, String operationType, String tempPath) {
		User operator = UserContext.getInstance().getUser();
		operator = (User) dao.retrieve(User.class, operator.getLoginName());
		Timestamp now = new Timestamp(System.currentTimeMillis());
		Collection<Dictionary> dictionaries = findDictionariesByTask(task.getId());
		for (Dictionary dictionary : dictionaries) {
			DictionaryHistory history = new DictionaryHistory();
			history.setDictionary(dictionary);
			history.setOperationType(operationType);
			history.setOperationTime(now);
			history.setOperator(operator);
			history.setTask(task);
			history.setTempPath(tempPath);
			dao.create(history);
		}
	}

	private Collection<Dictionary> findDictionariesByTask(Long taskId) {
		String hql = "select distinct td.label.dictionary from TaskDetail td where td.task.id=:taskId";
		Map param = new HashMap();
		param.put("taskId", taskId);
		return dao.retrieve(hql, param);
	}

	@Override
	public void logCreateTask(Task task) {
		logTask(task, DictionaryHistory.OP_CREATE_TASK, null);
	}

	@Override
	public void logReceiveTask(Task task, String tempPath) {
		logTask(task, DictionaryHistory.OP_RECEIVE_TASK, tempPath);
	}
	
	@Override
	public void logImportTask(Task task) {
		logTask(task, DictionaryHistory.OP_IMPORT_TASK, null);
	}

	@Override
	public void logCloseTask(Task task) {
		logTask(task, DictionaryHistory.OP_CLOSE_TASK, null);
	}

}