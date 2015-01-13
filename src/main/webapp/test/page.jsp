<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <title>Mangled date examples</title>
</head>
<body>
<h1>Javascript clip board test. </h1>

<div id="result"></div>

<script type="text/javascript" src="../js/lib/jquery-1.11.0.min.js"></script>
<script type="text/javascript" src="../js/jquery.paster_image_reader.js?=1"></script>
<script type="text/javascript">
    $(function ($) {
        $(document).on("paste", function(){
            alert("on paste event.");
        });

        return;
        $(document).pasteImageReader(function (content) {
            console.log("content=", content);
            var dataURL = content.dataURL;
            if (!dataURL) {
                console.warn("No data url found.");
                return;
            }
//            var img = $("<img src='" + dataURL + "'/>");
            var img = document.createElement('img');
            img.src = dataURL;
            console.log("img=%o,height=%o, width=%o", img, img.width, img.height);
            $('#result').css({backgroundImage: "url(" + dataURL + ")"}).width(img.width).height(img.height);
//            $('#result').append(img);
        });
    });
</script>

</body>
</html>