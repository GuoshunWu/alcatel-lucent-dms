define ['jqlayout','taskmng/task_grid'], ($)->
  $('#pageNavigator').val(window.location.pathname)
  $("#optional-container").layout {resizable: true, closable: true}

# file uploader
  ($("#progressbar").draggable({grid: [50, 20], opacity: 0.35}).css({
  'z-index': 100, width: 600, textAlign: 'center'
  'position': 'absolute', 'top': '45%', 'left': '30%'}).progressbar {
  change: (e, ui)->
    value = ($(@).progressbar "value").toPrecision(4) + '%'
    $('#barvalue', @).html(value).css {"display": "block", "textAlign": "center"}
  }).hide()

  taskFileUpload='taskFileUpload'
  #  create upload filebutton
  $('#uploadTask').button({label:'Browser'}).css({overflow: 'hidden'}).append $(
    "<input type='file' id='#{taskFileUpload}' name='upload' title='Choose file' multiple/>").css {
  position: 'absolute', top: 0, right: 0, margin: 0,
  border: '1px transparent', borderWidth: '0 0 40px 0px',
  opacity: 0, filter: 'alpha(opacity=0)', cursor: 'pointer'}





