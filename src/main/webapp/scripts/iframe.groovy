import groovy.json.JsonBuilder

response.contentType = 'text/html'

String buildHtml(builder) {
    builder {
        stamp System.currentTimeMillis()
        name 'Hello'
        value 'World'
    }
    "<script type:'text/javascript'>parent.${params.callback}(${builder.toPrettyString()});</script>"
}

def builder = new JsonBuilder()

while (true) {
    Thread.sleep(1000)
    println buildHtml(builder)
    out.flush()
}