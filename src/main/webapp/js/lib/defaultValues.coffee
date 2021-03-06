define [
  'jqgrid'
  'jqueryui'

  'i18n!nls/common'
], (
  $
  ui
  c18n
)->

  #ref http://jebaird.com/2010/05/24/setting-new-default-options-for-jquery-ui-widgets.html

  # Set the jq grid options globally
  $.extend($.jgrid.defaults, {
    loadui: "block"
    mtype: 'post'

    # enable cell edit input html text
    afterRestoreCell:(rowid, value, iRow, iCol)->
      $(@).jqGrid("setCell", rowid, iCol, $.jgrid.htmlEncode(value), false, false, true);
    afterSaveCell: (rowid, cellname, value, iRow, iCol)->
      grid = $(@)
      colProp = grid.jqGrid 'getColProp',cellname
#      console.log "rowid", rowid, cellname, value, iRow, iCol, colProp.edittype
      grid.jqGrid("setCell", rowid, iCol, $.jgrid.htmlEncode(value), false, false, true) if colProp.edittype isnt 'select'
#      if "clientArray" == cellsubmit then $.jgrid.htmlEncode(value) else value

    formatCell: (rowid, cellname, value, iRow, iCol)->
      # this is not need for value may contain encoded html string
#      $.jgrid.htmlDecode(value)



    beforeRequest: ->
      grid = $(@)
      grid.clearGridData() if grid.getRowData().length
      # console.log "Grid #{@.id} has cleared old data, now loading new data..."

    ###
    JQGrid 4.7 has this feature itself, but it use shift key instead of alt as enter input help key
    Due to some bugs found in jqGrid 4.7, rollback to jqGrid 4.6 now
    ###
    afterEditCell: (rowid, cellname, value, iRow, iCol)->
      grid = $(@)
      # remove original keydown handler and install a new one
      elemId = iRow + "_#{cellname}"
      #// cellname may include dot, which jquery considered a class selector
      editElem = $(document.getElementById(elemId)).off("keydown")
      originalValue = editElem.val()
      editElem.on('keydown', (e)->
        if $.ui.keyCode.ESCAPE == e.which
          grid.jqGrid('restoreCell', iRow, iCol)
          e.stopPropagation()
          return

        return unless $.ui.keyCode.ENTER == e.which
        if e.altKey
          $(this).val "#{$(this).val()}\n"
          return true
        grid.jqGrid('saveCell', iRow, iCol)
        false
      ).on('blur', (e)->
        if originalValue is editElem.val()
          grid.restoreCell iRow, iCol
        else
          grid.saveCell iRow, iCol
      )
  })

  # set jquery ui dialog default options

  $.extend($.ui.dialog.prototype.options, {
    autoOpen: false
    modal: true
    beforeClose: (event, ui)->
      # clear data in inner grid
      grid = $("table.ui-jqgrid-btable", @)
      return unless  grid.length
      setTimeout (->grid.clearGridData()), 300

    open: (event, ui)->
      me = $(@)
      grid = $("table.ui-jqgrid-btable", @)
      return unless  grid.length
#      util.adjustDialogAndInnerGridSize(me, grid)

    buttons: [
      {text: c18n.close, click: -> $(@).dialog "close"}
    ]
  })

  # default ajax options
  $.ajaxSetup {timeout: 1000 * 60 * 30, cache: false}
