define (require)->
  $ = require('jqlayout')

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

  dmsPanels = $ "#ui_center > div[id^=#{PANEL_PREFIX}]"


  showCenterPanel = (panelId) -> dmsPanels.each (index, panel)->
    if panel.id == panelId
      $(panel).show()
    else
      $(panel).hide()

  showCenterPanel ids.panel.welcome


  #export the method for other module use
  showProductPanel: ->showCenterPanel ids.panel.product
  showApplicationPanel: ->showCenterPanel ids.panel.application
  showWelcomePanel: ->showCenterPanel ids.panel.welcome

