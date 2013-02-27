define (require)->
  PANEL_PREFIX = 'DMS'
  panel =
    welcome: "#{PANEL_PREFIX}_welcomePanel"
    product: "#{PANEL_PREFIX}_productPanel"
    application: "#{PANEL_PREFIX}_applicationPanel"

  appmngPnlGroup = new (require 'dms-util').PanelGroup("div.dms_appmng_panel", "DMS_welcomePanel")

  showProductPanel: ->appmngPnlGroup.switchTo panel.product, ()->
    grid = $('#applicationGridList')
    parent = $('#applicationGrid_parent')
    #    console?.debug "parent.width=#{parent.width()}, parent.height=#{parent.height()}."
    grid.setGridWidth(parent.width() - 10).setGridHeight(parent.height() - 85)

  showApplicationPanel: ->appmngPnlGroup.switchTo panel.application, ()->
    parent = $('#dictionaryGridList_parent')
    #    console?.debug "parent.width=#{parent.width()}, parent.height=#{parent.height()}."
    $('#dictionaryGridList').setGridWidth(parent.width() - 10).setGridHeight(parent.height() - 85)
  showWelcomePanel: ->
    appmngPnlGroup.switchTo panel.welcome
  layout: appmngPnlGroup

