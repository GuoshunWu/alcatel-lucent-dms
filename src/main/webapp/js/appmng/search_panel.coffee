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
    postData.prop = 'dictionary.base.applicationBase.productBase.name, dictionary.base.applicationBase.name, dictionary.nameVersion, key,reference,maxLength,context.name,t,n,i'

    delete postData.app
    delete postData.prod

    grid.setGridParam(url: urls.labels).trigger 'reloadGrid'


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
    layout.showWelcomePanel()

  # add a button for collapse group

  $('#groupingToggle').button().click ()->
    grid = $('#globalSearchResultGrid')
    $("[id^='globalSearchResultGridghead_2_']").each (index, elem)->
#      console.log elem.id
      grid.groupingToggle(elem.id)
      true


