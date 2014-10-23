package com.alcatel_lucent.dms.controller

import com.alcatel_lucent.dms.service.DaoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

/**
 * Created by Administrator on 2014/5/17 0017.
 */

@Controller
@RequestMapping("/test/webmvc")
public class SampleController {

    @Autowired
    private DaoService dao

    @RequestMapping("/greeting")
    public String greeting(Model model) {
        model.addAttribute("message", "Hello World.")
        println "Hello, world."
        println "dao=" + dao
        com.alcatel_lucent.dms.model.Dictionary dict = dao.retrieve(com.alcatel_lucent.dms.model.Dictionary, 1L)
        println "dictionary = $dict.name $dict.version"
        "hello"
    }

    @RequestMapping("/helloExcel")
    public ModelAndView handleRequestInternal(Model model) {
        new ModelAndView("excel", ["wordList": ["hello", "world"]])
    }


}
