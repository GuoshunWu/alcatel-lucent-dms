define ['dms-util'], (util)->
#  console?.log "module appmng/layout loading."

  PANEL_PREFIX = 'DMS'
  panel =
    welcome: "#{PANEL_PREFIX}_welcomePanel"
    product: "#{PANEL_PREFIX}_productPanel"
    application: "#{PANEL_PREFIX}_applicationPanel"
    search: "#{PANEL_PREFIX}_searchPanel"

  appmngPnlGroup = new util.PanelGroup("div.dms_appmng_panel", "DMS_welcomePanel")

  showProductPanel: ->appmngPnlGroup.switchTo panel.product, ()->
    grid = $('#applicationGridList')
    parent = $('#applicationGrid_parent')
    #    console?.log "parent.width=#{parent.width()}, parent.height=#{parent.height()}."
    grid.setGridWidth(parent.width() - 10).setGridHeight(parent.height() - 85)

  showApplicationPanel: ->appmngPnlGroup.switchTo panel.application, ()->
    parent = $('#dictionaryGridList_parent')
#    console?.log "parent.width=#{parent.width()}, parent.height=#{parent.height()}."
    $('#dictionaryGridList').setGridWidth(parent.width() - 10).setGridHeight(parent.height() - 85)

  showWelcomePanel: ->
    appmngPnlGroup.switchTo panel.welcome

  showSearchPanel: ->appmngPnlGroup.switchTo panel.search, ()->
    parent = $('#globalSearchResultGrid_parent')
    #    console?.log "parent.width=#{parent.width()}, parent.height=#{parent.height()}."
    $('#globalSearchResultGrid').setGridWidth(parent.width() - 10).setGridHeight(parent.height() - 90)


  layout: appmngPnlGroup

