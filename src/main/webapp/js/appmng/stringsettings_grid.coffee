define [
  'jqgrid'
  'dms-util'
  'dms-urls'
  'i18n!nls/common', 'i18n!nls/appmng'
  'appmng/stringsettings_translation_grid'
], ($, util, urls, c18n, i18n, ltgrid)->
#  console?.log "module appmng/stringsetting_grid loading."

  lastEditedCell = null

  lockLabels = (lock = true, grid=$('#stringSettingsGrid'), btn = $('#custom_lock_stringSettingsGrid > div.ui-pg-div'))->

    icon = 'ui-icon-'
    text = unText = 'lock'
    if lock
      unText = "un#{text}"
      grid.setColProp('reference', {editable: false, classes: ''})
        .setColProp('key', {editable: false, classes: ''})
    else
      text = "un#{text}"
      grid.setColProp('reference', {editable: true, classes: 'editable-column'})
        .setColProp('key', {editable: true, classes: 'editable-column'})
    $('#custom_add_stringSettingsGrid, #custom_del_stringSettingsGrid').toggleClass('ui-state-disabled', lock)

    icon += "#{text}ed"
    text = i18n.dialog.stringsettings[unText]
    btn.html "<span class=\"ui-icon #{icon}\"></span>#{text}"

  dicGrid = $('#stringSettingsGrid').jqGrid(
    url: 'json/dummy.json', mtype: 'post', datatype: 'local'
    width: 880, height: 300
    pager: '#stringSettingsPager'
    editurl: urls.label.create
    rowNum: 10, rowList: [10, 20, 30]
    sortorder: 'asc'
    viewrecords: true
    gridview: true, multiselect: true, cellEdit: true, cellurl: urls.label.update
    colNames: ['Label', 'Reference Language', 'T', 'N', 'I', 'Max Length', 'Context', 'Description', ]
    colModel: [
      {name: 'key', index: 'key', width: 100, editable: false, align: 'left', search:false}
      {name: 'reference', index: 'reference', width: 200, edittype:'textarea', editable: false, align: 'left',search:false}
      {name: 't', index: 't', sortable: true, width: 15, align: 'right', formatter: 'showlink',search:false, search:false
      formatoptions:
        baseLinkUrl: '#', addParam: encodeURI("&status=2")
      }
      {name: 'n', index: 'n', formatter: 'showlink', sortable: true, width: 15, align: 'right',search:false
      formatoptions:
        baseLinkUrl: '#', addParam: encodeURI("&status=0")
      }
      {name: 'i', index: 'i', formatter: 'showlink', sortable: true, width: 15, align: 'right',search:false
      formatoptions:
        baseLinkUrl: '#', addParam: encodeURI("&status=1")
      }
      {name: 'maxLength', index: 'maxLength', width: 40, editable: true, classes: 'editable-column', align: 'right', search:false}
      {name: 'context', index: 'context.name', width: 40, editable: true, classes: 'editable-column', align: 'left'
      stype: 'select', searchoptions:
        value: ":All;#{c18n.transcontext}"
      editrules:
        required: true
      }
      {name: 'description', index: 'description', width: 60, editable: true, edittype:'textarea', classes: 'editable-column', align: 'left', search:false}
    ]
    gridComplete: ->
      grid = $(@)
      $('a', @).each (index, a)->$(a).before(' ').remove() if '0' == $(a).text()
      $('a', @).css('color', 'blue').click ()->
        param = util.getUrlParams(@href)
        rowData = grid.getRowData(param.id)
        param.key = rowData.key
        param.ref = rowData.reference

        $('#stringSettingsTranslationDialog').data(param: param).dialog 'open'
        false

    afterEditCell: (rowid, cellname, val, iRow, iCol)->lastEditedCell = {iRow: iRow, iCol: iCol, name: name, val: val}
    afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
      json = $.parseJSON(serverresponse.responseText)
      setTimeout (->
        dicGrid.trigger 'reloadGrid'
      ), 10 if 'reference' == cellname and 0== json.status

      [0 == json.status, json.message]
    beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
  ).setGridParam(datatype: 'json').jqGrid('navGrid', '#stringSettingsPager', {edit: false, add: false, del: false, search: false, view: false},{}, {}, {})

  dicGrid.navButtonAdd('#stringSettingsPager', {
    id: "custom_add_#{dicGrid.attr 'id'}"
    caption: c18n.add, buttonicon: "ui-icon-plus"
    position: "last", onClickButton: ()->
      $('#addLabelDialog').data('param', dict: dicGrid.getGridParam('postData').dict).dialog 'open'
    }
  ).navButtonAdd('#stringSettingsPager', {
  id: "custom_del_#{dicGrid.attr 'id'}"
  caption: c18n.del, buttonicon: "ui-icon-trash"
  position: "last", onClickButton: ()->
    if(rowIds = $(@).getGridParam('selarrrow')).length == 0
      $.msgBox (c18n.selrow.format c18n.label), null, {title: c18n.warning}
      return
    dicGrid.jqGrid 'delGridRow', rowIds, {msg: i18n.dialog.delete.delmsg.format(c18n.label), url: urls.label.del}
  }
  ).navSeparatorAdd("#stringSettingsPager",{sepclass : "ui-separator",sepcontent: ''})
  .navButtonAdd('#stringSettingsPager', {
  id: "custom_lock_#{dicGrid.attr 'id'}"
  caption: i18n.dialog.stringsettings.unlocklabels, buttonicon: "ui-icon-locked"
  position: "last", onClickButton: (e)->
    isLocked = $('span.ui-icon',e.target).hasClass('ui-icon-locked')
    lockLabels !isLocked, $(@), $(e.target)
    $(@).trigger 'reloadGrid'
  }
  )
  .setGroupHeaders(useColSpanStyle: true, groupHeaders: [
    {startColumnName: "t", numberOfColumns: 3, titleText: 'Status'}
  ]).filterToolbar(stringResult: true, searchOnEnter: false)


  saveLastEditedCell: ()->dicGrid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell

  lockLabels: lockLabels


