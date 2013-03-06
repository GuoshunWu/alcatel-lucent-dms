// Generated by CoffeeScript 1.5.0
(function() {
  var dependencies;

  dependencies = ['jqueryui', 'jqupload', 'iframetransport', 'i18n!nls/appmng', 'i18n!nls/common', 'dms-util', 'dms-urls', 'appmng/dialogs', 'appmng/dictionary_grid'];

  define(dependencies, function($, upload, iframetrans, i18n, c18n, util, urls, dialogs, grid) {
    var appInfo, dctFileUpload,
      _this = this;
    if (typeof console !== "undefined" && console !== null) {
      console.log("module appmng/application_panel loading.");
    }
    appInfo = {};
    $("#newAppVersion").button({
      text: false,
      label: '&nbsp;',
      icons: {
        primary: "ui-icon-plus"
      }
    }).attr('privilegeName', util.urlname2Action(urls.app.create_version)).click(function(e) {
      return $("#newApplicationVersionDialog").dialog("open");
    });
    $("#removeAppVersion").button({
      text: false,
      label: '&nbsp;',
      icons: {
        primary: "ui-icon-minus"
      }
    }).attr('privilegeName', util.urlname2Action(urls.app.remove_version)).click(function(e) {
      var id;
      id = $("#selAppVersion").val();
      if (!id) {
        return;
      }
      return $.post(urls.app.remove_version, {
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
    dctFileUpload = 'dctFileUpload';
    $('#uploadBrower').button({
      label: i18n.browse
    }).attr('privilegeName', util.urlname2Action('app/deliver-app-dict')).css({
      overflow: 'hidden'
    }).append($("<input type='file' id='" + dctFileUpload + "' name='upload' title='" + i18n.choosefile + "' accept='application/zip' multiple/>").css({
      position: 'absolute',
      top: -3,
      right: -3,
      border: '1px solid',
      borderWidth: '10px 180px 40px 20px',
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
        if (!$.browser.msie || parseInt($.browser.version.split('\.')[0]) >= 10) {
          this.pb = util.genProgressBar();
        }
        return $('#uploadBrower').button('disable');
      },
      progressall: function(e, data) {
        var progress;
        if ($.browser.msie && parseInt($.browser.version.split('\.')[0]) < 10) {
          return;
        }
        progress = data.loaded / data.total * 100;
        return this.pb.progressbar("value", progress);
      },
      done: function(e, data) {
        var jsonFromServer;
        $('#uploadBrower').button('enable');
        $.each(data.files, function(index, file) {
          return $('#uploadStatus').html("" + file.name + " " + i18n.uploadfinished);
        });
        if (!$.browser.msie || parseInt($.browser.version.split('\.')[0]) >= 10) {
          this.pb.parent().remove();
        }
        jsonFromServer = data.result;
        if (0 !== jsonFromServer.status) {
          $.msgBox(jsonFromServer.message, null, {
            title: c18n.error,
            height: 600,
            width: 800
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
      refresh: function(info) {
        $('#appDispProductName').html(info.parent.text);
        $('#appDispAppName').html(info.text);
        appInfo.base = {
          text: info.text,
          id: info.id
        };
        return $.getJSON("rest/applications/apps/" + info.id, {}, function(json) {
          var selAppVer;
          selAppVer = $('#selAppVersion', "div[id='appmng']");
          selAppVer.empty().append(util.json2Options(json));
          if (window.param.currentSelected.appId && -1 !== parseInt(param.currentSelected.appId)) {
            selAppVer.val(param.currentSelected.appId);
            window.param.currentSelected.appId = null;
          }
          return selAppVer.trigger("change");
        });
      }
    };
  });

}).call(this);
