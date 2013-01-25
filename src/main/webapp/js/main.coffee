define (require)->
  $ = require 'jqgrid'

  util = require 'dms-util'
  glayout = require 'globallayout'
  #  panels
  appmngPanel = require 'appmng/main'

  transPanel = require 'transmng/main'
  taskPanel = require 'taskmng/main'
  adminPanel = require 'admin/main1'

  ready = (param)->
    console?.debug "page ready..."
    $('#loading-container').fadeOut 'slow', ()->$(@).remove()

  ################################################## Initilaize #####################################################
  init = ()->
    dmsPanels = new util.PanelGroup('div.dms-panel', 'appmng.jsp', (oldpnl, newpnl)->
      # we need keep the panels to be informed if current product base changed
      # console?.debug "oldpnl= #{oldpnl}, newpnl= #{newpnl}."
      return if 'admin.jsp' == oldpnl or 'admin.jsp' == newpnl

      treeSelectedNode=$("#appTree").jstree 'get_selected'
      pbId =  $('#productBase', "div[id='#{oldpnl}']").val()
      # get product base id from old panel
      pbId = treeSelectedNode.attr('id') if oldpnl == 'appmng.jsp' and treeSelectedNode.length > 0 and treeSelectedNode.attr('type') == 'product'

      return if !pbId or '-1' == pbId
      # Trigger the new panel product stuff according to old panel product
      if newpnl == 'appmng.jsp'
        newPbId = treeSelectedNode.attr('id') if treeSelectedNode.length > 0 and treeSelectedNode.attr('type') == 'product'
        # newPbId maybe null case selected node maybe the application node
        return if newPbId and pbId == newPbId
        $("#appTree").jstree 'deselect_node', $("#appTree li [id=#{newPbId}][type=product]")
        $("#appTree").jstree 'select_node', $("#appTree li [id=#{pbId}][type=product]")
      else
        # Trigger the new panel product stuff according to old panel product
        pbSel = $('#productBase', "div[id='#{newpnl}']")
        pbSel.val(pbId).trigger 'change' if pbSel.val() != pbId
    )

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
        glayout.layout.resizeAll()
    ).parent().buttonset()
    # appmng.jsp as the current page on init
    $("span[id^='nav'][value='#{dmsPanels.currentPanel}']").trigger 'click'
  ################################################## Initilaized #####################################################
  init()

  ready()

