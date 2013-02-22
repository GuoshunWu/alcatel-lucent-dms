define (require)->
  $ = require 'jqlayout'
  glayout = require 'globallayout'
  ptree = require 'ptree'
  util = require 'dms-util'

 # TODO: need to be refined later
  require 'util'

  #  panels
  appmngPanel = require 'appmng/main'
  #
  transmngPanel = require 'transmng/main'
  taskmngPanel = require 'taskmng/main'
  adminPanel = require 'admin/main'


  ready = (param)->
    console?.debug "page ready..."
    util.afterInitilized(@)
    $('#loading-container').fadeOut 'slow', ()->$(@).remove()

  panelSwitchHandler = (oldpnl, newpnl)->
#    we need keep the panels to be informed if current product base changed
    console?.debug "oldpnl= #{oldpnl}, newpnl= #{newpnl}."
    return if 'admin' == oldpnl or 'admin' == newpnl

    treeSelectedNode=$("#appTree").jstree 'get_selected'
    pbId =  $('#productBase', "div[id='#{oldpnl}']").val()

  ################################################## Initilaize #####################################################
  init = ()->
    dmsPanels = new util.PanelGroup('div.dms-panel', 'appmng', panelSwitchHandler)
    # Handler for north navigation bar button set
    $('span.navigator-button').button().click(
      ()->
        currentPanel = "#{$(@).attr('value')}"
        if 'admin' == currentPanel
          glayout.layout.hide('west')
        else
          glayout.layout.show('west')

        $("#pageNavigator").val "#{currentPanel}.jsp"
        $('span.page-title').text $("#pageNavigator>option[value='#{currentPanel}.jsp']").text()

        #    switch class
        $("span[id^='nav']").removeClass 'navigator-button-currentpage'
        $("span[id^='nav'][value='#{currentPanel}']").addClass 'navigator-button-currentpage'

        $("span[id^='nav'] > span.ui-button-text > span").removeClass 'navigator-tab-title-currentpage'
        $("span[id^='nav'][value='#{currentPanel}'] > span.ui-button-text > span").addClass 'navigator-tab-title-currentpage'

        dmsPanels.switchTo currentPanel
        eval("#{currentPanel}Panel")?.onShow?()

      #     glayout.layout.resizeAll()
    ).parent().buttonset()

    # appmng panel as the current page on init
    $("span[id^='nav'][value='#{dmsPanels.currentPanel}']").trigger 'click'
  ################################################## Initilaized #####################################################

  init()
  ready(@)

