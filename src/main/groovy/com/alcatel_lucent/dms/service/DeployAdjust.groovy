package com.alcatel_lucent.dms.service

/**
 * Created with IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 13-11-5
 * Time: 下午8:59
 */
class DeployAdjust {

    /**
     * Replace data-main=xxx src=yyy?dust=currentTimeStamp as src=xxx and remove data-main when deploy
     *    example:
     *    before adjust: <script type="text/javascript" data-main="js/loginEntry.js" src="js/lib/require.js"></script>
     *    after  adjust: <script type="text/javascript" src="js/loginEntry.js"></script>
     * */
    static void adjustFile(File file) {
        if (!file.exists()) {
            println "File ${file.name} not exist, please make war:exploded first."
            return
        }
        String buildNumber = System.properties.getProperty("buildNumber")
        if (null == buildNumber || buildNumber.isEmpty()) buildNumber = System.currentTimeMillis() + ""

        println "Adjust file: ${file.name}".center(100, '=')
        String pattern = "(.*)(\\s*<script.*)(data-main=\\\"(.*)\\\"\\s*)(src=\\\")(js/lib/require\\.js)(\\\">\\s*</script>\\s*)(.*)"
        String replacedFileText = file.text.replaceAll pattern, "\$1\$2\$5\$4.js?dust=${buildNumber}\$7\$8"
        file.withPrintWriter { out ->
            out.print replacedFileText
        }
    }

    static void main(String... args) {
        def prop = System.properties.getProperty("main.jsp")
        if (null != prop) adjustFile new File(prop)
        prop = System.properties.getProperty("login.jsp")
        if (null != prop) adjustFile new File(prop)
    }
}
