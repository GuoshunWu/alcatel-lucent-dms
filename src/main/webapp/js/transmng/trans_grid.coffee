define [
  'jqgrid'
  'jqmsgbox'
  'blockui'

  'i18n!nls/transmng'
  'i18n!nls/common'
  'dms-urls'
  'dms-util'

  'transmng/grid.colmodel'
], ($, msgbox, blockui, i18n, c18n, urls, util, gmodel)->


  # prepare the grid column name and column model parameters for the grid.
  restoreSearchToolBarValue = (column, value)->
#    console?.log "Set default value to #{value} for #{column}"
    barSelector = "select[id=gs_#{column}]"
    $(barSelector).each (idx, elem)-> elem.value = value
    searchOpts = ($("#transGrid").jqGrid 'getColProp', column).searchoptions
    searchOpts.defaultValue = value

    $("#transGrid").jqGrid 'setColProp', column, searchoptions: searchOpts

  common =
    colNames: ['ID', 'Application', 'Version', 'Num of String']
    colModel: [
      {name: 'id', index: 'id', width: 55, align: 'center', hidden: true, frozen: true}
      {name: 'application', index: 'base.name', width: 100, editable: false, align: 'left', frozen: true, stype: 'select',
      searchoptions:
        value: ":All"
        dataEvents: [
          {
          type: 'change', fn: (e)->
            searchvalue = $("#transGrid").jqGrid 'getGridParam', 'searchvalue'
            searchvalue.app = e.target.value
            $("#transGrid").jqGrid('setGridParam', 'searchvalue':searchvalue)
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
          value: ":All;#{c18n.dictencodings}"
          dataEvents: [
            {
            type: 'change', fn: (e)->
              searchvalue = $("#transGrid").jqGrid 'getGridParam', 'searchvalue'
              searchvalue.encoding = e.target.value
              $("#transGrid").jqGrid 'setGridParam', 'searchvalue': searchvalue
            }
          ]
        }
        {name: 'format', index: 'base.format', width: 90, editable: false, align: 'left', frozen: true
        stype: 'select', searchoptions:
          value: ":All;#{c18n.dictformats}"
          dataEvents: [
            {
            type: 'change', fn: (e)->
              searchvalue = $("#transGrid").jqGrid 'getGridParam', 'searchvalue'
              searchvalue.format = e.target.value
              $("#transGrid").jqGrid 'setGridParam', 'searchvalue': searchvalue
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

  transGrid = $("#transGrid").jqGrid(
    url: urls.dicts
    mtype: 'post', postData:{}, datatype: 'local'
    width: 1000, height: 300
    viewrecords: true, gridview: true, multiselect: true
    rownumbers: true, shrinkToFit: false
    pager: '#transPager', rowNum: 60, rowList: [20, 30, 60, 120, 500, 1000]
    sortname: 'app.base.name', sortorder: 'asc'
    colNames: grid.dictionary.colNames, colModel: grid.dictionary.colModel
    #  customed option for save the toolbar search value and group headers
    searchvalue: {}, groupHeaders: []

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
          dialogs = require('transmng/dialogs')
          dialogs.showTransDetailDialog {dict:{id: rowid, name: rowData.dictionary}, language: language, languages: languages}
    #  customed method executed when the grid is created.
    afterCreate: (grid)->
      grid.setGridParam 'datatype': 'json'
      grid.setGroupHeaders {useColSpanStyle: true, groupHeaders: grid.getGridParam('groupHeaders')}
      grid.filterToolbar {stringResult: true, searchOnEnter: false} if getTableType() == 'dict'

      grid.navGrid '#transPager', {edit: false, add: false, del: false, search: false, view: false}
      #    grid.navButtonAdd "#transPager", {caption: "Clear", title: "Clear Search", buttonicon: 'ui-icon-refresh', position: 'first', onClickButton: ()->grid[0].clearToolbar()}
      grid.setFrozenColumns()
  )
  afterCreate = transGrid.getGridParam('afterCreate')
  afterCreate(transGrid) if afterCreate

  #  button for make all label as...
  $('#makeLabelTranslateStatus').attr('privilegeName', util.urlname2Action 'trans/update-status')
  .button(
    icons:
      primary: "ui-icon-triangle-1-n"
      secondary: "ui-icon-gear"
  )
  .click (e)->
    menu = $('#translationStatus').show().width($(@).width() - 3).position(my: "left bottom", at: "left top", of: @)
    $(document).one "click", ()->menu.hide()
    false

  $('#translationStatus').menu().hide().find("li").on 'click', (e)->
    transGrid = $("#transGrid")
    selectedRowIds = transGrid.getGridParam('selarrrow').join(',')
    ($.msgBox (c18n.selrow.format c18n.dict), null, title: c18n.warning; return) unless selectedRowIds

    $.blockUI()
    $.post 'trans/update-status', {type: getTableType(), transStatus: e.target.name, id: selectedRowIds}, (json)->
      ($.msgBox json.message, null, title: c18n.warning; return) unless json.status == 0
      $.unblockUI()
      $.msgBox i18n.msgbox.transstatus.msg, null, title: c18n.message
      transGrid.trigger 'reloadGrid'


  updateGrid: (param) ->
    transGrid = $("#transGrid")
    summary = ($(param.languages).map ->_this = @;($([0, 1, 2]).map ->"s(#{_this.id})[#{@}]").get().join(',')).get().join(',')
    gridParam = transGrid.getGridParam()

    #    console?.log 'Start loading data, setup the counter here...'
    #    window.param.refreshCounter = setInterval("console.log('tick');", 1000)

    isApp = (param.level == "app")
    if isApp
      url = urls.apps
      prop = "id,id,base.name,version,labelNum,#{summary}"
      transGrid.setColProp 'application', {search: false, index: 'base.name'}
      transGrid.setGridParam('sortname':'base.name')
      postData = {format: 'grid', prop: prop}
      postData[param.type] =  param.release.id

      gridParam.colNames = grid.application.colNames
      gridParam.colModel = grid.application.colModel

      transGrid.updateTaskLanguage param.languages
      transGrid.reloadAll url, postData
    else
      url = urls.dicts
      prop = "id,app.base.name,app.version,base.name,version,base.encoding,base.format,labelNum,#{summary}"

      gridParam.colNames = grid.dictionary.colNames
      gridParam.colModel = grid.dictionary.colModel

      # fill the search value dynamically for application
      searchoptions = transGrid.getColProp('application').searchoptions

      transGrid.setColProp('application', searchoptions: searchoptions, index: 'app.base.name')
      if 'prod' == param.type
        app = ":All"
        $.ajax {url: urls.apps, data: {prod:param.release.id, prop: "id,name"}, async: false, dataType: 'json', success: (json)->
          $(json).each ->app += ";#{@name}:#{@name}"
        }
        transGrid.setGridParam('sortname':'app.base.name')
      else
        # application
        app = "#{param.name}:#{param.name}"
        transGrid.setGridParam('sortname':'base.name')
      searchoptions.value = app

      postData = {format: 'grid', prop: prop}
      postData[param.type] = param.release.id

      transGrid.updateTaskLanguage param.languages
      # Use this for toolbar search restore.
      gridParam.datatype = 'local'
      transGrid = transGrid.reloadAll url, postData

      #  restore search option value by if exists
      searchvalue = $("#transGrid").jqGrid 'getGridParam', 'searchvalue'
      restoreSearchToolBarValue 'application', searchvalue.app if searchvalue.app
      restoreSearchToolBarValue 'encoding', searchvalue.encoding if searchvalue.encoding
      restoreSearchToolBarValue 'format', searchvalue.format if searchvalue.format

      transGrid.setGridParam('datatype': 'json')

      if searchvalue.app || searchvalue.encoding || searchvalue.format
        $("#transGrid")[0].triggerToolbar()
      else
        transGrid.trigger 'reloadGrid'

  getTotalSelectedRowInfo: ->
    transGrid = $("#transGrid")
    selectedRowIds = transGrid.getGridParam 'selarrrow'
    count = 0
    $(selectedRowIds).each ->
      row = transGrid.getRowData @
      count += parseInt row.numOfString

    {rowIds: selectedRowIds, totalLabels: count}
  getTableType: getTableType

