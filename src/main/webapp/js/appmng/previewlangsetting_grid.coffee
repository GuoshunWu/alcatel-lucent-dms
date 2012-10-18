define (require)->
  $ = require 'jqgrid'

  langSettingGrid = $('#previewLanguageSettingGrid').jqGrid {
  url: '', mtype: 'post', datatype: 'json'
  width: 500, height: 230
  pager: '#previewLangSettingPager'
  editurl: ""
  rowNum: 100
  sortname: 'language.name'
  sortorder: 'asc'
  viewrecords: true
  gridview: true, cellEdit: true, cellurl: '/app/deliver-update-dict-language'
  colNames: [ 'Code', 'Language', 'Charset']
  colModel: [
    {name: 'code', index: 'languageCode', width: 40, editable: false, align: 'center'}
    {name: 'languageId', index: 'language.name', width: 50, editable: true, edittype: 'select', align: 'left'}
    {name: 'charsetId', index: 'charset.name', width: 40, editable: true, edittype: 'select', align: 'center'}
  ]
  beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
    postData = $(@).getGridParam 'postData'
    dict: postData.dict, handler: postData.handler
  afterSubmit: (response, postdata)->
    jsonfromServer = eval "(#{response.responseText})"
    [jsonfromServer.status == 0 , jsonfromServer.message, -1]
  }
  langSettingGrid.jqGrid 'navGrid', '#previewLangSettingPager', {edit: false, add: false, del: false, search: false}, {}, {}

  #    query all the languages
  $.getJSON '/rest/languages', {prop: 'id,name'}, (languages)->
    langSettingGrid.setColProp 'languageId', editoptions: {value: ($(languages).map ()->"#{@id}:#{@name}").get().join(';')}
  #    query all the charsets
  $.getJSON '/rest/charsets', {prop: 'id,name'}, (charsets)->
    langSettingGrid.setColProp 'charsetId', editoptions: {value: ($(charsets).map ()->"#{@id}:#{@name}").get().join(';')}





