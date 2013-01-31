<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head><title>index.gsp</title></head>

<body>
<b><% println "hello gsp" %></b>

<p>
    <% wrd = "Groovy"
    for (i in wrd) {
    %>

<h1><%=i%> <br/>

    <% } %>
</p>
</body>
</html>