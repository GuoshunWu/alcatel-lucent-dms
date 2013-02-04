jQuery ($) ->
  progress = (msg, sep) ->
    sep = ";"  unless sep
    $(msg.split(sep)).each (index, elem) -> $("#progressbar").progressbar "value", parseFloat(elem)  if $.isNumeric(elem)

  long_polling = (cmd, evtId) ->
    postData =
      cmd: cmd
      evtId: evtId

    if cmd is "start"
      postData.freq = (if $("#eFreq").val() then parseInt($("#eFreq").val()) else 2000)
      postData.speed = (if $("#speed").val() then parseInt($("#speed").val()) else 1000)
    postData.freq = 2  if postData.freq < 2
    postData.speed = 2  if postData.speed < 2

    $.ajax(url,
      cache: false
      data: postData
      type: 'post'
      dataType: "json"
    ).done (data, textStatus, jqXHR) ->
      console.log data  if console
      progress data.msg
      return  if /done/.test(data.msg)
      setTimeout (->
        long_polling "process", data.evtId
      ), (if $("#pollingFreq").val() then parseInt($("#pollingFreq").val()) else 1000)

  url = "../scripts/cp.groovy"
  ### Initilize the page elements ###
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
      $(@).progressbar("value", 0).hide()
      $("#startAction").button("enable").button "option", "label", "Start"
  ).hide()

  $("#startAction").button().click (e) ->
    $("#progressbar").position(of: window).show()

    long_polling "start", -1
    $(this).button("disable").button "option", "label", "In progress..."


