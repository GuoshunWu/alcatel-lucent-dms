define ['jqgrid', 'require'], ($, require)->
  transGrid = $("#transGridList").jqGrid ({
  #  url: 'json/taskgrid.json'
  url: ''
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
  caption: 'Translation Task List'
  colNames: [
#    'O'
    'ID', 'Application', 'Version', 'Dictionary', 'Version', 'Encoding', 'Format', 'Num of String'
    , 'T', 'N', 'I', 'T', 'N', 'I', 'T', 'N', 'I'

  ]
  colModel: [
#    {name: 'ido', index: 'ido', width: 55, align: 'center', hidden: true, frozen: true}
    {name: 'id', index: 'id', width: 55, align: 'center', hidden: true, frozen: true}
    {name: 'application', index: 'application', width: 100, editable: true, stype: 'select',
    edittype: 'select', align: 'center', editoptions: {value: "All:All;0.00:0.00;12:12.00"}, frozen: true}
    {name: 'appVersion', index: 'appVersion', width: 90, editable: true, align: 'center', frozen: true}
    {name: 'dictionary', index: 'dictionary', width: 90, editable: true, align: 'center', frozen: true}
    {name: 'dictVersion', index: 'dictVersion', width: 90, editable: true, align: 'center', frozen: true}
    {name: 'encoding', index: 'encoding', width: 90, editable: true, align: 'center', frozen: true}
    {name: 'format', index: 'format', width: 90, editable: true, align: 'center', frozen: true}
    {name: 'numOfString', index: 'NumOfString', width: 80, align: 'center', frozen: true}


    {name: 'Arabic.T', index: 'Arabic.T', width: 20, align: 'center'}
    {name: 'Arabic.N', index: 'Arabic.N', width: 20, editable: true, align: 'center'}
    {name: 'Arabic.I', index: 'Arabic.I', width: 20, editable: true, align: 'center'}
    {name: 'Czech.T', index: 'Czech.T', width: 20, align: 'center'}
    {name: 'Czech.N', index: 'Czech.N', width: 20, editable: true, align: 'center'}
    {name: 'Czech.I', index: 'Czech.I', width: 20, editable: true, align: 'center'}
    {name: 'Chinese.T', index: 'Chinese.T', width: 20, align: 'center'}
    {name: 'Chinese.N', index: 'Chinese.N', width: 20, editable: true, align: 'center'}
    {name: 'Chinese.I', index: 'Chinese.I', width: 20, editable: true, align: 'center'}
  ]
  groupHeaders: [
    {startColumnName: 'Arabic.T', numberOfColumns: 3, titleText: '<bold>Arabic</bold>'}
    {startColumnName: 'Czech.T', numberOfColumns: 3, titleText: '<bold>Czech</bold>'}
    {startColumnName: 'Chinese.T', numberOfColumns: 3, titleText: '<bold>Chinese</bold>'}
  ]
  gridType: 'dictionary'
  afterCreate: (grid)->
    grid.navButtonAdd "#taskPager", {caption: "Clear", title: "Clear Search", buttonicon: 'ui-icon-refresh', position: 'first', onClickButton: ()->
      grid[0].clearToolbar()
    }

    #  grid.navButtonAdd "#taskPager", {caption: "Toggle", title: "Toggle Search Toolbar", buttonicon: 'ui-icon-pin-s', position: 'first', onClickButton: ()->
    #    grid[0].toggleToolbar()
    #  }
    grid.setGroupHeaders {useColSpanStyle: true,
    groupHeaders: grid.getGridParam 'groupHeaders'
    }
    #    grid.filterToolbar {stringResult: true, searchOnEnter: false}
    grid.navGrid '#taskPager', {edit: true, add: true, del: false, search: false, view: false}
    grid.setFrozenColumns()
  })


  transGrid.getGridParam('afterCreate') transGrid

  # test for UI
  $("#create").button().click ->
    transGrid.updateTaskLanguage(['Chinese', 'Chinese(Taiwan)', 'Indian'], 'json/taskgrid1.json')
  #  taskGrid.addTaskLanguage 'Japanese','json/taskgrid1.json'


  productReleaseChanged: (param) ->
    console.log "productReleaseChanged"
    prop = ($(param.languages).map ->_this = this;($([0, 1, 2]).map ->"s(#{_this.id})[#{this}]").get().join(',')).get().join(',')
    gridParam = transGrid.getGridParam()
    langugaeNames = ($(param.languages).map ->this.name).get()
    console.log param.languages
    if param.level == "app"
      console.log "Application level"
      if gridParam.gridType == 'dictionary'
        index = (gridParam.colNames.indexOf 'Dictionary') + 1
        gridParam.colNames = $.merge gridParam.colNames.slice(0, index), gridParam.colNames.slice(index + 1, gridParam.colNames.length)
        #     remove column name from grid
        gridParam.colNames = $.grep gridParam.colNames, (val, key)->  valnotin ['Dictionary', 'Encoding', 'Format']
        #     remove colModel from grid.
        gridParam.colModel = $.grep gridParam.colModel, (val, key)-> val.namenotin ['dictionary', 'dictVersion', 'encoding', 'format']
        gridParam.gridType = 'application'
      transGrid.updateTaskLanguage langugaeNames, 'json/taskgrid.json'
    #      transGrid.updateTaskLanguage ($(param.languages).map ->this.name).get(), "rest/app", {prod: param.release.id, prop: prop}
    else
      prop = "id,app.base.name,base.name,encoding,format,labelNum," + prop
      console.log "Dictionary level"
      if gridParam.gridType == 'application'
        gridParam.colNames = ['ID', 'Application', 'Version', 'Dictionary', 'Version', 'Encoding', 'Format', 'Num of String']
        $.merge gridParam.colModel, [
          {name: 'dictionary', index: 'dictionary', width: 90, editable: true, align: 'center', frozen: true}
          {name: 'dictVersion', index: 'dictVersion', width: 90, editable: true, align: 'center', frozen: true}
          {name: 'encoding', index: 'encoding', width: 90, editable: true, align: 'center', frozen: true}
          {name: 'format', index: 'format', width: 90, editable: true, align: 'center', frozen: true}
        ]
        gridParam.gridType = 'dictionary'
      transGrid.updateTaskLanguage langugaeNames, 'json/taskgrid.json'
#       transGrid.updateTaskLanguage ($(param.languages).map ->this.name).get(), "rest/dict", {prod: param.release.id, format: 'grid', prop: prop}