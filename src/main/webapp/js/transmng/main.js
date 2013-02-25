// Generated by CoffeeScript 1.4.0
(function() {

  define(function(require) {
    var c18n, dialogs, exportAppOrDicts, grid, init, nodeSelectHandler, onShow, ready, urls, util;
    c18n = require('i18n!nls/common');
    dialogs = require('transmng/dialogs');
    grid = require('transmng/trans_grid');
    util = require('dms-util');
    urls = require('dms-urls');
    nodeSelectHandler = function(node, nodeInfo) {
      var type;
      type = node.attr('type');
      if ('products' === type) {
        return;
      }
      $('#versionTypeLabel', "div[id='transmng']").text("" + nodeInfo.text);
      if ('product' === type) {
        $.getJSON(urls.prod_versions, {
          base: nodeInfo.id,
          prop: 'id,version'
        }, function(json) {
          return $('#selVersion', "div[id='transmng']").empty().append(util.json2Options(json)).trigger('change');
        });
        return;
      }
      if ('app' === type) {
        return $.getJSON("" + urls.app_versions + nodeInfo.id, function(json) {
          return $('#selVersion', "div[id='transmng']").empty().append(util.json2Options(json)).trigger('change');
        });
      }
    };
    onShow = function() {
      var gridParent;
      gridParent = $('.transGrid_parent');
      return $('#transGrid').setGridWidth(gridParent.width() - 10).setGridHeight(gridParent.height() - 110);
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
      return $("#exportForm", "#transmng").submit();
    };
    init = function() {
      if (typeof console !== "undefined" && console !== null) {
        console.debug("transmng panel init...");
      }
      $('#selVersion', "div[id='transmng']").change(function() {
        var nodeInfo, postData, type;
        if (!this.value || -1 === parseInt(this.value)) {
          return;
        }
        nodeInfo = (require('ptree')).getNodeInfo();
        type = nodeInfo.type;
        if (type.startWith('prod')) {
          type = type.slice(0, 4);
        }
        postData = {
          prop: 'id,name'
        };
        postData[type] = this.value;
        $.ajax({
          url: urls.languages,
          async: false,
          data: postData,
          dataType: 'json',
          success: function(languages) {
            var langTable;
            langTable = util.generateLanguageTable(languages);
            return $("#languageFilterDialog").empty().append(langTable);
          }
        });
        return dialogs.refreshGrid(false, grid);
      });
      $("#create", '#transmng').button().attr('privilegeName', util.urlname2Action('task/create-task')).click(function() {
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
      $('#languageFilter', '#transmng').button().click(function() {
        return dialogs.languageFilterDialog.dialog("open");
      });
      $(':radio[name=viewOption]').change(function() {
        return dialogs.refreshGrid(false, grid);
      });
      $("#exportTranslation", '#transmng').button().attr('privilegeName', util.urlname2Action('trans/export-translation-details')).click(function() {
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
      $("#exportExcel", '#transmng').click(function() {
        return exportAppOrDicts('excel');
      });
      return $("#exportPDF", '#transmng').click(function() {
        return exportAppOrDicts('pdf');
      });
    };
    ready = function() {
      onShow();
      return typeof console !== "undefined" && console !== null ? console.debug("transmng panel ready...") : void 0;
    };
    init();
    ready();
    return {
      onShow: onShow,
      nodeSelect: nodeSelectHandler
    };
  });

}).call(this);
