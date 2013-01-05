// Generated by CoffeeScript 1.3.3
(function() {
  var __indexOf = [].indexOf || function(item) { for (var i = 0, l = this.length; i < l; i++) { if (i in this && this[i] === item) return i; } return -1; };

  define(['jqgrid'], function($) {
    return $.jgrid.extend({
      getId: function() {
        return "#" + (this.attr('id'));
      },
      addColumns: function(newColNames, newColModelEntrys) {
        var gridParam;
        gridParam = this.getGridParam();
        gridParam.colNames = $.grep(gridParam.colNames, function(val, key) {
          return "" !== val;
        });
        gridParam.colModel = $.grep(gridParam.colModel, function(val, key) {
          return "rn" !== val.name;
        });
        $.merge(gridParam.colModel, newColModelEntrys);
        return $.merge(gridParam.colNames, newColNames);
      },
      reloadAll: function(url, postData) {
        var gridParam, newGrid;
        if (url == null) {
          url = this.getGridParam('url');
        }
        if (postData == null) {
          postData = this.getGridParam('postData');
        }
        gridParam = this.getGridParam();
        $(gridParam.colModel).each(function(index, colModel) {
          if (colModel.editable) {
            return colModel.classes = 'editable-column';
          }
        });
        this.GridUnload(this.getId());
        gridParam.url = url;
        if (postData) {
          gridParam.postData = postData;
        }
        delete gridParam.selarrrow;
        newGrid = $(this.getId()).jqGrid(gridParam);
        this.getGridParam('afterCreate')(newGrid);
        return newGrid;
      },
      addTaskLanguage: function(language) {
        var colModels, cols, level;
        cols = ['T', 'N', 'I'];
        level = $("input:radio[name='viewOption'][checked]").val();
        colModels = $(cols).map(function(index, elem) {
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
          if (elem === 'T') {
            model.classes = 'language-group-border';
          }
          if (level !== 'application') {
            model.formatter = 'showlink';
            model.formatoptions = {
              baseLinkUrl: '#',
              addParam: encodeURI("&languageId=" + language.id + "&languageName=" + model.name)
            };
          }
          return model;
        }).get();
        this.getGridParam('groupHeaders').push({
          startColumnName: "" + language.name + ".T",
          numberOfColumns: cols.length,
          titleText: "<bold>" + language.name + "</bold>"
        });
        return this.addColumns(cols, colModels);
      },
      updateTaskLanguage: function(languages) {
        var cols, gridParam,
          _this = this;
        if ($.isEmptyObject(languages)) {
          return;
        }
        if ($.isArray(languages) && 0 === languages.length) {
          return;
        }
        cols = ['T', 'N', 'I'];
        gridParam = this.getGridParam();
        gridParam.colNames = $.grep(gridParam.colNames, function(val, key) {
          return !(__indexOf.call(cols, val) >= 0);
        });
        gridParam.colModel = $.grep(gridParam.colModel, function(val, key) {
          return !/.+\.[TIN]/g.test(val.name);
        });
        if ($.isArray(languages)) {
          $(languages).each(function(index, language) {
            return _this.addTaskLanguage(language);
          });
          return;
        }
        return this.addTaskLanguage(languages);
      }
    });
  });

}).call(this);
