define [
  'defaultValues'
  'jqlayout'
  'blockui'
  'dms-util'
  'i18n!nls/common'

  'globallayout'
  'ptree'
  'appmng/main'
  'transmng/main'
  'taskmng/main'
  'ctxmng/main'
  'admin/main'
  'commons/dialogs'

#  'testcases'
], (
  defaultValues
  $
  blockui
  util
  c18n

  glayout
  ptree
  appmngPanel
  transmngPanel
  taskmngPanel
  ctxmngPanel
  adminPanel
  cdialogs
#  testcases
)->
  isFirst = true
  ready = (param)->
#    console?.log "page ready..."

    $.blockUI.defaults.message = '<h1><img src="images/busy.gif" />&nbsp;Please wait...</h1>'
    util.afterInitialized(@)
    $('#loading-container').fadeOut 'slow', ()->$(@).remove()

    cdialogs.tipOfDayDialog.dialog 'open' if window.param.currentUser.showTips
  window.param.currentPanel = 'appmng'

  panelSwitchHandler = (oldpnl, newpnl)->
    # The panels need to be informed if current product base changed
#    console?.log "oldpnl= #{oldpnl}, newpnl= #{newpnl}."
    window.param.currentPanel = newpnl
    if 'admin' == oldpnl or 'admin' == newpnl
      $('#adminTabs').tabs 'option', 'active', 2  if isFirst
      isFirst = false
      return

    treeSelectedNode=$("#appTree").jstree 'get_selected'
    nodeInfo = util.getProductTreeInfo()
    return if !nodeInfo or '-1' == nodeInfo.id
    type = nodeInfo.type

    tmp = type
    tmp = 'product' if 'prod' == tmp
    tmp += 'Id'

    selSelector = if 'appmng'== oldpnl and type=='app' then '#selAppVersion' else '#selVersion'
    oldVersion = $("#{selSelector}", "div[id='#{oldpnl}']")

    selSelector = if 'appmng'== newpnl and type=='app' then '#selAppVersion' else '#selVersion'
    newVersion = $("#{selSelector}", "div[id='#{newpnl}']")

    ###
     if the product or application version not changed when switch to new panel, then
     event should not be triggered.
    ###

    newTypeSaver = $("##{newpnl}")
#    console.log "type=%o version = %o , new type= %o version= %o", type, oldVersion.val(), newTypeSaver.attr('type'), newVersion.val()
    return if oldVersion.val() == newVersion.val() && type == newTypeSaver.attr('type')

    window.param.currentSelected[tmp]= $('#selVersion', "div[id='#{oldpnl}']").val()


    if newpnl in ['appmng']
      # trigger the js tree select node event
      $("#appTree").jstree('select_node', $("#appTree").jstree('get_selected'), true)
    else
      options = $('#selVersion option', "div[id='#{oldpnl}']").clone()
      value = $('#selVersion', "div[id='#{oldpnl}']").val()
      if 'appmng' == oldpnl and 'app' == treeSelectedNode.attr('type')
        options = $('#selAppVersion option', "div[id='#{oldpnl}']").clone()
        value = $('#selAppVersion', "div[id='#{oldpnl}']").val()
      $('#selVersion', "div[id='#{newpnl}']").empty().append(options).val(value).trigger 'change'

      if newpnl in ['transmng', 'taskmng','ctxmng']

        $('#versionTypeLabel', "div[id='#{newpnl}']").text nodeInfo.text
        $('#typeLabel',"div[id='#{newpnl}']").text "#{c18n[type].capitalize()}: "

  # update current panel node type when application or product version changed
  $("select[id='selVersion'], select[id='selAppVersion']").change(->
    $("##{window.param.currentPanel}").attr('type', util.getProductTreeInfo().type)
  )
  ################################################## Initilaize #####################################################
  init = ()->
    dmsPanels = new util.PanelGroup('div.dms-panel', 'none', panelSwitchHandler)
    # Handler for north navigation bar button set
    $('span.navigator-button').button().click(
      ()->
        currentPanel = "#{$(@).attr('value')}"
#        console?.log "currentPanel=#{currentPanel}, dmsPanels.currentPanel=#{dmsPanels.currentPanel}."
        return if currentPanel == dmsPanels.currentPanel

        if currentPanel in ['admin']
          glayout.layout.hide('west')
        else
          glayout.layout.show('west')
        padding = if currentPanel == 'ctxmng' then 0 else 10
        # special setting for context

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
