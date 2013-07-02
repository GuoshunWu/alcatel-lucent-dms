define [
  'jqlayout'
  'dms-util'
  'globallayout'
  'ptree'
  'appmng/main'
  'transmng/main'
  'taskmng/main'
  'ctxmng/main'
  'admin/main'
  'i18n!nls/common'
], ($, util, glayout, ptree, appmngPanel, transmngPanel, taskmngPanel, ctxmngPanel, adminPanel, c18n)->
  ready = (param)->
#    console?.log "page ready..."
    util.afterInitilized(@)
    $('#loading-container').fadeOut 'slow', ()->$(@).remove()

  panelSwitchHandler = (oldpnl, newpnl)->
    # we need keep the panels to be informed if current product base changed
#    console?.log "oldpnl= #{oldpnl}, newpnl= #{newpnl}."
    if 'admin' == oldpnl or 'admin' == newpnl
      $('#adminTabs').tabs 'select', 2
      return

    treeSelectedNode=$("#appTree").jstree 'get_selected'
    nodeInfo = util.getProductTreeInfo()
    return if !nodeInfo or '-1' == nodeInfo.id
    type = nodeInfo.type

    tmp=type
    tmp= 'product' if 'prod' == tmp
    tmp+='Id'
    window.param.currentSelected[tmp]= $('#selVersion', "div[id='#{oldpnl}']").val()

    if 'appmng' == newpnl
      $("#appTree").jstree('select_node', $("#appTree").jstree('get_selected'), true)
    else
      options = $('#selVersion option', "div[id='#{oldpnl}']").clone()
      value = $('#selVersion', "div[id='#{oldpnl}']").val()
      if 'appmng' == oldpnl and 'app' == treeSelectedNode.attr('type')
        options = $('#selAppVersion option', "div[id='#{oldpnl}']").clone()
        value = $('#selAppVersion', "div[id='#{oldpnl}']").val()
      $('#selVersion', "div[id='#{newpnl}']").empty().append(options).val(value).trigger 'change'

      if newpnl in ['transmng', 'taskmng']

        $('#versionTypeLabel', "div[id='#{newpnl}']").text nodeInfo.text
        $('#typeLabel',"div[id='#{newpnl}']").text "#{c18n[type].capitalize()}: "



  ################################################## Initilaize #####################################################
  init = ()->
    dmsPanels = new util.PanelGroup('div.dms-panel', 'none', panelSwitchHandler)
    # Handler for north navigation bar button set
    $('span.navigator-button').button().click(
      ()->
        currentPanel = "#{$(@).attr('value')}"
#        console?.log "currentPanel=#{currentPanel}, dmsPanels.currentPanel=#{dmsPanels.currentPanel}."
        return if currentPanel == dmsPanels.currentPanel

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
    $("span[id^='nav'][value='appmng']").trigger 'click'
  ################################################## Initilaized #####################################################

  init()
  ready(@)


