define [
  'jqgrid'
  'blockui'
  'jqmsgbox'
  'jqueryui'

  'i18n!nls/common'
  'dms-util'
  'dms-urls'

], ($, blockui, msgbox, ui, c18n, util, urls)->
  gridId = 'contextGrid'
  hGridId = "##{gridId}"

  deleteOptions = {
  msg: 'Delete selected text?'
  afterShowForm: (formid)->
    $(formid).parent().parent().position(my:'center', at: 'center', of: window)
  }

  modelLinkerFmt =
    formatoptions: {}
    unformat: (cellvalue, options, cell) ->cellvalue
    formatter: (cellvalue, options, rowObject)->
      return "" if !cellvalue or "0" == cellvalue
      "<a href=\"javascript:void(0);\" id=\"act_#{options.rowId}_#{options.colModel.name}_#{options.pos}\" style='color:blue'>#{cellvalue}</a>"

  grid = $(hGridId).jqGrid(
    url: urls.text.texts
    datatype: 'local', mtype: 'post'
    postData: {format:'grid', prop: 'reference, languageNum, t, n, i, refs'}
    pager: "#{hGridId}Pager" , rowNum: 15, rowList: [15, 50, 100]
    sortname: 'reference',  sortorder: 'asc'
    viewrecords: true, gridview: true
    caption: 'Text in Context'
    colNames: ['Reference text', 'Languages', 'T', 'N', 'I', 'Refs', 'Del']
    colModel: [
      {name: 'reference', index: 'reference', width: 700, editable:false, align: 'left'}
      $.extend {}, {name: 'languageNum', index: 'languageNum', sortable:false, width: 100, editable:false, align: 'right'}, modelLinkerFmt
      $.extend {}, {name: 't', index: 't', sortable:false, width: 20, editable:false, align: 'right'}, modelLinkerFmt
      $.extend {}, {name: 'n', index: 'n', sortable:false, width: 20, editable:false, align: 'right'}, modelLinkerFmt
      $.extend {}, {name: 'i', index: 'i', sortable:false, width: 20, editable:false, align: 'right'}, modelLinkerFmt
      $.extend {}, {name: 'refs', index: 'refs', editable:false, sortable:false, width: 50, align: 'right'}, modelLinkerFmt
      {name: 'del', index: 'del', sortable:false, width: 50, editable:false, align: 'center'
      formatter: (cellvalue, options, rowObject)->
        return "" if rowObject[options.pos - 1]

        divStr =  "<div id=\"delAct_#{options.rowId}\" style=\"float:left;margin-left:5px;\""
        divStr += "class='ui-pg-div ui-inline-del'"
        divStr += "onmouseout='jQuery(this).removeClass(\"ui-state-hover\");'"
        divStr += "onmouseover='jQuery(this).addClass(\"ui-state-hover\");'>"
        divStr += "<span class='ui-icon ui-icon-trash'></span>"
        divStr += "</div>"
        divStr
      }
    ]

    onSelectRow: (rowid, status, e)->
#      console.log "rowid=", rowid, "status=", status, "e=", e
      compareGrid = $('#compareContextGrid')
      postData = compareGrid.getGridParam 'postData'
      postData.text=rowid

      compareGrid.trigger 'reloadGrid'

    gridComplete: ->
      grid = $(@)
      #handlers = grid.getGridParam 'cellactionhandlers'

      $("a[id^='act']", @).click(->
        [_,rowid, name, pos] = @id.split('_')
        rowData = grid.getRowData(rowid)
        dlg = "Translation" if name in ['t', 'n', 'i']
        dlg = "Reference" if name in ['refs']
        dlg = 'Language' if name == 'languageNum'
        dialogId = "#ctx#{dlg}sDialog"
        $(dialogId).data('params', colname: name, id: rowid, rowData: rowData).dialog 'open'
      )

      $("div[id^='delAct']", @).click(->
        [_,rowid] = @id.split('_')

      )
  ).setGridParam(datatype: 'json')
  .navGrid "#{hGridId}Pager", {edit: false, add: false, del: false, search: false, view: false},{},{},deleteOptions

  grid: grid



