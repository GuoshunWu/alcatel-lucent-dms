// Generated by CoffeeScript 1.3.3
(function() {

  define(function(require, util, dialogs, i18n) {
    var $, deleteOptions, deleteRow, dicGrid, languageSetting, localIds, stringSetting;
    $ = require('jqgrid');
    util = require('util');
    dialogs = require('appmng/dialogs');
    i18n = require('i18n!nls/appmng');
    localIds = {
      dic_grid: '#dictionaryGridList'
    };
    deleteOptions = {
      reloadAfterSubmit: false,
      url: '/app/remove-dict',
      beforeShowForm: function(form) {
        var permanent;
        permanent = $('#permanentDeleteSignId', form);
        if (permanent.length === 0) {
          $("<tr><td>" + i18n.grid.permanenttext + "<td><input align='left' type='checkbox' id='permanentDeleteSignId'>").appendTo($("tbody", form));
        }
        return permanent != null ? permanent.removeAttr('checked') : void 0;
      },
      onclickSubmit: function(params, posdata) {
        return {
          appId: $("#selAppVersion").val(),
          permanent: Boolean($('#permanentDeleteSignId').attr("checked"))
        };
      },
      afterSubmit: function(response, postdata) {
        var jsonFromServer;
        jsonFromServer = eval("(" + response.responseText + ")");
        return [0 === jsonFromServer.status, jsonFromServer.message];
      }
    };
    languageSetting = function(rowData) {
      dialogs.langSettings.data("param", {
        dictId: rowData.id,
        refCode: rowData.langrefcode
      });
      return dialogs.langSettings.dialog('open');
    };
    stringSetting = function(rowData) {
      dialogs.stringSettings.data("param", rowData);
      return dialogs.stringSettings.dialog('open');
    };
    deleteRow = function(rowid) {
      console.log($.jgrid.del);
      return $(localIds.dic_grid).jqGrid('delGridRow', rowid, deleteOptions);
    };
    dicGrid = $(localIds.dic_grid).jqGrid({
      url: '',
      datatype: 'json',
      width: 1000,
      height: 350,
      pager: '#dictPager',
      editurl: "app/create-or-add-application",
      rowNum: 10,
      rowList: [10, 20, 30],
      sortname: 'base.name',
      sortorder: 'asc',
      viewrecords: true,
      cellEdit: true,
      cellurl: '/app/update-dict',
      gridview: true,
      multiselect: true,
      caption: 'Dictionary for Application',
      colNames: ['LangRefCode', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Action'],
      colModel: [
        {
          name: 'langrefcode',
          index: 'langrefcode',
          width: 55,
          align: 'center',
          hidden: true
        }, {
          name: 'name',
          index: 'base.name',
          width: 200,
          editable: false,
          align: 'left'
        }, {
          name: 'version',
          index: 'version',
          width: 25,
          editable: true,
          edittype: 'select',
          editoptions: {
            value: {}
          },
          align: 'center'
        }, {
          name: 'format',
          index: 'base.format',
          width: 60,
          editable: true,
          edittype: 'select',
          editoptions: {
            value: "DCT:DCT;Dictionary conf:Dictionary conf;Text properties:Text properties;XML labels:XML labels"
          },
          align: 'center'
        }, {
          name: 'encoding',
          index: 'base.encoding',
          width: 40,
          editable: true,
          edittype: 'select',
          editoptions: {
            value: 'ISO-8859-1:ISO-8859-1;UTF-8:UTF-8;UTF-16LE:UTF-16LE;UTF-16BE:UTF-16BE'
          },
          align: 'center'
        }, {
          name: 'labelNum',
          index: 'labelNum',
          width: 20,
          align: 'center'
        }, {
          name: 'action',
          index: 'action',
          width: 45,
          editable: false,
          align: 'center'
        }
      ],
      beforeProcessing: function(data, status, xhr) {
        var actIndex;
        actIndex = $(this).getGridParam('colNames').indexOf('Action');
        if ($(this).getGridParam('multiselect')) {
          --actIndex;
        }
        return $(data.rows).each(function(index) {
          var rowData;
          rowData = this;
          return this.cell[actIndex] = ($(['S', 'L', 'X']).map(function() {
            return "<A id='action_" + this + "_" + rowData.id + "_" + actIndex + "' href=# >" + this + "</A>";
          })).get().join('');
        });
      },
      afterEditCell: function(id, name, val, iRow, iCol) {
        var grid;
        grid = this;
        if (name === 'version') {
          return $.ajax({
            url: "/rest/dict?slibing=" + id + "&prop=id,version",
            async: false,
            dataType: 'json',
            success: function(json) {
              return $("#" + iRow + "_version", grid).append($(json).map(function() {
                var opt;
                opt = new Option(this.version, this.id);
                opt.selected = this.version === val;
                return opt;
              }));
            }
          });
        }
      },
      beforeSubmitCell: function(rowid, cellname, value, iRow, iCol) {
        var isVersion;
        isVersion = cellname === 'version';
        $(this).setGridParam({
          cellurl: isVersion ? '/app/change-dict-version' : '/app/update-dict'
        });
        if (isVersion) {
          return {
            appId: $("#selAppVersion").val(),
            newDictId: value
          };
        } else {
          return {};
        }
      },
      afterSubmitCell: function(serverresponse, rowid, cellname, value, iRow, iCol) {
        var jsonFromServer;
        jsonFromServer = eval("(" + serverresponse.responseText + ")");
        return [0 === jsonFromServer.status, jsonFromServer.message];
      },
      gridComplete: function() {
        return $('a[id^=action_]', this).button({
          create: function(e, ui) {
            var a, action, col, rowid, titles, _ref;
            _ref = this.id.split('_'), a = _ref[0], action = _ref[1], rowid = _ref[2], col = _ref[3];
            titles = {
              S: i18n.dialog.stringsettings.title,
              L: i18n.dialog.languagesettings.title,
              X: i18n.dialog["delete"].title
            };
            this.title = titles[action];
            return this.onclick = function(e) {
              var rowData;
              rowData = $('#dictionaryGridList').getRowData(rowid);
              delete rowData.action;
              rowData.id = rowid;
              switch (action) {
                case 'S':
                  return stringSetting(rowData);
                case 'L':
                  return languageSetting(rowData);
                case 'X':
                  return deleteRow(rowid);
                default:
                  return console.log('Invalid action');
              }
            };
          }
        });
      }
    });
    dicGrid.jqGrid('navGrid', '#dictPager', {
      add: false,
      edit: false,
      search: false
    }, {}, {}, deleteOptions);
    ($('#batchDelete').button({})).click(function() {
      return alert("Useless");
    });
    return {
      appChanged: function(app) {
        var appBase, url;
        url = "rest/dict?app=" + app.id + "&format=grid&prop=languageReferenceCode,base.name,version,base.format,base.encoding,labelNum";
        dicGrid.setGridParam({
          url: url,
          datatype: "json"
        }).trigger("reloadGrid");
        appBase = require('appmng/apptree').getSelected();
        return dicGrid.setCaption("Dictionary for Application " + appBase.text + " version " + app.version);
      }
    };
  });

}).call(this);
