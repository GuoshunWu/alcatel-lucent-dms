// Generated by CoffeeScript 1.3.3
(function() {

  define(['jqlayout', 'taskmng/task_grid'], function($) {
    var taskFileUpload;
    $('#pageNavigator').val(window.location.pathname);
    ($("#progressbar").draggable({
      grid: [50, 20],
      opacity: 0.35
    }).css({
      'z-index': 100,
      width: 600,
      textAlign: 'center',
      'position': 'absolute',
      'top': '45%',
      'left': '30%'
    }).progressbar({
      change: function(e, ui) {
        var value;
        value = ($(this).progressbar("value")).toPrecision(4) + '%';
        return $('#barvalue', this).html(value).css({
          "display": "block",
          "textAlign": "center"
        });
      }
    })).hide();
    taskFileUpload = 'taskFileUpload';
    return $('#uploadTask').button({
      label: 'Browser'
    }).css({
      overflow: 'hidden'
    }).append($("<input type='file' id='" + taskFileUpload + "' name='upload' title='Choose file' multiple/>").css({
      position: 'absolute',
      top: 0,
      right: 0,
      margin: 0,
      border: '1px transparent',
      borderWidth: '0 0 40px 0px',
      opacity: 0,
      filter: 'alpha(opacity=0)',
      cursor: 'pointer'
    }));
  });

}).call(this);