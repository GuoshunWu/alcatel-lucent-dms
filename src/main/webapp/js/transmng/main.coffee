define ['i18n!nls/common', 'dms-util', 'dms-urls', 'transmng/trans_grid', 'transmng/dialogs', 'ptree'], (i18n, util, urls, grid, dialogs, ptree)->
  nodeSelectHandler = (node, nodeInfo)->
    type=node.attr('type')
    return if 'products' == type

    $('#versionTypeLabel',"div[id='transmng']").text "#{nodeInfo.text}"
    if 'product' == type
      $.getJSON urls.prod_versions, {base: nodeInfo.id, prop: 'id,version'}, (json)->
        $('#selVersion',"div[id='transmng']").empty().append(util.json2Options(json)).trigger 'change'
      return

    if 'app' == type
      $.getJSON "#{urls.app_versions}#{nodeInfo.id}" , (json)->
        $('#selVersion',"div[id='transmng']").empty().append(util.json2Options(json)).trigger 'change'


  onShow = ()->
    gridParent = $('.transGrid_parent')
    $('#transGrid').setGridWidth(gridParent.width() - 10).setGridHeight(gridParent.height() - 110)

    # init product or application

  exportAppOrDicts = (ftype)->
    id = $('#productRelease').val()
    return if !id
    id = parseInt(id)
    return if -1 == id

    checkboxes = $("#languageFilterDialog input:checkbox[name='languages']:checked")
    languages = checkboxes.map(()-> return @id ).get().join(',')

    type = $("input:radio[name='viewOption'][checked]").val()
    type = type[..3]
    type = type[..2] if type[0] == 'a'

    $("#exportForm input[name='prod']").val id
    $("#exportForm input[name='language']").val languages
    $("#exportForm input[name='type']").val type
    $("#exportForm input[name='type']").val ftype if ftype
    $("#exportForm", "#transmng").submit()

  init = ()->
    console?.debug "transmng panel init..."
    $('#selVersion', "div[id='transmng']").change ->
      return if !@value or -1 == parseInt @value
      nodeInfo = ptree.getNodeInfo()
#      console?.log nodeInfo
      type = nodeInfo.type
      type = type[..3] if type.startWith('prod')

      postData = {prop: 'id,name'}
      postData[type] = @value

      $.ajax {url: urls.languages, async: false, data: postData, dataType: 'json', success: (languages)->
        langTable = util.generateLanguageTable languages
        $("#languageFilterDialog").empty().append langTable
      }
      dialogs.refreshGrid(false, grid)

    # Create buttons
    $("#create",'#transmng').button()
      .attr('privilegeName', util.urlname2Action 'task/create-task')
      .click ->
        info = grid.getTotalSelectedRowInfo()
        if !info.rowIds.length
          $.msgBox (c18n.selrow.format c18n[grid.getTableType()]), null, title: c18n.warning
          return
        dialogs.taskDialog.dialog "open"

    $('#languageFilter','#transmng').button().click ()->dialogs.languageFilterDialog.dialog "open"
    #    for view level
    $(':radio[name=viewOption]').change ->dialogs.refreshGrid(false, grid)

    $("#exportTranslation",'#transmng').button()
      .attr('privilegeName', util.urlname2Action 'trans/export-translation-details')
      .click ->
        info = grid.getTotalSelectedRowInfo()
        if !info.rowIds.length
          $.msgBox (c18n.selrow.format c18n[grid.getTableType()]), null, title: c18n.warning
          return
        dialogs.exportTranslationDialog.dialog 'open'

    #    add action for export
    $("#exportExcel", '#transmng').click ()->exportAppOrDicts 'excel'
    $("#exportPDF", '#transmng').click ()->exportAppOrDicts 'pdf'



  ready = ()->
    onShow()
    console?.debug "transmng panel ready..."

  init()
  ready()

  onShow: onShow
  nodeSelect: nodeSelectHandler


