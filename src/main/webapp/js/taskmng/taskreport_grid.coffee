define ['jqgrid'], ($)->
  colNames = ['Application', 'Total']
  colModel = [
    {name: 'name', index: 'dict', width: 240, editable: false, stype: 'select', align: 'left', frozen: true}
    {name: 'total', index: 'total', width: 90, editable: false, align: 'right', frozen: true}
  ]
  groupHeader = []

  grid = $("#reportGrid").jqGrid(
    url: 'json/transreportgrid.json'
    mtype: 'POST'
    editurl: "", datatype: 'local'
    width: $(window).width() * 0.78, height: 200, shrinkToFit: false
    pager: '#reportPager', rowNum: 60, rowList: [30, 60, 120]
    sortname: 'key', sortorder: 'asc', viewrecords: true, gridview: true, multiselect: false
    cellEdit: true, cellurl: ''
    colNames: colNames, colModel: colModel, groupHeaders: groupHeader
    gridComplete: ->
      #    console?.log "task id in report grid: #{$('#reportGrid').getGridParam('postData').task}"
      $('a', @).each (index, a)->$(a).before(' ').remove() if '0' == $(a).text()
      $('a', @).css('color', 'blue').click ()->
        param = {}

        $(@href.replace('#', '').split('?')[1..].join('&').split('&')).each (index, elem)->
          [k, v] = elem.split('=')
          param[k] = decodeURIComponent v

        rowData = grid.getRowData(parseInt param.id)
        allZero = true
        $(['T', 'N']).each (index, elem)->
          allZero = 0 == parseInt rowData["#{param.languaeName}.#{elem}"]
          allZero
        (console.log 'zero';return) if allZero
        $('#taskDetailDialog').data('param', task: $('#reportGrid').getGridParam('postData').task, language: param.languageId, translated: Number(param.languaeName.split('.')[1] == 'T'), context: param.id).dialog 'open'
        false

    afterCreate: (grid)->
      #    console.log "After Create in task report grid: #{grid.getGridParam('postData').task}"
      grid.setGroupHeaders {useColSpanStyle: true, groupHeaders: grid.getGridParam('groupHeaders')}
      grid.navGrid '#reportPager', {edit: false, add: false, del: false, search: false, view: false}
      grid.jqGrid 'setFrozenColumns'
  ).setGridParam(datatype: 'json')
  grid.getGridParam('afterCreate')(grid) if grid.getGridParam('afterCreate')


  regenerateGrid: (params)->
    gridId = '#reportGrid'
    gridParam = $(gridId).jqGrid 'getGridParam'
    $(gridId).GridUnload 'reportGrid'

    cols = ['T', 'N']

    gridParam.colNames = colNames.slice(0)
    gridParam.colModel = colModel.slice(0)
    gridParam.groupHeader = groupHeader.slice(0)

    $(params.languages).each (index, language)->
      $.merge gridParam.colNames, cols
      $.merge gridParam.colModel, $(cols).map(
        (index)->name: "#{language.name}.#{@}", sortable: false, index: "s(#{language.id})[#{index}]", width: 40, editable: false, align: 'center', formatter: 'showlink', formatoptions:
          {baseLinkUrl: '#', addParam: encodeURI("&languageId=#{language.id}&languaeName=#{language.name}.#{@}")}).get()

      gridParam.groupHeaders.push {startColumnName: "#{language.name}.T", numberOfColumns: cols.length, titleText: "<bold>#{language.name}</bold>"}

    gridParam.url = 'rest/task/summary'
    gridParam.postData = task: params.id, format: 'grid', prop: 'context.name,total,' + $(params.languages).map(
      (index, language)->$([0, 1]).map(
        (idx)->"s(#{language.id})[#{idx}]").get().join(',')
    ).get().join ','
    delete gridParam.selarrrow

    #    console?.log "before regenerate report grid task id: #{gridParam.postData.task}"
    newGrid = $(gridId).jqGrid gridParam
    gridParam.afterCreate newGrid













