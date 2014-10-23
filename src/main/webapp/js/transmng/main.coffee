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
], (c18n, i18n, util, urls, upload, iframetrans, transGridModule, dialogs, ptree)->
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
    $('#transGrid').setGridWidth(gridParent.width() - 10).setGridHeight(gridParent.height() - 150)

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
        $('#transSearchTextLanguage', "#transmng").empty()
        .append util.json2Options(languages, false, "name")
#        .multiselect('refresh')
      }

      transGridModule.refresh(false)

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

      # for multiple language select
#      languages = (checked.value for checked in selLang.multiselect("getChecked").get())
#      languages = if languages.length then languages.join(',') else 0
      languages = if selLang.val() then selLang.val() else "0"

      dialogs.showSearchResult(
          text: $('#transSearchText', '#transmng').val()
          version:
            id: selVer.val()
            text: $("option:selected", selVer).text()
          language:
            id: languages
            text: $("option:selected", selLang).text()
        )
    ).height(20).width(20)

    getAppVersion = (nodeInfo = util.getProductTreeInfo(), selVer = $('#selVersion', '#transmng'))->
      selVer = $('#selVersion', '#transmng')
      if not nodeInfo or -1 == nodeInfo.parent or nodeInfo.type != 'app'
        $.msgBox 'Please select application.'
        return

      unless selVer.val()
        $.msgBox "Application \"#{nodeInfo.text}\" has no version."
        return

      selVer.val()

    transHistoriesBtn = $("#transHistories", "#transmng").button(text:false, icons:{
      primary: "ui-icon-bookmark"}).click(()=>
      nodeInfo = util.getProductTreeInfo()
      selVer = $('#selVersion', '#transmng')
      appVersion = getAppVersion(nodeInfo, selVer)
      # show translation histories dialog

      $('#transHistoriesDialog').data(
        "params", {
          id: appVersion,
          "caption": "Translation changelog in Application #{nodeInfo.text} #{$("option:selected", selVer).text()}"
        }
      ).dialog 'open'
    ).height(20).width(20)

    $('#transSearchText', '#transmng').keydown (e)=>
      return true if e.which != 13
      searchActionBtn.trigger 'click'
      false

    #    add action for export

    $("#exportExcel", '#transmng').click ()->exportAppOrDicts 'excel'
    $("#exportPDF", '#transmng').click ()->exportAppOrDicts 'pdf'

    $('#checkTranslations', '#transmng').button().click ()->
      # get product
      nodeInfo = util.getProductTreeInfo()
      selVer = $('#selVersion', '#transmng')
      appVersion = getAppVersion(nodeInfo, selVer)
      selectedRowIds = $("#transGrid").getGridParam('selarrrow').join(',')
      ($.msgBox (c18n.selrow.format c18n.dict), null, title: c18n.warning; return) unless selectedRowIds
      
      $('#transmngTranslationCheckDialog').data(
        'param', {
          id: appVersion,
          dict: selectedRowIds,
          "caption": "Translation check result in Application #{nodeInfo.text} #{$("option:selected", selVer).text()}"}
      ).dialog('open')



  ready = ()->
    onShow()
#    console?.log "transmng panel ready..."

  init()
  ready()

  onShow: onShow
  nodeSelect: nodeSelectHandler


