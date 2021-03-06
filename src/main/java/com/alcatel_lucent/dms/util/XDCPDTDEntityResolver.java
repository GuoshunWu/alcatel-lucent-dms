package com.alcatel_lucent.dms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;


/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-12-19
 * Time: 上午11:19
 * To change this template use File | Settings | File Templates.
 */
public class XDCPDTDEntityResolver implements EntityResolver {

    protected static Logger log = LoggerFactory.getLogger(XDCPDTDEntityResolver.class);

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
//        System.out.println(String.format("publicId=%s, systemId=%s.", publicId == null ? "null" : publicId, systemId == null ? "null" : systemId));
        return new InputSource(getClass().getResourceAsStream("/dtds/XMLPROJECT.dtd"));
    }
}
