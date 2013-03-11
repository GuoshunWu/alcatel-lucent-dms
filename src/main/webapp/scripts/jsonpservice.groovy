/**
 * Following are the jsonp service
 * client call method:
 * $.getJSON('scripts/jsonpservice.groovy?callback=?').done(function(json, textStatus, jqXHR){})
 * */

response.contentType = 'text/javascript'
println "${params.callback}({'name':'Guoshun.Wu', birthday: '${new Date().format('yyyy-MM-dd')}'});"
