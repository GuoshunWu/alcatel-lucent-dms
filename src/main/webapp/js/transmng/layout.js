// Generated by CoffeeScript 1.4.0
(function() {

  define(['jqlayout', 'blockui', 'jqmsgbox', 'i18n!nls/common', 'i18n!nls/transmng', 'transmng/trans_grid', 'transmng/transdetail_grid', 'util', 'require'], function($, blockui, msgbox, c18n, i18n, grid, detailgrid, util) {
    var createButtons, createDialogs, createSelects, debugIntervalHandler, dialogs, exportAppOrDicts, ids, initPage, refreshGrid;
    util = require('util');
    ids = {
      languageFilterTableId: 'languageFilterTable',
      languageFilterDialogId: 'languageFilterDialog'
    };
    dialogs = null;
    refreshGrid = function(languageTrigger) {
      var checkboxes, param;
      if (languageTrigger == null) {
        languageTrigger = false;
      }
      param = {
        release: {
          id: $('#productRelease').val(),
          version: $("#productRelease option:selected").text()
        },
        level: $("input:radio[name='viewOption'][checked]").val()
      };
      checkboxes = $("#" + ids.languageFilterDialogId + " input:checkbox[name='languages']");
      param.languages = checkboxes.map(function() {
        if (this.checked) {
          return {
            id: this.id,
            name: this.value
          };
        }
      }).get();
      param.languageTrigger = languageTrigger;
      return grid.productReleaseChanged(param);
    };
    createDialogs = function() {
      var exportTranslationDialog, languageFilterDialog, taskDialog, transDetailDialog;
      languageFilterDialog = $("<div title='" + i18n.select.languagefilter.title + "' id='" + ids.languageFilterDialogId + "'>").dialog({
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
          taskname = "" + ($('#productBase option:selected').text()) + "_" + ($('#productRelease option:selected').text());
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
                prod: $('#productRelease').val(),
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
                  $('#pageNavigator').val('taskmng.jsp');
                  return $('#naviForm').submit();
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
          return $('#transSameWithRef', this).attr('checked', false);
        },
        create: function() {
          var postData, transDetailGrid,
            _this = this;
          $(this).dialog('option', 'width', $('#transDetailGridList').getGridParam('width') + 60);
          transDetailGrid = $("#transDetailGridList");
          postData = transDetailGrid.getGridParam('postData');
          $('#searchText', this).keydown(function(e) {
            if (e.which === 13) {
              return $('#searchAction', _this).trigger('click');
            }
          });
          $('#searchAction', this).attr('title', 'Search').button({
            text: false,
            icons: {
              primary: "ui-icon-search"
            }
          }).click(function() {
            postData.text = $('#searchText', _this).val();
            return transDetailGrid.trigger('reloadGrid');
          }).height(20).width(20);
          $('#transSameWithRef', this).change(function(e) {
            postData.nodiff = this.checked;
            if (typeof console !== "undefined" && console !== null) {
              console.debug(transDetailGrid.getGridParam('postData'));
            }
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
          return detailgrid.saveLastEditedCell();
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
      return {
        taskDialog: taskDialog,
        languageFilterDialog: languageFilterDialog,
        transDetailDialog: transDetailDialog,
        exportTranslationDialog: exportTranslationDialog
      };
    };
    debugIntervalHandler = function() {
      return console.log('Hello world.');
    };
    createSelects = function() {
      $('#productBase').change(function() {
        $('#productRelease').empty();
        if (parseInt($('#productBase').val()) === -1) {
          return false;
        }
        return $.getJSON("rest/products/version", {
          base: $(this).val(),
          prop: 'id,version'
        }, function(json) {
          $('#productRelease').append(util.newOption(c18n.select.release.tip, -1));
          $('#productRelease').append(util.json2Options(json, json[json.length - 1].id));
          return $('#productRelease').trigger("change");
        });
      });
      $('#productRelease').change(function() {
        if (-1 === parseInt(this.value)) {
          return;
        }
        $.ajax({
          url: "rest/languages",
          async: false,
          data: {
            prod: this.value,
            prop: 'id,name'
          },
          dataType: 'json',
          success: function(languages) {
            var langTable;
            langTable = util.generateLanguageTable(languages);
            return $("#languageFilterDialog").empty().append(langTable);
          }
        });
        return refreshGrid();
      });
      return $('#productRelease').trigger('change');
    };
    createButtons = function(dialogs) {
      $("#create").button().attr('privilegeName', util.urlname2Action('task/create-task')).click(function() {
        var info;
        info = grid.getTotalSelectedRowInfo();
        if (!info.rowIds.length) {
          $.msgBox(c18n.selrow.format(c18n[grid.getTableType()]), null, {
            title: c18n.warning
          });
          return;
        }
        return dialogs.taskDialog.dialog("open");
      });
      $('#languageFilter').button().click(function() {
        return dialogs.languageFilterDialog.dialog("open");
      });
      $(':radio[name=viewOption]').change(function() {
        return refreshGrid();
      });
      return $("#exportTranslation").button().attr('privilegeName', util.urlname2Action('trans/export-translation-details')).click(function() {
        var info;
        info = grid.getTotalSelectedRowInfo();
        if (!info.rowIds.length) {
          $.msgBox(c18n.selrow.format(c18n[grid.getTableType()]), null, {
            title: c18n.warning
          });
          return;
        }
        return dialogs.exportTranslationDialog.dialog('open');
      });
    };
    exportAppOrDicts = function(ftype) {
      var checkboxes, id, languages, type;
      id = $('#productRelease').val();
      if (!id) {
        return;
      }
      id = parseInt(id);
      if (-1 === id) {
        return;
      }
      checkboxes = $("#languageFilterDialog input:checkbox[name='languages']:checked");
      languages = checkboxes.map(function() {
        return this.id;
      }).get().join(',');
      type = $("input:radio[name='viewOption'][checked]").val();
      type = type.slice(0, 4);
      if (type[0] === 'a') {
        type = type.slice(0, 3);
      }
      $("#exportForm input[name='prod']").val(id);
      $("#exportForm input[name='language']").val(languages);
      $("#exportForm input[name='type']").val(type);
      if (ftype) {
        $("#exportForm input[name='type']").val(ftype);
      }
      return $("#exportForm").submit();
    };
    initPage = function() {
      var gridParent;
      dialogs = createDialogs();
      createSelects();
      createButtons(dialogs);
      $("#exportExcel").click(function() {
        return exportAppOrDicts('excel');
      });
      $("#exportPDF").click(function() {
        return exportAppOrDicts('pdf');
      });
      $('#loading-container').fadeOut('slow', function() {
        return $(this).remove();
      });
      util.afterInitilized(this);
      $('#optional-container').show();
      gridParent = $('.transGrid_parent');
      return $('#transGrid').setGridWidth(gridParent.width() - 10).setGridHeight(gridParent.height() - 110);
    };
    initPage();
    return {
      name: 'layout',
      showTransDetailDialog: function(param) {
        var map, status, transDetailGrid;
        $('#dictionaryName', dialogs.transDetailDialog).html(param.dict.name);
        $('#detailLanguageSwitcher', dialogs.transDetailDialog).empty().append(util.json2Options(param.languages, param.language.id, 'name'));
        transDetailGrid = $("#transDetailGridList");
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
        $('#detailLanguageSwitcher').trigger("change");
        return dialogs.transDetailDialog.dialog("open");
      }
    };
  });

}).call(this);
