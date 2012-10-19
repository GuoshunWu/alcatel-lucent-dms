// Generated by CoffeeScript 1.3.3
(function() {

  define(['require', 'appmng/dictlistpreview_grid', 'appmng/dictpreviewstringsettings_grid', 'appmng/previewlangsetting_grid'], function(require, grid, sgrid, lgrid) {
    var $, c18n, dictListPreview, dictPreviewLangSettings, dictPreviewStringSettings, i18n, ids, langSettings, newOrAddApplication, newProduct, newProductRelease, stringSettings,
      _this = this;
    $ = require('jqueryui');
    c18n = require('i18n!nls/common');
    i18n = require('i18n!nls/appmng');
    require('blockui');
    require('jqmsgbox');
    ids = {
      button: {
        new_product: 'newProduct',
        new_release: 'newVersion'
      },
      dialog: {
        new_product: 'newProductDialog',
        new_product_release: 'newProductReleaseDialog',
        new_or_add_application: 'newOrAddApplicationDialog'
      },
      productName: '#productName',
      product_duplication: '#dupVersion'
    };
    newProduct = $("#" + ids.dialog.new_product).dialog({
      autoOpen: false,
      height: 200,
      width: 400,
      modal: true,
      buttons: [
        {
          text: c18n.ok,
          click: function() {
            $.post('app/create-product', {
              name: $(ids.productName).val()
            }, function(json) {
              if (json.status !== 0) {
                $.msgBox(json.message, null, {
                  title: c18n.error,
                  width: 300,
                  height: 'auto'
                });
                return false;
              }
              return appptree.addNewProductBase({
                name: $(ids.productName).val(),
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
      ]
    });
    $("#" + ids.button.new_product).button().click(function(e) {
      return newProduct.dialog("open");
    });
    newProductRelease = $("#" + ids.dialog.new_product_release).dialog({
      autoOpen: false,
      height: 200,
      width: 500,
      modal: true,
      buttons: [
        {
          text: c18n.ok,
          click: function() {
            var dupVersionId, productBaseId, url, versionName;
            url = 'app/create-product-release';
            versionName = $('#versionName').val();
            dupVersionId = $("#dupVersion").val();
            productBaseId = (require('appmng/apptree')).getSelected().id;
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
        $(ids.product_duplication).append(new Option('', -1));
        return (require('appmng/product_panel')).getProductSelectOptions().appendTo($(ids.product_duplication));
      }
    });
    $("#" + ids.button.new_release).button({
      text: false,
      icons: {
        primary: "ui-icon-plus"
      }
    }).click(function(e) {
      return newProductRelease.dialog("open");
    });
    newOrAddApplication = $("#" + ids.dialog.new_or_add_application).dialog({
      autoOpen: false,
      height: 200,
      width: 400,
      modal: true,
      position: "center",
      show: {
        effect: 'drop',
        direction: "up"
      },
      create: function(event, ui) {
        var input;
        input = $('<input>').insertAfter($('#applicationName')).hide();
        $('#applicationName').data('myinput', input);
        input = $('<input>').insertAfter($("#version")).hide();
        $('#version').data('myinput', input);
        $("select", this).css('width', "80px");
        $("#applicationName").change(function() {
          var appBaseId, url;
          $("#version").empty().append(new Option('new', -1));
          appBaseId = $(this).val();
          if (-1 === parseInt(appBaseId)) {
            $(this).data('myinput').val("").show();
            $("#version").trigger("change");
            return;
          }
          $(this).data('myinput').hide();
          url = "rest/applications/apps/" + appBaseId;
          return $.getJSON(url, {}, function(json) {
            return $("#version").append($(json).map(function() {
              return new Option(this.version, this.id);
            })).trigger("change");
          });
        });
        return $("#version").change(function() {
          var appId;
          appId = $(this).val();
          if (-1 === parseInt(appId)) {
            $(this).data('myinput').val("").show();
            return;
          }
          return $(this).data('myinput').hide();
        });
      },
      open: function(event, ui) {
        var productId, url;
        productId = $("#selVersion").val();
        url = "rest/applications/base/" + productId;
        return $.getJSON(url, {}, function(json) {
          var appBasesOptions;
          appBasesOptions = $("#newOrAddApplicationDialog").find("#applicationName").empty().append(new Option('new', -1));
          return appBasesOptions.append($(json).map(function() {
            return new Option(this.name, this.id);
          })).trigger('change');
        });
      },
      buttons: [
        {
          text: c18n.ok,
          click: function() {
            var params, url;
            url = 'app/create-or-add-application';
            params = {
              productId: parseInt($("#selVersion").val()),
              appBaseId: parseInt($('#applicationName').val()),
              appId: parseInt($('#version').val()),
              appBaseName: $('#applicationName').data('myinput').val(),
              appVersion: $('#version').data('myinput').val()
            };
            $.post(url, params, function(json) {
              if (json.status !== 0) {
                $.msgBox(json.message, null, {
                  title: c18n.error
                });
                return;
              }
              if (-1 === params.appBaseId) {
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
      width: 'auto',
      height: 'auto',
      title: i18n.dialog.dictlistpreview.title,
      open: function() {
        var param, postData;
        param = $(this).data('param');
        if (!param) {
          return;
        }
        postData = {
          format: 'grid',
          handler: param.handler,
          prop: 'languageReferenceCode,base.name,version,base.format,base.encoding,labelNum'
        };
        return $('#dictListPreviewGrid').setGridParam({
          url: '/rest/delivery/dict',
          postData: postData
        }).trigger('reloadGrid');
      }
    });
    $('#import', dictListPreview).button({}).click(function() {
      var param, postData;
      param = dictListPreview.data("param");
      postData = {
        handler: param.handler,
        app: $('#selAppVersion').val()
      };
      $.blockUI({
        css: {
          backgroundColor: '#fff'
        },
        overlayCSS: {
          opacity: 0.2
        }
      });
      return $.post('/app/deliver-dict', postData, function(json) {
        $.unblockUI();
        console.log(json);
        if (json.status !== 0) {
          $.msgBox(json.message, null, {
            title: c18n.error
          });
          return;
        }
        return alert('Import successful.');
      });
    });
    dictPreviewStringSettings = $('#dictPreviewStringSettingsDialog').dialog({
      autoOpen: false,
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
      newProduct: newProduct,
      newProductRelease: newProductRelease,
      newOrAddApplication: newOrAddApplication,
      langSettings: langSettings
    };
  });

}).call(this);
