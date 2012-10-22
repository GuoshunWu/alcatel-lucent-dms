// Generated by CoffeeScript 1.3.3
(function() {

  define(['jqlayout', 'require', 'blockui', 'jqmsgbox', 'i18n!nls/common', 'i18n!nls/transmng', 'transmng/trans_grid', 'transmng/transdetail_grid'], function($, require, blockui, msgbox, c18n, i18n, grid, detailgrid) {
    var createButtons, createDialogs, createSelects, dialogs, ids, initPage, pageLayout, util;
    util = require('util');
    ids = {
      languageFilterTableId: 'languageFilterTable',
      languageFilterDialogId: 'languageFilterDialog',
      container: {
        page: 'optional-container'
      }
    };
    $('#pageNavigator').val(window.location.pathname);
    pageLayout = $("#" + ids.container.page).layout({
      resizable: true,
      closable: true
    });
    $(".header-footer").hover((function() {
      return $(this).addClass("ui-state-hover");
    }), function() {
      return $(this).removeClass("ui-state-hover");
    });
    dialogs = null;
    createDialogs = function() {
      var languageFilterDialog, taskDialog, transDetailDialog;
      languageFilterDialog = $("<div title='" + i18n.select.languagefilter.title + "' id='" + ids.languageFilterDialogId + "'>").dialog({
        autoOpen: false,
        position: [23, 126],
        height: 'auto',
        width: 'auto',
        show: {
          effect: 'slide',
          direction: "up"
        },
        create: function() {
          var _this = this;
          return $.getJSON('rest/languages?prop=id,name', {}, function(languages) {
            return $(_this).append(util.generateLanguageTable(languages));
          });
        },
        buttons: [
          {
            text: c18n.ok,
            click: function() {
              $('#productRelease').trigger("change");
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
      taskDialog = $("#createTranslationTaskDialog").dialog({
        autoOpen: false,
        width: 'auto',
        height: 'auto',
        position: [25, 100],
        show: {
          effect: 'slide',
          direction: "down"
        },
        open: function() {
          var info, langFilterTableId, nums, postData, tableType,
            _this = this;
          info = grid.getTotalSelectedRowInfo();
          tableType = grid.getTableType();
          nums = info.selectedNum;
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
        buttons: [
          {
            text: c18n.create,
            click: function() {
              var languages;
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
      transDetailDialog = $('#translationDetailDialog').dialog({
        autoOpen: false,
        width: 'auto',
        height: 400,
        create: function() {
          return $('#detailLanguageSwitcher').change(function() {
            var dict, language;
            dict = $('#translationDetailDialog').data("dict");
            language = {
              id: $(this).val(),
              name: $(this).find("option:selected").text()
            };
            return detailgrid.languageChanged({
              language: language,
              dict: dict
            });
          });
        }
      });
      return {
        taskDialog: taskDialog,
        languageFilterDialog: languageFilterDialog,
        transDetailDialog: transDetailDialog
      };
    };
    createSelects = function() {
      $.getJSON('rest/products/trans/productbases', {}, function(json) {
        $('#productBase').append(new Option(c18n.select.product.tip, -1));
        return $('#productBase').append($(json).map(function() {
          return new Option(this.name, this.id);
        }));
      });
      $('#productBase').change(function() {
        $('#productRelease').empty();
        if (parseInt($('#productBase').val()) === -1) {
          return false;
        }
        return $.getJSON("rest/products/" + ($('#productBase').val()), {}, function(json) {
          $('#productRelease').append(new Option(c18n.select.release.tip, -1));
          $('#productRelease').append($(json).map(function() {
            return new Option(this.version, this.id);
          }));
          return $('#productRelease').trigger("change");
        });
      });
      return $('#productRelease').change(function() {
        var param;
        param = {
          release: {
            id: $(this).val(),
            version: $(this).find("option:selected").text()
          },
          languages: ($(":checkbox[name='languages']", $("#" + ids.languageFilterDialogId)).map(function() {
            if (this.checked) {
              return {
                id: this.id,
                name: this.value
              };
            }
          })).get(),
          level: $(":radio[name='viewOption'][checked]").val()
        };
        if (!$('#productBase').val() || parseInt($('#productBase').val()) === -1) {
          return false;
        }
        if (!param.release.id || parseInt(param.release.id) === -1) {
          return false;
        }
        return grid.productReleaseChanged(param);
      });
    };
    createButtons = function(taskDialog, languageFilterDialog) {
      $("#create").button().click(function() {
        var info, type;
        require('jqmsgbox');
        info = grid.getTotalSelectedRowInfo();
        type = $(':radio[name=viewOption][checked]').val();
        if (!info.selectedNum) {
          $.msgBox(i18n.msgbox.createtranstask.msg.format(c18n[grid.getTableType()]), null, {
            title: c18n.warning
          });
          return;
        }
        return taskDialog.dialog("open");
      });
      $('#languageFilter').button().click(function() {
        return languageFilterDialog.dialog("open");
      });
      return $(':radio[name=viewOption]').change(function() {
        return $('#productRelease').trigger("change");
      });
    };
    initPage = function() {
      createSelects();
      dialogs = createDialogs();
      createButtons(dialogs.taskDialog, dialogs.languageFilterDialog, dialogs.transDetailDialog);
      $('#optional-container').show();
      return $('#loading-container').remove();
    };
    initPage();
    return {
      name: 'layout',
      showTransDetailDialog: function(param) {
        $('#dictionaryName', dialogs.transDetailDialog).html(param.dict.name);
        $('#detailLanguageSwitcher', dialogs.transDetailDialog).append($(param.languages).map(function(index) {
          var opt;
          opt = new Option(this.name, this.id);
          opt.selected = this.name === param.language.name;
          return opt;
        }));
        $('#translationDetailDialog').data('dict', param.dict);
        $('#detailLanguageSwitcher').trigger("change");
        return dialogs.transDetailDialog.dialog("open");
      }
    };
  });

}).call(this);
