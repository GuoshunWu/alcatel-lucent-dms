define [
  'jqueryui'
  'jqupload'
], ($, fileupload)->
  d = $('#hiDialog').dialog(
    autoOpen: false
    buttons: [
      {text: 'OK', click: (e)->
        console?.log fileupload
      }
    ]
  )

  $("#showDialog").button().on('click', (e)->
    d.dialog("open")
  )


