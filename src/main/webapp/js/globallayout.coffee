define (require)->
  require 'jqlayout'

  autoSizeGrids = ['applicationGridList', 'dictionaryGridList', 'transGrid']

  ready = (param)->
    console?.debug "global layout ready..."
  init = ()->
    layout = $('#global-container').layout(
      onresize: (name, element, state, options, layoutname)->
        # auto size trans grid when resize
        $('table.ui-jqgrid-btable').each (index, grid)->
          $(grid).setGridWidth(element.width() - 50, false) if 'center' == name and grid.id in autoSizeGrids

      defaults:
        size: 'auto'
        minSize: 50
        buttonClass: "button"  # default = 'ui-layout-button'
        togglerClass: "toggler"  # default = 'ui-layout-toggler'
        togglerLength_open: 35
        togglerLength_closed: 35
        hideTogglerOnSlide: true
        resizable: true
      north:
        minSize: 35
        togglerLength_closed: -1
        resizable: false
      west:
        size: 250
        spacing_closed: 21      # wider space when closed
        togglerLength_closed: 21      # make toggler 'square' - 21x21
        togglerAlign_closed: "top"    # align to top of resizer
        togglerLength_open: 0      # NONE - using custom togglers INSIDE west-pane

        slideTrigger_open: "click"   # default
        togglerTip_open: "Close This Pane"
        togglerTip_closed: "Open This Pane"
        resizerTip: "Resize This Pane"

        #	add 'bounce' option to default 'slide' effect
        fxName: 'slide'
        fxSpeed_open: 750
        fxSpeed_close: 1500
        fxSettings_open:
          { easing: "easeOutBounce" }
    )

    # save selector strings to vars so we don't have to repeat it
    # must prefix paneClass with "#optional-container >" to target ONLY the Layout panes
    # west pane
    westSelector = "#global-container > div.ui-layout-west"

    # CREATE SPANs for pin-buttons - using a generic class as identifiers
    $("<span />").addClass("pin-button").prependTo(westSelector)
    # BIND events to pin-buttons to make them functional
    layout.addPinBtn("#{westSelector} .pin-button", "west")
    #
    # CREATE SPANs for close-buttons - using unique IDs as identifiers
    $("<span />").attr("id", "west-closer").prependTo(westSelector)
    # BIND layout events to close-buttons to make them functional
    layout.addCloseBtn("#west-closer", "west")

    layout
  glayout = init()

  ready(@)

  layout: glayout