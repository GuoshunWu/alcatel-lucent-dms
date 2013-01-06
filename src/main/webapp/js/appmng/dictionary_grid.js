// Generated by CoffeeScript 1.3.3
(function() {

  define(function(require, util, dialogs, i18n) {
    var $, blockui, c18n, colModel, deleteOptions, dicGrid, handlers, lastEditedCell;
    $ = require('jqgrid');
    util = require('util');
    dialogs = require('appmng/dialogs');
    i18n = require('i18n!nls/appmng');
    require('jqmsgbox');
    c18n = require('i18n!nls/common');
    blockui = require('blockui');
    deleteOptions = {
      msg: i18n.dialog["delete"].delmsg.format(c18n.dict),
      top: 250,
      left: 550,
      reloadAfterSubmit: false,
      url: 'app/remove-dict',
      beforeShowForm: function(form) {
        var permanent;
        permanent = $('#permanentDeleteSignId', form);
        if (permanent.length === 0) {
          return $("<tr><td>" + i18n.grid.permanenttext + "<td><input align='left'checked type='checkbox' id='permanentDeleteSignId'>").hide().appendTo($("tbody", form));
        }
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
          dialogs.langSettings.on('dialogopen', {
            param: rowData
          }, $('#languageSettingsDialog').dialog('option', 'openEvent'));
          return dialogs.langSettings.dialog('open');
        }
      },
      'X': {
        title: i18n.dialog["delete"].title,
        handler: function(rowData) {
          return $('#dictionaryGridList').jqGrid('delGridRow', rowData.id, deleteOptions);
        }
      }
    };
    lastEditedCell = null;
    colModel = [
      {
        name: 'langrefcode',
        index: 'langrefcode',
        width: 55,
        align: 'left',
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
        classes: 'editable-column',
        edittype: 'select',
        editoptions: {
          value: {}
        },
        align: 'left'
      }, {
        name: 'format',
        index: 'base.format',
        width: 60,
        editable: true,
        edittype: 'select',
        editoptions: {
          value: "DCT:DCT;Dictionary conf:Dictionary conf;Text properties:Text properties;XML labels:XML labels"
        },
        align: 'left'
      }, {
        name: 'encoding',
        index: 'base.encoding',
        width: 40,
        editable: true,
        edittype: 'select',
        editoptions: {
          value: 'ISO-8859-1:ISO-8859-1;UTF-8:UTF-8;UTF-16LE:UTF-16LE;UTF-16BE:UTF-16BE'
        },
        align: 'left'
      }, {
        name: 'labelNum',
        index: 'labelNum',
        width: 20,
        align: 'right'
      }, {
        name: 'action',
        index: 'action',
        width: 80,
        editable: false,
        align: 'center'
      }
    ];
    $(colModel).each(function(index, colModel) {
      if (colModel.editable) {
        return colModel.classes = 'editable-column';
      }
    });
    dicGrid = $('#dictionaryGridList').jqGrid({
      url: 'json/dummy.json',
      datatype: 'local',
      width: 1000,
      height: 320,
      pager: '#dictPager',
      editurl: "app/create-or-add-application",
      rowNum: 999,
      loadonce: false,
      sortname: 'base.name',
      sortorder: 'asc',
      viewrecords: true,
      cellEdit: true,
      cellurl: 'app/update-dict',
      ajaxCellOptions: {
        async: false
      },
      gridview: true,
      multiselect: true,
      caption: 'Dictionary for Application',
      colNames: ['LangRefCode', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Action'],
      colModel: colModel,
      beforeProcessing: function(data, status, xhr) {
        var actIndex, actions, k, v;
        actIndex = $.inArray('Action', $(this).getGridParam('colNames'));
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
        lastEditedCell = {
          iRow: iRow,
          iCol: iCol,
          name: name,
          val: val
        };
        grid = this;
        if (name === 'version') {
          return $.ajax({
            url: "rest/dict?slibing=" + id + "&prop=id,version",
            async: false,
            dataType: 'json',
            success: function(json) {
              return $("#" + iRow + "_version", grid).append(util.json2Options(json, val));
            }
          });
        }
      },
      beforeSubmitCell: function(rowid, cellname, value, iRow, iCol) {
        var isVersion;
        isVersion = cellname === 'version';
        $(this).setGridParam({
          cellurl: isVersion ? 'app/change-dict-version' : 'app/update-dict'
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
          if (lastEditedCell) {
            grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol);
          }
          rowData = grid.getRowData(rowid);
          delete rowData.action;
          rowData.id = rowid;
          return handlers[action].handler(rowData);
        });
      }
    }).jqGrid('navGrid', '#dictPager', {
      add: false,
      edit: false,
      search: false,
      del: false
    }, {}, {}, deleteOptions).navButtonAdd('#dictPager', {
      caption: "",
      buttonicon: "ui-icon-trash",
      position: "first",
      onClickButton: function() {
        var rowIds;
        if ((rowIds = $(this).getGridParam('selarrrow')).length === 0) {
          $.msgBox(c18n.selrow.format(c18n.dict), null, {
            title: c18n.warning
          });
          return;
        }
        return $(this).jqGrid('delGridRow', rowIds, deleteOptions);
      }
    }).setGridParam({
      datatype: 'json'
    });
    $('#generateDict').button().width(170).attr('privilegeName', util.urlname2Action('app/deliver-app-dict')).click(function() {
      var dicts, filename, oldLabel,
        _this = this;
      dicts = dicGrid.getGridParam('selarrrow');
      if (!dicts || dicts.length === 0) {
        $.msgBox(c18n.selrow.format(c18n.dict), null, {
          title: c18n.warning
        });
        return;
      }
      filename = "" + ($('#appDispAppName').text()) + "_" + ($('#selAppVersion option:selected').text()) + "_" + (new Date().format('yyyyMMdd_hhmmss')) + ".zip";
      $(this).button('disable');
      oldLabel = $(this).button('option', 'label');
      $(this).button('option', 'label', i18n.generating);
      return $.post('app/generate-dict', {
        dicts: dicts.join(','),
        filename: filename
      }, function(json) {
        $(_this).button('option', 'label', oldLabel);
        $(_this).button('enable');
        if (json.status !== 0) {
          $.msgBox(json.message, null, {
            title: c18n.error
          });
          return;
        }
        return window.location.href = "app/download-app-dict.action?fileLoc=" + json.fileLoc;
      });
    });
    $('#batchAddLanguage').button().attr('privilegeName', util.urlname2Action('app/add-dict-language')).click(function() {
      var dicts;
      dicts = dicGrid.getGridParam('selarrrow');
      if (!dicts || dicts.length === 0) {
        $.msgBox(c18n.selrow.format(c18n.dict), null, {
          title: c18n.warning
        });
        return;
      }
      $('#addLanguageDialog').data('param', {
        dicts: dicts
      });
      return $('#addLanguageDialog').dialog('open');
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
