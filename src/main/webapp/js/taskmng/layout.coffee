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
  ($("#progressbar").draggable({grid: [50, 20], opacity: 0.35}).css({
  'z-index': 100, width: 600, textAlign: 'center'
  'position': 'absolute', 'top': '45%', 'left': '30%'}).progressbar {
  change: (e, ui)->
    value = ($(@).progressbar "value").toPrecision(4) + '%'
    $('#barvalue', @).html(value).css {"display": "block", "textAlign": "center"}
  }).hide()

  taskFileUpload = 'taskFileUpload'
  #  create upload filebutton
  $('#uploadTask').button({label: 'Upload'}).css({overflow: 'hidden'}).append $(
    "<input type='file' id='#{taskFileUpload}' name='upload' title='Choose file' multiple/>").css {
  position: 'absolute', top: 0, right: 0, margin: 0,
  border: '1px transparent', borderWidth: '0 0 40px 0px',
  opacity: 0, filter: 'alpha(opacity=0)', cursor: 'pointer'}

  #   show main page.
  $('#loading-container').fadeOut 'slow','swing',()->$(@).remove()
  util.afterInitilized(this)
  $('#optional-container').show()

#  $('#loading-container').slideUp 'slow', ()->$(@).remove()
#  $('#loading-container').toggle { effect: "scale", direction: "both" }
#  $('#loading-container').toggle { effect: "explode", pieces : 9 }




