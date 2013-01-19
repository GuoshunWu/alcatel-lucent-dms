define (require)->
  $ = require 'jqgrid'
  require 'jqlayout'
  util = require 'util'
  charsetgrid = require 'admin/charsetgrid'
  languagegrid = require 'admin/languagegrid'

  $('#adminTabs').tabs(
    show: (event, ui)->
      pheight = $(ui.panel).height()
      pwidth = $(ui.panel).width()
      console?.debug "height=#{pheight}, width=#{pwidth}."
      $('table.ui-jqgrid-btable', ui.panel).setGridHeight(pheight - 90).setGridWidth(pwidth - 20)
  )

  $('#loading-container').fadeOut 'slow', ()->$(@).remove()
  util.afterInitilized(this)

#  tabs = $('#adminTabs')
#
#  pheight = tabs.parent().height()
#  tabs.tabs 'option', 'pheight', pheight
#  console?.debug "init tabs height=#{pheight}."
#
#  $('div.ui-tabs-panel', tabs).height(pheight - 65)

