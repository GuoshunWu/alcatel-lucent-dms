define [
  'jqgrid'
  'jqmsgbox'
  'i18n!nls/transmng'
  'i18n!nls/common'
  'dms-util'
  'dms-urls'
], ($, msgbox, i18n, c18n, util, urls)->

  lastEditedCell = null

  ###
    find the labels which reference resemblant to the text and display in a modal dialog
  ###
  matchAction = (refText) ->
    # +_hibernate_class:com.alcatel_lucent.dms.model.Translation +text.reference:text~0.8 + status:2 + language.id:46
    languageId = $('#detailLanguageSwitcher').val()

    $('#transmngMatchTextDialog').dialog('open')
    grid = $("#transMatchTextGrid")
    postData = grid.getGridParam('postData')
    console?.log postData
#    grid.setGridParam().trigger 'reloadGrid'

  transDetailGrid = $("#transDetailGridList").jqGrid(
    url: 'json/transdetailgrid.json'
    mtype: 'POST', postData: {}, editurl: "", datatype: 'local'
    width: 'auto', height: 200, shrinkToFit: false
    rownumbers: true
    pager: '#transDetailsPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
    viewrecords: true, gridview: true, multiselect: true
    cellEdit: true, cellurl: urls.trans.update_status, ajaxCellOptions: {async: false}
    colNames: ['Label', 'Max Len.', 'Context', 'Reference language', 'Translation', 'Status','TransId', 'Trans.Src', 'Last updated','Match']
    colModel: [
      {name: 'key', index: 'key', width: 120, editable: false, stype: 'select', align: 'left', frozen: true}
      {name: 'maxlen', index: 'maxLength', width: 60, editable: false, align: 'right', frozen: true, search: false}
      {name: 'context', index: 'context.name', width: 80, align: 'left', frozen: true, search: false}
      {name: 'reflang', index: 'reference', width: 200, align: 'left', frozen: true, search: false}
      {name: 'translation', index: 'ct.translation', width: 200, align: 'left', edittype:'textarea',
      editable:true, classes: 'editable-column', search: false}
      {name: 'transStatus', index: 'ct.status', width: 100, align: 'left', editable: true, classes: 'editable-column', search: true,
      edittype: 'select', editoptions: {value: "0:#{i18n.trans.nottranslated};1:#{i18n.trans.inprogress};2:#{i18n.trans.translated}"},
      formatter: 'select',
      stype: 'select', searchoptions: {value: ":#{c18n.all};0:#{i18n.trans.nottranslated};1:#{i18n.trans.inprogress};2:#{i18n.trans.translated}"}
      }
      {name: 'transId', index: 'ct.id', width: 50, align: 'left', hidden:true, search: false}
      {name: 'transtype', index: 'ct.translationType', width: 70, align: 'left',
      formatter: 'select', editoptions: {value: i18n.trans.typefilter}
      search: true, stype: 'select', searchoptions: {value: i18n.trans.typefilter}
      }
      {name: 'lastUpdate', index: 'ct.lastUpdateTime', width: 100, align: 'left',search: false
      formatter: 'date', formatoptions:{srcformat:'ISO8601Long', newformat: 'Y-m-d H:i'}
      }
      {name: 'action', index: 'action', width: 50, align:'center', search: false, hidden: true
      formatter: (cellvalue, options, rowObject)->
        #"<img class='historyAct' id='matchAct_#{rowObject[3]}' src='images/history.png'>"
        ret ="<div id='matchAct_#{rowObject[3]}' style='display:inline-block' title=\"Match\" class=\"ui-state-default ui-corner-all\">"
        ret +="<span class=\"ui-icon ui-icon-search\"></span></div>"
        ret
      unformat:(cellvalue, options)->""
      }
    ]
    gridComplete: ->
      grid = $(@)
      $('div[id^=matchAct]', @).click(()->
        [_, ref]=@id.split('_')
        matchAction(ref)
      ).on('mouseover',()->
        $(@).addClass('ui-state-hover')
      ).on('mouseout', ()->
        $(@).removeClass('ui-state-hover')
      )

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
#      console?.log json
      if 'translation' == cellname and 1 == json.status
        dictList = "<ul>\n  <li>#{json.dicts.join('</li>\n  <li>')}</li>\n</ul>"
        showMsg = i18n.msgbox.updatetranslation.msg.format dictList
        delete json.dicts
        delete json.message
        delete json.status
        $('#transmngTranslationUpdate').html(showMsg).data('param', json).dialog 'open'

        return [true, json.message]

      [0 == json.status, json.message]

    afterCreate: (grid)->
      grid.setGridParam('datatype':'json')
      grid.navGrid '#transDetailsPager', {edit: false, add: false, del: false, search: false, view: false}
      grid.filterToolbar {stringResult: true, searchOnEnter: false}
  )
  afterCreate = transDetailGrid.jqGrid('getGridParam','afterCreate')
  afterCreate(transDetailGrid) if afterCreate

  $('#makeDetailLabelTranslateStatus').button(
    icons:
      primary: "ui-icon-triangle-1-n"
      secondary: "ui-icon-gear"
  )
  .attr('privilegeName', util.urlname2Action urls.trans.update_status)
  .click (e)->
    menu = $('#detailTranslationStatus').show().width($(@).width()).position(my: "left bottom", at: "left top", of: @)
    $(document).one "click", ()->menu.hide()
    false

  $('#detailTranslationStatus').menu().hide().find("li").on 'click', (e)->
    detailGrid = $("#transDetailGridList")
    ids = detailGrid.getGridParam('selarrrow').join(',')
    ctIds = $.map(ids, (element, index)->detailGrid.getRowData(element).transId)
    ($.msgBox (c18n.selrow.format c18n.label), null, title: c18n.warning; return) unless ids

    $.post urls.trans.update_status, {type: 'trans', transStatus: e.target.name, ctid: ctIds.join(','), id: ids}, (json)->
      ($.msgBox json.message, null, title: c18n.warning; return) unless json.status == 0
      detailGrid.trigger 'reloadGrid'
      $("#transGrid").trigger 'reloadGrid'


  languageChanged: (param)->
    transDetailGrid = $("#transDetailGridList")
    url = "rest/labels"
    prop = "key,maxLength,context.name,reference,ct.translation,ct.status,ct.id,ct.translationType,ct.lastUpdateTime"
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
