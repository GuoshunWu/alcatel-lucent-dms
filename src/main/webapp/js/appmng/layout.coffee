define ['jqlayout', 'module'], ($, module)->
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

#  console.log module
  pageLayout = $("##{ids.container.page}").layout {resizable: true, closable: true}
  dmsPanels = $ "##{ids.container.center} div[id^=#{PANEL_PREFIX}]"


  dmsPanels.addClass "ui-layout-content ui-corner-bottom"
  # ui-widget-content
  dmsPanels.css {paddingBottom: '1em', borderTop: 0}

  $(".header-footer").hover (->$(@).addClass "ui-state-hover"), -> $(@).removeClass "ui-state-hover"

  showCenterPanel = (panelId) -> dmsPanels.each (index, panel)->
    if panel.id == panelId
      $(panel).show()
    else
      $(panel).hide()

  showCenterPanel ids.panel.welcome

  #export the method for other module use
  showProductPanel: ->showCenterPanel ids.panel.product
  showApplicationPanel:->showCenterPanel ids.panel.application

