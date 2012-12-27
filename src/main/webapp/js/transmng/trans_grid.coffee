define ['jqgrid', 'util', 'jqmsgbox', 'transmng/grid.colmodel', 'blockui', 'i18n!nls/transmng', 'i18n!nls/common', 'require' ], ($, util, msgbox, gmodel, blockui, i18n, c18n, require)->
#prepare the grid column name and column model parameters for the grid.
  restoreSearchToolBarValue = (column, value)->
    console?.log "Set default value to #{value} for #{column}"
    searchOpts = ($("#transGrid").jqGrid 'getColProp', column).searchoptions
    searchOpts.defaultValue = value
    $("#transGrid").jqGrid 'setColProp', column, searchoptions: searchOpts

  common =
    colNames: ['ID', 'Application', 'Version', 'Num of String']
    colModel: [
      {name: 'id', index: 'id', width: 55, align: 'center', hidden: true, frozen: true}
      {name: 'application', index: 'base.name', width: 100, editable: false, align: 'left', frozen: true, stype: 'select'
      searchoptions:
        value: ":All"
        dataEvents: [
          {
          type: 'change', fn: (e)->
            searchvalue = $("#transGrid").jqGrid 'getGridParam', 'searchvalue'
            searchvalue.app = e.target.value
            $("#transGrid").jqGrid 'setGridParam', 'searchvalue', searchvalue
          }
        ]
      }
      {name: 'appVersion', index: 'version', width: 90, editable: false, align: 'left', frozen: true, search: false}
      {name: 'numOfString', index: 'labelNum', width: 80, align: 'right', frozen: true, search: false}
    ]

  grid =
    dictionary:
      colNames: (common.colNames[0..].insert 3, ['Dictionary', 'Version', 'Encoding', 'Format'])
      colModel: (common.colModel[0..].insert 3, [
        {name: 'dictionary', index: 'base.name', width: 90, editable: false, align: 'left', frozen: true, search: false}
        {name: 'dictVersion', index: 'version', width: 90, editable: false, align: 'left', frozen: true, search: false}
        {name: 'encoding', index: 'base.encoding', width: 90, editable: false, align: 'left', frozen: true
        stype: 'select', searchoptions:
          value: ':All;ISO-8859-1:ISO-8859-1;UTF-8:UTF-8;UTF-16LE:UTF-16LE;UTF-16BE:UTF-16BE'
          dataEvents: [
            {
            type: 'change', fn: (e)->
              searchvalue = $("#transGrid").jqGrid 'getGridParam', 'searchvalue'
              searchvalue.encoding = e.target.value
              $("#transGrid").jqGrid 'setGridParam', 'searchvalue', searchvalue
            }
          ]
        }
        {name: 'format', index: 'base.format', width: 90, editable: false, align: 'left', frozen: true
        stype: 'select', searchoptions:
          value: ":All;DCT:DCT;Dictionary conf:Dictionary conf;Text properties:Text properties;XML labels:XML labels"
          dataEvents: [
            {
            type: 'change', fn: (e)->
              searchvalue = $("#transGrid").jqGrid 'getGridParam', 'searchvalue'
              searchvalue.format = e.target.value
              $("#transGrid").jqGrid 'setGridParam', 'searchvalue', searchvalue
            }
          ]
        }
      ])
    application:
      colNames: ['Dummy'].concat common.colNames
      colModel: [
        {name: 'dummy', index: 'dummy', width: 55, align: 'center', hidden: true, frozen: true}
      ].concat common.colModel

  getTableType = ->if -1 == ($.inArray 'Dummy', $("#transGrid").getGridParam('colNames')) then  'dict' else 'app'

  ### Construct the grid with the column name(model) parameters above and other required parameters ###
  transGrid = $("#transGrid").jqGrid {
  url : 'rest/dict'
  mtype: 'post', postData: {}, datatype: 'local'
  width: $(window).innerWidth() * 0.95, height: 330
  rownumbers: true, shrinkToFit: false
  pager: '#transPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
  sortname: 'base.name', sortorder: 'asc', multiselect: true
  colNames: grid.dictionary.colNames, colModel: grid.dictionary.colModel

  beforeProcessing: (data, status, xhr)->
  gridComplete: ->
    transGrid = $(@)

    $('a', @).each (index, a)->$(a).before(' ').remove() if '0' == $(a).text()

    $('a', @).css('color', 'blue').click ()->
      pageParams = util.getUrlParams(@href)
      #      console?.log pageParams
      rowid = pageParams?.id
      language = id: pageParams.languageId, name: pageParams.languageName

      rowData = transGrid.getRowData(rowid)
      allZero = true
      $(['T', 'N', 'I']).each (index, elem)->
        num = parseInt rowData["#{language.name}.#{elem}"]
        allZero = 0 == num
        allZero

      (console?.log 'zero';return) if allZero
      util.getDictLanguagesByDictId rowid, (languages)=>
        transLayout = require('transmng/layout')
        transLayout.showTransDetailDialog {dict: {id: rowid, name: rowData.dictionary}, language: language, languages: languages}

  #  customed option for save the toolbar search value and group headers
  searchvalue: {}, groupHeaders: []
  #  customed method executed when the grid is created.
  afterCreate: (grid)->
    grid.setGridParam 'datatype': 'json'
    grid.setGroupHeaders {useColSpanStyle: true, groupHeaders: grid.getGridParam 'groupHeaders'}
    grid.filterToolbar {stringResult: true, searchOnEnter: false} if getTableType() == 'dict'

    grid.navGrid '#transPager', {edit: false, add: false, del: false, search: false, view: false}
    #    grid.navButtonAdd "#transPager", {caption: "Clear", title: "Clear Search", buttonicon: 'ui-icon-refresh', position: 'first', onClickButton: ()->grid[0].clearToolbar()}
    grid.setFrozenColumns()
  }
  transGrid.getGridParam('afterCreate') transGrid

  #  button for make all label as...
  ($("[id^=makeLabel]").button().click ()->
    transGrid = $("#transGrid")
    selectedRowIds = transGrid.getGridParam('selarrrow').join(',')
    if !selectedRowIds
      ($.msgBox (c18n.selrow.format c18n.dict), null, title: c18n.warning)
      return

    $.blockUI()
    $.post 'trans/update-status', {type: getTableType(), transStatus: @value, id: selectedRowIds}, (json)->
      (alert json.message; return) if json.status != 0
      $.unblockUI()
      $.msgBox i18n.msgbox.transstatus.msg, null, title: c18n.message
      transGrid.trigger 'reloadGrid'
  ).parent().buttonset()

  productReleaseChanged: (param) ->
    transGrid = $("#transGrid")
    summary = ($(param.languages).map ->_this = @;($([0, 1, 2]).map ->"s(#{_this.id})[#{@}]").get().join(',')).get().join(',')
    gridParam = transGrid.getGridParam()

    isApp = (param.level == "application")
    if isApp
      gridParam.colNames = grid.application.colNames
      gridParam.colModel = grid.application.colModel
      url = 'rest/applications'
      prop = "id,id,base.name,version,labelNum,#{summary}"
      transGrid.setColProp 'application', {search: false, index: 'base.name'}
    else
      gridParam.colNames = grid.dictionary.colNames
      gridParam.colModel = grid.dictionary.colModel
      url = 'rest/dict'
      prop = "id,app.base.name,app.version,base.name,version,base.encoding,base.format,labelNum,#{summary}"

      #      fill the search value dynamically for application
      searchoptions = transGrid.getColProp('application').searchoptions
      $.ajax {url: "rest/applications?prod=#{param.release.id}&prop=id,name", async: false, dataType: 'json', success: (json)->
        app = ":All"
        $(json).each ->app += ";#{@name}:#{@name}"
        searchoptions.value = app
      }
      transGrid.setColProp('application', searchoptions: searchoptions, index: 'app.base.name')

      #  restore search option value by if exists
      searchvalue = $("#transGrid").jqGrid 'getGridParam', 'searchvalue'
      restoreSearchToolBarValue 'application', searchvalue.app if searchvalue.app
      restoreSearchToolBarValue 'encoding', searchvalue.encoding if searchvalue.encoding
      restoreSearchToolBarValue 'format', searchvalue.format if searchvalue.format

    #      $("#transGrid")[0].triggerToolbar() if searchvalue.app || searchvalue.encoding || searchvalue.format


    postData = {prod: param.release.id, format: 'grid', prop: prop}
    transGrid.updateTaskLanguage param.languages
    transGrid.reloadAll url, postData


  getTotalSelectedRowInfo: ->
    transGrid = $("#transGrid")
    selectedRowIds = transGrid.getGridParam 'selarrrow'
    count = 0
    $(selectedRowIds).each ->
      row = transGrid.getRowData @
      count += parseInt row.numOfString

    {rowIds: selectedRowIds, totalLabels: count}
  getTableType: getTableType

