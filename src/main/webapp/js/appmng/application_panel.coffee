define [
  'jqueryui'
  'jqupload'
  'iframetransport'

  'i18n!nls/appmng'
  'i18n!nls/common'
  'dms-util'
  'dms-urls'

  'modernizr'

  'appmng/dialogs'
  'appmng/dictionary_grid'
], ($, upload, iframetrans, i18n, c18n
    util, urls,
    Modernizr
    dialogs, grid
)->
  appInfo = {}

  searchActionBtn = $('#appSearchAction', '#appmng').attr('title', 'Search').button(text: false, icons:{primary: "ui-icon-search"})
  .height(20).width(20).position(my: 'left center', at: 'right center', of: '#appSearchText').click(()=>
    selVer=$("#selAppVersion", '#DMS_applicationPanel')

    if !selVer.val() || -1 == selVer.val()
      node=util.getProductTreeInfo()
      $.msgBox c18n.noversion 'Application', node.text

      return
    dialogs.showSearchResult(
      text: $('#appSearchText', '#appmng').val()
      version:
        id: selVer.val()
        text:  $("option:selected", selVer).text()
      fuzzy: Boolean($('#appSearchText_fuzzy').attr('checked'))
    )
  )

  $('#appSearchText', '#appmng').keydown (e)->
    return true if e.which != 13
    searchActionBtn.trigger 'click'
    false


  $("#newAppVersion").button({text: false, label: '&nbsp;', icons:
    {primary: "ui-icon-plus"}}).
  attr('privilegeName', util.urlname2Action urls.app.create_version).
  click (e) =>$("#newApplicationVersionDialog").dialog("open")

  $("#removeAppVersion").button({text: false, label: '&nbsp;', icons:
    {primary: "ui-icon-minus"}}).
  attr('privilegeName', util.urlname2Action urls.app.remove_version).
  click (e) =>
    id = $("#selAppVersion").val()
    return if !id
    $.post urls.app.remove_version, {id: id, permanent: 'true'}, (json)->
      if json.status != 0
        $.msgBox json.message, null, {title: c18n.error}
        return
      $("#selAppVersion option:selected").remove()
      $("#selAppVersion").trigger 'change'

  uploadBrowserId = "#uploadBrowser"
  $("#selAppVersion").change (e)->
    appInfo.app = {version: $("option:selected", @).text(), id: if @value then @value else -1}
    uploadBtn=$(uploadBrowserId, '#appmng')

    unless uploadBtn.attr('privilegeName') in window.param.forbiddenPrivileges
      action = if $('option', @).length > 0 then 'enable' else 'disable'
      uploadBtn.button(action)

    grid.appChanged appInfo

  dctFileUpload = 'dctFileUpload'
  #  create upload filebutton
#  console.log c18n.supportedarchives
  $(uploadBrowserId).button(label: i18n.browse).attr('privilegeName', util.urlname2Action urls.app.deliver_dict).css(overflow: 'hidden').append $(
    "<input type='file' id='#{dctFileUpload}' name='upload' title='#{i18n.choosefile}' accept='#{c18n.supportedarchives}' multiple/>").css(
    position: 'absolute', top: -3, right: -3, border: '1px solid', borderWidth: '10px 180px 40px 20px',
    opacity: 0, filter: 'alpha(opacity=0)',
    cursor: 'pointer'
  )

  hasFileAPICapability= Modernizr.filereader && Modernizr.filesystem

  $("##{dctFileUpload}").fileupload {
  type: 'POST', dataType: 'json'
  url: urls.app.deliver_app_dict

  #  forceIframeTransport:true
  add: (e, data)->
    $.each data.files, (index, file) ->
    #      $('#uploadStatus').html "#{i18n.uploadingfile}#{file.name}"
    appId = $("#selAppVersion").val()
    return if !appId
    $(@).fileupload 'option', 'formData', [
      {name: 'appId', value: $("#selAppVersion").val()}
    ]
    data.submit()
    @pb=util.genProgressBar() if hasFileAPICapability
    $(uploadBrowserId).button 'disable'
  progressall: (e, data) ->
    return if !hasFileAPICapability
    progress = data.loaded / data.total * 100
    @pb.progressbar "value", progress
  done: (e, data)->
    $(uploadBrowserId).button 'enable'
    @pb.parent().remove() if hasFileAPICapability
    #    request handler
    jsonFromServer = data.result

    if(0 != jsonFromServer.status)
      $.msgBox jsonFromServer.message, null, {title: c18n.error, height: 600, width: 800}
      return
    delete jsonFromServer.message
    delete jsonFromServer.status

    pb = util.genProgressBar()
    util.updateProgress(urls.app.process_dict, jsonFromServer, (json)->
      pb.parent().remove()
      filename = json.event.msg
      $('#dictListPreviewDialog').data 'param', {handler: filename, appId: $("#selAppVersion").val()}
      $('#dictListPreviewDialog').dialog 'open'
    , pb)
  }

  getApplicationSelectOptions: ()->$('#selAppVersion').children('option').clone(true)
  refresh: (info)->
    $('#appDispProductName').html info.parent.text
    $('#appDispAppName').html info.text

    appInfo.base = {text: info.text, id: info.id}

    $.getJSON "rest/applications/apps/#{info.id}", {}, (json)->
      selAppVer=$('#selAppVersion', "div[id='appmng']")

      selAppVer.empty().append(util.json2Options json)

      if(window.param.currentSelected.appId and -1 != parseInt(param.currentSelected.appId))
        selAppVer.val(param.currentSelected.appId)
        window.param.currentSelected.appId=null
      selAppVer.trigger "change"
