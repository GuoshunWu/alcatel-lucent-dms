package com.alcatel_lucent.dms;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.lang3.EnumUtils;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    // dictionary importing modes
    public enum ImportingMode {
        DELIVERY, TRANSLATION
    }

    // dictionary format

    public static final String DICT_FORMAT_DCT = "DCT";
    public static final String DICT_FORMAT_MDC = "Dictionary conf";
    public static final String DICT_FORMAT_XML_LABEL = "XML labels";
    public static final String DICT_FORMAT_XML_PROP = "XML properties";
    public static final String DICT_FORMAT_XDCT = "XMLDict";
    public static final String DICT_FORMAT_STD_EXCEL = "Standard Excel";
    public static final String DICT_FORMAT_ICE_JAVA_ALARM = "ICE Java Alarm";
    public static final String DICT_FORMAT_TEXT_PROP = "Text properties";

    public enum DictionaryFormat {

        DCT(DICT_FORMAT_DCT),
        MDC(DICT_FORMAT_MDC),
        XML_LABEL(DICT_FORMAT_XML_LABEL),
        XML_PROP(DICT_FORMAT_XML_PROP),
        XDCT(DICT_FORMAT_XDCT),
        ICE_JAVA_ALARM(DICT_FORMAT_ICE_JAVA_ALARM),
        STD_EXCEL(DICT_FORMAT_STD_EXCEL),
        TEXT_PROP(DICT_FORMAT_TEXT_PROP);


        private static Map<String, DictionaryFormat> valueEnumMap = new HashMap<String, DictionaryFormat>();

        static {
            for (DictionaryFormat format : DictionaryFormat.values()) {
                valueEnumMap.put(format.value, format);
            }
        }

        /**
         * If it is a valid dictionary format
         *
         * @param format The dictionary format.
         */
        public static boolean isValid(String format) {
            return null != DictionaryFormat.valueEnumMap.get(format);
        }

        public static DictionaryFormat getEnum(String format){
            return valueEnumMap.get(format);
        }


        private String value;

        private DictionaryFormat(String value) {
            this.value = value;
        }

        public String doubleMe() {
            return value + ":" + value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

}
