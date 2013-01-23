define (require)->
  require 'jqlayout'

  ready = (param)->
    console?.debug "global layout ready..."

  layout = $('#global-container').layout(
    defaults: {size: 'auto'}
    north:
      minSize: 34
      togglerLength_closed: -1
      resizable: false
  )

  ready()

  layout: layout