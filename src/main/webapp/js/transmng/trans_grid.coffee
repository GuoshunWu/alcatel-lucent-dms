define [
  'multiselect',
  'jqmsgbox'
  'blockui'
  'jqgrid'
  'i18n!nls/transmng'
  'i18n!nls/common'
  'dms-urls'
  'dms-util'

  'transmng/grid.colmodel'
], ($, msgbox, blockui, jqgrid, i18n, c18n, urls, util, gmodel)->
  gridId = 'transGrid'
  hGridId = '#' + gridId
  pagerId = gridId + 'Pager'
  hPagerId = '#' + pagerId

  getTotalSelectedRowInfo = (transGrid = $(hGridId)) ->
    selectedRowIds = transGrid.getGridParam 'selarrrow'
    count = 0
    $(selectedRowIds).each ->
      row = transGrid.getRowData @
      count += parseInt row.numOfString

    {rowIds: selectedRowIds, totalLabels: count}

  refreshGrid = (languageTrigger = false)->
    nodeInfo = util.getProductTreeInfo()
    type = nodeInfo.type
    param =
      release:
        {id: $('#selVersion', "div[id='transmng']").val()
        version: $("#selVersion option:selected",
        "div[id='transmng']").text()
        }
      level: $("input:radio:checked[name='viewOption']").val()
      type: type
      name: nodeInfo.text
    checkboxes = $("#languageFilterDialog input:checkbox[name='languages']")
    param.languages = checkboxes.map(
      ()-> return {id: @id, name: @value} if @checked).get()
    param.languageTrigger = languageTrigger
    param.release.id = -1 unless param.release.id

    updateGrid param

  # prepare the grid column name and column model parameters for the grid.
  restoreSearchToolBarValue = (column, value)->
#    console?.log "Set default value to #{value} for #{column}"
    barSelector = "select[id=gs_#{column}]"
    $(barSelector).each (idx, elem)->
      elem.value = value
    searchOpts = ($(hGridId).jqGrid 'getColProp', column).searchoptions
    searchOpts.defaultValue = value

    $(hGridId).jqGrid 'setColProp', column, searchoptions: searchOpts

  common =
    colNames: ['ID', 'Application', 'Version', 'Num of String']
    colModel: [
      {name: 'id', index: 'id', width: 55, align: 'center', hidden: true, frozen: true}
      {name: 'application', index: 'base.name', width: 100, editable: false, align: 'left', frozen: true, stype: 'select'
      searchoptions:
        value: ":All"
        dataEvents: [{
          type: 'change', fn: (e)->
            searchvalue = $("#transGrid").jqGrid 'getGridParam', 'searchvalue'
            searchvalue.app = e.target.value
            $("#transGrid").jqGrid('setGridParam', 'searchvalue': searchvalue)
        }]
      }
      {name: 'appVersion', index: 'version', width: 90, editable: false, align: 'left', frozen: true, search: false}
      {name: 'numOfString', index: 'labelNum', width: 80, align: 'right', frozen: true, search: false, firstsortorder: 'desc'}
    ]

  grid =
    dictionary:
      colNames: (common.colNames[0..].insert 3, ['Dictionary', 'Version', 'Encoding', 'Format'])
      colModel: (common.colModel[0..].insert 3, [
        {name: 'dictionary', index: 'base.name', width: 90, editable: false, align: 'left', frozen: true, search: false}
        {name: 'dictVersion', index: 'version', width: 90, editable: false, align: 'left', frozen: true, search: false}
        {name: 'encoding', index: 'base.encoding', width: 90, editable: false, align: 'left', frozen: true
        stype: 'select'
        searchoptions:
          value: ":All;#{c18n.dictencodings}"
          dataEvents: [{
          type: 'change', fn: (e)->
            searchvalue = $(hGridId).jqGrid 'getGridParam', 'searchvalue'
            searchvalue.encoding = e.target.value
            $(hGridId).jqGrid 'setGridParam', 'searchvalue': searchvalue
          }]
        }
        {name: 'format', index: 'base.format', width: 90, editable: false, align: 'left', frozen: true
        stype: 'select'
        searchoptions:
          value: ":All;#{c18n.dictformats}"
          dataEvents: [{
            type: 'change', fn: (e)->
              searchvalue = $("#transGrid").jqGrid 'getGridParam', 'searchvalue'
              searchvalue.format = e.target.value
              $(hGridId).jqGrid 'setGridParam', 'searchvalue': searchvalue
          }]
        }
      ])
    application:
      colNames: ['Dummy'].concat common.colNames
      colModel: [
        {name: 'dummy', index: 'dummy', width: 55, align: 'center', hidden: true, frozen: true}
      ].concat common.colModel

  getTableType = ->if -1 == ($.inArray 'Dummy', $(hGridId).getGridParam('colNames')) then  'dict' else 'app'

  ### Construct the grid with the column name(model) parameters above and other required parameters ###
  isSelectAllReload = false
  transFileUpload = 'transFileUpload'

  ####################################################Create translation grid######################################################
  transGrid = $(hGridId).after($("<div>").attr("id", pagerId)).jqGrid(
    url: urls.dicts
    mtype: 'post', postData: {}, datatype: 'local'
    width: 1000, height: 300
    viewrecords: true, gridview: true, multiselect: true
    rownumbers: true, shrinkToFit: false
    pager: pagerId,
    rowNum: 50, rowList: [50, 100, 500, 9999]
#    toolbar: [true, "top"]
    sortname: 'app.base.name', sortorder: 'asc'
    colNames: grid.dictionary.colNames, colModel: grid.dictionary.colModel
    #  customed option for save the toolbar search value and group headers

    searchvalue: {}, groupHeaders: []
    onSelectAll: (aRowids, status)->
      records = $(@).jqGrid 'getGridParam', 'records'
      recCount = $(@).jqGrid 'getGridParam', 'reccount'
      return unless status and recCount < records
      # prompt user whether want to select all the records
      $.msgBox c18n.confirmloadall, ((keyPressed)=>
        return unless c18n.yes == keyPressed

        isSelectAllReload = true
        transGridPagerList = $ "select.ui-pg-selbox", hPagerId
        (option for option in $("option", transGridPagerList) when option.value > records)[0].selected = true
        transGridPagerList.trigger "change"
      ), {title: c18n.confirm, width: 510}, [c18n.yes, c18n.no]
    gridComplete: ->
      transGrid = $(@)
      # only do the rest of the actions when there are data in grid.
      return unless transGrid.getDataIDs().length

      $('a', @).each (index, a)->$(a).before(' ').remove() if '0' == $(a).text()

      $('a', @).css('color', 'blue').click((e)->
        pageParams = util.getUrlParams(@href)
        rowid = pageParams?.id
        language =
          id: pageParams.languageId, name: pageParams.languageName

        rowData = transGrid.getRowData(rowid)
        allZero = true
        $(['T', 'N', 'I']).each (index, elem)->
          num = parseInt rowData["#{language.name}.#{elem}"]
          allZero = 0 == num
          allZero
        (console?.log 'zero';return) if allZero

        map =
          'N': '0', 'I': '1', 'T': '2'
        status = language.name.split('.')[1]
        param =
          dict: {id: rowid, name: rowData.dictionary}
          searchStatus: map[status]
          language: language
          transsrc: ''
        transDialog = $('#translationDetailDialog')
        transDialog.dialog "close" if(transDialog.dialog("isOpen"))
        transDialog.data(param: param).dialog "open"
        e.preventDefault()
      ) if $("input:radio:checked[name='viewOption']").val() == 'dict'
      if isSelectAllReload
        transGrid.setSelection(id, true) for id in transGrid.getDataIDs()
        isSelectAllReload = false

    #  costume method executed when the grid is created.
    afterCreate: (grid)->
      grid.setGridParam('datatype': 'json')
      .setGroupHeaders(useColSpanStyle: true, groupHeaders: grid.getGridParam('groupHeaders'))

      grid.filterToolbar {stringResult: true, searchOnEnter: false} if getTableType() == 'dict'
      grid.navGrid(hPagerId, {edit: false, add: false, del: false, search: false, view: false})

      # add create task, export/import translation and change label status buttons to toolbar
#      .navSeparatorAdd(hPagerId)
#      .navButtonAdd(hPagerId,
#        caption: "CT", id: "custom_create_task_#{gridId}", title: "Create translation task..."
#        position: 'last', onClickButton: (e)->
#          info = getTotalSelectedRowInfo()
#          if !info.rowIds.length
#            $.msgBox (c18n.selrow.format c18n[getTableType()]), null, title: c18n.warning
#            return
#          $("#createTranslationTaskDialog").dialog "open"
#      )
#      .navButtonAdd(hPagerId,
#        caption: "ET", id: "custom_export_trans_#{gridId}", title: "Export translations..."
#        position: 'last', onClickButton: (e)->
#          info = getTotalSelectedRowInfo()
#          if !info.rowIds.length
#            $.msgBox (c18n.selrow.format c18n[getTableType()]), null, title: c18n.warning
#            return
#          $('#ExportTranslationsDialog').dialog 'open'
#      ).navButtonAdd(hPagerId,
#        caption: "IT", id: "custom_import_trans_#{gridId}", title: "Import translations..."
#        position: 'last', onClickButton: (e)->
#          console.log "custom import translation button action..."
#      )
#      .navButtonAdd(hPagerId,
#        caption: "Make all selected labels as"
#        id: "custom_update_label_status_#{gridId}", title: "Set labels status", buttonicon: 'ui-icon-triangle-1-n'
#        position: 'last', onClickButton: (e)->
#          me = $(e.target).parents('td.ui-pg-button')
#          menu = $('#translationStatus').show().width($(me).width() - 3).position(my: "left bottom", at: "left top", of: me)
#          $(document).one "click", ()->menu.hide()
#          false
#      )


      # for translation import button on toolbar
#      importTransButton = $("div.ui-pg-div", '#' + "custom_import_trans_#{gridId}")
#      .css(overflow: "hidden")
#      .append $(
#        "<input type='file' id='#{transFileUpload}' name='upload' multiple/>").css(
#        position: 'relative', top: '-2em',border: '1px solid',
#        zIndex: 10000
#        borderWidth: '100px 180px 40px 20px',
#        opacity: 0, filter: 'alpha(opacity=0)',
#        cursor: 'pointer'
#      )

      #build toolbar
      innerContainer = $("<div id='tb_importTranslation'>This is test..</div>").button()
      .width(200)
      .css(overflow: "hidden")
      $("<input type='file' id='tb_#{transFileUpload}' name='upload' multiple/>")
      .css(
        position: 'relative'
        top: '-2em', border: '1px solid',
        zIndex: 10000
        borderWidth: '100px 180px 40px 20px',
        opacity: 0, filter: 'alpha(opacity=0)',
        cursor: 'pointer')
      .appendTo(innerContainer)
      toolbar = $("#t_#{gridId}").append(innerContainer)

      nodeInfo = util.getProductTreeInfo()
      if nodeInfo
        caption = if "prod" == nodeInfo.type then "Product" else "Application"
        version  = $('#selVersion > option:selected', "#transmng").text()
        caption += " #{nodeInfo.text} version #{version}"
        grid.setCaption(caption)

      grid.setFrozenColumns()
  )
  transGrid.getGridParam('afterCreate')?(transGrid)

  # ---------------------------------------Create buttons in translation grid ------------------------------------------
#  $('#transSearchTextLanguage').multiselect(
#    minWidth: 200
#    selectedText : "# of # languages selected"
#    selectedList: 2
#    noneSelectedText: "Reference language only"
#  )

  # Create buttons
  $("#create", '#transmng').button()
  .attr('privilegeName', util.urlname2Action 'task/create-task')
  .click ->
    info = getTotalSelectedRowInfo()
    if !info.rowIds.length
      $.msgBox (c18n.selrow.format c18n[getTableType()]), null, title: c18n.warning
      return
    $("#createTranslationTaskDialog").dialog "open"

  $('#languageFilter', '#transmng').button().click ()->
    $('#languageFilterDialog').dialog "open"
  #    for view level
  $(':radio[name=viewOption]').change ->
    refreshGrid(false, grid)

  $("#exportTranslation", '#transmng').button()
  .attr('privilegeName', util.urlname2Action 'trans/export-translation-details')
  .click ->
    info = getTotalSelectedRowInfo()
    if !info.rowIds.length
      $.msgBox (c18n.selrow.format c18n[getTableType()]), null, title: c18n.warning
      return
    $('#ExportTranslationsDialog').dialog 'open'

  importTranslationId = "#importTranslation"
  #  create upload filebutton
  $(importTranslationId).button(label: i18n.transupload).attr('privilegeName', util.urlname2Action urls.trans.import_translation_details)
  .css(overflow: 'hidden')
  .append $(
    "<input type='file' id='#{transFileUpload}' name='upload' multiple/>").css(
    position: 'absolute', top: -3, right: -3, border: '1px solid', borderWidth: '10px 180px 40px 20px',
    opacity: 0, filter: 'alpha(opacity=0)',
    cursor: 'pointer'
  )

  transUploadFileInput = $('#' + transFileUpload)
  .fileupload {
    type: 'POST', dataType: 'json'
    url: urls.trans.import_translation_details

  #  forceIframeTransport:true
    add: (e, data)->
      $.each data.files, (index, file) ->
      data.submit()
      @pb = util.genProgressBar() if !$.browser.msie || parseInt($.browser.version.split('\.')[0]) >= 10
      $(importTranslationId).button 'disable'
    progressall: (e, data) ->
      return if $.browser.msie && parseInt($.browser.version.split('\.')[0]) < 10
      progress = data.loaded / data.total * 100
      @pb.progressbar "value", progress
    done: (e, data)->
      $(importTranslationId).button 'enable'
      @pb.parent().remove() if !$.browser.msie || parseInt($.browser.version.split('\.')[0]) >= 10
      #    request handler
      jsonFromServer = data.result
      #        console?.log jsonFromServer
      if(0 != jsonFromServer.status)
        $.msgBox jsonFromServer.message, null, {title: c18n.error}
        return
      $("#transGrid").trigger 'reloadGrid'
      $.msgBox jsonFromServer.message, null, {title: c18n.info}
  }

  #  button for make all label as...
  $('#makeLabelTranslateStatus').attr('privilegeName', util.urlname2Action urls.trans.update_status)
  .button(
    icons:
      secondary: "ui-icon-triangle-1-n"
  )
  .click (e)->
    menu = $('#translationStatus').show().width($(@).width() - 3).position(my: "left bottom", at: "left top", of: @)
    $(document).one "click", ()->
      menu.hide()
    false

  $('#translationStatus').menu().hide().find("li").on 'click', (e)->
    transGrid = $("#transGrid")
    selectedRowIds = transGrid.getGridParam('selarrrow').join(',')
    ($.msgBox (c18n.selrow.format c18n.dict), null, title: c18n.warning; return) unless selectedRowIds

    postData =
      type: getTableType()
      transStatus: e.target.name
      id: selectedRowIds
      lang: $("#languageFilterDialog input:checkbox[name='languages']:checked").map(->
        @id).get().join(',')

    $.blockUI()
    $.post urls.trans.update_status, postData, (json)->
      ($.msgBox json.message, null, title: c18n.warning; return) unless json.status == 0
      $.unblockUI()
      $.msgBox i18n.msgbox.transstatus.msg, null, title: c18n.message
      transGrid.trigger 'reloadGrid'

  # --------------------------------------------------------------------------------------------------------------------

  updateGrid = (param)->
    summary = ($(param.languages).map ->
      _this = @
      ($([0, 1, 2]).map ->"s(#{_this.id})[#{@}]").get().join(',')).get().join(',')
    gridParam = transGrid.getGridParam()

    isApp = (param.level == "app")

    if isApp
      url = urls.apps
      prop = "id,id,base.name,version,labelNum,#{summary}"
      transGrid.setColProp 'application', {search: false, index: 'base.name'}
      transGrid.setGridParam('sortname': 'base.name')
      postData = {format: 'grid', prop: prop}
      postData[param.type] = param.release.id

      gridParam.colNames = grid.application.colNames
      gridParam.colModel = grid.application.colModel

      transGrid.updateTaskLanguage param.languages
      transGrid.reloadAll url, postData
      return

    # dictionary type is dict
    url = urls.dicts
    prop = "id,app.base.name,app.version,base.name,version,base.encoding,base.format,labelNum,#{summary}"

    gridParam.colNames = grid.dictionary.colNames
    gridParam.colModel = grid.dictionary.colModel

    # fill the search value dynamically for application
    searchoptions = transGrid.getColProp('application').searchoptions

    transGrid.setColProp('application', searchoptions: searchoptions, index: 'app.base.name')

    if 'prod' == param.type
      app = ":All"
      $.ajax {url: urls.apps, data: {prod: param.release.id, prop: "id,name"}, async: false, dataType: 'json', success: (json)->
        $(json).each ->app += ";#{@name}:#{@name}"
      }
      transGrid.setGridParam('sortname': 'app.base.name')
    else
      # application
      app = "#{param.name}:#{param.name}"
      transGrid.setGridParam('sortname': 'base.name')

    searchoptions.value = app

    postData = {format: 'grid', prop: prop}
    postData[param.type] = param.release.id

    transGrid.updateTaskLanguage param.languages
    # Use this for toolbar search restore.
    gridParam.datatype = 'local'
    transGrid = transGrid.reloadAll url, postData

    #  restore search option value by if exists
    searchvalue = transGrid.jqGrid 'getGridParam', 'searchvalue'
    restoreSearchToolBarValue 'application', searchvalue.app if searchvalue.app
    restoreSearchToolBarValue 'encoding', searchvalue.encoding if searchvalue.encoding
    restoreSearchToolBarValue 'format', searchvalue.format if searchvalue.format

    transGrid.setGridParam('datatype': 'json')

    if searchvalue.app || searchvalue.encoding || searchvalue.format
      transGrid[0].triggerToolbar()
    else
      transGrid.trigger 'reloadGrid'


  #################################################################################################################################
  updateGrid: updateGrid
  getTotalSelectedRowInfo: getTotalSelectedRowInfo
  getTableType: getTableType
  refresh: refreshGrid

