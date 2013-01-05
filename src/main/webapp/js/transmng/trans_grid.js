// Generated by CoffeeScript 1.3.3
(function() {

  define(['jqgrid', 'util', 'jqmsgbox', 'transmng/grid.colmodel', 'blockui', 'i18n!nls/transmng', 'i18n!nls/common', 'require'], function($, util, msgbox, gmodel, blockui, i18n, c18n, require) {
    var common, getTableType, grid, restoreSearchToolBarValue, transGrid;
    restoreSearchToolBarValue = function(column, value) {
      var searchOpts;
      if (typeof console !== "undefined" && console !== null) {
        console.log("Set default value to " + value + " for " + column);
      }
      $("select[id=gs_" + column + "]").each(function(idx, elem) {
        return elem.value = value;
      });
      searchOpts = ($("#transGrid").jqGrid('getColProp', column)).searchoptions;
      searchOpts.defaultValue = value;
      return $("#transGrid").jqGrid('setColProp', column, {
        searchoptions: searchOpts
      });
    };
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
          align: 'left',
          frozen: true,
          stype: 'select',
          searchoptions: {
            value: ":All",
            dataEvents: [
              {
                type: 'change',
                fn: function(e) {
                  var searchvalue;
                  searchvalue = $("#transGrid").jqGrid('getGridParam', 'searchvalue');
                  searchvalue.app = e.target.value;
                  return $("#transGrid").jqGrid('setGridParam', 'searchvalue', searchvalue);
                }
              }
            ]
          }
        }, {
          name: 'appVersion',
          index: 'version',
          width: 90,
          editable: false,
          align: 'left',
          frozen: true,
          search: false
        }, {
          name: 'numOfString',
          index: 'labelNum',
          width: 80,
          align: 'right',
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
            editable: false,
            align: 'left',
            frozen: true,
            search: false
          }, {
            name: 'dictVersion',
            index: 'version',
            width: 90,
            editable: false,
            align: 'left',
            frozen: true,
            search: false
          }, {
            name: 'encoding',
            index: 'base.encoding',
            width: 90,
            editable: false,
            align: 'left',
            frozen: true,
            stype: 'select',
            searchoptions: {
              value: ':All;ISO-8859-1:ISO-8859-1;UTF-8:UTF-8;UTF-16LE:UTF-16LE;UTF-16BE:UTF-16BE',
              dataEvents: [
                {
                  type: 'change',
                  fn: function(e) {
                    var searchvalue;
                    searchvalue = $("#transGrid").jqGrid('getGridParam', 'searchvalue');
                    searchvalue.encoding = e.target.value;
                    return $("#transGrid").jqGrid('setGridParam', 'searchvalue', searchvalue);
                  }
                }
              ]
            }
          }, {
            name: 'format',
            index: 'base.format',
            width: 90,
            editable: false,
            align: 'left',
            frozen: true,
            stype: 'select',
            searchoptions: {
              value: ":All;DCT:DCT;Dictionary conf:Dictionary conf;Text properties:Text properties;XML labels:XML labels",
              dataEvents: [
                {
                  type: 'change',
                  fn: function(e) {
                    var searchvalue;
                    searchvalue = $("#transGrid").jqGrid('getGridParam', 'searchvalue');
                    searchvalue.format = e.target.value;
                    return $("#transGrid").jqGrid('setGridParam', 'searchvalue', searchvalue);
                  }
                }
              ]
            }
          }
        ])
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
    /* Construct the grid with the column name(model) parameters above and other required parameters
    */

    transGrid = $("#transGrid").jqGrid({
      url: 'rest/dict',
      mtype: 'post',
      postData: {},
      datatype: 'local',
      width: $(window).innerWidth() * 0.95,
      height: 310,
      rownumbers: true,
      shrinkToFit: false,
      pager: '#transPager',
      rowNum: 60,
      rowList: [10, 20, 30, 60, 120],
      sortname: 'base.name',
      sortorder: 'asc',
      multiselect: true,
      colNames: grid.dictionary.colNames,
      colModel: grid.dictionary.colModel,
      beforeProcessing: function(data, status, xhr) {},
      gridComplete: function() {
        transGrid = $(this);
        $('a', this).each(function(index, a) {
          if ('0' === $(a).text()) {
            return $(a).before(' ').remove();
          }
        });
        return $('a', this).css('color', 'blue').click(function() {
          var allZero, language, pageParams, rowData, rowid,
            _this = this;
          pageParams = util.getUrlParams(this.href);
          rowid = pageParams != null ? pageParams.id : void 0;
          language = {
            id: pageParams.languageId,
            name: pageParams.languageName
          };
          rowData = transGrid.getRowData(rowid);
          allZero = true;
          $(['T', 'N', 'I']).each(function(index, elem) {
            var num;
            num = parseInt(rowData["" + language.name + "." + elem]);
            allZero = 0 === num;
            return allZero;
          });
          if (allZero) {
            if (typeof console !== "undefined" && console !== null) {
              console.log('zero');
            }
            return;
          }
          return util.getDictLanguagesByDictId(rowid, function(languages) {
            var transLayout;
            transLayout = require('transmng/layout');
            return transLayout.showTransDetailDialog({
              dict: {
                id: rowid,
                name: rowData.dictionary
              },
              language: language,
              languages: languages
            });
          });
        });
      },
      searchvalue: {},
      groupHeaders: [],
      afterCreate: function(grid) {
        grid.setGridParam({
          'datatype': 'json'
        });
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
        return grid.setFrozenColumns();
      }
    });
    transGrid.getGridParam('afterCreate')(transGrid);
    ($("[id^=makeLabel]").button().click(function() {
      var selectedRowIds;
      transGrid = $("#transGrid");
      selectedRowIds = transGrid.getGridParam('selarrrow').join(',');
      if (!selectedRowIds) {
        $.msgBox(c18n.selrow.format(c18n.dict), null, {
          title: c18n.warning
        });
        return;
      }
      $.blockUI();
      return $.post('trans/update-status', {
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
        var gridParam, isApp, postData, prop, searchoptions, searchvalue, summary, url;
        transGrid = $("#transGrid");
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
          url = 'rest/applications';
          prop = "id,id,base.name,version,labelNum," + summary;
          transGrid.setColProp('application', {
            search: false,
            index: 'base.name'
          });
          postData = {
            prod: param.release.id,
            format: 'grid',
            prop: prop
          };
          transGrid.updateTaskLanguage(param.languages);
          return transGrid.reloadAll(url, postData);
        } else {
          gridParam.colNames = grid.dictionary.colNames;
          gridParam.colModel = grid.dictionary.colModel;
          url = 'rest/dict';
          prop = "id,app.base.name,app.version,base.name,version,base.encoding,base.format,labelNum," + summary;
          searchoptions = transGrid.getColProp('application').searchoptions;
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
              return searchoptions.value = app;
            }
          });
          transGrid.setColProp('application', {
            searchoptions: searchoptions,
            index: 'app.base.name'
          });
          postData = {
            prod: param.release.id,
            format: 'grid',
            prop: prop
          };
          transGrid.updateTaskLanguage(param.languages);
          gridParam.datatype = 'local';
          transGrid = transGrid.reloadAll(url, postData);
          searchvalue = $("#transGrid").jqGrid('getGridParam', 'searchvalue');
          if (searchvalue.app) {
            restoreSearchToolBarValue('application', searchvalue.app);
          }
          if (searchvalue.encoding) {
            restoreSearchToolBarValue('encoding', searchvalue.encoding);
          }
          if (searchvalue.format) {
            restoreSearchToolBarValue('format', searchvalue.format);
          }
          transGrid.setGridParam('datatype', 'json');
          if (searchvalue.app || searchvalue.encoding || searchvalue.format) {
            return $("#transGrid")[0].triggerToolbar();
          } else {
            return transGrid.trigger('reloadGrid');
          }
        }
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
