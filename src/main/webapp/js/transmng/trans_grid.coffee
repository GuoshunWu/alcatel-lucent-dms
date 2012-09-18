define ['jqgrid', 'util', 'require'], ($, util, require)->
#  a=["a","b","c"];
#  a.remove(1)
#  console.log(a)
  transGrid = $("#transGridList").jqGrid ({
  url: 'json/taskgrid.json'
  #  url: ''
  mtype: 'POST'
  postData: {}
  editurl: ""
  datatype: 'json'
  width: $(window).width() * 0.95
  #autowidth: true
  #height:'auto'
  height: 300
  shrinkToFit: false
  rownumbers: true
  loadonce: false # for reload the colModel
  pager: '#taskPager'
  rowNum: 10
  rowList: [10, 20, 30]
  sortname: 'name'
  sortorder: 'asc'
  viewrecords: true
  gridview: true
  multiselect: true
  multikey: "ctrlKey"
  caption: 'Translation Task List'
  colNames: ['ID', 'Application', 'Version', 'Dictionary', 'Version', 'Encoding', 'Format', 'Num of String']
  colModel: [
    {name: 'id', index: 'id', width: 55, align: 'center', hidden: true, frozen: true}
    {name: 'application', index: 'application', width: 100, editable: true, stype: 'select', edittype: 'select', align: 'center', editoptions: {value: "All:All;0.00:0.00;12:12.00"}, frozen: true}
    {name: 'appVersion', index: 'appVersion', width: 90, editable: true, align: 'center', frozen: true}
    {name: 'dictionary', index: 'dictionary', width: 90, editable: true, align: 'center', frozen: true}
    {name: 'dictVersion', index: 'dictVersion', width: 90, editable: true, align: 'center', frozen: true}
    {name: 'encoding', index: 'encoding', width: 90, editable: true, align: 'center', frozen: true}
    {name: 'format', index: 'format', width: 90, editable: true, align: 'center', frozen: true}
    {name: 'numOfString', index: 'NumOfString', width: 80, align: 'center', frozen: true}
  ]
  groupHeaders: []
  gridType: 'dictionary'
  afterCreate: (grid)->
    grid.setGroupHeaders {useColSpanStyle: true,
    groupHeaders: grid.getGridParam 'groupHeaders'
    }
    grid.filterToolbar {stringResult: true, searchOnEnter: false}
    grid.navGrid '#taskPager', {edit: false, add: false, del: false, search: false, view: false}
    grid.navButtonAdd "#taskPager", {caption: "Clear", title: "Clear Search", buttonicon: 'ui-icon-refresh', position: 'first', onClickButton: ()->
      grid[0].clearToolbar()
    }

    #    grid.navButtonAdd "#taskPager", {caption: "Toggle", title: "Toggle Search Toolbar", buttonicon: 'ui-icon-pin-s', position: 'first', onClickButton: ()->
    #      grid[0].toggleToolbar()
    #    }
    grid.setFrozenColumns()

  })
  transGrid.getGridParam('afterCreate') transGrid

  # test for UI
  $("#create").button().click ->
    alert "not implemented."


  productReleaseChanged: (param) ->
    prop = ($(param.languages).map ->_this = this;($([0, 1, 2]).map ->"s(#{_this.id})[#{this}]").get().join(',')).get().join(',')
    gridParam = transGrid.getGridParam()
    langugaeNames = ($(param.languages).map ->this.name).get()
    if param.level == "app"
      if gridParam.gridType == 'dictionary'
        gridParam.colNames = ['Dummy', 'ID', 'Application', 'Version', 'Num of String']

        gridParam.colModel = [
          {name: 'dummy', index: 'dummy', width: 55, align: 'center', hidden: true, frozen: true}
          {name: 'id', index: 'id', width: 55, align: 'center', hidden: true, frozen: true}
          {name: 'application', index: 'application', width: 100, editable: true, stype: 'select', edittype: 'select', align: 'center', editoptions: {value: "All:All;0.00:0.00;12:12.00"}, frozen: true}
          {name: 'appVersion', index: 'appVersion', width: 90, editable: true, align: 'center', frozen: true}
          {name: 'numOfString', index: 'NumOfString', width: 80, align: 'center', frozen: true}
        ]
        gridParam.gridType = 'application'
      transGrid.updateTaskLanguage langugaeNames, 'json/taskgrid.json'
    #      transGrid.updateTaskLanguage ($(param.languages).map ->this.name).get(), "rest/app", {prod: param.release.id, prop: prop}
    else
      prop = "id,app.base.name,base.name,encoding,format,labelNum," + prop
      if gridParam.gridType == 'application'
        console.log "generate new dictionary table"
        gridParam.colNames = ['ID', 'Application', 'Version', 'Dictionary', 'Version', 'Encoding', 'Format', 'Num of String']
        gridParam.colModel = [
          {name: 'id', index: 'id', width: 55, align: 'center', hidden: true, frozen: true}
          {name: 'application', index: 'application', width: 100, editable: true, stype: 'select', edittype: 'select', align: 'center', editoptions: {value: "All:All;0.00:0.00;12:12.00"}, frozen: true}
          {name: 'appVersion', index: 'appVersion', width: 90, editable: true, align: 'center', frozen: true}
          {name: 'dictionary', index: 'dictionary', width: 90, editable: true, align: 'center', frozen: true}
          {name: 'dictVersion', index: 'dictVersion', width: 90, editable: true, align: 'center', frozen: true}
          {name: 'encoding', index: 'encoding', width: 90, editable: true, align: 'center', frozen: true}
          {name: 'format', index: 'format', width: 90, editable: true, align: 'center', frozen: true}
          {name: 'numOfString', index: 'NumOfString', width: 80, align: 'center', frozen: true}
        ]
        gridParam.gridType = 'dictionary'
      transGrid.updateTaskLanguage langugaeNames, 'json/taskgrid.json'
#       transGrid.updateTaskLanguage ($(param.languages).map ->this.name).get(), "rest/dict", {prod: param.release.id, format: 'grid', prop: prop}