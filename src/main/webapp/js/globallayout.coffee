define (require)->
  require 'jqlayout'

  ready = (param)->
    console?.debug "global layout ready..."

  layout = $('#global-container').layout(
    defaults:
      size: 'auto'
      north:
        minSize: 37
        togglerLength_closed: -1
        resizable: false
  )

  ready()

  layout: layout