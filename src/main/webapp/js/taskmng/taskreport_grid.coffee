define ['jqgrid', 'util', 'require'], ($, util, require)->
  colNames = ['Dictionary', 'Total']
  colModel = [
    {name: 'name', index: 'dict', width: 240, editable: false, stype: 'select', align: 'left', frozen: true}
    {name: 'total', index: 'total', width: 90, editable: false, align: 'right', frozen: true}
  ]
  groupHeader = []
  grid = $("#reportGrid").jqGrid {

  url: 'json/transreportgrid.json'
  mtype: 'POST', postData: {}, editurl: "", datatype: 'json'
  width: $(window).width() * 0.6, height: 200, shrinkToFit: false
  pager: '#reportPager', rowNum: 60, rowList: [30, 60, 120]
  sortname: 'key', sortorder: 'asc', viewrecords: true, gridview: true, multiselect: true,
  cellEdit: true, cellurl: ''
  colNames: colNames, colModel: colModel, groupHeaders: groupHeader
  afterCreate: (grid)->
    grid.setGroupHeaders {useColSpanStyle: true, groupHeaders: grid.getGridParam('groupHeaders')}
    grid.navGrid '#reportPager', {edit: false, add: false, del: false, search: false, view: false}
    grid.setFrozenColumns()
  ondblClickRow: (rowid, iRow, iCol, e)->
    col = $(@).getGridParam('colModel')[iCol]
    [trs, id] = [/s\(\d+\)\[(\d+)\]/ig.exec(col.index), /s\((\d+)\)\[\d+\]/ig.exec(col.index)]
    return if !trs
    language = name: col.name.split('.')[0], translated: Number(!parseInt trs[1]), id: id[1]
    dialogs = require 'taskmng/dialogs'

    dialogs.viewDetail.data 'param', {task: $(@).getGridParam('postData').task, language: language.id, translated: language.translated, context: rowid}
    dialogs.viewDetail.dialog 'open'
  }

  grid.getGridParam('afterCreate') grid


  regenerateGrid: (params)->
    gridId = '#reportGrid'
    gridParam = $(gridId).jqGrid 'getGridParam'
    $(gridId).GridUnload 'reportGrid'

    cols = ['T', 'N']

    gridParam.colNames = colNames.slice(0)
    gridParam.colModel = colModel.slice(0)
    gridParam.groupHeader = groupHeader.slice(0)

    $(params.languages).each (index, language)->
      $.merge gridParam.colNames, cols
      $.merge gridParam.colModel, $(cols).map(
        (index)->name: "#{language.name}.#{@}", sortable: false, index: "s(#{language.id})[#{index}]", width: 40, editable: false, align: 'center').get()
      gridParam.groupHeaders.push {startColumnName: "#{language.name}.T", numberOfColumns: cols.length, titleText: "<bold>#{language.name}</bold>"}

    gridParam.url = '/rest/task/summary'
    gridParam.postData = task: params.id, format: 'grid', prop: 'context.name,total,' + $(params.languages).map(
      (index, language)->$([0, 1]).map(
        (idx)->"s(#{language.id})[#{idx}]").get().join(',')
    ).get().join ','
    delete gridParam.selarrrow

    newGrid = $(gridId).jqGrid gridParam
    gridParam.afterCreate newGrid













