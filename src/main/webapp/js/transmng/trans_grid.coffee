define ['jqgrid', 'util', 'require', 'jqmsgbox','i18n!nls/transmng','i18n!nls/common'], ($, util, require,msgbox,i18n,c18n)->
  common =
    {
    colNames: ['ID', 'Application', 'Version', 'Num of String']
    colModel: [
      {name: 'id', index: 'id', width: 55, align: 'center', hidden: true, frozen: true}
      {name: 'application', index: 'base.name', width: 100, editable: false, stype: 'select', align: 'center', frozen: true}
      {name: 'appVersion', index: 'version', width: 90, editable: true, align: 'center', frozen: true, search: false}
      {name: 'numOfString', index: 'labelNum', width: 80, align: 'left', frozen: true, search: false}
    ]
    }

  grid = {
  dictionary:
    {
    colNames: common.colNames.slice(0).insert 3, ['Dictionary', 'Version', 'Encoding', 'Format']
    colModel: common.colModel.slice(0).insert 3, [
      {name: 'dictionary', index: 'base.name', width: 90, editable: true, align: 'left', frozen: true, search: false}
      {name: 'dictVersion', index: 'version', width: 90, editable: true, align: 'center', frozen: true, search: false}
      {name: 'encoding', index: 'base.encoding', width: 90, editable: true, stype: 'select', searchoptions: {value: ':All;ISO-8859-1:ISO-8859-1;UTF-8:UTF-8;UTF-16LE:UTF-16LE;UTF-16BE:UTF-16BE'}, align: 'center', frozen: true}
      {name: 'format', index: 'base.format', width: 90, editable: true, stype: 'select', searchoptions: {value: ":All;DCT:DCT;Dictionary conf:Dictionary conf;Text properties:Text properties;XML labels:XML labels"}, align: 'center', frozen: true}
    ]
    ondblClickRow: (rowid, iRow, iCol, e)->
      language = {name: $(@).getGridParam('colModel')[iCol].name.split('.')[0], id: parseInt(/s\((\d+)\)\[\d+\]/ig.exec($(@).getGridParam('colModel')[iCol].index)[1])}
      dictName = $(@).getCell rowid, $(@).getGridParam('colNames').indexOf 'Dictionary'
      util.getDictLanguagesByDictId rowid, (languages)=>
        require('transmng/layout').showTransDetailDialog {dict: {id: rowid, name: dictName}, language: language, languages: languages}
    }
  application:
    {
    colNames: ['Dummy'].concat common.colNames
    colModel: [
      {name: 'dummy', index: 'dummy', width: 55, align: 'center', hidden: true, frozen: true}
    ].concat common.colModel
    }
  }

  getTableType = ->if -1 == ($.inArray 'Dummy', $("#transGrid").getGridParam('colNames')) then  'dict' else 'app'

  transGrid = $("#transGrid").jqGrid {
  url: '' # url: 'json/taskgrid.json'
  mtype: 'POST', postData: {}, editurl: "", datatype: 'json'
  width: $(window).width() * 0.95, height: 300, shrinkToFit: false
  rownumbers: true, loadonce: false # for reload the colModel
  pager: '#transPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
  sortname: 'base.name', sortorder: 'asc', viewrecords: true, gridview: true, multiselect: true
  caption: ''
  colNames: grid.dictionary.colNames, colModel: grid.dictionary.colModel
  groupHeaders: []
  afterCreate: (grid)->
    grid.setGroupHeaders {useColSpanStyle: true, groupHeaders: grid.getGridParam 'groupHeaders'}
    grid.filterToolbar {stringResult: true, searchOnEnter: false} if getTableType() == 'dict'

    grid.navGrid '#transPager', {edit: false, add: false, del: false, search: false, view: false}
    grid.navButtonAdd "#taskPager", {caption: "Clear", title: "Clear Search", buttonicon: 'ui-icon-refresh', position: 'first', onClickButton: ()->grid[0].clearToolbar()}
    grid.setFrozenColumns()
  }
  transGrid.getGridParam('afterCreate') transGrid

  ($("[id^=makeLabel]").button().click ()->
    transGrid = $("#transGrid")
    selectedRowIds = transGrid.getGridParam('selarrrow').join(',')
    if !selectedRowIds
      $.msgBox i18n.msgbox.rowsel.msg, null, title: c18n.warning
      return
    #    console.log "selected: #{selectedRowIds}, type: #{getTableType()}, value: #{@value}"

    $.post '/trans/update-status', {type: getTableType(), transStatus: @value, id: selectedRowIds}, (json)->
      (alert json.message; return) if json.status != 0
      $.msgBox i18n.msgbox.transstatus.msg, null, title: c18n.message
      transGrid.trigger 'reloadGrid'
  ).parent().buttonset()

  productReleaseChanged: (param) ->
    summary = ($(param.languages).map ->_this = @;($([0, 1, 2]).map ->"s(#{_this.id})[#{@}]").get().join(',')).get().join(',')
    gridParam = transGrid.getGridParam()

    isApp = (param.level == "application")
    if isApp
      gridParam.colNames = grid.application.colNames
      gridParam.colModel = grid.application.colModel
      gridParam.ondblClickRow = (()->)
      url = 'rest/applications'
      prop = "id,id,base.name,version,labelNum,#{summary}"
      transGrid.setColProp 'application', {search: false, index: 'base.name'}
    else
      gridParam.colNames = grid.dictionary.colNames
      gridParam.colModel = grid.dictionary.colModel
      gridParam.ondblClickRow = grid.dictionary.ondblClickRow
      url = 'rest/dict'
      prop = "id,app.base.name,app.version,base.name,version,base.encoding,base.format,labelNum,#{summary}"

      $.ajax {url: "rest/applications?prod=#{param.release.id}&prop=id,name",
      async: false, dataType: 'json', success: (json)->
        app = ":All"
        $(json).each ->app += ";#{@name}:#{@name}"
        transGrid.setColProp 'application', {search: true, searchoptions: {value: app}, index: 'app.base.name'}
      }

    postData = {prod: param.release.id, format: 'grid', prop: prop}
    transGrid.updateTaskLanguage param.languages, url, postData


  getTotalSelectedRowInfo: ->
    transGrid = $("#transGrid")
    selectedRowIds = transGrid.getGridParam 'selarrrow'
    count = 0
    $(selectedRowIds).each ->
      row = transGrid.getRowData @
      count += parseInt row.numOfString

    {rowIds: selectedRowIds, selectedNum: selectedRowIds.length, totalLabels: count}
  getTableType: getTableType

