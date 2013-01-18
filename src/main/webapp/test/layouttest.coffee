jQuery ($)->
  class PanelGroup
    constructor: (@panels, @currentPanel)->
    switchTo: (panelId, callback)->
      $("#{@panels}").hide()
      @currentPanel = panelId
      console?.debug "switch to #{@panels}[id='#{panelId}']."
      $("#{@panels}[id='#{panelId}']").fadeIn "fast", ()-> callback() if $.isFunction callback


  pg = new PanelGroup('div.panel', 'p1')
  pg.switchTo 'p2'

  tDialog = $('#testDialog').dialog(
    autoOpen: false
    title: 'Test Dialog.'
    buttons: [
      {text: 'Test', click: (e)->alert 'OK'}
      {text: 'Close', click: (e)->$(@).dialog 'close'}

    ]
  )

  tTabs = $('#p2 > .testTabs').tabs(
    show: (event, ui)->
      console?.log ui
    select: (event, ui)->
      console?.log ui
  )

  $('#switchPanel').button().click (e)->
    if pg.currentPanel == 'p1'
      tDialog.dialog('close')
      pg.switchTo 'p2', ()->
        height = $("##{pg.currentPanel}").height()
        tTabs.tabs 'option', 'height', height
        console?.debug "parent height=#{height}."
        $('#langAdmin').height(height - 70)
    else
      pg.switchTo 'p1'
      tDialog.dialog('open')


  # Create layout.
  layout = $('#layout-container').layout(
    applyDefaultStyles: true
  )
