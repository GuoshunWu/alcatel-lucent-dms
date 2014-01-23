package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.model.Language;
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

    @Autowired
    public void setDao(DaoService dao) {
        this.dao = dao;
    }


    @Override
    public PreferredTranslation createPreferredTranslation(String ref, String trans, String comment, Long languageId) {
        User user = UserContext.getInstance().getUser();
//        PreferredTranslation pt = new PreferredTranslation();
//        pt.setReference(ref);
//        pt.setTranslation(trans);
//        pt.setComment(comment);
//        pt.setLanguage((Language) dao.retrieve(Language.class, languageId));
//        pt.setCreator(user);
//        pt = (PreferredTranslation) dao.create(pt);

//        return pt;
        return null;
    }

    @Override
    public PreferredTranslation updatePreferredTranslation(Long id, String ref, String trans, String comment) {
        PreferredTranslation pt = (PreferredTranslation) dao.retrieve(PreferredTranslation.class, id);
//        if (null == pt) return null;
//        if(null!= ref) pt.setReference(ref);
//        if(null!= trans) pt.setTranslation(trans);
//        if(null!= comment) pt.setComment(comment);

        return pt;
    }

    @Override
    public void deletePreferredTranslations(Collection<Long> ids) {
        String hSQL = "delete PreferredTranslation where id in :ids";
        Map params = new HashMap();
        params.put("ids", ids);
        dao.delete(hSQL, params);
    }
}
