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
  matchAction = (refText, transId, labelId) ->
    # +_hibernate_class:com.alcatel_lucent.dms.model.Translation +text.reference:text~0.8 + status:2 + language.id:46
    languageId = $('#detailLanguageSwitcher').val()

    $('#transmngMatchTextDialog').dialog('open')
    grid = $("#transMatchTextGrid")
    postData = grid.getGridParam('postData')
    postData.language = languageId
    postData.text = refText
    postData.format = 'grid'
    postData.fuzzy = true
    postData.transId = transId
    postData.labelId = labelId
    postData.prop = 'reference, translation, score'

    grid.setGridParam(url: urls.translations, page: 1).trigger 'reloadGrid'

  transDetailGrid = $("#transDetailGridList").jqGrid(
    mtype: 'POST', postData: {}, editurl: "", datatype: 'local'
    width: 'auto', height: 200, shrinkToFit: false
    rownumbers: true
    pager: '#transDetailsPager', rowNum: 100, rowList: [20,50,100,200,500]
    viewrecords: true, gridview: true, multiselect: true
    cellEdit: true, cellurl: urls.trans.update_status, ajaxCellOptions: {async: false}
    colNames: ['Label', 'Max Len.', 'Context', 'Reference language', 'Translation', 'Status','TransId', 'Trans.Src', 'Last updated','Match','History']
    colModel: [
      {name: 'key', index: 'key', width: 120, editable: false, stype: 'select', align: 'left', frozen: true}
      {name: 'maxlen', index: 'maxLength', width: 60, editable: false, align: 'right', frozen: true, search: false}
      {name: 'context', index: 'context.name', width: 80, align: 'left', frozen: true, search: false}
      {name: 'reflang', index: 'reference', width: 200, align: 'left', frozen: true, search: false}
      {name: 'translation', index: 'ct.translation', width: 200, align: 'left', edittype:'textarea',
      editable:true, classes: 'editable-column', search: false}
      {name: 'transStatus', index: 'ct.status', width: 100, align: 'left', editable: true, classes: 'editable-column', search: true,
      edittype: 'select', editoptions: {value: c18n.translation.values},
      formatter: 'select',
      stype: 'select', searchoptions: {value: c18n.translation.values}
      }
      {name: 'transId', index: 'ct.id', width: 50, align: 'left', hidden:true, search: false}
      {name: 'transtype', index: 'ct.translationType', width: 70, align: 'left',
      formatter: 'select', editoptions: {value: i18n.trans.typefilter}
      search: true, stype: 'select', searchoptions: {value: i18n.trans.typefilter}
      }
      {name: 'lastUpdate', index: 'ct.lastUpdateTime', width: 100, align: 'left',search: false
      formatter: 'date', formatoptions:{srcformat:'ISO8601Long', newformat: 'Y-m-d H:i'}
      }
      {name: 'action', index: 'action', width: 50, align:'center', search: false, sortable: false
#      hidden: true
      formatter: (cellvalue, options, rowObject)->
        ret ="<div id='matchAct_#{options.rowId}_#{rowObject[3]}_#{rowObject[6]}' style='display:inline-block' title=\"Match\" class=\"ui-state-default ui-corner-all\">"
        ret +="<span class=\"ui-icon ui-icon-search\"></span></div>"
        ret
      unformat:(cellvalue, options)->""
      }

      {name: 'history', index: 'history', width: 40, editable: false, align: 'center', sortable: false, search: false, formatter: (cellvalue, options)->
        "<img class='historyAct' id='hisact_#{options.rowId}'  src='images/history.png'>"
      }
    ]
    gridComplete: ->
      grid = $(@)
      $('div[id^=matchAct]', @).click(()->
        [_, id, ref,transId]=@id.split('_')
        grid.getRowData()
        matchAction(ref, transId, id)
      ).on('mouseover',()->
        $(@).addClass('ui-state-hover')
      ).on('mouseout', ()->
        $(@).removeClass('ui-state-hover')
      )

      $('img.historyAct', @).click(()->
        grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell
        [_, rowid]=@id.split('_')
        rowData = grid.getRowData(rowid)
        delete rowData.action
        delete rowData.history
        rowData.labelId = rowid

        ($.msgBox c18n.history.nohistory, null, {title: c18n.error} ;return) unless rowData.transId? and parseInt(rowData.transId) > 0

        $('#translationHistoryDialogInDetailView').data('param', rowData).dialog 'open'
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
      if 'translation' == cellname
        if 1 == json.status
          dictList = "<ul>\n  <li>#{json.dicts.join('</li>\n  <li>')}</li>\n</ul>"
          showMsg = i18n.msgbox.updatetranslation.msg.format dictList
          delete json.dicts
          delete json.message
          delete json.status
          $('#transmngTranslationUpdate').html(showMsg).data('param', json).dialog 'open'
        else
          setTimeout (->
            transDetailGrid.trigger 'reloadGrid'
          ), 10
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
    ids = detailGrid.getGridParam('selarrrow')
    ctIds = (detailGrid.getRowData(id).transId for id in ids)
#    console.log "ids=%s, ctIds=%o", ids, ctIds
    ($.msgBox (c18n.selrow.format c18n.label), null, title: c18n.warning; return) unless ids.length

    $.post urls.trans.update_status, {type: 'trans', transStatus: e.target.name, ctid: ctIds.join(','), id: ids.join(", ")}, (json)->
      ($.msgBox json.message, null, title: c18n.warning; return) unless json.status == 0
      detailGrid.trigger 'reloadGrid'
      $("#transGrid").trigger 'reloadGrid'


  languageChanged: (param)->
    transDetailGrid = $("#transDetailGridList")
    url = urls.labels_normal
    prop = "key,maxLength,context.name,reference,ct.translation,ct.status,ct.id,ct.translationType,ct.lastUpdateTime"
#    idprop: 'ct.id'
    transDetailGrid.setGridParam url: url, datatype: "json", postData: {dict: param.dict.id, language: param.language.id, format: 'grid', prop: prop}

    #   set search tool bar status
    options = transDetailGrid.getColProp('transStatus').searchoptions
    options.defaultValue = param.searchStatus
    transDetailGrid.setColProp 'transStatus', searchoptions: options

    $('select#gs_transStatus','#translationDetailDialog').val param.searchStatus
    $('select#gs_transtype','#translationDetailDialog').val param.transsrc
    transDetailGrid[0].triggerToolbar()


  saveLastEditedCell: ()->
    transDetailGrid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell
    $("#transGrid").trigger 'reloadGrid' if transDetailGrid.getChangedCells('dirty').length > 0
