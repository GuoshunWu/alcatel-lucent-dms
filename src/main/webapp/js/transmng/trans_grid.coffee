taskGrid = $("#taskGridList").jqGrid ({
url: 'json/taskgrid.json'
editurl: ""
datatype: 'json'
width: $(window).width() *0.95
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
colNames: ['ID', 'Application', 'Dictionary', 'Encoding', 'Format', 'Num of String'
  , 'T', 'N', 'I', 'T', 'N', 'I', 'T', 'N', 'I'

]
colModel: [
  {name: 'id', index: 'id', width: 55, align: 'center', hidden: true, frozen: true}
  {name: 'application', index: 'application', width: 100, editable: true, stype: 'select', edittype: 'select', align: 'center', editoptions: {value: "All:All;0.00:0.00;12:12.00"}, frozen: true}
  {name: 'dictionary', index: 'dictionary', width: 90, editable: true, align: 'center', frozen: true}
  {name: 'encoding', index: 'encoding', width: 90, editable: true, align: 'center', frozen: true}
  {name: 'format', index: 'format', width: 90, editable: true, align: 'center', frozen: true}
  {name: 'numOfString', index: 'NumOfString', width: 80, align: 'center', frozen: true}

  {name: 'Arabic.T', index: 'T', width: 20, align: 'center'}
  {name: 'Arabic.N', index: 'N', width: 20, editable: true, align: 'center'}
  {name: 'Arabic.I', index: 'I', width: 20, editable: true, align: 'center'}
  {name: 'Czech.T', index: 'T', width: 20, align: 'center'}
  {name: 'Czech.N', index: 'N', width: 20, editable: true, align: 'center'}
  {name: 'Czech.I', index: 'I', width: 20, editable: true, align: 'center'}
  {name: 'Chinese.T', index: 'T', width: 20, align: 'center'}
  {name: 'Chinese.N', index: 'N', width: 20, editable: true, align: 'center'}
  {name: 'Chinese.I', index: 'I', width: 20, editable: true, align: 'center'}
]
groupHeaders: [
    {startColumnName: 'Arabic.T', numberOfColumns: 3, titleText: '<bold>Arabic</bold>'}
    {startColumnName: 'Czech.T', numberOfColumns: 3, titleText: '<bold>Czech</bold>'}
    {startColumnName: 'Chinese.T', numberOfColumns: 3, titleText: '<bold>Chinese</bold>'}
]
afterCreate: (grid)->
  grid.navGrid '#taskPager', {edit: true, add: true, del: false, search: false, view: false}

  grid.navButtonAdd "#taskPager", {caption: "Clear", title: "Clear Search", buttonicon: 'ui-icon-refresh', position: 'first', onClickButton: ()->
    grid[0].clearToolbar()
  }

  #  grid.navButtonAdd "#taskPager", {caption: "Toggle", title: "Toggle Search Toolbar", buttonicon: 'ui-icon-pin-s', position: 'first', onClickButton: ()->
  #    grid[0].toggleToolbar()
  #  }

  grid.setGroupHeaders {useColSpanStyle: true, groupHeaders: grid.getGridParam 'groupHeaders' }
  grid.filterToolbar {stringResult: true, searchOnEnter: false}
  grid.setFrozenColumns()
})
$("#languageFilterTable input[name='languages']").map () -> {id: this.id, checked: this.checked, name: this.value} if this.checked

taskGrid.getGridParam('afterCreate') taskGrid

# test for UI
$("#create").button().click ()->
#  taskGrid.addTaskLanguage 'Japanese','json/taskgrid1.json'
  taskGrid.updateTaskLanguage(['Japanese', 'Chinese', 'Indian'], 'json/taskgrid1.json')