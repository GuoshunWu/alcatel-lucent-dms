// Generated by CoffeeScript 1.6.2
(function() {
  define(['jqueryui', 'jqgrid', 'blockui', 'jqmsgbox', 'i18n!nls/common', 'i18n!nls/appmng', 'dms-urls', 'dms-util', 'appmng/dictlistpreview_grid', 'appmng/stringsettings_grid', 'appmng/report_chart', 'appmng/dictpreviewstringsettings_grid', 'appmng/previewlangsetting_grid', 'appmng/searchtext_grid'], function($, jqgrid, blockui, msgbox, c18n, i18n, urls, util, previewgrid, stgrid, chart) {
    var addApplication, addLanguage, dictListPreview, dictPreviewLangSettings, dictPreviewStringSettings, historyDlg, importReport, langSettings, lockLabels, newAppVersion, newProductVersion, searchResult, setContextTo, showSearchResult, stringSettings, stringSettingsTranslation;

    newProductVersion = $("#newProductReleaseDialog").dialog({
      autoOpen: false,
      height: 200,
      width: 500,
      modal: true,
      buttons: [
        {
          text: c18n.ok,
          click: function() {
            var dupVersionId, productBaseId, url, versionName;

            url = urls.product.create_version;
            versionName = $('#versionName').val();
            dupVersionId = $("#dupVersion").val();
            productBaseId = util.getProductTreeInfo().id;
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
            appBaseId = util.getProductTreeInfo().id;
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
              $('#selAppVersion').append("<option value='" + json.id + "' selected>" + versionName + "</option>").trigger('change');
              if (!json.productBaseId) {
                return;
              }
              return $('#addNewApplicationVersionToProductVersionDialog').data("param", json).dialog('open');
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
        return $("#dupDictsVersion").empty().append(util.newOption('', -1)).append($('#selAppVersion').children('option').clone(true));
      },
      close: function(event, ui) {
        return $("#appErrInfo").hide();
      }
    });
    $('#addNewApplicationVersionToProductVersionDialog').dialog({
      autoOpen: false,
      width: 350,
      modal: true,
      open: function() {
        var param;

        param = $(this).data('param');
        $('#productBaseName', this).text(param.productBaseName);
        return $('#productVersions', this).empty().append(util.json2Options(param.versions));
      },
      buttons: [
        {
          text: c18n.ok,
          click: function() {
            var params, url;

            url = 'app/add-application';
            params = {
              productId: $('#productVersions', this).val(),
              appId: ($(this).data('param')).id
            };
            $.post(url, params, function(json) {
              if (json.status !== 0) {
                $.msgBox(json.message, null, {
                  title: c18n.error
                });
              }
            });
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
          if (!appBaseId || -1 === parseInt(appBaseId)) {
            return;
          }
          url = "rest/applications/apps/" + appBaseId;
          return $.getJSON(url, {}, function(json) {
            return $("#version").append(util.json2Options(json)).trigger("change");
          });
        });
      },
      open: function(event, ui) {
        var productId,
          _this = this;

        productId = $("#selVersion").val();
        return $.getJSON("rest/applications/base/" + productId, {}, function(json) {
          var options;

          options = util.json2Options(json, false, 'name');
          if (!options) {
            $(_this).dialog('close');
            $.msgBox(i18n.dialog.addapplication.tip, null, {
              title: c18n.warn
            });
            return;
          }
          return $('#applicationName', _this).empty().append(options).trigger('change');
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
      title: i18n.dialog.languagesettings.title,
      width: 540,
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
    lockLabels = function(lock) {
      var grid;

      if (lock == null) {
        lock = true;
      }
      grid = $('#stringSettingsGrid');
      alert('Hello');
    };
    stringSettings = $('#stringSettingsDialog').dialog({
      autoOpen: false,
      title: i18n.dialog.stringsettings.title,
      modal: true,
      width: 910,
      height: 605,
      create: function(e, ui) {
        var _this = this;

        $('#searchText', this).keydown(function(e) {
          if (e.which !== 13) {
            return true;
          }
          $('#searchAction', _this).trigger('click');
          return false;
        });
        $('#searchAction', this).attr('title', 'Search').button({
          text: false,
          icons: {
            primary: "ui-icon-search"
          }
        }).click(function() {
          var grid;

          grid = $('#stringSettingsGrid');
          grid.getGridParam('postData').text = $('#searchText', _this).val();
          return grid.trigger('reloadGrid');
        }).height(20).width(20);
        $('#makeStringSettingsLabelTranslateStatus').button({
          icons: {
            primary: "ui-icon-triangle-1-n",
            secondary: "ui-icon-gear"
          }
        }).attr('privilegeName', util.urlname2Action(urls.app.update_label_status)).click(function(e) {
          var menu;

          menu = $('#stringSettingsTranslationStatus').show().width($(this).width()).position({
            my: "left bottom",
            at: "left top",
            of: this
          });
          $(document).one("click", function() {
            return menu.hide();
          });
          return false;
        });
        return $('#stringSettingsTranslationStatus').menu().hide().find("li").on('click', function(e) {
          var grid, ids;

          grid = $("#stringSettingsGrid");
          ids = grid.getGridParam('selarrrow');
          return $.post(urls.app.update_label_status, {
            type: 'trans',
            transStatus: e.target.name,
            id: ids.join(',')
          }, function(json) {
            if (json.status !== 0) {
              $.msgBox(json.message, null, {
                title: c18n.warning
              });
              return;
            }
            return grid.trigger('reloadGrid');
          });
        });
      },
      open: function(e, ui) {
        var param, postData;

        stgrid.lockLabels();
        $('#searchAction', this).position({
          my: 'left center',
          at: 'right center',
          of: '#searchText'
        });
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
          prop: "key,reference,t,n,i,maxLength,context.name,description"
        };
        return $('#stringSettingsGrid').setGridParam({
          url: 'rest/labels',
          page: 1,
          postData: postData
        }).trigger("reloadGrid");
      },
      close: function(event, ui) {
        var postData;

        postData = $('#stringSettingsGrid').getGridParam('postData');
        $('#transSameWithRef', this).attr('checked', false);
        delete postData.nodiff;
        $('#searchText', this).val("");
        delete postData.text;
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
      create: function() {
        $('#setContextMenu').menu().hide().find("li").on('click', function(e) {
          if (e.target.name !== 'Custom') {
            setContextTo(e.target.name);
            return;
          }
          return $('#customContext').dialog('open');
        });
        $('#setContexts').attr('privilegeName', util.urlname2Action('app/update-label')).button({
          icons: {
            primary: 'ui-icon-gear',
            secondary: "ui-icon-triangle-1-n"
          }
        }).click(function(e) {
          var menu;

          menu = $('#setContextMenu').show().width($(this).width() - 3).position({
            my: "right bottom",
            at: "right top",
            of: this
          });
          $(document).one("click", function() {
            return menu.hide();
          });
          return false;
        });
        return $('#setContextMenu').menu().hide().find("li").on('click', function(e) {
          if (e.target.name !== 'Custom') {
            setContextTo(e.target.name);
            return;
          }
          return $(this).dialog('open');
        });
      },
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
      ],
      close: function() {}
    });
    dictListPreview = $('#dictListPreviewDialog').dialog({
      autoOpen: false,
      modal: true,
      zIndex: 900,
      title: i18n.dialog.dictlistpreview.title,
      create: function() {
        return $(this).dialog('option', 'width', $('#dictListPreviewGrid').getGridParam('width') + 40);
      },
      buttons: [
        {
          text: i18n.dialog.dictlistpreview['import'],
          click: function() {
            var param, pb, postData;

            param = dictListPreview.data("param");
            postData = {
              handler: param.handler,
              app: $('#selAppVersion').val()
            };
            if (previewgrid.gridHasErrors()) {
              $.msgBox(i18n.dialog.dictlistpreview.check, null, {
                title: c18n.error
              });
              return;
            }
            dictListPreview.dialog('close');
            pb = util.genProgressBar();
            $.blockUI({
              message: ''
            });
            return util.updateProgress(urls.app.deliver_dict, postData, function(json) {
              var retJson;

              $.unblockUI();
              pb.parent().remove();
              retJson = $.parseJSON(json.event.msg);
              $('#importReportDialog').data('params', retJson).dialog('open');
              return $('#selAppVersion').trigger('change');
            }, pb);
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
      title: i18n.dialog.dictpreviewstringsettings.title,
      create: function() {
        return $(this).dialog('option', 'width', $('#dictPreviewStringSettingsGrid').getGridParam('width') + 40);
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
      title: i18n.dialog.languagesettings.title,
      open: function() {
        var param, postData;

        $(this).dialog('option', 'width', $('#previewLanguageSettingGrid').getGridParam('width') + 40);
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

        return $('#languageName', this).change(function(e) {
          var postData;

          postData = {
            prop: 'languageCode,charset.id',
            'language': $('#languageName', _this).val(),
            dict: $(_this).data('param').dicts.join(',')
          };
          $.post('rest/preferredCharset', postData, function(json) {
            $('#addLangCode', _this).val(json.languageCode);
            return $('#charset', _this).val(json['charset.id']);
          });
          return $.getJSON('rest/charsets', {
            prop: 'id,name'
          }, function(charsets) {
            return $('#charset', _this).append("<option value='-1'>" + c18n.selecttip + "</option>").append(util.json2Options(charsets, false, 'name'));
          });
        });
      },
      open: function(event, ui) {
        var _this = this;

        $.getJSON('rest/languages', {
          prop: 'id,name'
        }, function(languages) {
          return $('#languageName', _this).append("<option value='-1'>" + c18n.selecttip + "</option>").append(util.json2Options(languages, false, 'name')).trigger('change');
        });
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
    stringSettingsTranslation = $('#stringSettingsTranslationDialog').dialog({
      autoOpen: false,
      modal: true,
      width: 840,
      open: function(event, ui) {
        var param;

        param = $(this).data('param');
        if (!param) {
          return;
        }
        return $('#stringSettingsTranslationGrid').setGridParam({
          url: 'rest/label/translation',
          page: 1,
          postData: {
            label: param.id,
            format: 'grid',
            status: param.status,
            prop: 'languageCode,language.name,translation'
          }
        }).setCaption(i18n.dialog.stringsettingstrans.caption.format(param.key, param.ref)).trigger("reloadGrid");
      },
      buttons: [
        {
          text: c18n.close,
          click: function(e) {
            return $(this).dialog('close');
          }
        }
      ]
    });
    historyDlg = $('#historyDialog').dialog({
      autoOpen: false,
      modal: true,
      width: 845,
      open: function(event, ui) {
        var param;

        param = $(this).data('param');
        if (!param) {
          return;
        }
        return $('#historyGrid').setGridParam({
          url: 'rest/dictHistory',
          page: 1,
          postData: {
            dict: param.id,
            format: 'grid',
            status: param.status,
            prop: 'operationTime,operationType,task.name,operator.name'
          }
        }).setCaption(i18n.dialog.history.caption.format(param.name)).trigger("reloadGrid");
      },
      buttons: [
        {
          text: c18n.close,
          click: function(e) {
            return $(this).dialog('close');
          }
        }
      ]
    });
    $('#addLabelDialog').dialog({
      autoOpen: false,
      modal: true,
      width: 500,
      create: function() {
        this.addHandler = function(me) {
          var errMsg, postData, val, _i, _len, _ref;

          postData = $(me).data('param');
          errMsg = [];
          _ref = ['key', 'reference', 'maxLength', 'context', 'description'];
          for (_i = 0, _len = _ref.length; _i < _len; _i++) {
            val = _ref[_i];
            postData[val] = $("#" + val, me).val();
            if (val === 'maxLength' || val === 'description') {
              continue;
            }
            if (!$("#" + val, me).val()) {
              errMsg.push(c18n.required.format($("label[for='" + val + "']", me).text().trim().slice(0, -1)));
            }
          }
          if (errMsg.length > 0) {
            $('#errMsg', me).html("<hr/><ul><li>" + (errMsg.join('</li><li>')) + "</li></ul>");
            return false;
          }
          $.post(urls.label.create, postData, function(json) {
            if (json.status !== 0) {
              $.msgBox(json.message, null, {
                title: c18n.error
              });
              return;
            }
            return $('#stringSettingsGrid').trigger("reloadGrid");
          });
          $('#errMsg', me).empty();
          return $('#' + ['key', 'reference', 'maxLength', 'description'].join(', #'), me).val('');
        };
        return true;
      },
      open: function() {
        $('#errMsg', this).empty();
        return $('#' + ['key', 'reference', 'maxLength', 'description'].join(', #'), this).val('');
      },
      buttons: [
        {
          text: i18n.dialog.stringsettings.add,
          click: function() {
            return this.addHandler(this);
          }
        }, {
          text: i18n.dialog.stringsettings.addandclose,
          click: function() {
            if (this.addHandler(this)) {
              return $(this).dialog("close");
            }
          }
        }, {
          text: c18n.cancel,
          click: function() {
            return $(this).dialog("close");
          }
        }
      ]
    });
    searchResult = $('#searchTextDialog').dialog({
      autoOpen: false,
      modal: true,
      width: 920,
      open: function() {
        var grid, node, params, postData, typeText;

        params = $(this).data('params');
        node = util.getProductTreeInfo();
        typeText = 'prod' === node.type ? 'product' : 'application';
        grid = $('#searchTextGrid');
        postData = grid.getGridParam('postData');
        postData.format = 'grid';
        postData.text = params.text;
        postData.prop = 'dictionary.base.applicationBase.name,dictionary.base.name,key,reference,maxLength,context.name,t,n,i';
        delete postData.app;
        delete postData.prod;
        postData[node.type] = params.version.id;
        return grid.setCaption(i18n.dialog.searchtext.caption.format(params.text, typeText, node.text, params.version.text)).setGridParam({
          url: urls.labels
        }).trigger('reloadGrid');
      },
      buttons: [
        {
          text: c18n.close,
          click: function() {
            return $(this).dialog("close");
          }
        }
      ]
    });
    importReport = $('#importReportDialog').dialog({
      autoOpen: false,
      width: 600,
      modal: true,
      open: function() {
        var appInfo, json, msg, statisticsTabId, title;

        msg = "{\n\"dictNum\": 5,\n\"labelNum\": 247,\n\"translationNum\": 5435,\n\"translationWC\": 34141,\n\"distinctTranslationNum\": 4503,\n\"distinctTranslationWC\": 30813,\n\"untranslatedNum\": 299,\n\"untranslatedWC\": 1301,\n\"translatedNum\": 4204,\n\"translatedWC\": 29512,\n\"matchedNum\": 391,\n\"matchedWC\": 2656\n}";
        json = $.parseJSON(msg);
        json = $(this).data('params');
        appInfo = ("" + ($('#appDispAppName').text()) + " " + ($('#selAppVersion option:selected').text())).trim();
        if (!appInfo) {
          appInfo = 'Demo 1.0';
        }
        statisticsTabId = '#importReportStatistics';
        json.translatedNum -= json.matchedNum;
        json.translatedWC -= json.matchedWC;
        json.untranslatedNum += json.matchedNum;
        json.untranslatedWC += json.matchedWC;
        $('#dupTrans', statisticsTabId).html(json.translationNum - json.distinctTranslationNum).parent().next().children('span').html("" + (json.translationWC - json.distinctTranslationWC));
        $('#distinctTrans1', statisticsTabId).html(json.distinctTranslationNum).parent().next().children('span').html("" + json.distinctTranslationWC);
        $('#totalTrans', statisticsTabId).html(json.translationNum).parent().next().children('span').html("" + json.translationWC);
        $('#dupRatio', statisticsTabId).html(((1 - json.distinctTranslationNum / json.translationNum) * 100).toFixed(2) + '%').parent().next().children('span').html("" + (((1 - json.distinctTranslationWC / json.translationWC) * 100).toFixed(2)) + "%");
        $('#distinctTrans2', statisticsTabId).html(json.distinctTranslationNum).parent().next().children('span').html("" + json.distinctTranslationWC);
        $('#translated', statisticsTabId).html(json.translatedNum).parent().next().children('span').html("" + json.translatedWC);
        $('#untranslated', statisticsTabId).html(json.untranslatedNum).parent().next().children('span').html("" + json.untranslatedWC);
        $('#transRatio', statisticsTabId).html((json.translatedNum / json.distinctTranslationNum * 100).toFixed(2) + '%').parent().next().children('span').html("" + ((json.translatedWC / json.distinctTranslationWC * 100).toFixed(2)) + "%");
        $('#untranslated1', statisticsTabId).html(json.untranslatedNum).parent().next().children('span').html("" + json.untranslatedWC);
        $('#autoTrans', statisticsTabId).html(json.matchedNum).parent().next().children('span').html("" + json.matchedWC);
        $('#noMatch', statisticsTabId).html(json.untranslatedNum - json.matchedNum).parent().next().children('span').html("" + (json.untranslatedWC - json.matchedWC));
        $('#autoRatio', statisticsTabId).html((json.matchedNum / json.untranslatedNum * 100).toFixed(2) + '%').parent().next().children('span').html("" + ((json.matchedWC / json.untranslatedWC * 100).toFixed(2)) + "%");
        appInfo = ("" + ($('#appDispAppName').text()) + " " + ($('#selAppVersion option:selected').text())).trim();
        if (!appInfo) {
          appInfo = 'Demo 1.0';
        }
        title = i18n.dialog.dictlistpreview.success.format(json.labelNum, json.dictNum, appInfo);
        $('#title', this).html(title);
        return chart.showChart(title, json);
      },
      buttons: [
        {
          text: c18n.ok,
          click: function() {
            return $(this).dialog("close");
          }
        }
      ]
    });
    showSearchResult = function(params) {
      return searchResult.data('params', params).dialog('open');
    };
    return {
      addLanguage: addLanguage,
      dictPreviewLangSettings: dictPreviewLangSettings,
      dictPreviewStringSettings: dictPreviewStringSettings,
      dictListPreview: dictListPreview,
      stringSettings: stringSettings,
      newProductVersion: newProductVersion,
      newAppVersion: newAppVersion,
      addApplication: addApplication,
      langSettings: langSettings,
      stringSettingsTranslation: stringSettingsTranslation,
      historyDlg: historyDlg,
      showSearchResult: showSearchResult
    };
  });

}).call(this);
