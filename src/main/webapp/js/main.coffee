dependencies = [
  'jqlayout'
  'dms-util'
  'globallayout'
  'ptree'

  'appmng/main'
  'transmng/main'
  'taskmng/main'
  'admin/main'
]
define dependencies, ($, util, glayout, ptree, appmngPanel, transmngPanel, taskmngPanel, adminPanel)->
  ready = (param)->
    console?.debug "page ready..."
    util.afterInitilized(@)
    $('#loading-container').fadeOut 'slow', ()->$(@).remove()

  panelSwitchHandler = (oldpnl, newpnl)->
    # we need keep the panels to be informed if current product base changed
    console?.debug "oldpnl= #{oldpnl}, newpnl= #{newpnl}."
    return if 'admin' == oldpnl or 'admin' == newpnl

    treeSelectedNode=$("#appTree").jstree 'get_selected'
    return if 0 == treeSelectedNode.length or '-1' == treeSelectedNode.attr('id')

    if 'appmng' == newpnl
      type = treeSelectedNode.attr('type')
      if 'product' == type
        window.param.currentSelected.productId = $('#selVersion', "div[id='#{oldpnl}']").val()
      else
        window.param.currentSelected.appId = $('#selVersion', "div[id='#{oldpnl}']").val()
      $("#appTree").jstree('select_node', $("#appTree").jstree('get_selected'), true)
    else
      options = $('#selVersion option', "div[id='#{oldpnl}']").clone()
      value = $('#selVersion', "div[id='#{oldpnl}']").val()
      options = $('#selAppVersion option', "div[id='#{oldpnl}']").clone() if 'appmng' == oldpnl and 'app' == treeSelectedNode.attr('type')
      $('#versionTypeLabel', "div[id='#{newpnl}']").text $("#appTree").jstree('get_text', treeSelectedNode) if 'appmng' != oldpnl

      $('#selVersion', "div[id='#{newpnl}']").empty().append(options).val(value).trigger 'change'

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


