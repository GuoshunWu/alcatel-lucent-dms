define ['jqlayout'], ($)->
  autoSizeGrids = ['applicationGridList', 'dictionaryGridList', 'transGrid', 'taskGrid', 'globalSearchResultGrid']

  ready = (param)->
#    console?.log "global layout ready..."
  init = ()->
    layout = $('#global-container').layout(
      onresize: (name, element, state, options, layoutname)->
#        auto size trans grid when resize
        $('table.ui-jqgrid-btable').each (index, grid)->
          if 'center' == name and grid.id in autoSizeGrids
            $(grid).setGridWidth(element.width() - 50, 'globalSearchResultGrid' == grid.id )

      defaults:
        size: 'auto'
        minSize: 50
        buttonClass: "button"  # default = 'ui-layout-button'
        togglerClass: "toggler"  # default = 'ui-layout-toggler'
        togglerLength_open: 35
        togglerLength_closed: 35
        hideTogglerOnSlide: true
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
        resizable: true

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

    westPanel = $("div.ui-layout-west", "#global-container")

    # BIND events to pin-buttons to make them functional
    westPanel.prepend $("<span id='dms-west-pin-button'></span>")
    layout.addPinBtn("#dms-west-pin-button", "west")
#
#    # BIND layout events to close-buttons to make them functional
    westPanel.prepend $("<span id='dms-west-closer'></span>")
    layout.addCloseBtn("#dms-west-closer", "west")

    layout

  glayout = init()
  ready(@)
  layout: glayout