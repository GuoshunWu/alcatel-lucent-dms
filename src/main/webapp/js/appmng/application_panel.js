// Generated by CoffeeScript 1.3.3
(function() {

  define(function(require) {
    var $, appInfo, c18n, dctFileUpload, dialogs, grid, i18n,
      _this = this;
    $ = require('jqueryui');
    require('appmng/langsetting_grid');
    require('appmng/stringsettings_grid');
    require('jqupload');
    require('iframetransport');
    dialogs = require('appmng/dialogs');
    grid = require('appmng/dictionary_grid');
    i18n = require('i18n!nls/appmng');
    c18n = require('i18n!nls/appmng');
    appInfo = {};
    $("#newAppVersion").button({
      text: false,
      label: '&nbsp;',
      icons: {
        primary: "ui-icon-plus"
      }
    }).click(function(e) {
      return dialogs.newAppVersion.dialog("open");
    });
    $("#removeAppVersion").button({
      text: false,
      label: '&nbsp;',
      icons: {
        primary: "ui-icon-minus"
      }
    }).click(function(e) {
      var id;
      id = $("#selAppVersion").val();
      if (!id) {
        return;
      }
      return $.post('app/remove-application', {
        id: id,
        permanent: 'true'
      }, function(json) {
        if (json.status !== 0) {
          $.msgBox(json.message, null, {
            title: c18n.error
          });
          return;
        }
        $("#selAppVersion option:selected").remove();
        return $("#selAppVersion").trigger('change');
      });
    });
    $("#selAppVersion").change(function(e) {
      appInfo.app = {
        version: $("option:selected", this).text(),
        id: this.value ? this.value : -1
      };
      return grid.appChanged(appInfo);
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
    dctFileUpload = 'dctFileUpload';
    $('#uploadBrower').button({
      label: i18n.browse
    }).css({
      overflow: 'hidden'
    }).append($("<input type='file' id='" + dctFileUpload + "' name='upload' title='" + i18n.choosefile + "' accept='application/zip' multiple/>").css({
      position: 'absolute',
      top: -3,
      right: -3,
      border: '1px solid',
      borderWidth: '1px 1px 10px 0px',
      opacity: 0,
      filter: 'alpha(opacity=0)',
      cursor: 'pointer'
    }));
    $("#" + dctFileUpload).fileupload({
      type: 'POST',
      dataType: 'json',
      url: "app/deliver-app-dict",
      add: function(e, data) {
        var appId;
        $.each(data.files, function(index, file) {});
        appId = $("#selAppVersion").val();
        if (!appId) {
          return;
        }
        $(this).fileupload('option', 'formData', [
          {
            name: 'appId',
            value: $("#selAppVersion").val()
          }
        ]);
        data.submit();
        if (!$.browser.msie) {
          return $("#progressbar").show();
        }
      },
      progressall: function(e, data) {
        var progress;
        progress = data.loaded / data.total * 100;
        return $('#progressbar').progressbar("value", progress);
      },
      done: function(e, data) {
        var jsonFromServer;
        $.each(data.files, function(index, file) {
          return $('#uploadStatus').html("" + file.name + " " + i18n.uploadfinished);
        });
        if (!$.browser.msie) {
          $("#progressbar").hide();
        }
        jsonFromServer = data.result;
        if (0 !== jsonFromServer.status) {
          $.msgBox(jsonFromServer.message, null, {
            title: c18n.error
          });
          return;
        }
        $('#dictListPreviewDialog').data('param', {
          handler: jsonFromServer.filename,
          appId: $("#selAppVersion").val()
        });
        return $('#dictListPreviewDialog').dialog('open');
      }
    });
    return {
      getApplicationSelectOptions: function() {
        return $('#selAppVersion').children('option').clone(true);
      },
      addNewApplication: function(app) {
        var newOption;
        newOption = new Option(app.version, app.id);
        newOption.selected = true;
        return $('#selAppVersion').append(newOption).trigger('change');
      },
      refresh: function(info) {
        $('#appDispProductName').html(info.parent.text);
        $('#appDispAppName').html(info.text);
        appInfo.base = {
          text: info.text,
          id: info.id
        };
        return $.getJSON("rest/applications/apps/" + info.id, {}, function(json) {
          $("#selAppVersion").empty().append($(json).map(function() {
            return new Option(this.version, this.id);
          }));
          $("#selAppVersion option:last").attr('selected', true);
          return $("#selAppVersion").trigger("change");
        });
      }
    };
  });

}).call(this);
