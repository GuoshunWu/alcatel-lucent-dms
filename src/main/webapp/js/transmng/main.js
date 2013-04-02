// Generated by CoffeeScript 1.6.2
(function() {
  define(['i18n!nls/common', 'dms-util', 'dms-urls', 'transmng/trans_grid', 'transmng/dialogs', 'ptree'], function(c18n, util, urls, grid, dialogs, ptree) {
    var exportAppOrDicts, init, nodeSelectHandler, onShow, ready;

    nodeSelectHandler = function(node, nodeInfo) {
      var type;

      type = node.attr('type');
      if ('products' === type) {
        return;
      }
      if (type === 'product') {
        type = 'prod';
      }
      $('#typeLabel', "div[id='transmng']").text("" + (c18n[type].capitalize()) + ": ");
      $('#versionTypeLabel', "div[id='transmng']").text("" + nodeInfo.text);
      if ('prod' === type) {
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
      var checkboxes, id, info, languages, level, type;

      info = util.getProductTreeInfo();
      id = $('#selVersion', "div[id='transmng']").val();
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
      level = info.type;
      if ('product' === level) {
        level = 'prod';
      }
      $("input[name='prod'], input[name='app']", '#exportForm').prop('name', level).val(id);
      $("#exportForm input[name='language']").val(languages);
      $("#exportForm input[name='type']").val(type);
      if (ftype) {
        $("#exportForm input[name='ftype']").val(ftype);
      }
      return $("#exportForm", "#transmng").submit();
    };
    init = function() {
      var searchActionBtn,
        _this = this;

      $('#selVersion', "div[id='transmng']").change(function() {
        var nodeInfo, postData;

        nodeInfo = util.getProductTreeInfo();
        postData = {
          prop: 'id,name'
        };
        postData[nodeInfo.type] = this.value ? this.value : -1;
        $.ajax({
          url: urls.languages,
          async: false,
          data: postData,
          dataType: 'json',
          success: function(languages) {
            var langTable;

            langTable = util.generateLanguageTable(languages);
            $("#languageFilterDialog").empty().append(langTable);
            languages.unshift({
              id: -1,
              name: 'Reference'
            });
            return $('#transSearchTextLanguage', "#transmng").empty().append(util.json2Options(languages, false, "name"));
          }
        });
        return dialogs.refreshGrid(false, grid);
      });
      searchActionBtn = $('#transSearchAction', '#transmng').attr('title', 'Search').button({
        text: false,
        icons: {
          primary: "ui-icon-search"
        }
      }).click(function() {
        return alert('To be implemented.');
      }).height(20).width(20).position({
        my: 'left center',
        at: 'right center',
        of: '#transSearchTextLanguage'
      });
      $('#transSearchText', '#transmng').keydown(function(e) {
        if (e.which === 13) {
          return searchActionBtn.trigger('click');
        }
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
      return onShow();
    };
    init();
    ready();
    return {
      onShow: onShow,
      nodeSelect: nodeSelectHandler
    };
  });

}).call(this);
