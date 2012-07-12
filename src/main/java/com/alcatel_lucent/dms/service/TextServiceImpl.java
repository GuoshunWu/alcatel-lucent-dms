package com.alcatel_lucent.dms.service;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;

public class TextServiceImpl extends BaseServiceImpl implements TextService {
	
	public Context getContextByName(String name) {
		return (Context) getDao().retrieveOne(
				"from Context where name=:name",
				JSONObject.fromObject(String.format("{'name':'%s'}",
						name)));
	}
	
	public Text getText(Long ctxId, String reference) {
		Map params = new HashMap();
		params.put("reference", reference);
		params.put("contextid", ctxId);
		return (Text) dao.retrieveOne(
				"from Text where reference= :reference and context.id=:contextid",
				params);
	}
	
	public Text addText(Long ctxId, String reference) throws BusinessException {
		Context ctx = (Context) dao.retrieve(Context.class, ctxId);
		Text text = new Text();
		text.setContext(ctx);
		text.setReference(reference);
		text.setStatus(Text.STATUS_NOT_TRANSLATED);
		return (Text) dao.create(text);
	}
	
	public Translation getTranslation(Long ctxId, String reference, Long languageId) {
		Map param = new HashMap();
		param.put("ctxId", ctxId);
		param.put("reference", reference);
		param.put("languageId", languageId);
		return (Translation) dao.retrieveOne(
				"from translation " +
				"where text.context.id=:ctxId " +
				"and text.reference=:reference " +
				"and language.id=:languageId", param);
	}
	
	public Text addTranslations(Long ctxId, String reference, Map<Long, String> translations) {
		Text text = getText(ctxId, reference);
		if (text == null) {
			text = addText(ctxId, reference);
		}
		return addTranslations(text.getId(), translations);
	}
	
	public Text addTranslations(Long textId, Map<Long, String> translations) {
		Text text = (Text) dao.retrieve(Text.class, textId);
		for (Long languageId : translations.keySet()) {
			Translation dbTrans = text.getTranslation(languageId);
			if (dbTrans == null) {
				Translation trans = new Translation();
				trans.setText(text);
				trans.setLanguage((Language) dao.retrieve(Language.class, languageId));
				trans.setTranslation(translations.get(languageId));
				dao.create(trans, false);
			} else {
				dbTrans.setTranslation(translations.get(languageId));
			}
		}
		return text;
	}

}
