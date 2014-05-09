package com.alcatel_lucent.dms.service.dao;

import com.alcatel_lucent.dms.model.TranslationMatch;
import com.alcatel_lucent.dms.service.DaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: guoshunw
 * Date: 13-6-14
 * Time: 上午10:50
 */


@Repository
public class TranslationLuceneDao {

    private static Logger log = LoggerFactory.getLogger(TranslationLuceneDao.class);
    @Autowired
    private DaoService daoService;


    public List<TranslationMatch> getAllTranslationMatch(Long transId, Long langId, String text,
                                                         Integer firstResult, Integer maxResult, String sIdx, String sOrd, Boolean fuzzy) {

        return null;
    }

    public List<TranslationMatch> getAllTranslationMatch(Long transId, Long langId, String text,
                                                         Integer firstResult, Integer maxResult, String sIdx, String sOrd) {
        return getAllTranslationMatch(transId, langId, text, firstResult, maxResult, sIdx, sOrd, true);
    }
}
