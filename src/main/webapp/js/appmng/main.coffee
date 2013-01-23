define (require)->
  $ = require 'jqgrid'
  require 'jqlayout'

  util = require 'dms-util'
  tree = require 'appmng/producttree'

  ################################################## Initilaize #####################################################


  layout = $('#appmng-container').layout(
    defaults:
      size: 'auto'
      west:
        size: 250
        spacing_closed: 21      # wider space when closed
        togglerLength_closed: 21      # make toggler 'square' - 21x21
        togglerAlign_closed: "top"    # align to top of resizer
        togglerLength_open: 0      # NONE - using custom togglers INSIDE west-pane
        togglerTip_open: "Close West Pane"
        togglerTip_closed: "Open West Pane"
        resizerTip_open: "Resize West Pane"
        slideTrigger_open: "click"   # default
        initClosed: false
        resizable: true
  )

  # save selector strings to vars so we don't have to repeat it
  # must prefix paneClass with "#optional-container >" to target ONLY the Layout panes
  # west pane
  westSelector = "#appmng-container > .ui-layout-west"

  # CREATE SPANs for pin-buttons - using a generic class as identifiers
  $("<span></span>").addClass("pin-button").prependTo(westSelector)
  # BIND events to pin-buttons to make them functional
  layout.addPinBtn("#{westSelector} .pin-button", "west")

  # CREATE SPANs for close-buttons - using unique IDs as identifiers
  $("<span></span>").attr("id", "west-closer").prependTo(westSelector)
  # BIND layout events to close-buttons to make them functional
  layout.addCloseBtn("#west-closer", "west")
  ################################################## Initilaized ####################################################

  ready = (param)->
    console?.debug "appmng panel ready..."
  ready()
