define (require)->
  $ = require 'jqgrid'
  require 'jqlayout'

  util = require 'dms-util'

  ########## reference legency codes here ###########
  require 'blockui'
  dialogs = require 'appmng/dialogs'
  layout = require 'appmng/layout'
  #    initialize appmng panels
  #  appmngPnlGroup = new util.PanelGroup("div.dms_appmng_panel", "DMS_welcomePanel")

  tree = require 'appmng/producttree'

  init = ()->
  ################################################## Initilaize #####################################################
    layout = $('#appmng-container').layout(
      name: 'appmnglayout'           #NO FUNCTIONAL USE, but could be used by custom code to 'identify' a layout
      defaults:
        size: 'auto'
        minSize: 50
        #        paneClass: "pane"               # default = 'ui-layout-pane'
        resizerClass: "resizer"        # default = 'ui-layout-resizer'
        togglerClass: "toggler"        # default = 'ui-layout-toggler'
        buttonClass: "button"          # default = 'ui-layout-button'
        contentSelector: ".content"    # inner div to auto-size so only it scrolls not the entire pane!
        contentIgnoreSelector: "span"   # 'paneSelector' for content to 'ignore' when measuring room for content
        togglerLength_open: 35
        togglerLength_closed: 35
        hideTogglerOnSlide: true
        togglerTip_open: "Close This Pane"
        togglerTip_closed: "Open This Pane"
        resizerTip: "Resize This Pane"
        # effect defaults - overridden on some panes
        fxName: 'slide'
        fxSpeed_open: 750
        fxSpeed_close: 1500
        fxSettings_open: { easing: "easeInQuint" }
        fxSettings_close: { easing: "easeOutQuint" }
      west:
        size: 250
        spacing_closed: 21      # wider space when closed
        togglerLength_closed: 21      # make toggler 'square' - 21x21
        togglerAlign_closed: "top"    # align to top of resizer
        togglerLength_open: 0      # NONE - using custom togglers INSIDE west-pane

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
