define [
  'jqgrid'
  'blockui'
  'jqmsgbox'
  'jqueryui'

  'i18n!nls/common'
  'dms-util'
  'dms-urls'

], ($, blockui, msgbox, ui, c18n, util, urls)->
  gridId = 'languagesLinkGrid'
  hGridId = "##{gridId}"

  grid = $(hGridId).jqGrid(
    url: urls.text.translations
    datatype: 'local', mtype: 'post'
    postData: { format:'grid', prop: 'language.name'}
    width: 570, height: 400
    colNames: ['Language']
    colModel: [
      {name: 'language', index: 'language', editable:false, align: 'left'}
    ]
  ).setGridParam(datatype: 'json')
  .navGrid "#{hGridId}Pager", {edit: false, add: false, del: false, search: false, view: false},{},{},{}

  grid: grid



