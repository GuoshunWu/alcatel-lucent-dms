define ['jqlayout'], ($)->
  init = ()->
    gridMap =
      'center': 'contextGrid'
      'south' : 'compareContextGrid'

    $('#layout-container', "#ctxmng").layout(
      onresize: (name, element, state, options, layoutname)->
        return if !gridMap[name]
        $("##{gridMap[name]}").setGridWidth(element.width() - 50).setGridHeight(element.height() - 70)

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