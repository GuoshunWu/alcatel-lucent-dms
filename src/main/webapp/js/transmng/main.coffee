define (require)->

  i18n = require 'i18n!nls/transmng'
  c18n = require 'i18n!nls/common'

  grid = require 'transmng/trans_grid'

  dialogs = require 'transmng/dialogs'
  util = require 'dms-util'

  exportAppOrDicts = (ftype)->
    id = $('#productRelease').val()
    return if !id
    id = parseInt(id)
    return if -1 == id

    checkboxes = $("#languageFilterDialog input:checkbox[name='languages']:checked")
    languages = checkboxes.map(
      ()-> return @id
    ).get().join(',')

    type = $("input:radio[name='viewOption'][checked]").val()
    type = type[..3]
    type = type[..2] if type[0] == 'a'

    $("#exportForm input[name='prod']").val id
    $("#exportForm input[name='language']").val languages
    $("#exportForm input[name='type']").val type
    $("#exportForm input[name='type']").val ftype if ftype
    $("#exportForm").submit()

  init = ()->
    console?.debug "transmng panel init..."

    # selects on summary panel
    #  load product in product base
    $('#productBase').change ()->
      $('#productRelease').empty()
      return false if parseInt($('#productBase').val()) == -1

      $.getJSON "rest/products/version", {base: $(@).val(), prop: 'id,version'}, (json)->
        $('#productRelease').append util.newOption(c18n.select.release.tip, -1)
        $('#productRelease').append util.json2Options json, json[json.length - 1].id
        $('#productRelease').trigger "change"


    $('#productRelease').change ->
      return if -1 == parseInt @value
      $.ajax {url: "rest/languages", async: false, data: {prod: @value, prop: 'id,name'}, dataType: 'json', success: (languages)->
        langTable = util.generateLanguageTable languages
        $("#languageFilterDialog").empty().append langTable
      }
      dialogs.refreshGrid(false, grid)
    $('#productRelease').trigger 'change'

    # Create buttons
    $("#create").button()
    .attr('privilegeName', util.urlname2Action 'task/create-task')
    .click ->
      info = grid.getTotalSelectedRowInfo()
      if !info.rowIds.length
        $.msgBox (c18n.selrow.format c18n[grid.getTableType()]), null, title: c18n.warning
        return
      dialogs.taskDialog.dialog "open"

    $('#languageFilter').button().click ()->dialogs.languageFilterDialog.dialog "open"
    #    for view level
    $(':radio[name=viewOption]').change ->dialogs.refreshGrid(false, grid)

    $("#exportTranslation").button()
    .attr('privilegeName', util.urlname2Action 'trans/export-translation-details')
    .click ->
      info = grid.getTotalSelectedRowInfo()
      if !info.rowIds.length
        $.msgBox (c18n.selrow.format c18n[grid.getTableType()]), null, title: c18n.warning
        return
      dialogs.exportTranslationDialog.dialog 'open'

    #    add action for export
    $("#exportExcel").click ()->exportAppOrDicts 'excel'
    $("#exportPDF").click ()->exportAppOrDicts 'pdf'


  ready = ()->
    console?.debug "transmng panel ready..."
    gridParent = $('.transGrid_parent')
    $('#transGrid').setGridWidth(gridParent.width() - 10).setGridHeight(gridParent.height() - 110)

  init()
  ready()