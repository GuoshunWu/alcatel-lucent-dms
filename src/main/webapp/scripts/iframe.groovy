/**
 * Following are the jsonp service
 * client call method:
 * $.getJSON('scripts/jsonpservice.groovy?callback=?').done(function(json, textStatus, jqXHR){})
 * */

html.html() {
    head() {
        title('Hello push html.')
        script(src:"../js/lib/jquery-1.7.2.min.js")
        script("""
        alert('Hello, world.');
        console.log(\$('#content', document).get(0));
       """)
    }
    body() {
        h1("This is a test.")
    }
}
