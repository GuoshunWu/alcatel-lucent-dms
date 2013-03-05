jQuery ($) ->

  base = '/dms'

  randomStr = (length = 10, alphbet = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz')->
    rstr = ''
    for ch in alphbet
      rstr += alphbet[Math.floor Math.random() * alphbet.length]
      length--
      break if 0 == length
    rstr

  ###
    generate a progress bar
  ###
  window.genProgressBar = (autoDispaly = true, autoRemoveWhenCompleted=true)->
    randStr = randomStr(5)
    pbContainer=$("""
                  <div id="pb_container_#{randStr}"  class="progressbar-container ui-widget-content">
                  <div class="progressbar-msg">
                  Loading...
                  </div>
                  <div id="progressbar_#{randStr}" class="progressbar progressbar-indeterminate">
                  <div class="progressbar-label">0.00%</div>
                  </div>
                  </div>
                  """).appendTo(document.body)
      .draggable(
        create: ()->
          $("#progressbar_#{randStr}", @).progressbar(
            max: 100
            create: (e, ui) ->
              @label = $('div.progressbar-label', @)
              @msg = $('div.progressbar-msg', pbContainer)
            change: (e, ui) ->
              $(@).toggleClass('progressbar-indeterminate', $(@).progressbar('value') in [0, -1])
              @msg.html $(@).data('msg') if $(@).is(":data(msg)")
              @label.html "#{$(@).progressbar('value').toPrecision(4)}%"
            complete: (e, ui) ->
              pbContainer.remove() if autoRemoveWhenCompleted
          )
      ).hide()
    pbContainer.show().position(my: 'center', at: 'center', of: window) if autoDispaly
    $("#progressbar_#{randStr}", pbContainer)


  long_polling = (url, postData, callback) ->
    postData.pqCmd = 'start' if !postData || !postData.pqCmd

    if postData.pqCmd is "start"
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
#        console?.log data
        callback data.event if callback
        return if data.event.cmd in ['done', 'error']
        setTimeout (->long_polling url, {pqCmd: "process", pqId: data.pqId}, callback), pollingInterval

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


  $("#startAction").button().click (e) ->
    pb = genProgressBar()
#    $(this).button("disable").button "option", "label", "In progress..."

    long_polling("../scripts/cp.groovy", {},(event)->
      console.log event
      if event.cmd in ['done', 'error']
        pb.progressbar 'value', 100
        return

      pb.data 'msg', event.msg
      pb.progressbar 'value', Number(event.percent)
    )


