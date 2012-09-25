// Generated by CoffeeScript 1.3.3
(function() {

  define(['jqueryui', 'appmng/dictionary_grid', 'appmng/langsetting_grid', 'i18n!nls/appmng', 'jsfileuploader/jquery.iframe-transport', 'jsfileuploader/jquery.fileupload'], function($, grid, langGrid, i18n) {
    $("#selAppVersion").change(function() {
      return grid.appChanged({
        version: $(this).find("option:selected").text(),
        id: $(this).val()
      });
    });
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
    $('#uploadBrower').button({
      label: i18n.browse
    }).css({
      overflow: 'hidden'
    }).append($("<input type='file' id='dctFileUpload' name='upload' title='" + i18n.choosefile + "' multiple/>").css({
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
    $("#dctFileUpload").fileupload({
      type: 'POST',
      url: 'app/deliver-app-dict',
      add: function(e, data) {
        $.each(data.files, function(index, file) {
          return $('#uploadStatus').html("Uploading file: " + file.name);
        });
        data.submit();
        if (!$.browser.msie) {
          return $("#progressbar").show();
        }
      },
      done: function(e, data) {
        $.each(data.files, function(index, file) {
          return $('#uploadStatus').html("" + file.name + " upload finished.");
        });
        if (!$.browser.msie) {
          return $("#progressbar").hide();
        }
      },
      progressall: function(e, data) {
        var progress;
        progress = data.loaded / data.total * 100;
        return $('#progressbar').progressbar("value", progress);
      }
    });
    return {
      refresh: function(info) {
        $('#appDispProductName').html(info.parent.text);
        $('#appDispAppName').html(info.text);
        return $.getJSON("rest/applications/apps/" + info.id, {}, function(json) {
          $("#selAppVersion").empty().append($(json).map(function() {
            return new Option(this.version, this.id);
          }));
          return $("#selAppVersion").trigger("change");
        });
      }
    };
  });

}).call(this);
