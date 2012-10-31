// Generated by CoffeeScript 1.3.3
(function() {

  define(['require', 'appmng/dictlistpreview_grid', 'appmng/dictpreviewstringsettings_grid', 'appmng/previewlangsetting_grid'], function(require, grid, sgrid, lgrid) {
    var $, addApplication, c18n, dictListPreview, dictPreviewLangSettings, dictPreviewStringSettings, i18n, ids, langSettings, newAppVersion, newProductVersion, stringSettings;
    $ = require('jqueryui');
    c18n = require('i18n!nls/common');
    i18n = require('i18n!nls/appmng');
    require('blockui');
    require('jqmsgbox');
    ids = {
      button: {
        new_product: 'newProduct'
      },
      dialog: {
        new_product: 'newProductDialog',
        new_product_release: 'newProductReleaseDialog',
        new_or_add_application: 'addApplicationDialog'
      },
      productName: '#productName',
      product_duplication: '#dupVersion'
    };
    newProductVersion = $("#" + ids.dialog.new_product_release).dialog({
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
        $(ids.product_duplication).empty().append(new Option('', -1));
        return (require('appmng/product_panel')).getProductSelectOptions().appendTo($(ids.product_duplication));
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
            url = '/app/create-application';
            versionName = $('#appVersionName').val();
            dupVersionId = $("#dupDictsVersion").val();
            appBaseId = (require('appmng/navigatetree')).getNodeInfo().id;
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
        return $("#dupDictsVersion").empty().append(new Option('', -1)).append((require('appmng/application_panel')).getApplicationSelectOptions());
      }
    });
    addApplication = $("#" + ids.dialog.new_or_add_application).dialog({
      autoOpen: false,
      height: 'auto',
      width: 'auto',
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
            return $("#version").append($(json).map(function() {
              return new Option(this.version, this.id);
            })).trigger("change");
          });
        });
      },
      open: function(event, ui) {
        var productId, url,
          _this = this;
        productId = $("#selVersion").val();
        console.log(productId);
        url = "rest/applications/base/" + productId;
        return $.getJSON(url, {}, function(json) {
          return $('#applicationName', _this).empty().append($(json).map(function() {
            return new Option(this.name, this.id);
          })).trigger('change');
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
      zIndex: 900,
      width: 'auto',
      height: 'auto',
      title: i18n.dialog.languagesettings.title,
      open: function(e, ui) {
        var param, postData;
        param = $(this).data("param");
        $('#refCode').val(param.langrefcode);
        postData = {
          dict: param.id,
          format: 'grid',
          prop: 'languageCode,language.name,charset.name'
        };
        return $('#languageSettingGrid').setGridParam({
          url: '/rest/dictLanguages',
          postData: postData
        }).trigger("reloadGrid");
      }
    });
    stringSettings = $('#stringSettingsDialog').dialog({
      autoOpen: false,
      width: 'auto',
      height: 'auto',
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
          url: '/rest/labels',
          postData: postData
        }).trigger("reloadGrid");
      }
    });
    dictListPreview = $('#dictListPreviewDialog').dialog({
      autoOpen: false,
      modal: true,
      zIndex: 900,
      width: 'auto',
      height: 'auto',
      title: i18n.dialog.dictlistpreview.title,
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
            $.blockUI({
              css: {
                backgroundColor: '#fff'
              },
              overlayCSS: {
                opacity: 0.2
              }
            });
            return $.post('/app/deliver-dict', postData, function(json) {
              var appInfo;
              $.unblockUI();
              if (json.status !== 0) {
                return;
                $.msgBox(json.message, null, {
                  title: c18n.error
                });
              }
              appInfo = "" + ($('#appDispAppName').text()) + " " + ($('#selAppVersion option:selected').text());
              return $.msgBox(i18n.dictlistpreview.success.format(appInfo, null, {
                title: c18n.info
              }));
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
          url: '/rest/delivery/dict',
          postData: postData
        }).trigger('reloadGrid');
      }
    });
    dictPreviewStringSettings = $('#dictPreviewStringSettingsDialog').dialog({
      autoOpen: false,
      modal: true,
      zIndex: 920,
      width: 'auto',
      height: 'auto',
      title: i18n.dialog.dictpreviewstringsettings.title,
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
          url: '/rest/delivery/labels',
          postData: postData
        }).trigger("reloadGrid");
      }
    });
    dictPreviewLangSettings = $('#dictPreviewLanguageSettingsDialog').dialog({
      autoOpen: false,
      modal: true,
      zIndex: 920,
      width: 'auto',
      height: 'auto',
      title: i18n.dialog.languagesettings.title,
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
          url: '/rest/delivery/dictLanguages',
          postData: postData
        }).trigger("reloadGrid");
      }
    });
    return {
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
