define [
  'i18n!nls/common'
  'dms-util'
  'dms-urls'

  'transmng/trans_grid'
  'transmng/dialogs'
  'ptree'
], (c18n, util, urls, grid, dialogs, ptree)->
  nodeSelectHandler = (node, nodeInfo)->
    type=node.attr('type')
    return if 'products' == type

    type = 'prod' if type == 'product'

    $('#typeLabel',"div[id='transmng']").text "#{c18n[type].capitalize()}: "
    $('#versionTypeLabel',"div[id='transmng']").text "#{nodeInfo.text}"

    if 'prod' == type
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
    info = util.getProductTreeInfo()

    id = $('#selVersion',"div[id='transmng']").val()
    return if !id
    id = parseInt(id)
    return if -1 == id

    checkboxes = $("#languageFilterDialog input:checkbox[name='languages']:checked")
    languages = checkboxes.map(()-> return @id ).get().join(',')

    type = $("input:radio[name='viewOption'][checked]").val()

    level = info.type
    level = 'prod' if 'product' == level

    $("input[name='prod'], input[name='app']", '#exportForm').prop('name', level).val id
    $("#exportForm input[name='language']").val languages
    $("#exportForm input[name='type']").val type
    $("#exportForm input[name='ftype']").val ftype if ftype
    $("#exportForm", "#transmng").submit()

  init = ()->
    console?.debug "transmng panel init..."
    $('#selVersion', "div[id='transmng']").change ->
      return if !@value or -1 == parseInt @value
      nodeInfo = util.getProductTreeInfo()
#      console?.log nodeInfo
      postData = {prop: 'id,name'}
      postData[nodeInfo.type] = @value

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


