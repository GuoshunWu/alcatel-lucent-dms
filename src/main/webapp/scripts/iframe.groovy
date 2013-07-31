import com.alcatel_lucent.dms.service.Worker
import groovy.json.JsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

Logger log = LoggerFactory.getLogger(this.getClass())

response.contentType = 'text/html'
JsonBuilder jsonBuilder = new JsonBuilder()

String buildHtml(event, builder = jsonBuilder) {
    builder {
        stamp System.currentTimeMillis()
        msg event
    }
    "<script>parent.${params.callback}(${builder.toPrettyString()});</script>"
}

BlockingQueue<String> events = new LinkedBlockingQueue<>()
new Thread(new Worker(events, Integer.parseInt(params.speed), Integer.parseInt(params.freq)), "Worker_${session.id}").start()
fout = new PrintWriter(out, true)
log.info "params: ${params}."

while (event = events.take()) {
    String toClient = buildHtml(event, jsonBuilder)
    log.info("To client info: ${toClient}")
    fout.println(toClient)
    if (event.startsWith('done')) break
}