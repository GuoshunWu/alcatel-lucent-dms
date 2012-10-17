define (require, util, i18n)->
  $ = require 'jqgrid'
  util = require 'util'
  i18n = require 'i18n!nls/appmng'
  require('jqmsgbox')
  c18n = require 'i18n!nls/common'

  dicGrid = $('#dictListPreviewGrid').jqGrid {
  url: '', datatype: 'json', editurl: "",
  width: 1000, minHeight: 200, height: 240
  pager: '#dictListPreviewPager',rowNum: 30, rowList: [10, 20, 30]
  sortname: 'base.name', sortorder: 'asc'
  viewrecords: true, cellEdit: true, cellurl: '/app/update-dict'
  gridview: true, multiselect: true
  caption: 'Dictionary for Application'
  colNames: ['LangRefCode', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Action']
  colModel: [
    {name: 'langrefcode', index: 'langrefcode', width: 55, align: 'center', hidden: true}
    {name: 'name', index: 'base.name', width: 200, editable: false, align: 'left'}
    {name: 'version', index: 'version', width: 25, editable: true, editoptions: {value: {}}, align: 'center'}
    {name: 'format', index: 'base.format', width: 60, editable: true, edittype: 'select',
    editoptions: {value: "DCT:DCT;Dictionary conf:Dictionary conf;Text properties:Text properties;XML labels:XML labels"},
    align: 'center'}
    {name: 'encoding', index: 'base.encoding', width: 40, editable: true, edittype: 'select',
    editoptions: {value: 'ISO-8859-1:ISO-8859-1;UTF-8:UTF-8;UTF-16LE:UTF-16LE;UTF-16BE:UTF-16BE'}, align: 'center'}
    {name: 'labelNum', index: 'labelNum', width: 20, align: 'center'}
    {name: 'action', index: 'action', width: 45, editable: false, align: 'center'}
  ]
  #  beforeProcessing: (data, status, xhr)->
  #    actIndex = $(@).getGridParam('colNames').indexOf('Action')
  #    --actIndex if $(@).getGridParam('multiselect')
  #    $(data.rows).each (index)->
  #      rowData = @
  #      @cell[actIndex] = ($(['S', 'L', 'X']).map ->"<A id='action_#{@}_#{rowData.id}_#{actIndex}' href=# >#{@}</A>").get().join('')
  #  afterEditCell: (id, name, val, iRow, iCol)->
  #    grid = @
  #    if name == 'version'
  #    #        console.log "name=#{name},id=#{id},val=#{val}"
  #      $.ajax {url: "/rest/dict?slibing=#{id}&prop=id,version", async: false, dataType: 'json', success: (json)->
  #        $("##{iRow}_version", grid).append $(json).map ()->opt = new Option(@version, @id);opt.selected = @version == val; opt
  #      }
  #  beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
  #    isVersion = cellname == 'version'
  #    $(@).setGridParam cellurl: if isVersion then '/app/change-dict-version' else '/app/update-dict'
  #    if isVersion then {appId: $("#selAppVersion").val(), newDictId: value} else {}
  #
  #  afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
  #    jsonFromServer = eval "(#{serverresponse.responseText})"
  #    [0 == jsonFromServer.status, jsonFromServer.message]
  #  gridComplete: ->
  #    $('a[id^=action_]', @).button {
  #    create: (e, ui)->
  #      [a, action, rowid, col]=@id.split('_')
  #      titles =
  #        S: i18n.dialog.stringsettings.title
  #        L: i18n.dialog.languagesettings.title
  #        X: i18n.dialog.delete.title
  #      @title = titles[action]
  #      @onclick = (e)->
  #        rowData = $('#dictionaryGridList').getRowData(rowid)
  #        delete rowData.action
  #        rowData.id = rowid
  #        switch action
  #          when 'S'
  #            stringSetting rowData
  #          when 'L'
  #            languageSetting rowData
  #          when 'X'
  #            deleteRow rowid
  #          else
  #            console.log 'Invalid action'
  #    }
  }
#  dicGrid.jqGrid 'navGrid', '#dictListPreviewPager', {add: false, edit: false, search: false}, {}, {}, {}

  previewUpdate: (param)->
    dicGrid = $('#dictListPreviewGrid')
    dicGrid.setGridParam url: '/rest/delivery/dict',postData: {format: 'grid', handler: param.handler, prop: 'languageReferenceCode,base.name,version,base.format,base.encoding,labelNum'}
    dicGrid.trigger 'reloadGrid'






