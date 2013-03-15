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
                  <div id="pb_container_#{randStr}"  class="progressbar-container">
                  <div class="progressbar-msg">
                  Loading...
                  </div>
                  <div id="progressbar_#{randStr}" class="progressbar">
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
              @msg.html $(@).data('msg') if $(@).is(":data(msg)")
              @label.html "#{$(@).progressbar('value').toPrecision(4)}%"
            complete: (e, ui) ->
              pbContainer.remove() if autoRemoveWhenCompleted
          )
      ).hide()
    pbContainer.show().position(my: 'center', at: 'center', of: window) if autoDispaly
    $("#progressbar_#{randStr}", pbContainer)


  long_polling =(url, postData, callback, pb)->
    # call by terminal user
    postData.pqCmd = 'start' if !postData || !postData.pqCmd
    #    console?.log "postData="
    #    console?.log postData

    # initlize the test parameters
    if postData.pqCmd is "start"
      postData.freq = (if $("#eFreq").val() then parseInt($("#eFreq").val()) else 2000)
      postData.speed = (if $("#speed").val() then parseInt($("#speed").val()) else 1000)
    postData.freq = 2  if postData.freq < 2
    postData.speed = 2  if postData.speed < 2

    pollingInterval = if $("#pollingFreq").val() then parseInt($("#pollingFreq").val()) else 1000
    reTryAjax = (retryTimes = Number.MAX_VALUE, retryCounter = 0)->
      $.ajax(url,
        cache: false
        data: postData
        type: 'post'
        dataType: "json"
      ).done((data, textStatus, jqXHR) ->
        #        console?.log data
        if 'error' == data.event.cmd
          $.msgBox event.msg, null, {title: c18n.error}
          return

        if 'done' == data.event.cmd
          callback? data
          return

        if pb
          pb.toggleClass('progressbar-indeterminate', -1 == data.event.percent)
          pb.data 'msg', data.event.msg
          pb.progressbar 'value', data.event.percent
        else
          callback? data

        setTimeout (->long_polling  url, {pqCmd: 'process', pqId: data.pqId}, callback, pb), pollingInterval
      ).fail((jqXHR, textStatus, errorThrown)->
        if 'timeout' != textStatus
#          console?.log "error: #{textStatus}"
          return

        if(retryTimes > 0)
#          console?.log "Request #{textStatus}, I will retry in #{pollingInterval} milliseconds."
          setTimeout (->reTryAjax(--retryTimes, ++retryCounter)), pollingInterval
        else
#          console?.log "I have retried #{retryCounter} times. There may be a network connection issue, please check network cable."
      )

    reTryAjax(10)


  $("#startAction").button().click (e) ->
    pb = genProgressBar()

    long_polling("../scripts/cp.groovy", {},(event)->
      pb.parent().remove()
    ,pb)


