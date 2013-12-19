define ['formvalidate', 'i18n!nls/login'],($, i18n)->

  $("span#version").html window.param?.version
  $("span#buildNumber").html window.param?.buildNumber

  timeZoneOffset = -new Date().getTimezoneOffset() * 60 * 1000   # in millisecond
  $("#idTimeZoneOffset").val timeZoneOffset

  $.formValidator.initConfig(formID: "loginForm", autoTip: true, (onError: (msg)->
  #    alert(msg)
  ), inIframe: false)
  $("#idLoginname").formValidator(onShow: "", onFocus: "", onCorrect: "")
  .inputValidator(min: 1, max: 30, onError: i18n.nameerrtip)
  $("#idPassword").formValidator(onShow: "", onFocus: "", onCorrect: "")
  .inputValidator(min: 1, max: 30, onError: i18n.pwderrtip)

  $('#loginForm').on 'submit', ()->
    if $.formValidator.pageIsValid()
#      console?.log 'client validation passed, make button grey to avoid resubmit'
      $("#idSubmit").attr('disabled', true).css('color', 'grey')

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

