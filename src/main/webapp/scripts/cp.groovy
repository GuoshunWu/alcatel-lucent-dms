import javax.servlet.AsyncContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

//import groovy.swing.SwingBuilder
//
//import javax.swing.JFrame
//
//def swingBuilder = new SwingBuilder()
//swingBuilder.frame(title: 'Hello Groovy Swing',
//        defaultCloseOperation: JFrame.HIDE_ON_CLOSE,
//        size: [200, 300],
//        show: true
//) {
//    panel() {
//        button(text: "Test", actionPerformed: {
//            println "Hello, world."
//        })
//        label("Hello Groovy Swing.")
//    }
//}
int fileSize = 10000
int currentProgress = 0
boundary = ('A'..'T').join()

Random r = new Random()

AsyncContext asyncContext = request.startAsync();

HttpServletRequest req = (HttpServletRequest) asyncContext.getRequest();
HttpServletResponse res = (HttpServletResponse) asyncContext.getResponse();
res.contentType = 'text/html'
res.setStatus(HttpServletResponse.SC_OK)

System.out.println("=========A response to ${request.remoteAddr} ==========")

while (currentProgress < fileSize) {
    percent = (float) currentProgress / fileSize * 100

    res.writer.println(String.format("%2.1f%%", percent))
    res.flushBuffer()

//    out.println("console.log('${String.format("%2.1f%%", percent)}');")
    currentProgress += r.nextInt(1000)
    try {
        Thread.sleep(1000)
    } catch (InterruptedException e) {
        out.println("<h4>${e}</h4>")
    }
}

asyncContext.complete()
