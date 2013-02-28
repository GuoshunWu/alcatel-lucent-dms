// Generated by CoffeeScript 1.5.0
(function() {

  define(function(require) {
    var $, c18n, dialogs, handlers, i18n, prop, taskGrid, util;
    $ = require('jqgrid');
    util = require('util');
    dialogs = require('taskmng/dialogs');
    c18n = require('i18n!nls/common');
    i18n = require('i18n!nls/taskmng');
    require('blockui');
    require('jqmsgbox');
    require('jqupload');
    require('iframetransport');
    handlers = {
      'Download': {
        title: 'Download',
        url: 'task/generate-task-files',
        handler: function(param) {
          var filename;
          filename = "" + ($('#productBase option:selected').text()) + "_" + ($('#productRelease option:selected').text()) + "_translation";
          filename += "_" + (new Date().format('yyyyMMdd_hhmmss')) + ".zip";
          $.blockUI();
          return $.post('task/generate-task-files', {
            id: param.id,
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
            downloadForm = $('#downloadTaskFiles');
            $('#fileLoc', downloadForm).val(json.fileLoc);
            return downloadForm.submit();
          });
        }
      },
      'View…': {
        title: 'View…',
        url: '',
        handler: function(param) {
          dialogs.transReport.data('param', {
            id: param.id,
            viewReport: true
          });
          return dialogs.transReport.dialog('open');
        }
      },
      'Close': {
        title: 'Close',
        url: 'task/close-task',
        handler: function(param) {
          if (param.status === '1') {
            return;
          }
          $.blockUI;
          return $.post('task/close-task', {
            id: param.id
          }, function(json) {
            $.unblockUI();
            if (json.status !== 0) {
              $.msgBox(json.message, null, {
                title: c18n.error
              });
              return;
            }
            return $("#taskGrid").trigger('reloadGrid');
          });
        }
      },
      'Upload': {
        title: 'Upload',
        url: 'task/receive-task-files',
        handler: (function(param) {})
      }
    };
    prop = "name,createTime,lastUpdateTime,status";
    taskGrid = $("#taskGrid").jqGrid({
      mtype: 'POST',
      editurl: "",
      datatype: 'local',
      width: $(window).width() * 0.95,
      height: 400,
      shrinkToFit: false,
      cellactionhandlers: handlers,
      rownumbers: true,
      loadonce: false,
      pager: '#taskPager',
      rowNum: 60,
      rowList: [10, 20, 30, 60, 120],
      sortname: 'createTime',
      sortorder: 'desc',
      viewrecords: true,
      gridview: true,
      multiselect: false,
      cellEdit: true,
      cellurl: '',
      colNames: ['Task', 'Creator', 'Create time', 'Last upload time', 'Status', 'Actions'],
      colModel: [
        {
          name: 'name',
          index: 'name',
          width: 250,
          editable: false,
          stype: 'select',
          align: 'left'
        }, {
          name: 'creator.name',
          index: 'creator.name',
          width: 100,
          editable: false,
          stype: 'select',
          align: 'left'
        }, {
          name: 'createTime',
          index: 'createTime',
          width: 150,
          editable: false,
          align: 'right'
        }, {
          name: 'lastUpdateTime',
          index: 'lastUpdateTime',
          width: 150,
          align: 'left'
        }, {
          name: 'status',
          index: 'status',
          width: 80,
          align: 'left',
          editable: false,
          edittype: 'select',
          editoptions: {
            value: "0:" + i18n.task.open + ";1:" + i18n.task.closed
          },
          formatter: 'select'
        }, {
          name: 'actions',
          index: 'actions',
          width: 260,
          align: 'center',
          formatter: function(cellvalue, options, rowObject) {
            return $.map(handlers, function(value, index) {
              if ('1' === rowObject[4] && (index === 'Upload' || index === 'Close')) {
                return;
              }
              if (index === 'Upload') {
                return "<a id='upload_" + index + "_" + options.rowId + "'title='" + value.title + "' ></a>";
              }
              return "<A id='action_" + index + "_" + options.rowId + "' style='color:blue' title='" + value.title + "'href=# >" + index + "</A>";
            }).join('&nbsp;&nbsp;&nbsp;&nbsp;');
          }
        }
      ],
      beforeProcessing: function(data, status, xhr) {},
      gridComplete: function() {
        var grid;
        grid = $(this);
        handlers = grid.getGridParam('cellactionhandlers');
        $('a[id^=action_]', this).click(function() {
          var a, action, col, rowData, rowid, _ref;
          _ref = this.id.split('_'), a = _ref[0], action = _ref[1], rowid = _ref[2], col = _ref[3];
          rowData = grid.getRowData(rowid);
          delete rowData.actions;
          rowData.id = rowid;
          return handlers[action].handler(rowData);
        });
        $("#progressbar").draggable({
          grid: [50, 20],
          opacity: 0.35
        }).progressbar({
          create: function(e, ui) {
            this.label = $('.progressbar-label', this);
            return $(this).position({
              my: 'center',
              at: 'center',
              of: window
            });
          },
          change: function(e, ui) {
            return this.label.html(($(this).progressbar("value").toPrecision(4)) + "%");
          }
        }).hide();
        $('a[id^=upload_]', this).button({
          label: 'Upload'
        }, {
          create: function(e, ui) {
            var fileInput, rowid, _, _ref;
            _ref = this.id.split('_'), _ = _ref[0], _ = _ref[1], rowid = _ref[2];
            fileInput = $("<input type='file' id='" + this.id + "_fileInput' name='upload' accept='application/zip' multiple/>").css({
              position: 'absolute',
              top: 0,
              right: 0,
              border: '1px solid',
              borderWidth: '10px 180px 40px 20px',
              opacity: 0,
              filter: 'alpha(opacity=0)',
              cursor: 'pointer'
            }).appendTo(this);
            return fileInput.fileupload({
              type: 'POST',
              dataType: 'json',
              url: "task/receive-task-files",
              formData: [
                {
                  name: 'id',
                  value: rowid
                }
              ],
              acceptFileTypes: /zip$/i,
              add: function(e, data) {
                data.submit();
                if (!$.browser.msie) {
                  return $("#progressbar").show();
                }
              },
              progressall: function(e, data) {
                var progress;
                progress = data.loaded / data.total * 100;
                return $('#progressbar').progressbar("value", progress);
              },
              done: function(e, data) {
                var jsonFromServer;
                if (!$.browser.msie) {
                  $("#progressbar").hide();
                }
                jsonFromServer = data.result;
                if (0 !== jsonFromServer.status) {
                  $.msgBox(jsonFromServer.message, null, {
                    title: c18n.error
                  });
                  return;
                }
                dialogs.transReport.data('param', {
                  id: rowid
                });
                return dialogs.transReport.dialog('open');
              }
            });
          }
        }).removeClass().addClass('ui-button').css({
          overflow: 'hidden'
        });
        return $('a[id^=upload_] .ui-button-text').css({
          textDecoration: 'underline',
          color: 'blue'
        });
      },
      afterCreate: function(grid) {
        grid.setGridParam({
          'datatype': 'json'
        });
        return grid.navGrid('#taskPager', {
          edit: false,
          add: false,
          del: false,
          search: false,
          view: false
        });
      }
    });
    taskGrid.getGridParam('afterCreate')(taskGrid);
    return {
      productVersionChanged: function(product) {
        taskGrid = $("#taskGrid");
        prop = "name,creator.name,createTime,lastUpdateTime,status";
        return taskGrid.setGridParam({
          url: 'rest/tasks',
          postData: {
            prod: product.release.id,
            format: 'grid',
            prop: prop
          }
        }).trigger("reloadGrid");
      }
    };
  });

}).call(this);
