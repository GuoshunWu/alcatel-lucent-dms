dependencies = [
  'jqueryui'
  'jqupload'
  'iframetransport'

  'i18n!nls/appmng'
  'i18n!nls/common'
  'dms-util'
  'dms-urls'

  'appmng/dialogs'
  'appmng/dictionary_grid'
]

define dependencies, ($, upload, iframetrans, i18n, c18n, util, urls, dialogs, grid)->

  console?.log "module appmng/application_panel loading."

  appInfo = {}

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

  $("#selAppVersion").change (e)->
    appInfo.app = {version: $("option:selected", @).text(), id: if @value then @value else -1}
    grid.appChanged appInfo

  dctFileUpload = 'dctFileUpload'
  #  create upload filebutton
  $('#uploadBrower').button(label: i18n.browse).attr('privilegeName', util.urlname2Action('app/deliver-app-dict')).css({overflow: 'hidden'}).append $(
    "<input type='file' id='#{dctFileUpload}' name='upload' title='#{i18n.choosefile}' accept='application/zip' multiple/>").css(
    position: 'absolute', top: -3, right: -3, border: '1px solid', borderWidth: '10px 180px 40px 20px',
    opacity: 0, filter: 'alpha(opacity=0)',
    cursor: 'pointer'
  )


  $("##{dctFileUpload}").fileupload {
  type: 'POST', dataType: 'json'
  url: "app/deliver-app-dict"

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
    @pb=util.genProgressBar() if !$.browser.msie || parseInt($.browser.version.split('\.')[0]) >= 10
    $('#uploadBrower').button 'disable'
  progressall: (e, data) ->
    return if $.browser.msie && parseInt($.browser.version.split('\.')[0]) < 10
    progress = data.loaded / data.total * 100
    @pb.progressbar "value", progress
  done: (e, data)->
    $('#uploadBrower').button 'enable'

    $.each data.files, (index, file) ->$('#uploadStatus').html "#{file.name} #{i18n.uploadfinished}"
    @pb.parent().remove() if !$.browser.msie || parseInt($.browser.version.split('\.')[0]) >= 10
    #    request handler
    jsonFromServer = data.result

    if(0 != jsonFromServer.status)
      $.msgBox jsonFromServer.message, null, {title: c18n.error, height: 600, width: 800}
      return

    $('#dictListPreviewDialog').data 'param', {handler: jsonFromServer.filename, appId: $("#selAppVersion").val()}
    $('#dictListPreviewDialog').dialog 'open'
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
