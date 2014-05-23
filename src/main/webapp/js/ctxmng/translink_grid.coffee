define [
  'jqgrid'
  'blockui'
  'jqmsgbox'
  'jqueryui'

  'i18n!nls/common'
  'dms-util'
  'dms-urls'

], ($, blockui, msgbox, ui, c18n, util, urls)->
  gridId = 'transLinkGrid'
  hGridId = "##{gridId}"

  grid = $(hGridId).jqGrid(
    url: urls.text.translations
    datatype: 'local', mtype: 'post'
    postData: { format:'grid', prop: 'language.name, translation'}
    width: 600, height: 400
    colNames: ['Language', 'Translation']
    colModel: [
      {name: 'language', index: 'language', editable:false, align: 'left'}
      {name: 'tramslation', index: 'translation', editable:false, align: 'left'}
    ]
  ).setGridParam(datatype: 'json')
  .navGrid "#{hGridId}Pager", {edit: false, add: false, del: false, search: false, view: false},{},{},{}

  grid: grid



