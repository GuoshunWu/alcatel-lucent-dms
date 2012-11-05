define ['jqlayout', 'taskmng/task_grid', 'require', 'i18n!nls/common', 'taskmng/dialogs'], ($, grid, require, c18n, dialogs)->
  $('#pageNavigator').val(window.location.pathname)
  $("#optional-container").layout {resizable: true, closable: true}
  productInfo = {}
  if window.location.search
    $(window.location.search.split('?')[1].split('&')).each (index, elem)->
      kv = elem.split('=')
      productInfo[kv[0]] = kv[1]

  # selects on summary panel
  $.getJSON 'rest/products', {prop: 'id,name'}, (json)->
    $('#productBase').append new Option(c18n.select.product.tip, -1)
    $('#productBase').append $(json).map ()->
      opt = new Option @name, @id
      opt.selected = true if productInfo.productBase and @id == parseInt(productInfo.productBase)
      opt
    $('#productBase').trigger 'change'


  #  load product in product base
  $('#productBase').change ()->
    $('#productRelease').empty()
    return false if parseInt($('#productBase').val()) == -1

    $.getJSON "/rest/products/version", {base: $(@).val(), prop: 'id,version'}, (json)->
      $('#productRelease').append new Option(c18n.select.release.tip, -1)
      $('#productRelease').append $(json).map ()->
        opt = new Option @version, @id
        if productInfo.product and @id == parseInt(productInfo.product)
          opt.selected = true
        opt
      $('#productRelease').trigger "change"

  $('#productRelease').change ->
  #    todo: refresh grid according to the product release
    param = {
    release: {id: $(@).val(), version: $(@).find("option:selected").text()}
    }
    if !$('#productBase').val() || parseInt($('#productBase').val()) == -1
    #        $.msgBox i18n.select.product.msg, null,title: i18n.select.product.msgtitle
      return false

    if !param.release.id || parseInt(param.release.id) == -1
    #        $.msgBox i18n.select.release.msg, null, title: i18n.select.release.msgtitle
      return false
    grid.productVersionChanged param

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
  $('#optional-container').show()
  $('#loading-container').remove()





