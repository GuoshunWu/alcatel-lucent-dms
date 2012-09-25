define ['jqgrid', 'require','util','appmng/dialogs'], ($, require,util,dialogs)->
  localIds = {
  dic_grid: '#dictionaryGridList'
  }

  dicGrid = $(localIds.dic_grid).jqGrid {
  url: ''
  datatype: 'json'
  width: 1000
  height: 350
  pager: '#dictPager'
  editurl: "app/create-or-add-application"
  rowNum: 10
  rowList: [10, 20, 30]
  sortname: 'base.name'
  sortorder: 'asc'
  viewrecords: true
  gridview: true
  caption: 'Dictionary for Application'
  colNames: ['LangRefCode', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Action']
  colModel: [
    {name: 'langrefcode', index: 'langrefcode', width: 55, align: 'center', hidden: true}
    {name: 'name', index: 'base.name', width: 200, editable: true, align: 'left'}
    {name: 'version', index: 'version', width: 25, editable: true, align: 'center'}
    {name: 'format', index: 'base.format', width: 20, editable: true, align: 'center'}
    {name: 'encoding', index: 'base.encoding', width: 40, editable: true, align: 'center'}
    {name: 'labelNum', index: 'labelNum', width: 20, align: 'center'}
    {name: 'action', index: 'action', width: 90, editable: true, align: 'center'}
  ]
  beforeProcessing: (data, status, xhr)->
    actIndex = $(@).getGridParam('colNames').indexOf('Action')

    $(data.rows).each (index)->
      ids =
        S: "action_s#{index}#{actIndex}"
        L: "action_l#{index}#{actIndex}"
        X: "action_x#{index}#{actIndex}"
      rowData = @

      @cell[actIndex] = ($(['S', 'L', 'X']).map ->"<A href=\"javascript:dctgrid=require('appmng/dictionary_grid');
                      dctgrid.cellAction('#{this}',#{rowData.id})\" id='#{ids[this]}'>#{this}</A>").get().join('')
  }
  dicGrid.jqGrid('navGrid', '#dictPager', {edit: false, add: true, del: false, search: false, view: false})

  appChanged: (app)->
    url = "rest/dict?app=#{app.id}&format=grid&prop=languageReferenceCode,base.name,version,base.format,base.encoding,labelNum"
    dicGrid.setGridParam({url: url, datatype: "json"}).trigger("reloadGrid")
    appBase = require('appmng/apptree').getSelected()
    dicGrid.setCaption "Dictionary for Application #{appBase.text} version #{app.version}"

  cellAction: (action, rowId)->
    rowData = $('#dictionaryGridList').getRowData(rowId)
    dialogs.langSetting.data "param", {dictId: rowId, refCode: rowData.langrefcode}
    dialogs.langSetting.dialog 'open'


