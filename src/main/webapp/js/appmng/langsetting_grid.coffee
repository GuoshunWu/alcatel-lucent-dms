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
  gridview: true,multiselect: true, cellEdit: true, cellurl: '/app/update-dict-language'
  colNames: [ 'Code', 'Language', 'Charset']
  colModel: [
    {name: 'code', index: 'languageCode', width: 40, editable: false, align: 'center'}
    {name: 'name', index: 'language.name', width: 50, editable: true, edittype: 'select', align: 'left'}
    {name: 'charset', index: 'charset.name', width: 40, editable: true, edittype: 'select', align: 'center'}
  ]
  gridComplete: ()->
#    query all the languages
    $.getJSON 'rest/languages', {prop:'id,name'}, (languages)=>
      $(@).setColProp 'name', editoptions: {value: ($(languages).map ()->"#{@id}:#{@name}").get().join(';')}
#    query all the charset
  }
  langSettingGrid.jqGrid 'navGrid', '#langSettingPager', {edit: false, add: true, del: true, search: false},{},{},{
    url:'/app/remove-dict-language'
  }


