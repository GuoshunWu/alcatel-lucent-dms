define (require)->
  $ = require('jqlayout')
  util = require 'util'

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
    parent = $('#applicationGrid_parent')
    $('#applicationGridList').setGridWidth(parent.width() - 10).setGridHeight(parent.height() - 90)

  showApplicationPanel: ->appmngPnlGroup.switchTo ids.panel.application, ()->
    parent = $('#dictionaryGridList_parent')
    $('#dictionaryGridList').setGridWidth(parent.width() - 10).setGridHeight(parent.height() - 90)
  showWelcomePanel: ->
    appmngPnlGroup.switchTo ids.panel.welcome

