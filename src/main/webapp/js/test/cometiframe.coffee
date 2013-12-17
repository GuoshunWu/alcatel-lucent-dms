# additional help prototype method
String::trim = ()->@replace(/(^\s*)|(\s*$)/g, "")


loadScript = (src, doc = window.document, async = true, charset = 'utf-8', flag = 'nothing')->
  head = doc.getElementsByTagName('head')[0];
  script = doc.createElement('script');

  script.type = 'text/javascript';
  script.charset = charset;
  script.async = async;
  script.src = src;
  head.appendChild(script);

jQuery(($)->
  $("#accessIframe").button().click ()->
    if frames.length == 0
      console.log "There is no frame in this page."
      return false

    false
    iBody = frames[0].document;
    console.log $(iBody).text().trim()
    false

  progress = (msg, sep) ->
    sep = ";"  unless sep
    $(msg.split(sep)).each (index, elem) -> $("#progressbar").progressbar "value", parseFloat(elem)  if $.isNumeric(elem)

  $("#pushStart").button().on 'click', pushForm: $('#pushForm'),(e)->
    window[$("input[name='callback']").val()] = (data)->
      console.log(data)
      progress data.msg
      $('#content').html "#{new Date(data.stamp).toLocaleString()}<br/>Msg: #{data.msg}"
      if /^done/.test(data.msg)
        $("#pushStart").button("enable")
        delete window[$("input[name='callback']").val()]

    $("#progressbar").progressbar("value", 0).show().position(my: 'center', at: 'center', of: window)
    $("#pushStart").button("disable")

    e.data.pushForm.submit()


  $("#progressbar").draggable(
    grid: [50, 20]
    opacity: 0.35
  ).progressbar(
    max: 100
    create: (e, ui) ->
      @label = $('div.progressbar-label', @)
      $(@).position(my: 'center', at: 'center', of: window)
    change: (e, ui) ->
      @label.html ($(this).progressbar("value").toPrecision(4)) + "%"
    complete: (e, ui) ->
      $(@).hide()
  ).hide()

)
