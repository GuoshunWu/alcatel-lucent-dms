define (require)->
  $ = require 'formvalidate'
  i18n = require 'i18n!nls/login'

  $.formValidator.initConfig(formID: "loginForm", autoTip: true, (onError: (msg)->
  #    alert(msg)
  ), inIframe: false)
  $("#idLoginname").formValidator(onShow: "", onFocus: "", onCorrect: "")
  .inputValidator(min: 1, max: 30, onError: i18n.nameerrtip)
  $("#idPassword").formValidator(onShow: "", onFocus: "", onCorrect: "")
  .inputValidator(min: 1, max: 30, onError: i18n.pwderrtip)

  $('#loginForm').bind 'submit', ()->$("#idSubmit").attr('disabled', true).css('color', 'grey') if $.formValidator.pageIsValid()

#  test jqueryui dialog
#  require 'jqueryui'
#
#  $('#testForDialog').dialog {
#  autoOpen: true
#  title: 'A test'
#  width: 'auto'
#  buttons: [
#    {text: 'OK', click: ()-> alert 'OK'}
#    {text: 'Cancel', click: ()-> alert 'Cancel'}
#  ]
#  }
#  require 'jqform'
#  require 'blockui'
#
#  $('#loginForm').ajaxForm(
#    dataType: 'json'
#    beforeSubmit: (formData, jqForm, options)->
#      $('#loginForm').block(message: '<h1><img src="images/busy.gif" />Login...Please wait</h1>')
#    success: (json, statusText, xhr, $form)->
#      $('#loginForm').unblock()
#      if 0 != json.status
#        $('#loginStatus').text json.message
#        return
#      window.location = "http://#{window.location.hostname}:80/appmng.jsp"
#  )

