package com.alcatel_lucent.dms.controller;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.service.DictionaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

/**
 * Created by guoshunw on 2014/9/26.
 */

@Controller
@RequestMapping("/dictValidation")
public class DictionaryErrorAndWarningsController {


    private static Logger log = LoggerFactory.getLogger(DictionaryErrorAndWarningsController.class);
    @Autowired
    private DictionaryService dictionaryService;

    @RequestMapping("/errors")
    public
    @ResponseBody
    Collection<BusinessException> getDictionaryErrors(@RequestParam(value = "dictId") Long dictId) {
        log.info("dictId={}", dictId);
        Collection validations = dictionaryService.findDictionaryValidations(dictId, "errors");
        return validations;
    }


    @RequestMapping("/warnings")
    public
    @ResponseBody
    Collection<BusinessWarning> getDictionaryWarnings(@RequestParam(value = "dictId") Long dictId) {
        log.info("dictId={}", dictId);
        Collection validations = dictionaryService.findDictionaryValidations(dictId, "warnings");
        return validations;
    }

}
