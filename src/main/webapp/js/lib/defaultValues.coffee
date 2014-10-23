define [
  'jqgrid'
  'jqueryui'

  'i18n!nls/common'
  'dms-util'
], (
  $
  ui
  c18n
  util
)->

  #ref http://jebaird.com/2010/05/24/setting-new-default-options-for-jquery-ui-widgets.html

  # Set the jq grid options globally
  $.extend($.jgrid.defaults, {
    loadui: "block"
    mtype: 'post'
    beforeRequest: ->
      grid = $(@)
      grid.clearGridData() if grid.getRowData().length
      # console.log "Grid #{@.id} has cleared old data, now loading new data..."
  })

  # set jquery ui dialog default options

  $.extend($.ui.dialog.prototype.options, {
    autoOpen: false
    modal: true
    beforeClose: (event, ui)->
      # clear data in inner grid
      grid = $("table.ui-jqgrid-btable", @)
      return unless  grid.length
      grid.clearGridData()

#    open: (event, ui)->
#      me = $(@)
#      grid = $("table.ui-jqgrid-btable", @)
#      console.log "grid=", grid
#      util.adjustDialogAndInnerGridSize(me, grid)

    buttons: [
      {text: c18n.close, click: -> $(@).dialog "close"}
    ]
  })

  # default ajax options
  $.ajaxSetup {timeout: 1000 * 60 * 30, cache: false}
