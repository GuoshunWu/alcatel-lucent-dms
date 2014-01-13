import groovy.json.JsonBuilder

/**
 * Created by guoshunw on 14-1-7.
 */

JsonBuilder builder = new JsonBuilder()

//Response json
def root = builder {
    name 'Guoshun.Wu'
    birthday new Date().format('yyyy-MM-dd HH:mm:ss.SSS')
}


response.contentType = "application/json"
int times = 5;
while (times-- > 0) {
    root['birthday'] = new Date().format('yyyy-MM-dd HH:mm:ss.SSS')

    out.println builder.toPrettyString()
    out.flush()
    Thread.sleep(500)
}
