// Generated by CoffeeScript 1.5.0
(function() {

  define(function(require) {
    var c18n, detailgrid, exportTranslationDialog, grid, i18n, languageFilterDialog, ready, refreshGrid, taskDialog, transDetailDialog, transGrid, util;
    i18n = require('i18n!nls/transmng');
    c18n = require('i18n!nls/common');
    util = require('dms-util');
    grid = require('transmng/trans_grid');
    detailgrid = require('transmng/transdetail_grid');
    transGrid = grid;
    refreshGrid = function(languageTrigger, grid) {
      var checkboxes, nodeInfo, param, type;
      if (languageTrigger == null) {
        languageTrigger = false;
      }
      if (grid == null) {
        grid = transGrid;
      }
      nodeInfo = (require('ptree')).getNodeInfo();
      type = nodeInfo.type;
      if (type.startWith('prod')) {
        type = type.slice(0, 4);
      }
      param = {
        release: {
          id: $('#selVersion', "div[id='transmng']").val(),
          version: $("#selVersion option:selected", "div[id='transmng']").text()
        },
        level: $("input:radio[name='viewOption'][checked]").val(),
        type: type,
        name: nodeInfo.text
      };
      checkboxes = $("#languageFilterDialog input:checkbox[name='languages']");
      param.languages = checkboxes.map(function() {
        if (this.checked) {
          return {
            id: this.id,
            name: this.value
          };
        }
      }).get();
      param.languageTrigger = languageTrigger;
      grid.updateGrid(param);
      return typeof console !== "undefined" && console !== null ? console.debug("transmng panel dialogs init...") : void 0;
    };
    languageFilterDialog = $("<div title='" + i18n.select.languagefilter.title + "' id='languageFilterDialog'>").dialog({
      autoOpen: false,
      position: [23, 126],
      height: 'auto',
      width: 1100,
      show: {
        effect: 'slide',
        direction: "up"
      },
      buttons: [
        {
          text: c18n.ok,
          click: function() {
            $(this).dialog("close");
            return refreshGrid(true);
          }
        }, {
          text: c18n.cancel,
          click: function() {
            return $(this).dialog("close");
          }
        }
      ]
    });
    exportTranslationDialog = $('#ExportTranslationsDialog').dialog({
      autoOpen: false,
      modal: true,
      width: 1100,
      height: 'auto',
      position: [25, 100],
      show: {
        effect: 'slide',
        direction: "down"
      },
      open: function() {
        var info, langFilterTableId, postData, tableType,
          _this = this;
        info = grid.getTotalSelectedRowInfo();
        tableType = grid.getTableType();
        langFilterTableId = "languageFilter_" + ($(this).attr('id'));
        $("#" + langFilterTableId).remove();
        postData = {
          prop: 'id,name'
        };
        postData[tableType] = info.rowIds.join(',');
        return $.getJSON('rest/languages', postData, function(languages) {
          if (languages.length > 0) {
            return $(_this).append(util.generateLanguageTable(languages, langFilterTableId));
          }
        });
      },
      buttons: [
        {
          text: c18n['export'],
          click: function() {
            var dicts, langids, languages, me;
            me = $(this);
            languages = ($(":checkbox[name='languages']", this).map(function() {
              if (this.checked) {
                return {
                  id: this.id,
                  name: this.value
                };
              }
            })).get();
            if (languages.length === 0) {
              $.msgBox(i18n.msgbox.createtranstask.msg.format(c18n.language), null, {
                title: c18n.warning
              });
              return;
            }
            langids = $(languages).map(function() {
              return this.id;
            }).get().join(',');
            dicts = $(grid.getTotalSelectedRowInfo().rowIds).map(function() {
              return this;
            }).get().join(',');
            window.location.href = "trans/export-translation-details?dict=" + dicts + "&lang=" + langids;
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
    taskDialog = $("#createTranslationTaskDialog").dialog({
      autoOpen: false,
      modal: true,
      width: 1100,
      height: 'auto',
      position: [25, 100],
      show: {
        effect: 'slide',
        direction: "down"
      },
      create: function() {},
      open: function() {
        var info, langFilterTableId, nums, postData, tableType, taskname,
          _this = this;
        info = grid.getTotalSelectedRowInfo();
        taskname = "" + ($('#versionTypeLabel', '#transmng').text()) + "_" + ($('#selVersion option:selected', '#transmng').text());
        taskname += "_" + (new Date().format('yyyyMMddhhmmss'));
        $('#taskName').val(taskname).select();
        tableType = grid.getTableType();
        nums = info.rowIds.length;
        $("#dictSelected").html("<b>" + nums + "</b>");
        if ('app' === tableType) {
          nums = -1;
        }
        $("#totalLabels").html("<b>" + info.totalLabels + "</b>");
        langFilterTableId = "languageFilter_" + ($(this).attr('id'));
        $("#" + langFilterTableId).remove();
        postData = {
          prop: 'id,name'
        };
        postData[tableType] = info.rowIds.join(',');
        return $.getJSON('rest/languages', postData, function(languages) {
          if (languages.length > 0) {
            return $(_this).append(util.generateLanguageTable(languages, langFilterTableId));
          }
        });
      },
      close: function() {
        return $('#transTaskErr').hide();
      },
      buttons: [
        {
          text: c18n.create,
          click: function() {
            var dicts, langids, languages, name;
            taskDialog = $(this);
            languages = ($(":checkbox[name='languages']", this).map(function() {
              if (this.checked) {
                return {
                  id: this.id,
                  name: this.value
                };
              }
            })).get();
            if (languages.length === 0) {
              $.msgBox(i18n.msgbox.createtranstask.msg.format(c18n.language), null, {
                title: c18n.warning
              });
              return;
            }
            name = $('#taskName').val();
            if ('' === name) {
              $('#transTaskErr').show();
              return;
            }
            langids = $(languages).map(function() {
              return this.id;
            }).get().join(',');
            dicts = $(grid.getTotalSelectedRowInfo().rowIds).map(function() {
              return this;
            }).get().join(',');
            taskDialog.parent().block();
            return $.post('task/create-task', {
              prod: $('#selVersion', '#transmng').val(),
              language: langids,
              dict: dicts,
              name: name
            }, function(json) {
              taskDialog.parent().unblock();
              if (json.status !== 0) {
                $.msgBox(json.message, null, {
                  title: c18n.error
                });
                return;
              }
              $.msgBox(i18n.msgbox.createtranstask.confirm, (function(keyPressed) {
                if (c18n.yes !== keyPressed) {
                  $("#transGrid").trigger('reloadGrid');
                  return;
                }
                return $("span[id^='nav'][value='taskmng']").trigger('click');
              }), {
                title: c18n.confirm
              }, [c18n.yes, c18n.no]);
              return taskDialog.dialog("close");
            });
          }
        }, {
          text: c18n.cancel,
          click: function() {
            return $(this).dialog("close");
          }
        }
      ]
    });
    transDetailDialog = $('#translationDetailDialog').dialog({
      autoOpen: false,
      width: 860,
      height: 'auto',
      modal: true,
      open: function() {
        $('#searchAction', this).position({
          my: 'left center',
          at: 'right center',
          of: '#searchText'
        });
        return $('#detailLanguageSwitcher').trigger("change");
      },
      create: function() {
        var postData, transDetailGrid,
          _this = this;
        $(this).dialog('option', 'width', $('#transDetailGridList').getGridParam('width') + 60);
        transDetailGrid = $("#transDetailGridList");
        postData = transDetailGrid.getGridParam('postData');
        $('#transDetailSearchText', this).keydown(function(e) {
          if (e.which === 13) {
            return $('#transDetailSearchAction', _this).trigger('click');
          }
        });
        $('#transDetailSearchAction', this).attr('title', 'Search').button({
          text: false,
          icons: {
            primary: "ui-icon-search"
          }
        }).click(function() {
          postData.text = $('#transDetailSearchText', _this).val();
          return transDetailGrid.trigger('reloadGrid');
        }).height(20).width(20);
        $('#transSameWithRef', this).change(function(e) {
          postData.nodiff = this.checked;
          return transDetailGrid.trigger('reloadGrid');
        });
        return $('#detailLanguageSwitcher').change(function() {
          var language, param;
          param = $('#translationDetailDialog').data("param");
          language = {
            id: $(this).val(),
            name: $("option:selected", this).text()
          };
          return detailgrid.languageChanged({
            language: language,
            dict: param.dict,
            searchStatus: param.searchStatus
          });
        });
      },
      close: function(event, ui) {
        var postData;
        detailgrid.saveLastEditedCell();
        postData = $("#transDetailGridList").getGridParam('postData');
        $('#transSameWithRef', this).attr('checked', false);
        delete postData.nodiff;
        $('#searchText', this).val("");
        return delete postData.text;
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
    ready = function() {
      return typeof console !== "undefined" && console !== null ? console.debug("transmng panel dialogs ready...") : void 0;
    };
    ready();
    return {
      taskDialog: taskDialog,
      languageFilterDialog: languageFilterDialog,
      transDetailDialog: transDetailDialog,
      exportTranslationDialog: exportTranslationDialog,
      refreshGrid: refreshGrid,
      showTransDetailDialog: function(param) {
        var map, status;
        $('#dictionaryName', transDetailDialog).html(param.dict.name);
        $('#detailLanguageSwitcher', transDetailDialog).empty().append(util.json2Options(param.languages, param.language.id, 'name'));
        map = {
          'N': '0',
          'I': '1',
          'T': '2'
        };
        status = param.language.name.split('.')[1];
        $('#translationDetailDialog').data('param', {
          dict: param.dict,
          searchStatus: map[status]
        });
        return transDetailDialog.dialog("open");
      }
    };
  });

}).call(this);
