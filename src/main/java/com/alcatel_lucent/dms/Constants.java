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
    public enum DictionaryFormat {
        DCT("DCT"),
        MDC("Dictionary conf"),
        XML_LABEL("XML labels"),
        XML_PROP("XML properties"),
        XDCT("XMLDict"),
        ICE_JAVA_ALARM("ICE Java Alarm"),
        STD_EXCEL("Standard Excel"),
        TEXT_PROP("Text properties");

        private static Map<String, DictionaryFormat> valueEnumMap = new HashMap<String, DictionaryFormat>();

        static {
            for (DictionaryFormat format : DictionaryFormat.values()) {
                valueEnumMap.put(format.value, format);
            }
        }

        private String value;

        private DictionaryFormat(String value) {
            this.value = value;
        }

        /**
         * If it is a valid dictionary format
         *
         * @param format The dictionary format.
         */
        public static boolean isValid(String format) {
            return null != DictionaryFormat.valueEnumMap.get(format);
        }

        public String doubleMe() {
            return value + ":" + value;
        }

        public static DictionaryFormat getEnum(String format){
            return valueEnumMap.get(format);
        }

        @Override
        public String toString() {
            return value;
        }
    }

}
