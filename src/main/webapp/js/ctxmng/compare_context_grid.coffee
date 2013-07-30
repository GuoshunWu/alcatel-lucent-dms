define [
  'jqgrid'
  'blockui'
  'jqmsgbox'
  'jqueryui'

  'i18n!nls/common'
  'dms-util'
  'dms-urls'

], ($, blockui, msgbox,ui, c18n, util, urls)->

  gridId= 'compareContextGrid'
  hGridId = "##{gridId}"

  modelLinkerFmt =
    formatoptions: {}
    unformat: (cellvalue, options, cell) ->cellvalue
    formatter: (cellvalue, options, rowObject)->
      return "" if !cellvalue or "0" == cellvalue
      "<a href=\"javascript:void(0);\" id=\"act_#{options.rowId}_#{options.colModel.name}_#{options.pos}\" style='color:blue'>#{cellvalue}</a>"

  grid = $(hGridId).jqGrid(
    url: urls.text.texts
    datatype: 'local', mtype: 'post'
    postData: {format:'grid', prop: 'reference, languageNum, t, n, i, refs, refs'}

    colNames: ['Reference text', 'Languages', 'T', 'N', 'I', 'Refs', 'Diff']
    colModel: [
      {name: 'reference', index: 'reference', width: 700, editable:false, align: 'left'}
      $.extend {}, {name: 'languages', index: 'languages', sortable:false, width: 100, editable:false, align: 'right'}, modelLinkerFmt
      $.extend {}, {name: 't', index: 't', sortable:false, width: 20, editable:false, align: 'right'}, modelLinkerFmt
      $.extend {}, {name: 'n', index: 'n', sortable:false, width: 20, editable:false, align: 'right'}, modelLinkerFmt
      $.extend {}, {name: 'i', index: 'i', sortable:false, width: 20, editable:false, align: 'right'}, modelLinkerFmt
      $.extend {}, {name: 'refs', index: 'refs', editable:false, sortable:false, width: 50, align: 'right'}, modelLinkerFmt
      $.extend {},{name: 'diff', index: 'diff', sortable:false, width: 50, editable:false, align: 'right'}, modelLinkerFmt
    ]

    gridComplete: ->
      grid = $(@)
      #handlers = grid.getGridParam 'cellactionhandlers'
      $("a[id^='act']", @).click(->
        [_,rowid, name, pos] = @id.split('_')
        rowData = grid.getRowData(rowid)
        dlg = "Translation" if name in ['t', 'n', 'i']
        dlg = "Reference" if name in ['refs']
        dlg = "Difference" if name in ['diff']

        dialogId = "#ctx#{dlg}sDialog"
        #        console?.log("rowid=#{rowid}, name=#{name}, pos=#{pos}, dialogId=#{dialogId}")
        $(dialogId).data('params', colname: name, id: rowid, rowData: rowData).dialog 'open'
      )

  ).setGridParam(datatype: 'json')
  .navGrid "#{hGridId}Pager", {edit: false, add: false, del: false, search: false, view: false}

  grid:grid



