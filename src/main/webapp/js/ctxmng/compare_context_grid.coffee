define [
  'jqgrid'
  'blockui'
  'jqmsgbox'
  'jqueryui'

  'i18n!nls/common'
  'i18n!nls/ctxmng'
  'dms-util'
  'dms-urls'

], ($, blockui, msgbox, ui, c18n, i18n, util, urls)->
  gridId = 'compareContextGrid'
  hGridId = "##{gridId}"

  modelLinkerFmt =
    formatoptions: {}
    unformat: (cellvalue, options, cell) ->
      cellvalue
    formatter: (cellvalue, options, rowObject)->
      return "" if !cellvalue or "0" == cellvalue
      "<a href=\"javascript:void(0);\" id=\"act_#{options.rowId}_#{options.colModel.name}_#{options.pos}\" style='color:blue'>#{cellvalue}</a>"

  grid = $(hGridId).jqGrid(
    url: urls.text.diff_texts
    datatype: 'local', mtype: 'post'
    postData: {format: 'grid', prop: 'reference, context.key, languageNum, t, n, i, refs, diff, context.id'}
    caption: i18n.compareGridTitle
    colNames: ['Reference text', 'Context', 'Languages', 'T', 'N', 'I', 'Refs', 'Diff',  'Context ID', 'Merge',]
    colModel: [
      {name: 'reference', index: 'reference', width: 700, editable: false, align: 'left', hidden: true}
      {name: 'context', index: 'context.key', width: 700, editable: false, align: 'left'}
      $.extend {}, {name: 'languageNum', index: 'languageNum', sortable: false, width: 100, editable: false, align: 'right'}, modelLinkerFmt
      $.extend {}, {name: 't', index: 't', sortable: false, width: 20, editable: false, align: 'right'}, modelLinkerFmt
      $.extend {}, {name: 'n', index: 'n', sortable: false, width: 20, editable: false, align: 'right'}, modelLinkerFmt
      $.extend {}, {name: 'i', index: 'i', sortable: false, width: 20, editable: false, align: 'right'}, modelLinkerFmt
      $.extend {}, {name: 'refs', index: 'refs', editable: false, sortable: false, width: 50, align: 'right'}, modelLinkerFmt
      $.extend {}, {name: 'diff', index: 'diff', sortable: false, width: 50, editable: false, align: 'right'}, modelLinkerFmt
      {name: 'contextId', index: 'context.id', width: 700, editable: false, align: 'left', hidden: true}
      {name: 'merge', index: 'merge', editable:false, width: 40, align: 'center', unformat: (cellvalue, options, cell) ->cellvalue
      formatter: (cellvalue, options, rowObject)->
          return "" if "0" != rowObject[options.pos - 2] || !rowObject[options.pos - 2]
          "<a href=\"javascript:void(0);\" title='M' id=\"act_#{options.rowId}_#{options.colModel.name}_#{options.pos}_M\" style='color:blue'>M</a>"
      }
    ]

    gridComplete: ->
      grid = $(@)
      $("a[id^='act']", @).click(->
        [_,rowid, name, pos] = @id.split('_')
        rowData = grid.getRowData(rowid)
        dialogMap = {
          'refs': 'Reference'
          'diff': 'Difference'
          'languageNum': 'Language'
          'merge': 'Merge'
        }
        dlg = if name in ['t', 'n', 'i'] then "Translation" else dialogMap[name]
        $("#ctx#{dlg}sDialog").data('params', colname: name, id: rowid, rowData: rowData).dialog 'open'
      )
  ).setGridParam(datatype: 'json')
  .navGrid "#{hGridId}Pager", {edit: false, add: false, del: false, search: false, view: false}

  grid: grid



