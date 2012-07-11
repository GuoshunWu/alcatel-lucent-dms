package com.alcatel_lucent.dms.service;

import java.util.Map;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;

public interface TextService {
	
	Text getText(Long ctxId, String reference);
	
	Text addText(Long ctxId, String reference) throws BusinessException;
	
	Translation getTranslation(Long ctxId, String reference, Long languageId);
	
	Text addTranslations(Long ctxId, String reference, Map<Long, String> translations);
	
	Text addTranslations(Long textId, Map<Long, String> translations);
}
