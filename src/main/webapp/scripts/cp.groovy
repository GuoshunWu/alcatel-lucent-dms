import com.alcatel_lucent.dms.service.Worker
import groovy.json.JsonBuilder
import org.apache.commons.lang3.math.NumberUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric

Logger log = LoggerFactory.getLogger(this.getClass())

def builder = new JsonBuilder()

if ('start' == params.pqCmd) {
    String eventId = "event_${randomAlphanumeric(10)}"
    BlockingQueue<String> events = new LinkedBlockingQueue<>()
    log.info("Set an event queue [${eventId}] to session [${session.id}].")
    session[eventId] = events
    new Thread(new Worker(events, Integer.parseInt(params.speed), Integer.parseInt(params.freq)), "Worker_${session.id}_${eventId}").start()
    builder {
        pqId eventId
        event {
            cmd "process"
            msg "Start process..."
            percent Integer.valueOf(-1)
        }
    }
} else if ('process' == params.pqCmd) {
    String queueMsg
    queue = session[params.pqId]
    if (null == queue) {
        queueMsg = 'done'
    } else {
        while (queueMsg = queue.take()) {
            if (queueMsg.startsWith('done')) {
                log.info("Remove event queue [${params.pqId}] from session [${session.id}].")
                session.removeAttribute params.pqId
                break
            }
            if (queue.empty) break
        }
    }

    builder {
        pqId params.pqId
        event {
            cmd queueMsg.startsWith("done") ? "done" : "process"
            msg queueMsg.startsWith("done") ? "completed." : "In process..."
            percent NumberUtils.isNumber(queueMsg) ? Float.parseFloat(queueMsg) : Integer.valueOf(-1)
        }
    }
} else {
    builder {
        event {
            cmd "done."
            msg "none"
            percent -1
        }
    }
}

response.contentType = 'application/json'

//builder.content.date = new Date().format("yyyy-MM-dd HH:mm:ss")
log.info("sent event: ${builder.toPrettyString()}")
println builder.toPrettyString()
