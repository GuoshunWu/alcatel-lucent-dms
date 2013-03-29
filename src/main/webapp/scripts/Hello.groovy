import groovy.xml.MarkupBuilder
import org.apache.commons.io.HexDump

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-7
 * Time: 上午9:37
 * To change this template use File | Settings | File Templates.
 */

html.html() {
    head() {
        title('Hello system Groovlet')
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
            tr(){
                td("Asynchronous support: ")
                td(request.asyncSupported)
            }
            tr(){
                td("Params: ")
                td(params.p1)
            }
        }
    }
}
p=params.p1
System.out.println "Parameter p1= $p"
if (null!=p){
//    System.out.println(Integer.toHexString((int)p.charAt(0))+", "+ Integer.toHexString((int)p.charAt(1)))
    p = new String(p.getBytes("iso-8859-1"))
    System.out.println(p)
}