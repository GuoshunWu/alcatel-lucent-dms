define ['jquery', 'appmng/dictionary_grid'], ($, grid)->
  console.log "application panel initlized"
  $("#selAppVersion").change ->
    console.log "dictionary update"

  refresh: (info)->
    console.log info
    $('#appDispProductName').html info.parent.text
    $('#appDispAppName').html info.text

    $.getJSON "rest/applications/apps/#{info.id}", {}, (json)->
      $("#selAppVersion").empty().append ($(json).map ()-> new Option this.version, this.id)
      $("#selAppVersion").trigger "change"
