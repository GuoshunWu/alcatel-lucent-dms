// Generated by CoffeeScript 1.3.3
(function() {
  var __indexOf = [].indexOf || function(item) { for (var i = 0, l = this.length; i < l; i++) { if (i in this && this[i] === item) return i; } return -1; };

  define(['jqgrid'], function($) {
    return $.jgrid.extend({
      getId: function() {
        return "#" + (this.attr('id'));
      },
      addColumns: function(newColNames, newColModelEntrys, url) {
        var gridParam;
        gridParam = this.getGridParam();
        gridParam.colNames = $.grep(gridParam.colNames, function(val, key) {
          return "" !== val;
        });
        gridParam.colModel = $.grep(gridParam.colModel, function(val, key) {
          return "rn" !== val.name;
        });
        $.merge(gridParam.colModel, newColModelEntrys);
        $.merge(gridParam.colNames, newColNames);
        return this.reloadAll(url);
      },
      reloadAll: function(url) {
        var gridParam, newGrid;
        if (!url) {
          return;
        }
        gridParam = this.getGridParam();
        log('recreate grid...');
        this.GridUnload(this.getId());
        gridParam.url = url;
        newGrid = $(this.getId()).jqGrid(gridParam);
        return this.getGridParam('afterCreate')(newGrid);
      },
      addTaskLanguage: function(language, url) {
        var colModels, cols;
        cols = ['T', 'N', 'I'];
        colModels = ($(cols).map(function() {
          return {
            name: "" + language + "." + this,
            index: "" + language + "." + this,
            width: 20,
            editable: false,
            align: 'center'
          };
        })).get();
        this.getGridParam('groupHeaders').push({
          startColumnName: "" + language + ".T",
          numberOfColumns: 3,
          titleText: "<bold>" + language + "</bold>"
        });
        return this.addColumns(cols, colModels, url);
      },
      updateTaskLanguage: function(languages, url) {
        var cols, gridParam,
          _this = this;
        if ($.isEmptyObject(languages)) {
          return false;
        }
        cols = ['T', 'N', 'I'];
        gridParam = this.getGridParam();
        gridParam.colNames = $.grep(gridParam.colNames, function(val, key) {
          return __indexOf.call(cols, val) < 0;
        });
        gridParam.colModel = $.grep(gridParam.colModel, function(val, key) {
          return !/.+\.[TIN]/g.test(val.name);
        });
        if (!$.isArray(languages)) {
          this.addTaskLanguage(languages, url);
          return;
        }
        $(languages).each(function(index, language) {
          if (index === languages.length - 1) {
            return false;
          }
          return _this.addTaskLanguage(language);
        });
        return this.addTaskLanguage(languages[languages.length - 1], url);
      }
    });
  });

}).call(this);
