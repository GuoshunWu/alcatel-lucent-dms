define [
  'i18n!nls/common'
  'i18n!nls/transmng'
  'dms-util'
  'dms-urls'

  'jqupload'
  'iframetransport'

  'transmng/trans_grid'
  'transmng/dialogs'
  'ptree'
], (c18n, i18n, util, urls, upload, iframetrans, grid, dialogs, ptree)->
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
#    console?.log "transmng panel init..."


    $('#selVersion', "div[id='transmng']").change ->
      nodeInfo = util.getProductTreeInfo()
#      console?.log nodeInfo
      postData = {prop: 'id,name'}
      postData[nodeInfo.type] = if @value then @value else -1

      $.ajax {url: urls.languages, async: false, data: postData, dataType: 'json', success: (languages)->
        langTable = util.generateLanguageTable languages
        $("#languageFilterDialog").empty().append langTable

        # for search text
#        languages.unshift(id: 1, name: 'Reference')
        $('#transSearchTextLanguage', "#transmng").empty().append util.json2Options(languages, false, "name")

      }
      dialogs.refreshGrid(false, grid)

    searchActionBtn = $('#transSearchAction','#transmng').attr('title', 'Search').button(text: false, icons:
      {primary: "ui-icon-search"}).click(()=>
      selVer = $('#selVersion', '#transmng')
      selLang = $('#transSearchTextLanguage', '#transmng')

      nodeInfo = util.getProductTreeInfo()

      if not nodeInfo or -1 == nodeInfo.parent
        $.msgBox 'Please select product or application.'
        return

      typeText = if 'prod' == nodeInfo.type then 'Product' else 'Application'
      unless selVer.val()

        $.msgBox "#{typeText.capitalize()} \"#{nodeInfo.text}\" has no version."
        return



      dialogs.showSearchResult(
          text: $('#transSearchText', '#transmng').val()
          version:
            id: selVer.val()
            text: $("option:selected", selVer).text()
          language:
            id: if selLang.val() then selLang.val() else "0"
            text: $("option:selected", selLang).text()
        )
    ).height(20).width(20).position(my: 'left center', at: 'right center', of: '#transSearchTextLanguage')

    transHistoriesBtn = $("#transHistories", "#transmng").button(text:false, icons:{
      primary: "ui-icon-bookmark"}).click(()=>
      selVer = $('#selVersion', '#transmng')
      nodeInfo = util.getProductTreeInfo()

      if not nodeInfo or -1 == nodeInfo.parent or nodeInfo.type != 'app'
        $.msgBox 'Please select application.'
        return

      unless selVer.val()
        $.msgBox "Application \"#{nodeInfo.text}\" has no version."
        return

      # show translation histories dialog

      $('#transHistoriesDialog').data(
        "params", id: selVer.val(),
        "caption": "Translation histories in Application #{nodeInfo.text} #{$("option:selected", selVer).text()}"
      ).dialog 'open'
    ).height(20).width(20)

    $('#transSearchText', '#transmng').keydown (e)=>
      return true if e.which != 13
      searchActionBtn.trigger 'click'
      false

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

    importTranslationId = "#importTranslation"
    transFileUpload = 'transFileUpload'

    #  create upload filebutton
    $(importTranslationId).button(label: i18n.transupload).attr('privilegeName', urls.trans.import_translation_details).css(overflow: 'hidden')
    .append $(
      "<input type='file' id='#{transFileUpload}' name='upload' accept='application/zip' multiple/>").css(
      position: 'absolute', top: -3, right: -3, border: '1px solid', borderWidth: '10px 180px 40px 20px',
      opacity: 0, filter: 'alpha(opacity=0)',
      cursor: 'pointer'
    )

    $("##{transFileUpload}").fileupload {
      type: 'POST', dataType: 'json'
      url: urls.trans.import_translation_details

    #  forceIframeTransport:true
      add: (e, data)->
        $.each data.files, (index, file) ->
        data.submit()
        @pb=util.genProgressBar() if !$.browser.msie || parseInt($.browser.version.split('\.')[0]) >= 10
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

#        pb = util.genProgressBar()
#        util.updateProgress(urls.app.process_dict, jsonFromServer, (json)->
#          pb.parent().remove()
#          filename = json.event.msg
#          console?.log filename
#        , pb)
    }

    #    add action for export

    $("#exportExcel", '#transmng').click ()->exportAppOrDicts 'excel'
    $("#exportPDF", '#transmng').click ()->exportAppOrDicts 'pdf'



  ready = ()->
    onShow()
#    console?.log "transmng panel ready..."

  init()
  ready()

  onShow: onShow
  nodeSelect: nodeSelectHandler


