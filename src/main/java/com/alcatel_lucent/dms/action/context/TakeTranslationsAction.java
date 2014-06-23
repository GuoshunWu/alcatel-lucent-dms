package com.alcatel_lucent.dms.action.context;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.rest.TranslationPair;
import com.alcatel_lucent.dms.service.TranslationService;
import com.google.common.collect.Sets;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class TakeTranslationsAction extends JSONAction {
	
	private TranslationService translationService;

    private String translationPairs;

	@Override
	protected String performAction() throws Exception {
		log.info("[TakeTranslationsAction] translationPairs=" + translationPairs);
        Collection<TranslationPair> pairs = converter(translationPairs);
        translationService.takeTranslations(pairs);
        return SUCCESS;
	}

    public TranslationService getTranslationService() {
        return translationService;
    }

    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

    public String getTranslationPairs() {
        return translationPairs;
    }

    public void setTranslationPairs(String translationPairs) {
        this.translationPairs = translationPairs;
    }

    private Collection<TranslationPair> converter(String JSONString){
        JSONArray array = JSONArray.fromObject(JSONString);
        Collection<TranslationPair> pairs = new ArrayList<TranslationPair>();
        for(Object obj: array){
            JSONObject jsonObj = (JSONObject) obj;
            Translation a = new Translation();
            a.setId(jsonObj.getLong("textA"));
            Translation b = new Translation();
            b.setId(jsonObj.getLong("textB"));
            TranslationPair pair = new TranslationPair(a, b);
            pair.setTake(jsonObj.getString("take"));
            pairs.add(pair);
        }
        return pairs;
    }
}
