jQuery ($)->
  class PanelGroup
    constructor: (@panels, @currentPanel)->
    switchTo: (panelId)->
      $("#{@panels}").hide()
      @currentPanel = panelId
      console?.debug "switch to #{@panels}[id='#{panelId}']."
      $("#{@panels}[id='#{panelId}']").fadeIn "fast",()->console.log "Hello, world."


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
    heightStyle: "fill"
    activate: (event, ui)->
      console?.log ui
    load: (event, ui)->
      console?.log ui
    create: (event, ui)->
      console?.log ui
    heightStyle: "fill"
  )

  $('#switchPanel').button().click (e)->
    if pg.currentPanel == 'p1'
      tDialog.dialog('close')
      pg.switchTo 'p2'
    else
      pg.switchTo 'p1'
      tDialog.dialog('open')


  # Create layout.
  layout = $('#layout-container').layout(
    applyDefaultStyles: true
  )
