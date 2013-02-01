import com.alcatel_lucent.dms.service.Worker
import groovy.json.JsonBuilder

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

def builder = new JsonBuilder()
builder {
    content.date = new Date().format("yyyy-MM-dd HH:mm:ss")
}

if ('start' == params.cmd) {
    BlockingQueue<String> events = session.events
    if (null == events) {
        events = new LinkedBlockingQueue<String>();
        session.events = events
    }
    events.clear()
    new Thread(new Worker(events), "Worker_${session.id}").start()
    builder.content.msg = "process"
} else if ('process' == params.cmd) {
    String event = session.events.take()
    builder.content.msg = event
} else {
    builder.content.msg = 'No operation.'
}

response.contentType = 'application/json'
println builder.toPrettyString()
