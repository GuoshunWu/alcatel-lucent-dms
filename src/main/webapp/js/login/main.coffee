define (require)->
  $ = require 'jqueryui'
  require 'jqvalidate'
  require 'jqform'
  require 'blockui'

  i18n = require 'i18n!nls/login'

  $('#loginForm').validate(
    rules:
      loginname: "required"
      password: 'required'
    messages:
      loginname: i18n.namerequired
      password: i18n.pwdrequired
    errorPlacement: (error, element)->
      error.appendTo(element.parent("td").next("td"))
    debug: false
  )


  $('#loginForm').ajaxForm(
    dataType: 'json'
    beforeSubmit: (formData, jqForm, options)->
      $('#loginForm').block(message: '<h1><img src="images/busy.gif" />Login...Please wait</h1>')
    success: (json, statusText, xhr, $form)->
      $('#loginForm').unblock()
      if 0 != json.status
        $('#loginStatus').text json.message
        return
      window.location = 'appmng.jsp'
  )

