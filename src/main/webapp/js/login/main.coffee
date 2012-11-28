define (require)->
  $ = require 'formvalidate'
  i18n = require 'i18n!nls/login'

  $.formValidator.initConfig(formID: "loginForm", autoTip: true, (onError: (msg)->
  #    alert(msg)
  ), inIframe: false)
  $("#idLoginname").formValidator(onShow: i18n.nameshowtip, onFocus: i18n.namefocustip, onCorrect: "")
  .inputValidator(min: 1, max: 30, onError: i18n.nameerrtip)
  $("#idPassword").formValidator(onShow: i18n.pwdshowtip, onFocus: i18n.pwdfocustip, onCorrect: "")
  .inputValidator(min: 1, max: 30, onError: i18n.pwderrtip)

  $('#loginForm').bind 'submit', ()->$("#idSubmit").attr('disabled', true).css('color', 'grey') if $.formValidator.pageIsValid()

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

