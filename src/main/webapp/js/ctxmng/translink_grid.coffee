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
    width: 810, height: 400
    colNames: ['Language', 'Translation']
    colModel: [
      {name: 'language', width: 200, index: 'language', editable:false, align: 'left'}
      {name: 'translation', width: 610, index: 'translation', editable:false, align: 'left'}
    ]
  ).setGridParam(datatype: 'json')
  .navGrid "#{hGridId}Pager", {edit: false, add: false, del: false, search: false, view: false},{},{},{}

  grid: grid



