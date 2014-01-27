define [
  'jqueryui'
  'dms-util'
  'dms-urls'
  'i18n!nls/common'
], (
  $
  util
  urls
  c18n
)->
  tipOfDayDialog = $('#tipOfTheDayDialog').dialog(
    height: 350 , minHeight: 200
    width: 600, minWidth: 300
    autoOpen: false
    create:(e, ui)->
      btnPanel = $(@).next('div.ui-dialog-buttonpane')
      # keep button width consistent
      $('button[role=button]', btnPanel).width '92px'

      # create Show Tip on start time checkbox
      tipDayDisplay = $("<input type='checkbox' id='displayTipOnLogin'>").prop('checked', !window.param.currentUser.showTips).css("verticalAlign", "middle")
      tipDispalyCheckLabel = $("<label for='displayTipOnLogin'>#{c18n.tipofday.showtip}</label>").css("verticalAlign", "middle")

      btnPanel.prepend($("<div>").append(tipDayDisplay).append(tipDispalyCheckLabel))

      @tipFiles = param.tipFiles.split ','
      @currentTipIndex = util.randomNum 0, @tipFiles.length

      isTipCircular = false

      @checkTipIndex=()=>
        @currentTipIndex = @tipFiles.length - 1 if @currentTipIndex < 0
        @currentTipIndex = 0 if @currentTipIndex > @tipFiles.length - 1

        unless isTipCircular
          $('#tipPrevious').button 'option', 'disabled', @currentTipIndex ==0
          $('#tipNext').button 'option', 'disabled', @currentTipIndex >= @tipFiles.length - 1


    open: (e, ui)->
      @checkTipIndex()
      tipFile = @tipFiles[@currentTipIndex]
      $('#tipContent', @).load(tipFile)
      console.log "@currentTipIndex=%o", @currentTipIndex

    buttons: [
      {
        text: c18n.tipofday.previous,id: 'tipPrevious', click: (e)->
          --@currentTipIndex
          @checkTipIndex()
          tipFile = @tipFiles[@currentTipIndex]
          $('#tipContent', @).load(tipFile)
      }
      {
        text: c18n.tipofday.next, id: 'tipNext', click: (e)->
          ++@currentTipIndex
          @checkTipIndex()
          tipFile = @tipFiles[@currentTipIndex]
          $('#tipContent', @).load(tipFile)

      }
      {text: c18n.close, click: (e)->
        $(@).dialog 'close'

        isShowTip  = !$('#displayTipOnLogin').prop("checked")
        return if isShowTip == window.param.currentUser.showTips
        $.post urls.user.update, {oper: 'edit', loginName: window.param.currentUser.loginName, isShowTip: isShowTip}, (result)->
          window.param.currentUser.showTips = isShowTip
          console.log(result)
      }
    ]
  )

  $('#showTipOfDay').on('mouseover',()->
    $("img", @).prop "src", "images/tips/ktip24.png"
  ).on('mouseout', ()->
    $("img",@).prop "src", "images/tips/lightbulb.png"
  ).on('click', ()->
    tipOfDayDialog.dialog 'open'
  )

  tipOfDayDialog: tipOfDayDialog