package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.PreferredReference;
import com.alcatel_lucent.dms.model.PreferredTranslation;
import com.alcatel_lucent.dms.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 13-7-25
 * Time: PM 3:07
 */
@Service("preferredTranslationService")
public class PreferredTranslationServiceImpl  extends  BaseServiceImpl implements PreferredTranslationService {

    private static Logger log = LoggerFactory.getLogger(PreferredTranslationServiceImpl.class);


    @Override
    public PreferredTranslation updatePreferredTranslation(Long preferredReferenceId, Long id, Long languageId, String translation, String comment) {
        PreferredTranslation pt = (PreferredTranslation) dao.retrieve(PreferredTranslation.class, id);
        if (null == pt){
            pt = new PreferredTranslation();
            pt.setLanguage((Language) dao.retrieve(Language.class, languageId));
            pt.setCreator(UserContext.getInstance().getUser());
            pt.setPreferredReference((PreferredReference) dao.retrieve(PreferredReference.class, preferredReferenceId));
            pt = (PreferredTranslation) dao.create(pt);
        }

        if(null!= translation) pt.setTranslation(translation);
        if(null!= comment) pt.setComment(comment);

        return pt;
    }
}
