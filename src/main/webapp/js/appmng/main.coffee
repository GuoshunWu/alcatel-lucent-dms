define (require)->
  $ = require 'jqgrid'
  require 'jqlayout'

  util = require 'dms-util'
  tree = require 'appmng/producttree'

  init = ()->
  ################################################## Initilaize #####################################################
    layout = $('#appmng-container').layout(
      name: 'appmnglayout'           #NO FUNCTIONAL USE, but could be used by custom code to 'identify' a layout
      defaults:
        buttonClass: "button"
        paneClass: "pane"               # default = 'ui-layout-pane'
        resizerClass: "resizer"        # default = 'ui-layout-resizer'
        togglerClass: "toggler"        # default = 'ui-layout-toggler'
        buttonClass: "button"          # default = 'ui-layout-button'
        contentSelector: ".content"    # inner div to auto-size so only it scrolls not the entire pane!
        contentIgnoreSelector: "span"   # 'paneSelector' for content to 'ignore' when measuring room for content

        size: 'auto'
        fxSettings_open: { easing: "easeInQuint" }
        fxSettings_close: { easing: "easeOutQuint" }
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
          fxSettings_open: { easing: "easeOutBounce" }
    )

    # save selector strings to vars so we don't have to repeat it
    # must prefix paneClass with "#optional-container >" to target ONLY the Layout panes
    # west pane
    westSelector = "#appmng-container > div.ui-layout-west"

    # CREATE SPANs for pin-buttons - using a generic class as identifiers
    $("<span></span>").addClass("pin-button").prependTo(westSelector)
    # BIND events to pin-buttons to make them functional
    layout.addPinBtn("#{westSelector} .pin-button", "west")
    #
    # CREATE SPANs for close-buttons - using unique IDs as identifiers
    $("<span></span>").attr("id", "west-closer").prependTo(westSelector)
    # BIND layout events to close-buttons to make them functional
    layout.addCloseBtn("#west-closer", "west")
  ################################################## Initilaized ####################################################

  ready = (param)->
    console?.debug "appmng panel ready..."

  init()
  ready()
