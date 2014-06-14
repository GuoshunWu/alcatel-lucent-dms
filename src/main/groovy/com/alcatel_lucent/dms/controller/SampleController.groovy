package com.alcatel_lucent.dms.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

/**
 * Created by Administrator on 2014/5/17 0017.
 */

@Controller
@RequestMapping("/test/webmvc")
class SampleController {

    @RequestMapping("/greeting")
    String greeting(Model model) {
        model.addAttribute("message", "Hello World.")
        println "Hello, world."
        "hello"
    }

    @RequestMapping("/helloExcel")
    ModelAndView handleRequestInternal(Model model) {
        new ModelAndView("excel", ["wordList": ["hello", "world"]])
    }
}
