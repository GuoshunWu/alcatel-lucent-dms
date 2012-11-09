// Generated by CoffeeScript 1.3.3
(function() {
  var __indexOf = [].indexOf || function(item) { for (var i = 0, l = this.length; i < l; i++) { if (i in this && this[i] === item) return i; } return -1; };

  define(['jqgrid'], function($) {
    return $.jgrid.extend({
      getId: function() {
        return "#" + (this.attr('id'));
      },
      addColumns: function(newColNames, newColModelEntrys, url, postData) {
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
        return this.reloadAll(url, postData);
      },
      reloadAll: function(url, postData) {
        var gridParam, newGrid;
        if (!url) {
          return;
        }
        gridParam = this.getGridParam();
        this.GridUnload(this.getId());
        gridParam.url = url;
        if (postData) {
          gridParam.postData = postData;
        }
        delete gridParam.selarrrow;
        newGrid = $(this.getId()).jqGrid(gridParam);
        return this.getGridParam('afterCreate')(newGrid);
      },
      addTaskLanguage: function(language, url, postData) {
        var colModels, cols, level;
        cols = ['T', 'N', 'I'];
        level = $("input:radio[name='viewOption'][checked]").val();
        colModels = $(cols).map(function(index) {
          var model;
          model = {
            name: "" + language.name + "." + this,
            sortable: false,
            index: "s(" + language.id + ")[" + index + "]",
            width: 40,
            align: 'right',
            search: false,
            editable: false
          };
          if (level !== 'application') {
            model.formatter = 'showlink';
            model.formatoptions = {
              baseLinkUrl: '#',
              addParam: encodeURI("&languageId=" + language.id + "&languaeName=" + language.name)
            };
          }
          return model;
        }).get();
        this.getGridParam('groupHeaders').push({
          startColumnName: "" + language.name + ".T",
          numberOfColumns: cols.length,
          titleText: "<bold>" + language.name + "</bold>"
        });
        return this.addColumns(cols, colModels, url, postData);
      },
      updateTaskLanguage: function(languages, url, postData) {
        var cols, gridParam,
          _this = this;
        if ($.isEmptyObject(languages)) {
          return false;
        }
        cols = ['T', 'N', 'I'];
        gridParam = this.getGridParam();
        gridParam.colNames = $.grep(gridParam.colNames, function(val, key) {
          return !(__indexOf.call(cols, val) >= 0);
        });
        gridParam.colModel = $.grep(gridParam.colModel, function(val, key) {
          return !/.+\.[TIN]/g.test(val.name);
        });
        if (!$.isArray(languages)) {
          this.addTaskLanguage(languages, url, postData, 0);
          return;
        }
        if (0 === languages.length) {
          this.reloadAll(url, postData);
          return;
        }
        return $(languages).each(function(index, language) {
          if (index < languages.length - 1) {
            return _this.addTaskLanguage(language);
          } else {
            return _this.addTaskLanguage(language, url, postData);
          }
        });
      }
    });
  });

}).call(this);
