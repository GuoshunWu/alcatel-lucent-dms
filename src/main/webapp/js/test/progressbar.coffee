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
    pollingInterval = if $("#pollingFreq").val() then parseInt($("#pollingFreq").val()) else 1000

    timeout = if $("#timeout").val() then parseInt($("#timeout").val()) else 5000

    reTryAjax = (retryTimes = Number.MAX_VALUE, retryCounter = 0)->
      $.ajax(url,
        cache: false
        data: postData
        timeout: timeout
        type: 'post'
        dataType: "json"
      ).done((data, textStatus, jqXHR) ->
        console?.log data
        progress data.msg
        if /done/.test(data.msg)
          progress "100"
          return
        setTimeout (->long_polling "process", data.evtId), pollingInterval

      ).fail((jqXHR, textStatus, errorThrown)->
        if 'timeout' != textStatus
          console?.log "error: #{textStatus}"
          return

        if(retryTimes > 0)
          console?.log "Request #{textStatus}, I will retry in #{pollingInterval} milliseconds."
          setTimeout (->reTryAjax(--retryTimes, ++retryCounter)), pollingInterval
        else
          console?.log "I have retried #{retryCounter} times. There may be a network connection issue, please check network cable."
      )

    reTryAjax(10)


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
      $(@).hide()
      $("#startAction").button("enable").button "option", "label", "Start"
  ).hide()

  $("#startAction").button().click (e) ->
    $("#progressbar").progressbar("value", 0).show().position(my: 'center', at: 'center', of: window)

    long_polling "start", -1
    $(this).button("disable").button "option", "label", "In progress..."


