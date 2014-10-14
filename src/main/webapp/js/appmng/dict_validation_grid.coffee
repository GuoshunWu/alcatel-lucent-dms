define [
  'jqgrid'
  'blockui'
  'jqmsgbox'
  'jqueryui'

  'i18n!nls/common'
  'dms-util'
  'dms-urls'
  'commons/validationMapping'

], (
  $, blockui, msgbox, ui, c18n, util, urls,
  mapping
)->
  gridId = 'dictValidationGrid'
  hGridId = '#' + gridId
  pagerId = gridId + 'Pager'
  hPagerId = '#' + pagerId

  editOptionValues = $.map(mapping, (elem, index)->
    index + ":" + elem
  ).join(";")

  selectColumns = ['code']
  selectElements = {}

  grid = $(hGridId).after($("<div>").attr("id", pagerId)).jqGrid(
    url: urls.dict_validation
    datatype: 'local', mtype: 'post'
    postData: { format:'grid', prop: 'code,message'}
    rowNum: 20, rowList: [20, 50, 100, 200]
    viewrecords: true

    loadComplete: (data)->
      grid = $(@)

      $.each(selectElements, (colName, select)->
        return unless select
        currentSelected = select.val()
        newValue = util.buildSearchSelectValues(grid, colName, currentSelected)
        select.empty().append(newValue)
      ) if selectColumns.length

    gridview: true,
    pager: pagerId,
    width: 810, height: 400
    colNames: ['Type', 'Message']
    colModel: [
      {name: 'code', width: 200, index: 'code', editable:false, align: 'left',
      formatter: 'select', editoptions: {value: editOptionValues}, search: true
      }
      {name: 'message', width: 610, index: 'message', editable:false, align: 'left'}
    ]
  ).setGridParam(datatype: 'json')
  .navGrid(hPagerId, {edit: false, add: false, del: false, search: false, view: false},{},{},{})

  selectElements = util.setSearchSelect grid, selectColumns, selectElements

  grid.filterToolbar {stringResult: true, searchOnEnter: true}

  grid: grid



