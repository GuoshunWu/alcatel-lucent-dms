define (require)->
  $ = require 'jqgrid'

  util = require 'dms-util'
  glayout = require 'globallayout'
  #  panels
  appmngPanel = require 'appmng/main'

  ready = (param)->
    console?.debug "page ready..."
#    glayout.layout.resizeAll()
    $('#loading-container').fadeOut 'slow', ()->$(@).remove()

  ################################################## Initilaize #####################################################
  init = ()->
    dmsPanels = new util.PanelGroup('div.dms-panel', 'appmng.jsp')
    $('span.navigator-button').button().click(
      ()->
        currentPanel = "#{$(@).attr('value')}"
        $('span.page-title').text $("#pageNavigator>option[value='#{currentPanel}']").text()
        #    switch class
        $("span[id^='nav']").removeClass 'navigator-button-currentpage'
        $("span[id^='nav'][value='#{currentPanel}']").addClass 'navigator-button-currentpage'

        $("span[id^='nav'] > span.ui-button-text > span").removeClass 'navigator-tab-title-currentpage'
        $("span[id^='nav'][value='#{currentPanel}'] > span.ui-button-text > span").addClass 'navigator-tab-title-currentpage'

        dmsPanels.switchTo currentPanel
    ).parent().buttonset()

    # appmng.jsp as the current page on init
    currentBtn = $("span[id^='nav'][value='appmng.jsp']")
    currentBtn.addClass 'navigator-button-currentpage'
    $("span.ui-button-text > span", currentBtn).addClass('navigator-tab-title-currentpage')
    dmsPanels.switchTo 'appmng.jsp'
  ################################################## Initilaized #####################################################
  init()
  ready()

