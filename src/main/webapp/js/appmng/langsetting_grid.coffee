define (require)->
  $ = require 'jqgrid'

  langSettingGrid = $('#languageSettingGrid').jqGrid {
  url: '', mtype: 'post', datatype: 'json'
  width: 500, height: 230
  pager: '#langSettingPager'
  editurl: ""
  rowNum: 10
  rowList: [10, 20, 30]
  sortname: 'language.name'
  sortorder: 'asc'
  viewrecords: true
  gridview: true, multiselect: true, cellEdit: true, cellurl: '/app/update-dict-language'
  colNames: [ 'Code', 'Language', 'Charset']
  colModel: [
    {name: 'code', index: 'languageCode', width: 40, editable: false, align: 'left'}
    {name: 'languageId', index: 'language.name', width: 50, editable: true, edittype: 'select', align: 'left'}
    {name: 'charsetId', index: 'charset.name', width: 40, editable: true, edittype: 'select', align: 'left'}
  ]
  gridComplete: ->
  #    $('#languageSettingGrid').getGridParam('postData').dict
  }
  langSettingGrid.jqGrid 'navGrid', '#langSettingPager', {edit: false, add: true, del: true, search: false}, {}, {
  #    prmAdd
#  zIndex:1010
  url: '/app/add-dict-language'
  onclickSubmit: (params, posdata)->{dicts: $('#languageSettingGrid').getGridParam('postData').dict}
  onClose: ->$('#languageSettingGrid').setColProp 'code', editable: false
  beforeInitData: ->$('#languageSettingGrid').setColProp 'code', editable: true
  afterSubmit:(response, postdata)->
    jsonfromServer = eval "(#{response.responseText})"
    [jsonfromServer.status ==0 ,jsonfromServer.message,-1]
  }, {
  #    prmDel
  url: '/app/remove-dict-language'
  }

  #    query all the languages
  $.getJSON '/rest/languages', {prop: 'id,name'}, (languages)->
    langSettingGrid.setColProp 'languageId', editoptions: {value: ($(languages).map ()->"#{@id}:#{@name}").get().join(';')}
  #    query all the charsets
  $.getJSON '/rest/charsets', {prop: 'id,name'}, (charsets)->
    langSettingGrid.setColProp 'charsetId', editoptions: {value: ($(charsets).map ()->"#{@id}:#{@name}").get().join(';')}





