define ['jqgrid', 'util', 'require'], ($, util, require)->
  common =
    {
    colNames: ['ID', 'Application', 'Version', 'Num of String']
    colModel: [
      {name: 'id', index: 'id', width: 55, align: 'center', hidden: true, frozen: true}
      {name: 'application', index: 'base.name', width: 100, editable: false, stype: 'select', align: 'center', frozen: true}
      {name: 'appVersion', index: 'version', width: 90, editable: true, align: 'center', frozen: true, search: false}
      {name: 'numOfString', index: 'labelNum', width: 80, align: 'center', frozen: true, search: false}
    ]
    }

  grid = {
  dictionary:
    {
    colNames: common.colNames.slice(0).insert 3, ['Dictionary', 'Version', 'Encoding', 'Format']
    colModel: common.colModel.slice(0).insert 3, [
      {name: 'dictionary', index: 'base.name', width: 90, editable: true, align: 'center', frozen: true, search: false}
      {name: 'dictVersion', index: 'version', width: 90, editable: true, align: 'center', frozen: true, search: false}
      {name: 'encoding', index: 'encoding', width: 90, editable: true, align: 'center', frozen: true, search: false}
      {name: 'format', index: 'format', width: 90, editable: true, align: 'center', frozen: true, search: false}
    ]
    }
  application:
    {
    colNames: common.colNames.slice(0).insert 0, 'Dummy'
    colModel: common.colModel.slice(0).insert 0, {name: 'dummy', index: 'dummy', width: 55, align: 'center', hidden: true, frozen: true}
    }
  }
  transGrid = $("#transGridList").jqGrid {
  url: '' # url: 'json/taskgrid.json'
  mtype: 'POST', postData: {}, editurl: "", datatype: 'json'
  width: $(window).width() * 0.95, height: 300, shrinkToFit: false
  rownumbers: true, loadonce: false # for reload the colModel
  pager: '#taskPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
  sortname: 'base.name', sortorder: 'asc', viewrecords: true, gridview: true, multiselect: true
  #  , multikey: "ctrlKey"
  caption: 'Translation Task List'
  colNames: grid.dictionary.colNames, colModel: grid.dictionary.colModel
  groupHeaders: []
  afterCreate: (grid)->
    grid.setGroupHeaders {useColSpanStyle: true, groupHeaders: grid.getGridParam 'groupHeaders'}
    grid.filterToolbar {stringResult: true, searchOnEnter: false}
    grid.navGrid '#taskPager', {edit: false, add: false, del: false, search: false, view: false}
    grid.navButtonAdd "#taskPager", {caption: "Clear", title: "Clear Search", buttonicon: 'ui-icon-refresh', position: 'first', onClickButton: ()->grid[0].clearToolbar()}
    grid.setFrozenColumns()
  }
  transGrid.getGridParam('afterCreate') transGrid


  productReleaseChanged: (param) ->
    summary = ($(param.languages).map ->_this = this;($([0, 1, 2]).map ->"s(#{_this.id})[#{this}]").get().join(',')).get().join(',')
    gridParam = transGrid.getGridParam()

    langugaeNames = ($(param.languages).map ->this.name).get()
    isApp = (param.level == "application")
    gridParam.colNames = if isApp then grid.application.colNames else grid.dictionary.colNames
    gridParam.colModel = if isApp then grid.application.colModel else  grid.dictionary.colModel

    eprop = "id,app.base.name,app.version,base.name,version,base.encoding,base.format,labelNum,"
    eprop = 'id,id,base.name,version,labelNum,' if isApp

    prop = eprop + summary
    postData = {prod: param.release.id, format: 'grid', prop: prop}
    url = if isApp then 'rest/applications' else 'rest/dict'
    $.ajax {url: "rest/applications?prod=#{param.release.id}&prop=id,name",
    async: false, dataType: 'json', success: (json)->
      app = {}
      $(json).each -> $(app).attr this.id, this.name
      transGrid.setColProp 'application', {searchoptions: {value: app}}
    }

    transGrid.updateTaskLanguage langugaeNames, url, postData
  #
  getTotalSelectedRowInfo: ->
    transGrid = $("#transGridList")
    selectedRowIds = transGrid.getGridParam 'selarrrow'
    count = 0
    $(selectedRowIds).each ->
      row = $("#transGridList").getRowData this
      count += parseInt row.numOfString

    {rowIds: selectedRowIds, selectedNum: selectedRowIds.length, totalLabels: count}
  getTableType: ->
    if -1 == ($.inArray 'Dummy', $("#transGridList").getGridParam('colNames')) then  'dict' else 'app'

