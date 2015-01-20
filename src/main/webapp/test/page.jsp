<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <title>Mangled date examples</title>
    <style type="text/css" media="screen" >
        #result{
            width: 400px;
            height: 300px;
            border: dashed #6a5acd;
        }
    </style>
</head>
<body>
<h1>Javascript clip board test. </h1>

<div id='result' contenteditable='true'>Paste</div>

<script type="text/javascript" src="../js/lib/jquery-1.11.0.min.js"></script>
<script type="text/javascript" src="../js/jquery.paster_image_reader.js?=1"></script>
<script type="text/javascript">

    $(function ($) {

        $('#result').pasteImageReader(function (content) {
            console.log("content=", content);
            var dataURL = content.dataURL;
            if (!dataURL) {
                console.warn("No data url found.");
                return;
            }
            var img = new Image();
            img.src = dataURL;
            console.log("img=%o,height=%o, width=%o, this=%o", img, img.width, img.height, this);
            this.css({backgroundImage: "url(" + dataURL + ")"}).width(img.width).height(img.height);
//            $('#result').append(img);
        });
    });
</script>

</body>
</html>