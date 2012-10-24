package com.alcatel_lucent.dms.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Product;
import com.alcatel_lucent.dms.model.Task;
import com.alcatel_lucent.dms.model.TaskDetail;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;

@Service("taskService")
public class TaskServiceImpl extends BaseServiceImpl implements TaskService {
	
	private static Logger log = Logger.getLogger(TaskServiceImpl.class);

	@Autowired
	private LanguageService langService;
	 
	@Override
	public Task createTask(Long productId, String name,
			Collection<Long> dictIds, Collection<Long> languageIds) {
		Task task = new Task();
		task.setName(name);
		task.setProduct((Product) dao.retrieve(Product.class, productId));
		task.setCreateTime(new Date());
		task.setStatus(Task.STATUS_OPEN);
		task = (Task) dao.create(task);
		
		log.info("Preparing translation task details...");
		String hql = "select distinct dl.language,l.text,ct " +
				"from Dictionary d join d.labels l join d.dictLanguages dl " +
				"left join l.origTranslations lt " +
				"left join l.text.translations ct " +
				"where lt.language=dl.language and ct.language=dl.language " +
				"and d.id in (:dictIds) and dl.language.id in (:langIds) " +
				"and (lt is null or lt.needTranslation=1) " +
				"and (ct is null or ct.status=:status) " +
				"order by dl.language.id,d.id,l.sortNo";
		Map param = new HashMap();
		param.put("dictIds", dictIds);
		param.put("langIds", languageIds);
		param.put("status", Translation.STATUS_UNTRANSLATED);
		Collection<Object[]> resultSet = dao.retrieve(hql, param);
		log.info("Creating " + resultSet.size() + " task details...");
		Collection<Translation> newTransList = new ArrayList<Translation>();
		if (resultSet != null) {
			for (Object[] row : resultSet) {
				Language language = (Language) row[0];
				Text text = (Text) row[1];
				Translation translation = (Translation) row[2];
				if (translation == null) {
					translation = new Translation();
					translation.setText(text);
					translation.setLanguage(language);
					translation.setTranslation(text.getReference());
					translation.setStatus(Translation.STATUS_IN_PROGRESS);
					newTransList.add(translation);
				} else {
					translation.setStatus(Translation.STATUS_IN_PROGRESS);
				}
				TaskDetail td = new TaskDetail();
				td.setTask(task);
				td.setLanguage(language);
				td.setText(text);
				td.setOrigTranslation(translation.getTranslation());
				dao.create(td, false);
			}
		}
		for (Translation trans : newTransList) {
			dao.create(trans);
		}

		return task;
	}

	@Override
	public void cancelTask(Long taskId) throws BusinessException {
		Task task = (Task) dao.retrieve(Task.class, taskId);
		if (task.getStatus() != Task.STATUS_OPEN) {
			throw new BusinessException(BusinessException.INVALID_TASK_STATUS);
		}
		task.setStatus(Task.STATUS_CANCELED);
		String hql = "select ct from Translation ct,Task t join t.details td " +
				"where td.text=ct.text and td.language=ct.language " +
				"and t.id=:taskId";
		Map param = new HashMap();
		param.put("taskId", taskId);
		Collection<Translation> transList = dao.retrieve(hql, param);
		for (Translation trans : transList) {
			trans.setStatus(Translation.STATUS_UNTRANSLATED);
		}
	}
}
