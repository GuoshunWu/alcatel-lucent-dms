define [
  'jqgrid'
  'jqmsgbox'
  'i18n!nls/transmng'
  'i18n!nls/common'
  'dms-util'
  'dms-urls'
], ($, msgbox, i18n, c18n, util, urls)->

  lastEditedCell = null

  transDetailGrid = $("#transDetailGridList").jqGrid(
    url: 'json/transdetailgrid.json'
    mtype: 'POST', postData: {}, editurl: "", datatype: 'local'
    width: 'auto', height: 200, shrinkToFit: false
    rownumbers: true
    pager: '#transDetailsPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
    viewrecords: true, gridview: true, multiselect: true
    cellEdit: true, cellurl: urls.trans.update_status, ajaxCellOptions: {async: false}
    colNames: ['Label', 'Max Length', 'Context', 'Reference language', 'Translation', 'Status','TransId']
    colModel: [
      {name: 'key', index: 'key', width: 100, editable: false, stype: 'select', align: 'left', frozen: true}
      {name: 'maxlen', index: 'maxLength', width: 90, editable: false, align: 'right', frozen: true, search: false}
      {name: 'context', index: 'context.name', width: 80, align: 'left', frozen: true, search: false}
      {name: 'reflang', index: 'reference', width: 150, align: 'left', frozen: true, search: false}
      {name: 'translation', index: 'ct.translation', width: 150, align: 'left', editable:true, classes: 'editable-column', search: false}
      {name: 'transStatus', index: 'ct.status', width: 150, align: 'left', editable: true, classes: 'editable-column', search: true,
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
        {ctid: ctid}
    afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
      json = $.parseJSON(serverresponse.responseText)
      # edit translation in cell is different from common cell editor
      if 'translation' == cellname and 1 == json.status
        dictList = "<ul>\n  <li>#{json.dicts.join('</li>\n  <li>')}</li>\n</ul>"


        $.msgBox i18n.msgbox.updatetranslation.msg.format(dictList), ((keyPressed)->
          postData = $.extend confirm: c18n.yes == keyPressed , json
          delete postData.dicts
          delete postData.message
          delete postData.status
#          console?.log postData
          $.post urls.trans.update_translation, postData , (json1)->
            unless json1.status == 0
              $.msgBox(json1.message, null, title: c18n.error)
              return
            $("#transDetailGridList").trigger 'reloadGrid'
        ), {title: c18n.confirm, width: 600}, [c18n.yes, c18n.no]

        return [true, json.message]
      [0 == json.status, json.message]

    afterCreate: (grid)->
      grid.setGridParam('datatype':'json')
      grid.navGrid '#transDetailsPager', {edit: false, add: false, del: false, search: false, view: false}
      grid.filterToolbar {stringResult: true, searchOnEnter: false}
  )
  transDetailGrid.jqGrid('getGridParam','afterCreate') transDetailGrid

  $('#makeDetailLabelTranslateStatus').button(
    icons:
      primary: "ui-icon-triangle-1-n"
      secondary: "ui-icon-gear"
  )
  .attr('privilegeName', util.urlname2Action 'trans/update-status')
  .click (e)->
    menu = $('#detailTranslationStatus').show().width($(@).width()).position(my: "left bottom", at: "left top", of: @)
    $(document).one "click", ()->menu.hide()
    false

  $('#detailTranslationStatus').menu().hide().find("li").on 'click', (e)->
    detailGrid = $("#transDetailGridList")
    ids = detailGrid.getGridParam('selarrrow')
    ctIds = $.map(ids, (element, index)->detailGrid.getRowData(element).transId)
    $.post 'trans/update-status', {type: 'trans', transStatus: e.target.name, ctid: ctIds.join(','), id: ids.join(',')}, (json)->
      ($.msgBox json.message, null, title: c18n.warning; return) unless json.status == 0
      detailGrid.trigger 'reloadGrid'
      $("#transGrid").trigger 'reloadGrid'


  languageChanged: (param)->
    transDetailGrid = $("#transDetailGridList")
    url = "rest/labels"
    prop = "key,maxLength,context.name,reference,ct.translation,ct.status,ct.id"
#    idprop: 'ct.id'
    transDetailGrid.setGridParam url: url, datatype: "json", postData: {dict: param.dict.id, language: param.language.id, format: 'grid', prop: prop}

    #   set search tool bar status
    options = transDetailGrid.getColProp('transStatus').searchoptions
    options.defaultValue = param.searchStatus
    transDetailGrid.setColProp 'transStatus', searchoptions: options

    $('#gs_transStatus').val param.searchStatus
    transDetailGrid[0].triggerToolbar()


  saveLastEditedCell: ()->
    transDetailGrid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell
    $("#transGrid").trigger 'reloadGrid' if transDetailGrid.getChangedCells('dirty').length > 0
