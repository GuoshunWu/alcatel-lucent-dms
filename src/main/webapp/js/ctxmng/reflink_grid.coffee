define [
  'jqgrid'
  'blockui'
  'jqmsgbox'
  'jqueryui'

  'i18n!nls/common'
  'dms-util'
  'dms-urls'

], ($, blockui, msgbox, ui, c18n, util, urls)->
  gridId = 'refLinkGrid'
  hGridId = "##{gridId}"

  grid = $(hGridId).jqGrid(
    width: 600
    url: urls.text.refs
    datatype: 'local', mtype: 'post'
    postData: {
      format:'grid'
      prop: '''dictionary.base.applicationBase.productBase.name,
                      dictionary.base.applicationBase.name,
                      dictionary.base.name,
                      key'''
    }
    colNames: ['Product', 'Application', 'Dictionary', 'Label Key']
    colModel: [
      {name: 'product', index: 'dictionary.base.applicationBase.productBase.name', editable:false, align: 'left'}
      {name: 'application', index: 'dictionary.base.applicationBase.name', editable:false, align: 'left'}
      {name: 'dictionary', index: 'dictionary.base.name', editable:false, align: 'left'}
      {name: 'labelkey', index: 'key', editable:false, align: 'left'}
    ]
  ).setGridParam(datatype: 'json')
  .navGrid "#{hGridId}Pager", {edit: false, add: false, del: false, search: false, view: false},{},{},{}

  grid: grid



