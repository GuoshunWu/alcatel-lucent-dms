import groovy.xml.MarkupBuilder
/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-7
 * Time: 上午9:37
 * To change this template use File | Settings | File Templates.
 */

html.html() {
    head() {
        title('Hello system groovlet')
    }
    body() {
        p('Hello system groovlet')
        table(border: '1') {
            tr() {
                td('Servlet container:')
                td("${application.serverInfo}")
            }
            tr() {
                td('Spring config parameter:')
                td("${application.getInitParameter('contextConfigLocation')}")
            }
            tr() {
                td('Log4j config parameter:')
                td("${application.getInitParameter('log4jConfigLocation')}")
            }
        }
    }
}