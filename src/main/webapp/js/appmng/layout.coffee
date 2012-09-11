pageLayout = $("##{ids.container.page}").layout {resizable: true, closable: true}
dmsPanels = $("##{ids.container.center}").children "div[id^=#{PANEL_PREFIX}]"


dmsPanels.addClass "ui-layout-content ui-corner-bottom" # ui-widget-content
dmsPanels.css {paddingBottom: '1em', borderTop: 0}

#$(".header-footer") -> () -> $(this).addClass ('ui-state-hover'),() -> $(this).removeClass ('ui-state-hover')

showCenterPanel = (panelId) -> dmsPanels.each (index, panel)->
  if panel.id == panelId
    $(panel).show()
  else
    $(panel).hide()

showCenterPanel ids.welcome

#export the method for other module use
exports.layout.showCenterPanel=showCenterPanel

