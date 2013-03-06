define ['util'],(util)->

  PANEL_PREFIX = 'DMS'
  ids = {
  container:
    {
    page: 'optional-container'
    center: 'ui_center'
    }
  panel:
    {
    welcome: PANEL_PREFIX + "_welcomePanel"
    product: PANEL_PREFIX + "_productPanel"
    application: PANEL_PREFIX + "_applicationPanel"
    }
  }
  appmngPnlGroup = new util.PanelGroup("#ui_center > div.content > div[id^=#{PANEL_PREFIX}]", "DMS_welcomePanel")

  showProductPanel: ->appmngPnlGroup.switchTo ids.panel.product, ()->
    grid = $('#applicationGridList')
    parent = $('#applicationGrid_parent')
    #    console?.debug "parent.width=#{parent.width()}, parent.height=#{parent.height()}."
    grid.setGridWidth(parent.width() - 10).setGridHeight(parent.height() - 85)

  showApplicationPanel: ->appmngPnlGroup.switchTo ids.panel.application, ()->
    parent = $('#dictionaryGridList_parent')
    #    console?.debug "parent.width=#{parent.width()}, parent.height=#{parent.height()}."
    $('#dictionaryGridList').setGridWidth(parent.width() - 10).setGridHeight(parent.height() - 85)
  showWelcomePanel: ->
    appmngPnlGroup.switchTo ids.panel.welcome

