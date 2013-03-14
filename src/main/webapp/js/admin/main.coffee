define ['require','jqueryui', 'admin/languagegrid', 'admin/charsetgrid'], (require, $)->
  init = ()->
    console?.log "transmng panel init..."
    $('#adminTabs').tabs(
      show: (event, ui)->
        pheight = $(ui.panel).height()
        pwidth = $(ui.panel).width()
#        console?.log "height=#{pheight}, width=#{pwidth}."
        $('table.ui-jqgrid-btable', ui.panel).setGridHeight(pheight - 90).setGridWidth(pwidth - 20)
    )

    tabs = $('#adminTabs')
    pheight = tabs.parent().height()
    tabs.tabs 'option', 'pheight', pheight
#    console?.log "init tabs height=#{pheight}."

    $('div.ui-tabs-panel', tabs).height(pheight - 50)
    tabs.tabs 'select', 2
    #  console?.log "language grid height=#{$('#languageGrid').getGridParam('height')}."
#    $('#languageGrid').setGridHeight('100%')

  ready = ()->
    console?.log "transmng panel ready..."
  init()
  ready()





