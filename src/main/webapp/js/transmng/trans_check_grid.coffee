define [
  'jqgrid'
  'jqmsgbox'
  'i18n!nls/transmng'
  'i18n!nls/common'
  'dms-util'
  'dms-urls'
  'commons/validationMapping'
], (
  $, msgbox, i18n,
  c18n, util, urls,
  mapping
)->
  lastEditedCell = null

  lastEditedCell = null
  gridId = 'transCheckGrid'
  hGridID = "##{gridId}"
  pagerId = "#{gridId}Pager"
  hPagerId = '#' + pagerId

  selectColumns = ['context', 'language' , 'status']
  selectElements = {}

  uniqueErrors = (grid)->
    colName= 'transWarnings'
    errorColValues = grid.jqGrid('getCol', colName)
    uniqueValues = []
    errorCodes = []
    for errors in errorColValues
      for error in errors when -1 == errorCodes.indexOf(error.code)
        uniqueValues.push(code: error.code, message: error.message)
        errorCodes.push error.code
    uniqueValues

  buildErrorsSelectValue = (grid, currentSelected) ->
    uniqueValues = uniqueErrors(grid)
#    console.log "mapping=%o, uniqueValues=%o", mapping, uniqueValues
    "<option value=''>All</option>" + uniqueValues.map((error)->
      display  = if mapping[error.code] then mapping[error.code] else error.message
      isSelected = ""
      isSelected = "selected" if currentSelected and error.code + "" == currentSelected
      "<option #{isSelected} value='#{error.code}'>#{display}</option>"
    ).join("\n")

  grid = $(hGridID).after($("<div>").attr("id", pagerId)).jqGrid(
    datatype: 'local', url: urls.translation_check
    width: 'auto', height: 300
    rownumbers: true
    pager: pagerId, rowNum: 20, rowList: [20, 50, 100, 200]
    viewrecords: true, gridview: true, multiselect: false
    cellEdit: true, cellurl: urls.trans.update_status
    postData: {
      format: 'grid',
      prop : 'app.name,dictionary.name,key,maxLength,context.name,reference,ct.translation, ct.status, ct.language.name, ct.transWarnings, ct.id, id'
      idprop: 'ct.id'
    }
    caption: 'result'
    colNames: ['Application','Dictionary','Label', 'Max Length', 'Context', 'Reference', 'Translation', 'Status', 'Language', 'Warnings', 'TransId', 'labelId']
    colModel: [
      {name: 'app', index: 'app.name', hidden: true, width: 50, editable: false, stype: 'select', align: 'left'}
      {name: 'dict', index: 'dictionary.base.name', width: 150, editable: false, align: 'left', search: true}
      {name: 'key', index: 'key', width: 150, editable: false, align: 'left', search:true}
      {name: 'maxlen', index: 'maxLength', width: 60, editable: false, align: 'right', search: false}
      {name: 'context', index: 'context.name', width: 80, align: 'left', search: true}
      {name: 'reference', index: 'reference', width: 160, align: 'left', search: true}
      {name: 'translation', index: 'ct.translation', width: 160, edittype: 'textarea', editable: true, classes: 'editable-column', search: true}
      {name: 'transStatus', index: 'ct.status', width: 100, align: 'left', editable: true,search: true, classes: 'editable-column',
      edittype: 'select', editoptions: {value: "0:#{i18n.trans.nottranslated};1:#{i18n.trans.inprogress};2:#{i18n.trans.translated}"},formatter: 'select',
      stype: 'select', searchoptions: {value: ":#{c18n.all};0:#{i18n.trans.nottranslated};1:#{i18n.trans.inprogress};2:#{i18n.trans.translated}"}
      }
      {name: 'language', index: 'ct.language.name', width: 100, align: 'left', search: true}
      {name: 'transWarnings', index: 'ct.transWarnings', width: 300, align: 'left', search: false, search: true, stype: 'select',
      searchoptions: {
        value: ":All"
        dataInit:(elem)->selectElements['errors'] = $(elem)
      }
      formatter: (cellvalue, options, rowObject)->
        jsonErrors = $.parseJSON(cellvalue)
        convertedValue = $.map(jsonErrors, (error, index)->
          "<li code='#{error.code}'>#{error.message}</li>"
        )
        .join("\n");
        convertedValue = "<ul style='color: red;line-height: .6em; margin-top: 5px;margin-bottom: 5px; padding-left: 2em '>#{convertedValue}</ul>"
        #console.log "jsonErrors=%o, convertedValue=%o", $.parseJSON(cellvalue), convertedValue
        return  convertedValue
      unformat:(cellvalue, options, cell)->
        values = $('ul > li', cell).map((index, elem)->
          "code":$(elem).attr('code'), "message":$(elem).text()
        ).get()
#        console.log "cellvalue=%o, options=%o, cell=%o, values=%o", cellvalue, options, cell, values
        values
      }
      {name: 'transId', index: 'ct.id', hidden:true}
      {name: 'labelId', index: 'id', hidden:true}
    ]

    loadComplete: (data)->
      grid = $(@)

      $.each(selectElements, (colName, select)->
        return unless select
        currentSelected = select.val()
        newValue = util.buildSearchSelectValues(grid, colName, currentSelected)
        if('errors' == colName)
          newValue = buildErrorsSelectValue(grid, currentSelected)
        select.empty().append(newValue)
      ) if selectColumns.length


    afterEditCell: (rowid, name, val, iRow, iCol)->
      lastEditedCell = {iRow: iRow, iCol: iCol, name: name, val: val}

    beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
      rowData = $(@).getRowData(rowid)
#      console?.log "rowid=#{rowid}, cellname=#{cellname}, value=#{value}, iRow=#{iRow}, iCol=#{iCol}, rowData=%o", rowData

      if 'transStatus' == cellname
        $(@).setGridParam('cellurl': urls.trans.update_status)
        return {type: 'trans', ctid: rowData.transId}
      if 'translation' == cellname
        $(@).setGridParam('cellurl': urls.trans.update_translation)
        return {ctid: rowData.transId, labelId: rowData.labelId}
    afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
      json = $.parseJSON(serverresponse.responseText)
      # edit translation in cell is different from common cell editor
      # other column update

#      console.log "json =%o, cellname=%o", json, cellname
      if 'translation' != cellname
        # currently update translation status need reload the grid, trans grid will be update when the search dialog is closed
        success = 0 == json.status
        setTimeout (->
          grid.trigger 'reloadGrid'
        ), 10  if success
        # flag to reload transGrid when container dialog close
        $('#transmngTranslationCheckDialog').data("translationStatusUpdated", true)
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
  .navGrid(hPagerId, {edit: false, add: false, del: false, search: false, view: false})

  selectElements = util.setSearchSelect grid, selectColumns, selectElements
  grid.filterToolbar {stringResult: true, searchOnEnter: true}



  saveLastEditedCell: ()->
    return unless lastEditedCell
    if lastEditedCell
      console.log "saving last modified cell %o", lastEditedCell
      grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol)
#      if grid.getChangedCells('dirty').length > 0
      $("#transGrid").trigger 'reloadGrid'
