import com.sun.swing.internal.plaf.synth.resources.synth_sv
import groovy.json.JsonBuilder

/**
 * Following are the jsonp service
 * client call method:
 * $.getJSON('scripts/jsonpservice.groovy?callback=?').done(function(json, textStatus, jqXHR){})
 * */
def builder = new JsonBuilder()

//Response json
builder {
    name 'Guoshun.Wu'
    birthday new Date().format('yyyy-MM-dd HH:mm:ss.SSS')
}


/**
 * JSONP service call example
 * */
//response.contentType = 'text/javascript'
//println "${params.callback}(${builder..toPrettyString()});"

// JSON
response.contentType = "application/json"

System.out.println "Got Parameters: ${params}"
//System.out.println("JSON to write back:${builder.toPrettyString()}")
println builder.toPrettyString()