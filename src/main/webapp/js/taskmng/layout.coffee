define ['jqlayout', 'taskmng/task_grid', 'i18n!nls/common', 'taskmng/dialogs', 'util', 'require'], ($, grid, c18n, dialogs, util, require)->
  require 'jqueryui'

  $('#productBase').change ()->
    $('#productRelease').empty()
    return false if parseInt($('#productBase').val()) == -1

    $.getJSON "rest/products/version", {base: $(@).val(), prop: 'id,version'}, (json)->
      $('#productRelease')
      .append(util.newOption c18n.select.release.tip, -1)
      .append(util.json2Options json).trigger 'change'

  $('#productRelease').change ->
  #    todo: refresh grid according to the product release
    param = {
    release: {id: $(@).val(), version: $(@).find("option:selected").text()}
    }
    return false if !param.release.id || parseInt(param.release.id) == -1

    grid.productVersionChanged param

  $('#productRelease option:last').attr('selected', true) if !param.currentSelected.productId || '-1' == String(param.currentSelected.productId)
  $('#productRelease').trigger 'change'

  # file uploader
  $("#progressbar").draggable(grid: [50, 20], opacity: 0.35).progressbar(
    create: (e, ui) ->
      @label = $('.progressbar-label', @)
      $(@).position(my: 'center', at: 'center', of: window)
    change: (e, ui)->
      @label.html ($(this).progressbar("value").toPrecision(4)) + "%"
  ).hide()

  #   show main page.
  $('#loading-container').fadeOut 'slow', 'swing', ()->$(@).remove()
  util.afterInitilized(this)
  $('#optional-container').show()
  gridParent = $('.taskGrid_parent')

  $('#taskGrid').setGridWidth(gridParent.width() - 10).setGridHeight(gridParent.height() - 60)

#  $('#loading-container').slideUp 'slow', ()->$(@).remove()
#  $('#loading-container').toggle { effect: "scale", direction: "both" }
#  $('#loading-container').toggle { effect: "explode", pieces : 9 }




