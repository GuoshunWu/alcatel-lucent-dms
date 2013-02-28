jQuery ($) ->

  randomStr = (length=10, alphbet = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz')->
    rstr = ''
    for ch in alphbet
      rstr += alphbet[Math.floor Math.random() * alphbet.length]
      length--
      break if 0 == length
    rstr


  long_polling = (cmd, evtId, url, callback) ->
    postData =
      cmd: cmd
      evtId: evtId

    # initlize the test parameters
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
        callback data.msg
        return if /done/.test(data.msg)
        setTimeout (->long_polling "process", data.evtId, url, callback), pollingInterval
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

  ### create progress bar and update its progress if necessary. ###
  window.getProgressBar = ()->
    $("""
      <div id="progressbar_#{randomStr(5)}" class="progressbar">
      <div class="progressbar-label">
      Loading...
      </div>
      </div>
      """)
      .appendTo(document.body)
      .draggable(
        grid: [50, 20]
        opacity: 0.35
      ).progressbar(
        max: 100
        value: 0
        create: (e, ui) ->
          @label = $('div.progressbar-label', @)
          $(@).position(my: 'center', at: 'center')
        change: (e, ui) ->
          value = $(@).progressbar("value")
          @label.html (value.toPrecision(4)) + "%"
        complete: (e, ui) ->
        # $(@).remove()
      )

  $("#startAction").button().click (e) ->
    pb = getProgressBar()

    callback = (msg, sep = ";")->
      return if !pb
      console?.log msg
      pb.remove() if /done/.test(msg)
      $(msg.split(sep)).each (index, elem)->
        percent = parseFloat(elem) if $.isNumeric(elem)
        pb.progressbar("value", percent)
    long_polling('start', null , '../scripts/cp.groovy', callback)




