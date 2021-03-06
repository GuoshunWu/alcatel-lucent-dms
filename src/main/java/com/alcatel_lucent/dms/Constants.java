package com.alcatel_lucent.dms;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.lang3.EnumUtils;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static final int MAX_TEXT_LENGTH = 65535;

    // dictionary importing modes
    public enum ImportingMode {
        DELIVERY,			// take translated strings only, with overwrite, used when import dictionary, add language, update context
        TRANSLATION,		// take all translations, overwrite, used when import translation, receive task, manual update translation
        SUPPLEMENT,			// take translated strings only, no overwrite, used when supply DEFAULT context automatically
        TEST				// same as DELIVERY except that all changes will be rolled back at the end of transaction
    }

    // dictionary format

    public enum DictionaryFormat {

        DCT("DCT"),
        MDC("Dictionary conf"),
        XML_LABEL("XML labels"),
        XML_PROP("XML properties"),
        XML_ANDROID("XML android"),
        XML_Help("XML help"),
        XDCT("XMLDict"),
        ICE_JAVA_ALARM("ICE Java Alarm"),
        STD_EXCEL("Standard Excel"),
        TEXT_PROP("Text properties"),
        ACS_TEXT("ACS text"),
        OTC_EXCEL("OTC Excel"),
        OTC_WEB("OTC Web"),
        VOICE_APP("Voice App"),
        PO("PO"),
        VITAL_SUITE("Vital Suite"),
        NOE_STRING("NOE String"),
        IOS_RESOURCE("iOS Resource"),
        TS("TS"),
        TMX("TMX");


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

        public static DictionaryFormat getEnum(String format) {
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
