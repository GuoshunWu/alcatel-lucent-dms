// Generated by CoffeeScript 1.3.3
(function() {

  define(['require', 'appmng/dictlistpreview_grid', 'appmng/dictpreviewstringsettings_grid', 'appmng/previewlangsetting_grid'], function(require, grid, sgrid, lgrid) {
    var $, addApplication, addLanguage, c18n, dictListPreview, dictPreviewLangSettings, dictPreviewStringSettings, i18n, langSettings, newAppVersion, newProductVersion, setContextTo, stringSettings, util;
    $ = require('jqueryui');
    c18n = require('i18n!nls/common');
    i18n = require('i18n!nls/appmng');
    require('blockui');
    require('jqmsgbox');
    util = require('util');
    newProductVersion = $("#newProductReleaseDialog").dialog({
      autoOpen: false,
      height: 200,
      width: 500,
      modal: true,
      buttons: [
        {
          text: c18n.ok,
          click: function() {
            var dupVersionId, productBaseId, tree, url, versionName;
            url = 'app/create-product-release';
            versionName = $('#versionName').val();
            dupVersionId = $("#dupVersion").val();
            tree = require('appmng/navigatetree');
            productBaseId = tree.getNodeInfo().id;
            if (!versionName) {
              $("#productErrInfo").show();
              return;
            }
            $.post(url, {
              version: versionName,
              dupVersionId: dupVersionId,
              id: productBaseId
            }, function(json) {
              if (json.status !== 0) {
                $.msgBox(json.message, null, {
                  title: c18n.error,
                  width: 300,
                  height: 'auto'
                });
                return;
              }
              return (require('appmng/product_panel')).addNewProduct({
                version: versionName,
                id: json.id
              });
            });
            return $(this).dialog("close");
          }
        }, {
          text: c18n.cancel,
          click: function() {
            return $(this).dialog("close");
          }
        }
      ],
      open: function(event, ui) {
        $('#dupVersion').empty().append(util.newOption('', -1));
        return (require('appmng/product_panel')).getProductSelectOptions().appendTo($('#dupVersion'));
      },
      close: function(event, ui) {
        var errDiv;
        return errDiv = $("#productErrInfo").hide();
      }
    });
    newAppVersion = $("#newApplicationVersionDialog").dialog({
      autoOpen: false,
      height: 200,
      width: 500,
      modal: true,
      buttons: [
        {
          text: c18n.ok,
          click: function() {
            var appBaseId, dupVersionId, url, versionName;
            url = 'app/create-application';
            versionName = $('#appVersionName').val();
            dupVersionId = $("#dupDictsVersion").val();
            appBaseId = (require('appmng/navigatetree')).getNodeInfo().id;
            if (!versionName) {
              $("#appErrInfo").show();
              return;
            }
            $.post(url, {
              version: versionName,
              dupVersionId: dupVersionId,
              id: appBaseId
            }, function(json) {
              if (json.status !== 0) {
                $.msgBox(json.message, null, {
                  title: c18n.error,
                  width: 300,
                  height: 'auto'
                });
                return;
              }
              return (require('appmng/application_panel')).addNewApplication({
                version: versionName,
                id: json.id
              });
            });
            return $(this).dialog("close");
          }
        }, {
          text: c18n.cancel,
          click: function() {
            return $(this).dialog("close");
          }
        }
      ],
      open: function(event, ui) {
        return $("#dupDictsVersion").empty().append(util.newOption('', -1)).append((require('appmng/application_panel')).getApplicationSelectOptions());
      },
      close: function(event, ui) {
        return $("#appErrInfo").hide();
      }
    });
    addApplication = $("#addApplicationDialog").dialog({
      autoOpen: false,
      height: 'auto',
      width: 300,
      modal: true,
      position: "center",
      show: {
        effect: 'drop',
        direction: "up"
      },
      create: function(event, ui) {
        $("select", this).css('width', "80px");
        return $("#applicationName").change(function() {
          var appBaseId, url;
          $("#version").empty();
          appBaseId = $(this).val();
          if (-1 === parseInt(appBaseId)) {
            return;
          }
          url = "rest/applications/apps/" + appBaseId;
          return $.getJSON(url, {}, function(json) {
            return $("#version").append(util.json2Options(json)).trigger("change");
          });
        });
      },
      open: function(event, ui) {
        var productId, url,
          _this = this;
        productId = $("#selVersion").val();
        url = "rest/applications/base/" + productId;
        return $.getJSON(url, {}, function(json) {
          return $('#applicationName', _this).empty().append(util.json2Options(json, false, 'name')).trigger('change');
        });
      },
      buttons: [
        {
          text: c18n.ok,
          click: function() {
            var params, url;
            url = 'app/add-application';
            params = {
              productId: parseInt($("#selVersion").val()),
              appId: parseInt($('#version').val())
            };
            $.post(url, params, function(json) {
              if (json.status !== 0) {
                $.msgBox(json.message, null, {
                  title: c18n.error
                });
                return;
              }
              if (-1 === params.appBaseId) {
                params.appBaseId = json.appBaseId;
                (require('appmng/apptree')).addNewApplicationBase(params);
              }
              return $("#applicationGridList").trigger("reloadGrid");
            });
            return $(this).dialog("close");
          }
        }, {
          text: c18n.cancel,
          click: function() {
            return $(this).dialog("close");
          }
        }
      ]
    });
    langSettings = $('#languageSettingsDialog').dialog({
      autoOpen: false,
      modal: true,
      width: 530,
      height: 'auto',
      title: i18n.dialog.languagesettings.title,
      resize: function(event, ui) {
        return $('#languageSettingGrid').setGridWidth(ui.size.width - 35, true).setGridHeight(ui.size.height - 180, true);
      },
      /*
            Customed option for dialog open
            When user click the language action link in dictionary grid, the handler for that link will bind open event
            for this dialog and pass the related parameters to it.
            e.data will be the passed parameters
      */

      openEvent: function(e, ui) {
        var param, postData;
        param = e.data.param;
        $('#refCode').val(param.langrefcode);
        postData = {
          dict: param.id,
          format: 'grid',
          prop: 'languageCode,language.name,charset.name'
        };
        return $('#languageSettingGrid').setGridParam({
          url: 'rest/dictLanguages',
          page: 1,
          postData: postData
        }).trigger("reloadGrid");
      },
      close: function(event, ui) {
        return (require('appmng/langsetting_grid')).saveLastEditedCell();
      },
      buttons: [
        {
          text: c18n.close,
          click: function() {
            return $(this).dialog('close');
          }
        }
      ]
    });
    stringSettings = $('#stringSettingsDialog').dialog({
      autoOpen: false,
      height: 'auto',
      width: 750,
      title: i18n.dialog.stringsettings.title,
      modal: true,
      zIndex: 900,
      open: function(e, ui) {
        var param, postData;
        param = $(this).data("param");
        if (!param) {
          return;
        }
        $('#dictName', this).val(param.name);
        $('#dictVersion', this).val(param.version);
        $('#dictFormat', this).val(param.format);
        $('#dictEncoding', this).val(param.encoding);
        postData = {
          dict: param.id,
          format: 'grid',
          prop: "key,reference,maxLength,context.name,description"
        };
        return $('#stringSettingsGrid').setGridParam({
          url: 'rest/labels',
          page: 1,
          postData: postData
        }).trigger("reloadGrid");
      },
      close: function(event, ui) {
        return (require('appmng/stringsettings_grid')).saveLastEditedCell();
      },
      buttons: [
        {
          text: c18n.close,
          click: function() {
            return $(this).dialog('close');
          }
        }
      ]
    });
    $('#setContexts').button({
      icons: {
        secondary: "ui-icon-triangle-1-s"
      }
    }).on('click', {
      dialog: stringSettings
    }, function(e) {
      var menu;
      menu = $('#setContextMenu').show().position({
        my: "right bottom",
        at: "right top",
        of: this
      });
      $(document).one("click", function() {
        return menu.hide();
      });
      return false;
    });
    setContextTo = function(context, labelids) {
      if (context == null) {
        context = 'Default';
      }
      if (labelids == null) {
        labelids = $('#stringSettingsGrid').getGridParam('selarrrow');
      }
      if (labelids.length === 0) {
        $.msgBox(i18n.dialog.customcontext.labeltip, null, {
          title: c18n.warn
        });
        return;
      }
      return $.post('app/update-label', {
        id: labelids.join(','),
        context: context
      }, function(json) {
        if (json.status !== 0) {
          $.msgBox(json.message, null, {
            title: c18n.error
          });
          return;
        }
        return $('#stringSettingsGrid').trigger('reloadGrid');
      });
    };
    $('#customContext').dialog({
      autoOpen: false,
      modal: true,
      buttons: [
        {
          text: c18n.ok,
          click: function() {
            var context;
            if (!(context = $('#contextName', this).val())) {
              $('#customCtxErrorMsg').empty().html(i18n.dialog.customcontext.namerequired);
              return;
            }
            setContextTo(context);
            return $(this).dialog('close');
          }
        }, {
          text: c18n.cancel,
          click: function() {
            return $(this).dialog('close');
          }
        }
      ]
    });
    $('#setContextMenu').menu().hide().find("li").on('click', function(e) {
      if (typeof console !== "undefined" && console !== null) {
        console.log('debug');
      }
      if (e.target.name !== 'Custom') {
        setContextTo(e.target.name);
        return;
      }
      return $('#customContext').dialog('open');
    });
    dictListPreview = $('#dictListPreviewDialog').dialog({
      autoOpen: false,
      modal: true,
      zIndex: 900,
      height: 'auto',
      width: 1030,
      title: i18n.dialog.dictlistpreview.title,
      resize: function(event, ui) {
        return $('#dictListPreviewGrid').setGridWidth(ui.size.width - 55, true).setGridHeight(ui.size.height - 165, true);
      },
      buttons: [
        {
          text: i18n.dialog.dictlistpreview["import"],
          click: function() {
            var param, postData;
            param = dictListPreview.data("param");
            postData = {
              handler: param.handler,
              app: $('#selAppVersion').val()
            };
            if (grid.gridHasErrors()) {
              $.msgBox(i18n.dialog.dictlistpreview.check, null, {
                title: c18n.error
              });
              return;
            }
            dictListPreview.dialog('close');
            $.blockUI();
            return $.post('app/deliver-dict', postData, function(json) {
              var appInfo;
              $.unblockUI();
              if (json.status !== 0) {
                $.msgBox(json.message, null, {
                  title: c18n.error
                });
                return;
              }
              appInfo = "" + ($('#appDispAppName').text()) + " " + ($('#selAppVersion option:selected').text());
              $.msgBox(i18n.dialog.dictlistpreview.success.format(appInfo, null, {
                title: c18n.info
              }));
              return $('#selAppVersion').trigger('change');
            });
          }
        }
      ],
      open: function() {
        var param, postData;
        param = $(this).data('param');
        if (!param) {
          return;
        }
        postData = {
          appId: param.appId,
          format: 'grid',
          handler: param.handler,
          prop: 'languageReferenceCode,base.name,version,base.format,base.encoding,labelNum,errorCount,warningCount'
        };
        return $('#dictListPreviewGrid').setGridParam({
          url: 'rest/delivery/dict',
          page: 1,
          postData: postData
        }).trigger('reloadGrid');
      }
    });
    dictPreviewStringSettings = $('#dictPreviewStringSettingsDialog').dialog({
      autoOpen: false,
      modal: true,
      zIndex: 920,
      height: 'auto',
      width: 730,
      title: i18n.dialog.dictpreviewstringsettings.title,
      resize: function(event, ui) {
        return $('#dictPreviewStringSettingsGrid').setGridWidth(ui.size.width - 35, true).setGridHeight(ui.size.height - 210, true);
      },
      open: function() {
        var param, postData;
        param = $(this).data('param');
        if (!param) {
          return;
        }
        $('#previewDictName', this).val(param.name);
        $('#previewDictVersion', this).val(param.version);
        $('#previewDictFormat', this).val(param.format);
        $('#previewDictEncoding', this).val(param.encoding);
        postData = {
          handler: param.handler,
          dict: param.id,
          format: 'grid',
          prop: "key,reference,maxLength,context.name,description"
        };
        return $('#dictPreviewStringSettingsGrid').setGridParam({
          url: 'rest/delivery/labels',
          page: 1,
          postData: postData
        }).trigger("reloadGrid");
      },
      close: function(event, ui) {
        return (require('appmng/dictpreviewstringsettings_grid')).saveLastEditedCell();
      },
      buttons: [
        {
          text: c18n.close,
          click: function() {
            return $(this).dialog('close');
          }
        }
      ]
    });
    dictPreviewLangSettings = $('#dictPreviewLanguageSettingsDialog').dialog({
      autoOpen: false,
      modal: true,
      zIndex: 920,
      width: 530,
      height: 'auto',
      title: i18n.dialog.languagesettings.title,
      resize: function(event, ui) {
        return $('#previewLanguageSettingGrid').setGridWidth(ui.size.width - 35, true).setGridHeight(ui.size.height - 180, true);
      },
      open: function() {
        var param, postData;
        param = $(this).data('param');
        if (!param) {
          return;
        }
        $('#previewRefCode').val(param.langrefcode);
        postData = {
          handler: param.handler,
          dict: param.id,
          format: 'grid',
          prop: 'languageCode,language.name,charset.name'
        };
        return $('#previewLanguageSettingGrid').setGridParam({
          url: 'rest/delivery/dictLanguages',
          page: 1,
          postData: postData
        }).trigger("reloadGrid");
      },
      close: function(event, ui) {
        return (require('appmng/previewlangsetting_grid')).saveLastEditedCell();
      },
      buttons: [
        {
          text: c18n.close,
          click: function() {
            return $(this).dialog('close');
          }
        }
      ]
    });
    addLanguage = $('#addLanguageDialog').dialog({
      autoOpen: false,
      create: function(event, ui) {
        var _this = this;
        $.getJSON('rest/languages', {
          prop: 'id,name'
        }, function(languages) {
          return $('#languageName', _this).append("<option value='-1'>" + c18n.selecttip + "</option>").append(util.json2Options(languages, false, 'name')).change(function(e) {
            var postData;
            postData = {
              prop: 'languageCode,charset.id',
              'language': $('#languageName', _this).val(),
              dict: $(_this).data('param').dicts.join(',')
            };
            return $.post('rest/preferredCharset', postData, function(json) {
              $('#addLangCode', _this).val(json.languageCode);
              return $('#charset', _this).val(json['charset.id']);
            });
          });
        });
        return $.getJSON('rest/charsets', {
          prop: 'id,name'
        }, function(charsets) {
          return $('#charset', _this).append("<option value='-1'>" + c18n.selecttip + "</option>").append(util.json2Options(charsets, false, 'name'));
        });
      },
      open: function(event, ui) {
        $('#addLangCode', this).select();
        $('#charset', this).val('-1');
        return $('#languageName', this).val('-1');
      },
      buttons: [
        {
          text: 'Add',
          icons: {
            primary: "ui-icon-locked"
          },
          click: function(e) {
            var postData,
              _this = this;
            postData = {
              dicts: $('#addLanguageDialog').data('param').dicts.join(','),
              languageId: $('#addLanguageDialog #languageName').val(),
              charsetId: $('#addLanguageDialog #charset').val(),
              code: $('#addLanguageDialog #addLangCode').val()
            };
            $('#errorMsg', this).empty();
            if (!postData.code || '-1' === postData.languageId || '-1' === postData.charsetId) {
              if (!postData.code) {
                $('#errorMsg', this).append($("<li>" + i18n.dialog.addlanguage.coderequired + "</li>"));
              }
              if ('-1' === postData.languageId) {
                $('#errorMsg', this).append($("<li>" + i18n.dialog.addlanguage.languagetip + "</li>"));
              }
              if ('-1' === postData.charsetId) {
                $('#errorMsg', this).append($("<li>" + i18n.dialog.addlanguage.charsettip + "</li>"));
              }
              return;
            }
            return $.post('app/add-dict-language', postData, function(json) {
              if (json.status !== 0) {
                $.msgBox(json.message, null, {
                  title: c18n.error
                });
                return;
              }
              if (-1 === postData.dicts.indexOf(',')) {
                $('#languageSettingGrid').trigger("reloadGrid");
              }
              $(_this).dialog('close');
              return $.msgBox(i18n.dialog.addlanguage.successtip.format($('#languageName option:selected').text(), null, {
                title: c18n.error
              }));
            });
          }
        }, {
          text: 'Cancel',
          click: function(e) {
            return $(this).dialog('close');
          }
        }
      ]
    });
    return {
      addLanguage: addLanguage,
      dictPreviewLangSettings: dictPreviewLangSettings,
      dictPreviewStringSettings: dictPreviewStringSettings,
      dictListPreview: dictListPreview,
      stringSettings: stringSettings,
      newProductVersion: newProductVersion,
      newAppVersion: newAppVersion,
      addApplication: addApplication,
      langSettings: langSettings
    };
  });

}).call(this);
