import com.alcatel_lucent.dms.service.Worker
import groovy.json.JsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

Logger log = LoggerFactory.getLogger(this.getClass())

def builder = new JsonBuilder()
builder { content.date = new Date().format("yyyy-MM-dd HH:mm:ss") }

if ('start' == params.cmd) {
    String eventId = "event_${new Random(System.currentTimeMillis()).nextLong()}"
    BlockingQueue<String> events = new LinkedBlockingQueue<>()
    log.info("Set an event queue [${eventId}] to session [${session.id}].")
    session[eventId] = events
    new Thread(new Worker(events), "Worker_${session.id}_${eventId}").start()
    builder {
        msg "process"
        evtId eventId
    }

} else if ('process' == params.cmd) {
    String event = session[params.evtId].take()
    builder {
        msg event
        evtId params.evtId
    }
    if (event.startsWith('done')) {
        log.info("Remove event queue [${params.evtId}] from session [${session.id}].")
        session.removeAttribute params.evtId
    }
} else {
    builder {
        msg 'No operation.'
        evtId 'unknown'
    }
}

response.contentType = 'application/json'
println builder.toPrettyString()
