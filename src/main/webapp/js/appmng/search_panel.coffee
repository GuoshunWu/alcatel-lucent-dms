define [
  'jqueryui'

  'i18n!nls/appmng'
  'i18n!nls/common'
  'dms-util'
  'dms-urls'

  'appmng/layout'
  'appmng/global_search_grid'
], ($, i18n, c18n, util, urls, layout, sgrid)->

  searchText = (text)->
    grid = $('#globalSearchResultGrid')

    postData = grid.getGridParam('postData')

    postData.format = 'grid'
    postData.text = text
    postData.prop = 'prod.base.name,prod.version, app.name,app.version, dictionary.base.name, dictionary.version, key,reference,maxLength,context.name,t,n,i'

    delete postData.app
    delete postData.prod

    grid.setCaption(i18n.dialog.searchtext.globalcaption.format text)
      .setGridParam(url: urls.labels).trigger 'reloadGrid'


  searchResultActionBtn = $('#globalSearchInResultPanelAction', '#appmng').attr('title', 'Search').button(
    text: false, icons:{primary: "ui-icon-search"})
    .height(20).width(20)
    .position(my: 'left center', at: 'right center', of: '#globalSearchInResultPanel')

  searchResultActionBtn.click ()->searchText $('#globalSearchInResultPanel', '#appmng').val()


  globalSearchInResultPanel =  $('#globalSearchInResultPanel', '#appmng').keydown (e)->
    return true if e.which != 13
    searchResultActionBtn.trigger 'click'
    false

  #  search button in welcome panel
  searchActionBtn = $('#globalSearchAction', '#appmng').attr('title', 'Search').button(text: false, icons:
    {primary: "ui-icon-search"})
    .height(20).width(20).position(my: 'left center', at: 'right center', of: '#globalSearch').click(()->
      searchValue = $('#globalSearch', '#appmng').val()
      globalSearchInResultPanel.val(searchValue)
      layout.showSearchPanel()
      searchText(searchValue)
    )


  $('#globalSearch', '#appmng').keydown (e)->
    return true if e.which != 13
    searchActionBtn.trigger 'click'
    false

  $('#goBackToWelcome').button().click ()->
    $('#globalSearchResultGrid').clearGridData()
    layout.showWelcomePanel()


