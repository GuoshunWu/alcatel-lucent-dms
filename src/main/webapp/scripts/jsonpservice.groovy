response.contentType = 'text/javascript'
println "${params.callback}({'name':'Guoshun.Wu', birthday: '${new Date().format('yyyy-MM-dd')}'});"
