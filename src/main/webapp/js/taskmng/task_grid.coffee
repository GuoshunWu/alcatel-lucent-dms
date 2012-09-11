#$ ()->

taskGrid = $("#taskGridList").jqGrid ({
url: ''
editurl: ""
datatype: 'json'
#width: 1000
autowidth: true
height: 320
pager: '#taskPager'
rowNum: 10
rowList: [10, 20, 30]
sortname: 'name'
sortorder: 'asc'
viewrecords: true
gridview: true
caption: 'Translation Task List'
colNames: ['ID', 'Application', 'Dictionary', 'Encoding', 'Format', 'Num of String', 'T', 'N', 'I', 'T', 'N', 'I', 'T', 'N', 'I']
colModel: [
  {name: 'id', index: 'id', width: 55, align: 'center', hidden: true}
  {name: 'application', index: 'application', width: 100, editable: true, align: 'center'}
  {name: 'dictionary', index: 'dictionary', width: 90, editable: true, align: 'center'}
  {name: 'encoding', index: 'encoding', width: 90, editable: true, align: 'center'}
  {name: 'format', index: 'format', width: 90, editable: true, align: 'center'}
  {name: 'numOfString', index: 'NumOfString', width: 80, align: 'center'}
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
})
taskGrid.jqGrid('navGrid', '#taskPager', {edit: false, add: true, del: false, search: false, view: false})
taskGrid.jqGrid 'setGroupHeaders', {useColSpanStyle: true
groupHeaders: [
  {startColumnName: 'Arabic.T', numberOfColumns: 3, titleText: '<bold>Arabic</bold>'}
  {startColumnName: 'Czech.T', numberOfColumns: 3, titleText: '<bold>Czech</bold>'}
  {startColumnName: 'Chinese.T', numberOfColumns: 3, titleText: '<bold>Chinese</bold>'}
]}
taskGrid.jqGrid 'filterToolbar',{stringResult: true,searchOnEnter : false}


