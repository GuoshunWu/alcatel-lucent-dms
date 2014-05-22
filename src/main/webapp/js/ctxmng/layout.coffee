define ['jqlayout'], ($)->
  init = ()->
    $('#layout-container', "#ctxmng").layout(
      onresize: (name, element, state, options, layoutname)->
       $('#contextGrid').setGridWidth(element.width() - 25).setGridHeight(element.height() - 150) if 'center' == name
       $('#compareContextGrid').setGridWidth(element.width() - 25).setGridHeight(element.height() - 50) if 'south' == name

      defaults:
        size: 'auto'
        buttonClass: "button"  # default = 'ui-layout-button'
        togglerClass: "toggler"  # default = 'ui-layout-toggler'
        togglerLength_open: 35
        togglerLength_closed: 35
        hideTogglerOnSlide: true
#      south:
    )
  layout = init()
  ready = (param)->

  ready(@)

  layout: layout