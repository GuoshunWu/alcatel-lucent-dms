package com.alcatel_lucent.dms.service

/**
 * Created with IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 13-11-5
 * Time: 下午8:59
 */
class DeployAdjust {

    static void adjustFile(File file) {
        if (!file.exists()) {
            println "File ${file.name} not exist, please make war:exploded first."
            return
        }
        println "Adjust file: ${file.name}".center(100, '=')
        String pattern = "(.*)(\\s*<script.*)(data-main=\\\"(.*)\\\"\\s*)(src=\\\")(js/lib/require\\.js)(\\\">\\s*</script>\\s*)(.*)"
        String replacedFileText = file.text.replaceAll pattern, "\$1\$2\$5\$4\$7\$8"
        file.withPrintWriter { out ->
            out.print replacedFileText
        }
    }

    static void main(String... args) {
        adjustFile  new File(System.properties.getProperty("main.jsp"))
        adjustFile  new File(System.properties.getProperty("login.jsp"))
    }
}