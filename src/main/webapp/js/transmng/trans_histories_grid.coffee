define [
  'jqgrid'
  'jqmsgbox'
  'i18n!nls/transmng'
  'i18n!nls/common'
  'dms-util'
  'dms-urls'
], ($, msgbox, i18n, c18n, util, urls)->

  lastEditedCell = null
  gridId = 'transHistoriesGrid'
  hGridID = "##{gridId}"
  pagerId = "##{gridId}Pager"

  grid = $("#{hGridID}").jqGrid(
    mtype: 'post', datatype: 'local', url: urls.app_translation_histories
    width: 'auto', height: 460
    rownumbers: true
    pager: pagerId, rowNum: 20, rowList: [20, 50, 100, 500]
    viewrecords: true, gridview: true, multiselect: false
    cellEdit: true, cellurl: urls.trans.update_status
    postData: {
      format: 'grid',
      prop : 'historyLabel.dictionary.name, historyLabel.key, parent.text.context.name , parent.text.reference, operationTime, operationType, operator.name, translation, status, memo'
    }
    toolbar: [true, 'top']
    sortorder: 'desc'
    colNames: ['Dictionary','Label', 'Context', 'Reference', 'Operation Time', 'Operation Type','Operator', 'Translation', 'Status', 'Memo']
    colModel: [
      {name: 'dict', index: 'l.dictionary.base.name', width: 110, editable: false, align: 'left', frozen: true, search: true}
      {name: 'key', index: 'l.key', width: 155, editable: false, align: 'left', frozen: true, search: true}
      {name: 'context', index: 'h.parent.text.context.name', width: 70, align: 'left', frozen: true, search: true
      stype: 'select', searchoptions:value: ":All;#{c18n.transcontext}"
      }
      {name: 'reference', index: 'h.parent.text.reference', width: 110, align: 'left', frozen: true, search: true}
      {name: 'operationTime', index: 'h.operationTime', width: 125, editable: false, align: 'left', search: false}
      {name: 'operationType', index: 'h.operationType', width: 60, editable: false, align: 'left', formatter: 'select',
      stype: 'select', searchoptions: {value:":#{c18n.all};#{c18n.transoptype}"}, editoptions:{value: c18n.transoptype}}
      {name: 'operator.name', index: 'h.operator.name', width: 80, editable: true, align: 'left'}
      {name: 'translation', index: 'h.translation', width: 110, align: 'left'}
      {name: 'status', index: 'h.status', width: 90, formatter: 'select', editoptions:{value: c18n.translation.values}, align: 'left'
      stype: 'select', searchoptions: {value: ":#{c18n.all};0:#{i18n.trans.nottranslated};1:#{i18n.trans.inprogress};2:#{i18n.trans.translated}"}
      }
      {name: 'memo', index: 'h.memo', width: 70, editable: true, align: 'left', hidden:true}
    ]
    afterEditCell: (rowid, cellname, val, iRow, iCol)->
      lastEditedCell = {iRow: iRow, iCol: iCol, name: cellname, val: val}


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
  .navGrid(pagerId, {edit: false, add: false, del: false, search: false, view: false})
  .filterToolbar {stringResult: true, searchOnEnter: false}


  #init toolbar for the grid
  $("#operationTimeBegin, #operationTimeEnd").datepicker(
    autoOpen:false, dateFormat: "yy-mm-dd"
  )
#  .prop("readonly", false)
  .width(80).change(()->
    grid.setGridParam(
      postData: {from: $('#operationTimeBegin').val(), to: $('#operationTimeEnd').val()}
    ).trigger 'reloadGrid'
  )

  $("#operationTimeBegin").datepicker("setDate", "-7d")
  $("#operationTimeEnd").datepicker("setDate", "+0d")

  $("#transHistoriesDialogSearchToolBar").appendTo("#t_#{gridId}")

  saveLastEditedCell: ()->
    return unless lastEditedCell
    grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell
    $("#transGrid").trigger 'reloadGrid' if grid.getChangedCells('dirty').length > 0
