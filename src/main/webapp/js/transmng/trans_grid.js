// Generated by CoffeeScript 1.3.3
(function() {

  define(['jqgrid', 'util', 'require', 'jqmsgbox', 'transmng/grid.colmodel', 'blockui'], function($, util, require, msgbox) {
    var c18n, common, getTableType, grid, i18n, transGrid;
    i18n = require('i18n!nls/transmng');
    c18n = require('i18n!nls/common');
    common = {
      colNames: ['ID', 'Application', 'Version', 'Num of String'],
      colModel: [
        {
          name: 'id',
          index: 'id',
          width: 55,
          align: 'center',
          hidden: true,
          frozen: true
        }, {
          name: 'application',
          index: 'base.name',
          width: 100,
          editable: false,
          stype: 'select',
          align: 'center',
          frozen: true
        }, {
          name: 'appVersion',
          index: 'version',
          width: 90,
          editable: true,
          align: 'center',
          frozen: true,
          search: false
        }, {
          name: 'numOfString',
          index: 'labelNum',
          width: 80,
          align: 'left',
          frozen: true,
          search: false
        }
      ]
    };
    grid = {
      dictionary: {
        colNames: common.colNames.slice(0).insert(3, ['Dictionary', 'Version', 'Encoding', 'Format']),
        colModel: common.colModel.slice(0).insert(3, [
          {
            name: 'dictionary',
            index: 'base.name',
            width: 90,
            editable: true,
            align: 'left',
            frozen: true,
            search: false
          }, {
            name: 'dictVersion',
            index: 'version',
            width: 90,
            editable: true,
            align: 'center',
            frozen: true,
            search: false
          }, {
            name: 'encoding',
            index: 'base.encoding',
            width: 90,
            editable: true,
            stype: 'select',
            searchoptions: {
              value: ':All;ISO-8859-1:ISO-8859-1;UTF-8:UTF-8;UTF-16LE:UTF-16LE;UTF-16BE:UTF-16BE'
            },
            align: 'center',
            frozen: true
          }, {
            name: 'format',
            index: 'base.format',
            width: 90,
            editable: true,
            stype: 'select',
            searchoptions: {
              value: ":All;DCT:DCT;Dictionary conf:Dictionary conf;Text properties:Text properties;XML labels:XML labels"
            },
            align: 'center',
            frozen: true
          }
        ]),
        ondblClickRow: function(rowid, iRow, iCol, e) {
          var dictName, language,
            _this = this;
          language = {
            name: $(this).getGridParam('colModel')[iCol].name.split('.')[0],
            id: parseInt(/s\((\d+)\)\[\d+\]/ig.exec($(this).getGridParam('colModel')[iCol].index)[1])
          };
          dictName = $(this).getCell(rowid, $(this).getGridParam('colNames').indexOf('Dictionary'));
          return util.getDictLanguagesByDictId(rowid, function(languages) {
            return require('transmng/layout').showTransDetailDialog({
              dict: {
                id: rowid,
                name: dictName
              },
              language: language,
              languages: languages
            });
          });
        }
      },
      application: {
        colNames: ['Dummy'].concat(common.colNames),
        colModel: [
          {
            name: 'dummy',
            index: 'dummy',
            width: 55,
            align: 'center',
            hidden: true,
            frozen: true
          }
        ].concat(common.colModel)
      }
    };
    getTableType = function() {
      if (-1 === ($.inArray('Dummy', $("#transGrid").getGridParam('colNames')))) {
        return 'dict';
      } else {
        return 'app';
      }
    };
    transGrid = $("#transGrid").jqGrid({
      url: '',
      mtype: 'POST',
      postData: {},
      editurl: "",
      datatype: 'json',
      width: $(window).width() * 0.95,
      height: 350,
      shrinkToFit: false,
      rownumbers: true,
      loadonce: false,
      pager: '#transPager',
      rowNum: 60,
      rowList: [10, 20, 30, 60, 120],
      sortname: 'base.name',
      sortorder: 'asc',
      viewrecords: true,
      gridview: true,
      multiselect: true,
      colNames: grid.dictionary.colNames,
      colModel: grid.dictionary.colModel,
      groupHeaders: [],
      afterCreate: function(grid) {
        grid.setGroupHeaders({
          useColSpanStyle: true,
          groupHeaders: grid.getGridParam('groupHeaders')
        });
        if (getTableType() === 'dict') {
          grid.filterToolbar({
            stringResult: true,
            searchOnEnter: false
          });
        }
        grid.navGrid('#transPager', {
          edit: false,
          add: false,
          del: false,
          search: false,
          view: false
        });
        grid.navButtonAdd("#transPager", {
          caption: "Clear",
          title: "Clear Search",
          buttonicon: 'ui-icon-refresh',
          position: 'first',
          onClickButton: function() {
            return grid[0].clearToolbar();
          }
        });
        return grid.setFrozenColumns();
      }
    });
    transGrid.getGridParam('afterCreate')(transGrid);
    ($("[id^=makeLabel]").button().click(function() {
      var selectedRowIds;
      transGrid = $("#transGrid");
      selectedRowIds = transGrid.getGridParam('selarrrow').join(',');
      if (!selectedRowIds) {
        $.msgBox(i18n.msgbox.rowsel.msg, null, {
          title: c18n.warning
        });
        return;
      }
      $.blockUI({
        css: {
          backgroundColor: '#fff'
        },
        overlayCSS: {
          opacity: 0.2
        }
      });
      return $.post('/trans/update-status', {
        type: getTableType(),
        transStatus: this.value,
        id: selectedRowIds
      }, function(json) {
        if (json.status !== 0) {
          alert(json.message);
          return;
        }
        $.unblockUI();
        $.msgBox(i18n.msgbox.transstatus.msg, null, {
          title: c18n.message
        });
        return transGrid.trigger('reloadGrid');
      });
    })).parent().buttonset();
    return {
      productReleaseChanged: function(param) {
        var gridParam, isApp, postData, prop, summary, url;
        summary = ($(param.languages).map(function() {
          var _this;
          _this = this;
          return ($([0, 1, 2]).map(function() {
            return "s(" + _this.id + ")[" + this + "]";
          })).get().join(',');
        })).get().join(',');
        gridParam = transGrid.getGridParam();
        isApp = param.level === "application";
        if (isApp) {
          gridParam.colNames = grid.application.colNames;
          gridParam.colModel = grid.application.colModel;
          gridParam.ondblClickRow = (function() {});
          url = 'rest/applications';
          prop = "id,id,base.name,version,labelNum," + summary;
          transGrid.setColProp('application', {
            search: false,
            index: 'base.name'
          });
        } else {
          gridParam.colNames = grid.dictionary.colNames;
          gridParam.colModel = grid.dictionary.colModel;
          gridParam.ondblClickRow = grid.dictionary.ondblClickRow;
          url = 'rest/dict';
          prop = "id,app.base.name,app.version,base.name,version,base.encoding,base.format,labelNum," + summary;
          $.ajax({
            url: "rest/applications?prod=" + param.release.id + "&prop=id,name",
            async: false,
            dataType: 'json',
            success: function(json) {
              var app;
              app = ":All";
              $(json).each(function() {
                return app += ";" + this.name + ":" + this.name;
              });
              return transGrid.setColProp('application', {
                search: true,
                searchoptions: {
                  value: app
                },
                index: 'app.base.name'
              });
            }
          });
        }
        postData = {
          prod: param.release.id,
          format: 'grid',
          prop: prop
        };
        return transGrid.updateTaskLanguage(param.languages, url, postData);
      },
      getTotalSelectedRowInfo: function() {
        var count, selectedRowIds;
        transGrid = $("#transGrid");
        selectedRowIds = transGrid.getGridParam('selarrrow');
        count = 0;
        $(selectedRowIds).each(function() {
          var row;
          row = transGrid.getRowData(this);
          return count += parseInt(row.numOfString);
        });
        return {
          rowIds: selectedRowIds,
          totalLabels: count
        };
      },
      getTableType: getTableType
    };
  });

}).call(this);
