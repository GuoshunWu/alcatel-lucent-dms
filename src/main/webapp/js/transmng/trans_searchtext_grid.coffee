define [
  'jqgrid'
  'jqmsgbox'
  'i18n!nls/transmng'
  'i18n!nls/common'
  'dms-util'
  'dms-urls'
], ($, msgbox, i18n, c18n, util, urls)->
  lastEditedCell = null

  grid = $("#transSearchTextGrid").jqGrid(
    mtype: 'POST', datatype: 'local'
    width: 'auto', height: 300
    rownumbers: true
    pager: '#transSearchTextGridPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
    viewrecords: true, gridview: true, multiselect: false
    cellEdit: true, cellurl: urls.trans.update_status
    caption: 'result'
    colNames: ['Application','Dictionary','Label', 'Max Length', 'Context', 'Reference language', 'Translation', 'Status','TransId']
    colModel: [
      {name: 'app', index: 'app.name', width: 50, editable: false, stype: 'select', align: 'left', frozen: true}
      {name: 'dict', index: 'dictionary.base.name', width: 150, editable: false, align: 'left', frozen: true, search: false}
      {name: 'key', index: 'key', width: 150, editable: false, stype: 'select', align: 'left', frozen: true}
      {name: 'maxlen', index: 'maxLength', width: 70, editable: false, align: 'right', frozen: true, search: false}
      {name: 'context', index: 'context.name', width: 80, align: 'left', frozen: true, search: false}
      {name: 'reference', index: 'reference', width: 160, align: 'left', frozen: true, search: false}
      {name: 'translation', index: 'ct.translation', width: 160, align: 'left', edittype:'textarea', editable: true, classes: 'editable-column', search: false}
      {name: 'transStatus', index: 'ct.status', width: 100, align: 'left', editable: true,search: true, classes: 'editable-column',
      edittype: 'select', editoptions: {value: "0:#{i18n.trans.nottranslated};1:#{i18n.trans.inprogress};2:#{i18n.trans.translated}"},
      formatter: 'select',
      stype: 'select', searchoptions: {value: ":#{c18n.all};0:#{i18n.trans.nottranslated};1:#{i18n.trans.inprogress};2:#{i18n.trans.translated}"}
      }
      {name: 'transId', index: 'ct.id', width: 150, align: 'left', hidden:true, search: false}
    ]
    afterEditCell: (rowid, cellname, val, iRow, iCol)->
      lastEditedCell = {iRow: iRow, iCol: iCol, name: name, val: val}

    beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
#      console?.log "rowid=#{rowid}, cellname=#{cellname}, value=#{value}, iRow=#{iRow}, iCol=#{iCol}"
      ctid = $(@).getRowData(rowid).transId

      if 'transStatus' == cellname
        $(@).setGridParam('cellurl': urls.trans.update_status)
        return {type: 'trans', ctid: ctid}
      if 'translation' == cellname
        $(@).setGridParam('cellurl': urls.trans.update_translation)
        return {ctid: ctid}
    afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
      json = $.parseJSON(serverresponse.responseText)
      # edit translation in cell is different from common cell editor
      # other column update
      if 'translation' != cellname
        # currently update translation status need reload the grid, trans grid will be update when the search dialog is closed
        success = 0 == json.status
#        setTimeout (->
#          grid.trigger 'reloadGrid'
#        ), 10  if success
        return [success, json.message]

      # translation column update
      return [false, json.message] if json.status>1
      # json.status =1 indicate sucessful, but need confirm

      if 1 == json.status
        dictList = "<ul>\n  <li>#{json.dicts.join('</li>\n  <li>')}</li>\n</ul>"
        showMsg = i18n.msgbox.updatetranslation.msg.format dictList
        delete json.dicts
        delete json.message
        delete json.status
        json.gridToReload = grid
        $('#transmngTranslationUpdate').html(showMsg).data('param', json).dialog 'open'
        # should not reload grid before the dialog close
        return [true, json.message]
        # this request is ok, and leave other things to the dialog

      # this request is ok json.status = 1 would make a popup error dialog
      # reload the grid to indicate the change
      setTimeout (->grid.trigger 'reloadGrid'), 10
      [true, json.message]
  )
  .setGridParam('datatype':'json')
  .navGrid('#transSearchTextGridPager', {edit: false, add: false, del: false, search: false, view: false})


  saveLastEditedCell: ()->
    grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell
    $("#transGrid").trigger 'reloadGrid' if grid.getChangedCells('dirty').length > 0
