<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>index.gsp</title>
</head>

<style type="text/css">
.rectangle-blue-border {
    border: 1px solid blue;
    width: 300;
    height: 200;
}
</style>

<body>
<form action="../scripts/Hello.groovy" method="get">
    P1: <input name="p1"/>
    <input type="submit"/>
</form>

<div id="dropTarget" class="rectangle-blue-border">
    Hello
</div>

<script type="text/javascript">
    "use strict";
    var dropTarget = document.getElementById("dropTarget");
    function handleEvent(e){
        e.preventDefault();
        console.log(e);

    }
    dropTarget.addEventListener("dragenter",  handleEvent, false);

</script>

</body>
</html>