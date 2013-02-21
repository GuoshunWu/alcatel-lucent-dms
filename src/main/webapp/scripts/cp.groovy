import com.alcatel_lucent.dms.service.Worker
import groovy.json.JsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric

Logger log = LoggerFactory.getLogger(this.getClass())

def builder = new JsonBuilder()

if ('start' == params.cmd) {
    String eventId = "event_${randomAlphanumeric(10)}"
    BlockingQueue<String> events = new LinkedBlockingQueue<>()
    log.info("Set an event queue [${eventId}] to session [${session.id}].")
    session[eventId] = events
    new Thread(new Worker(events, Integer.parseInt(params.speed), Integer.parseInt(params.freq)), "Worker_${session.id}_${eventId}").start()
    builder {
        msg "process"
        evtId eventId
    }

} else if ('process' == params.cmd) {
    StringBuilder buf = new StringBuilder()
    String event

    if (null != session[params.evtId]) { // add the end sign to avoid it be lost when write to client
        while (event = session[params.evtId].take()) {
            buf.append(event)
            if (event.startsWith('done')) {
                log.info("Remove event queue [${params.evtId}] from session [${session.id}].")
                session.removeAttribute params.evtId
                break
            }
            if (session[params.evtId].empty) break
            buf.append(';')
        }
    } else
        buf.append('done.')
    builder {
        msg buf.toString()
        evtId params.evtId
    }
} else {
    builder {
        msg 'No operation.'
        evtId 'unknown'
    }
}

response.contentType = 'application/json'

builder.content.date = new Date().format("yyyy-MM-dd HH:mm:ss")
log.info("sent event: ${builder.toPrettyString()}")
println builder.toPrettyString()
