define (require)->
  $ = require 'jqgrid'
  i18n = require 'i18n!nls/appmng'
  c18n = require 'i18n!nls/common'

  lastEditedCell = null

  langSettingGrid = $('#languageSettingGrid').jqGrid {
  url: 'json/dummy.json', mtype: 'post', datatype: 'json'
  width: 500, height: 230
  pager: '#langSettingPager'
  editurl: ""
  rowNum: 10
  rowList: [10, 20, 30]
  sortname: 'language.name'
  sortorder: 'asc'
  viewrecords: true
  #  ajaxGridOptions:{async:false}
  gridview: true, multiselect: true, cellEdit: true, cellurl: 'app/update-dict-language'
  colNames: [ 'Code', 'Language', 'Charset']
  colModel: [
    {name: 'code', index: 'languageCode', width: 40, editable: false, align: 'left'}
    {name: 'languageId', index: 'language.name', width: 50, editable: true, classes: 'editable-column', edittype: 'select', align: 'left'}
    {name: 'charsetId', index: 'charset.name', width: 40, editable: true, classes: 'editable-column', edittype: 'select', align: 'left'}
  ]
  afterEditCell: (rowid, cellname, val, iRow, iCol)->lastEditedCell = {iRow: iRow, iCol: iCol, name: name, val: val}
  gridComplete: ->
  #    $('#languageSettingGrid').getGridParam('postData').dict
  }
  langSettingGrid.jqGrid 'navGrid', '#langSettingPager', {edit: false, add: false, del: true, search: false}, {}, {
  #    prmAdd
  zIndex: 2000
  modal: true
  url: 'app/add-dict-language'
  onclickSubmit: (params, posdata)->{dicts: $('#languageSettingGrid').getGridParam('postData').dict}
  onClose: ->$('#languageSettingGrid').setColProp 'code', editable: false
  beforeInitData: ->$('#languageSettingGrid').setColProp 'code', editable: true
  afterSubmit: (response, postdata)->
    jsonfromServer = eval "(#{response.responseText})"
    [jsonfromServer.status == 0 , jsonfromServer.message, -1]
  }, {
  #    prmDel
  zIndex: 2000
  top: 250, left: 550
  msg: i18n.dialog.delete.delmsg.format c18n.language
  url: 'app/remove-dict-language'
  }
  #  custom button for add language
  langSettingGrid.navButtonAdd '#langSettingPager', {caption: "", buttonicon: "ui-icon-plus", position: "first"
  onClickButton: ()->
    $('#addLanguageDialog').data 'param', dicts: [$('#languageSettingGrid').getGridParam('postData').dict]
    $('#addLanguageDialog').dialog "open"
  }

  #    query all the languages
  $.getJSON 'rest/languages', {prop: 'id,name'}, (languages)->
    langSettingGrid.setColProp 'languageId', editoptions: {value: ($(languages).map ()->"#{@id}:#{@name}").get().join(';')}
  #    query all the charsets
  $.getJSON 'rest/charsets', {prop: 'id,name'}, (charsets)->
    langSettingGrid.setColProp 'charsetId', editoptions: {value: ($(charsets).map ()->"#{@id}:#{@name}").get().join(';')}

  saveLastEditedCell: ()->langSettingGrid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell





