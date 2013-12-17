package com.alcatel_lucent.dms.service

import groovy.util.slurpersupport.GPathResult
import org.dom4j.Document
import org.dom4j.io.SAXReader

/**
 * Created with IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 13-12-1
 * Time: 下午10:13
 */

File pomFile = new File(getClass().getResource("/").path + "../../", "pom.xml")
GPathResult document = new XmlSlurper().parse(pomFile)
document.dependencies.dependency.each{

    println "${it.scope!=''?it.scope:'runtime'} \"${it.groupId}:${it.artifactId}:${it.version}\""
}