import groovy.json.JsonBuilder

response.contentType = 'text/html'
def builder = new JsonBuilder()

while (true) {
    Thread.sleep(1000)

    builder {
        stamp System.currentTimeMillis()
        name 'Hello'
        value 'World'
    }
    html.html() {
        body() {
            script(type: 'text/javascript', """parent.${params.callback}(${builder.toPrettyString()});""")
        }
    }
}