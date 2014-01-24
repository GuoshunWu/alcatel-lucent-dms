define [
  'require'
  'jqueryui'
  'blockui'
  'jqmsgbox'

  'i18n!nls/admin'
  'i18n!nls/common'

  'dms-urls'
  'dms-util'

  './languagegrid'
  './charsetgrid'
  './usergrid'
  './glossarygrid'
  './preferredtransgrid'
  './webtestsMain'


], (require, $, blockui,jqmsgbox, i18n, c18n, urls, util)->
  init = ()->
    #    console?.log "transmng panel init..."
    isFirst = true
    $('#adminTabs').tabs(
      activate: (event, ui)->
        pheight = $(ui.newPanel).height()
        pwidth = $(ui.newPanel).width()
#        console?.log "height=#{pheight}, width=#{pwidth}."
        if isFirst
          $('table.ui-jqgrid-btable', @).setGridHeight(pheight - 90).setGridWidth(pwidth - 20)
          $('#preferredTranslationGrid',@).setGridHeight(pheight - 113)
        isFirst = false
    )

    tabs = $('#adminTabs')
    pheight = tabs.parent().height()
    tabs.tabs 'option', 'pheight', pheight

    $('div.ui-tabs-panel', tabs).height(pheight - 50)
    # console?.log "language grid height=#{$('#languageGrid').getGridParam('height')}."

    $('#buildLuceneIndex').button().click (e)->
      pb = util.genProgressBar()
      util.updateProgress(urls.config.create_index, {}, (json)->
        pb.parent().remove()
        msg = json.event.msg
        $.msgBox msg, null, {title: c18n.info, width: 300, height: 'auto'}
      , pb)

    # init dialogs

  ready = ()->
    #    console?.log "transmng panel ready..."
  init()
  ready()





