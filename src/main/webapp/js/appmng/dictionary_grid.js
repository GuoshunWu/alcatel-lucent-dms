// Generated by CoffeeScript 1.3.3
(function() {

  define(function(require, util, dialogs, i18n) {
    var $, blockui, c18n, deleteOptions, dicGrid, handlers, localIds;
    $ = require('jqgrid');
    util = require('util');
    dialogs = require('appmng/dialogs');
    i18n = require('i18n!nls/appmng');
    require('jqmsgbox');
    c18n = require('i18n!nls/common');
    blockui = require('blockui');
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
    handlers = {
      'String': {
        title: i18n.dialog.stringsettings.title,
        handler: function(rowData) {
          dialogs.stringSettings.data("param", rowData);
          return dialogs.stringSettings.dialog('open');
        }
      },
      'Language': {
        title: i18n.dialog.languagesettings.title,
        handler: function(rowData) {
          dialogs.langSettings.data("param", rowData);
          return dialogs.langSettings.dialog('open');
        }
      },
      'X': {
        title: i18n.dialog["delete"].title,
        handler: function(rowData) {
          return $(localIds.dic_grid).jqGrid('delGridRow', rowData.id, deleteOptions);
        }
      }
    };
    dicGrid = $(localIds.dic_grid).jqGrid({
      url: '',
      datatype: 'json',
      width: 1000,
      height: 330,
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
          width: 80,
          editable: false,
          align: 'center'
        }
      ],
      beforeProcessing: function(data, status, xhr) {
        var actIndex, actions, k, v;
        actIndex = $(this).getGridParam('colNames').indexOf('Action');
        if ($(this).getGridParam('multiselect')) {
          --actIndex;
        }
        actions = [];
        for (k in handlers) {
          v = handlers[k];
          actions.push(k);
        }
        return $(data.rows).each(function(index) {
          var rowData;
          rowData = this;
          return this.cell[actIndex] = $(actions).map(function() {
            return "<A id='action_" + this + "_" + rowData.id + "_" + actIndex + "'style='color:blue' title='" + handlers[this].title + "' href=# >" + this + "</A>";
          }).get().join('&nbsp;&nbsp;&nbsp;&nbsp;');
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
        var grid;
        grid = $(this);
        return $('a[id^=action_]', this).click(function() {
          var a, action, col, rowData, rowid, _ref;
          _ref = this.id.split('_'), a = _ref[0], action = _ref[1], rowid = _ref[2], col = _ref[3];
          rowData = grid.getRowData(rowid);
          delete rowData.action;
          rowData.id = rowid;
          return handlers[action].handler(rowData);
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
    ($('#generateDict').button({})).click(function() {
      var dicts, filename;
      dicts = dicGrid.getGridParam('selarrrow');
      if (!dicts || dicts.length === 0) {
        $.msgBox(c18n.selrow, null, {
          title: c18n.warning
        });
        return;
      }
      filename = "" + ($('#appDispAppName').text()) + "_" + ($('#selAppVersion option:selected').text()) + "_" + (new Date().format('yyyyMMdd_hhmmss')) + ".zip";
      $.blockUI({
        css: {
          backgroundColor: '#fff'
        },
        overlayCSS: {
          opacity: 0.2
        }
      });
      return $.post('/app/generate-dict', {
        dicts: dicts.join(','),
        filename: filename
      }, function(json) {
        var downloadForm;
        $.unblockUI();
        if (json.status !== 0) {
          $.msgBox(json.message, null, {
            title: c18n.error
          });
          return;
        }
        downloadForm = $('#downloadDict');
        $('#fileLoc', downloadForm).val(json.fileLoc);
        return downloadForm.submit();
      });
    });
    ($('#batchAddLanguage').button({})).click(function() {
      var dicts;
      dicts = dicGrid.getGridParam('selarrrow');
      if (!dicts || dicts.length === 0) {
        $.msgBox(c18n.selrow, null, {
          title: c18n.warning
        });
        return;
      }
      return $('#languageSettingGrid').editGridRow("new", {
        url: '/app/add-dict-language',
        onclickSubmit: function(params, posdata) {
          return {
            dicts: dicts.join(',')
          };
        },
        beforeInitData: function() {
          return $('#languageSettingGrid').setColProp('code', {
            editable: true
          });
        },
        onClose: function() {
          return $('#languageSettingGrid').setColProp('code', {
            editable: false
          });
        },
        afterSubmit: function(response, postdata) {
          var jsonfromServer;
          jsonfromServer = eval("(" + response.responseText + ")");
          return [jsonfromServer.status === 0, jsonfromServer.message, -1];
        }
      });
    });
    return {
      appChanged: function(param) {
        var prop;
        prop = "languageReferenceCode,base.name,version,base.format,base.encoding,labelNum";
        dicGrid.setGridParam({
          url: 'rest/dict',
          postData: {
            app: param.app.id,
            format: 'grid',
            prop: prop
          }
        }).trigger("reloadGrid");
        return dicGrid.setCaption("Dictionary for Application " + param.base.text + " version " + param.app.version);
      }
    };
  });

}).call(this);
