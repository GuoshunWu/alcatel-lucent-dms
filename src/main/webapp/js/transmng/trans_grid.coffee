define ['jqgrid', 'util', 'require'], ($, util, require)->
  grid = {
  dictionary:
    {
    colNames: ['ID', 'Application', 'Version', 'Dictionary', 'Version', 'Encoding', 'Format', 'Num of String']
    colModel: [
      {name: 'id', index: 'id', width: 55, align: 'center', hidden: true, frozen: true}
      {name: 'application', index: 'app.base.name', width: 100, editable: true, stype: 'select', edittype: 'select', align: 'center', editoptions: {value: "All:All;0.00:0.00;12:12.00"}, frozen: true}
      {name: 'appVersion', index: 'app.version', width: 90, editable: true, align: 'center', frozen: true}
      {name: 'dictionary', index: 'base.name', width: 90, editable: true, align: 'center', frozen: true}
      {name: 'dictVersion', index: 'version', width: 90, editable: true, align: 'center', frozen: true}
      {name: 'encoding', index: 'encoding', width: 90, editable: true, align: 'center', frozen: true}
      {name: 'format', index: 'format', width: 90, editable: true, align: 'center', frozen: true}
      {name: 'numOfString', index: 'labelNum', width: 80, align: 'center', frozen: true}
    ]
    }
  application:
    {
    colNames: ['Dummy', 'ID', 'Application', 'Version', 'Num of String']
    colModel: [
      {name: 'dummy', index: 'dummy', width: 55, align: 'center', hidden: true, frozen: true}
      {name: 'id', index: 'id', width: 55, align: 'center', hidden: true, frozen: true}
      {name: 'application', index: 'base.name', width: 100, editable: true, stype: 'select', edittype: 'select', align: 'center', editoptions: {value: "All:All;0.00:0.00;12:12.00"}, frozen: true}
      {name: 'appVersion', index: 'version', width: 90, editable: true, align: 'center', frozen: true}
      {name: 'numOfString', index: 'labelNum', width: 80, align: 'center', frozen: true}
    ]
    }
  }
  transGrid = $("#transGridList").jqGrid {
  url: 'json/taskgrid.json' #  url: ''
  mtype: 'POST', postData: {}, editurl: "", datatype: 'json'
  width: $(window).width() * 0.95, height: 300, shrinkToFit: false
  rownumbers: true, loadonce: false # for reload the colModel
  pager: '#taskPager', rowNum: 10, rowList: [10, 20, 30]
  sortname: 'base.name', sortorder: 'asc', viewrecords: true, gridview: true, multiselect: true, multikey: "ctrlKey"
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
    isApp = (param.level == "app")
    gridParam.colNames = if isApp then grid.application.colNames else grid.dictionary.colNames
    gridParam.colModel = if isApp then grid.application.colModel else  grid.dictionary.colModel

    eprop = "id,app.base.name,app.version,base.name,version,base.encoding,base.format,labelNum,"
    eprop = 'id,id,base.name,version,labelNum,' if isApp

    prop = eprop + summary
    postData = {prod: param.release.id, format: 'grid', prop: prop}
    url = if isApp then 'rest/applications' else 'rest/dict'
    transGrid.updateTaskLanguage langugaeNames, url ,postData
