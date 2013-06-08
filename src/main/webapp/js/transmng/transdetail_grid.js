// Generated by CoffeeScript 1.6.2
(function() {
  define(['jqgrid', 'jqmsgbox', 'i18n!nls/transmng', 'i18n!nls/common', 'dms-util', 'dms-urls'], function($, msgbox, i18n, c18n, util, urls) {
    var afterCreate, lastEditedCell, matchAction, transDetailGrid;

    lastEditedCell = null;
    /*
      find the labels which reference resemblant to the text and display in a modal dialog
    */

    matchAction = function(refText, transId) {
      var grid, languageId, postData;

      languageId = $('#detailLanguageSwitcher').val();
      $('#transmngMatchTextDialog').dialog('open');
      grid = $("#transMatchTextGrid");
      postData = grid.getGridParam('postData');
      postData.language = languageId;
      postData.text = refText;
      postData.format = 'grid';
      postData.fuzzy = true;
      postData.transId = transId;
      postData.prop = 'reference, translation, score';
      if (typeof console !== "undefined" && console !== null) {
        console.log(postData);
      }
      return grid.setGridParam({
        url: urls.translations,
        page: 1
      }).trigger('reloadGrid');
    };
    transDetailGrid = $("#transDetailGridList").jqGrid({
      url: 'json/transdetailgrid.json',
      mtype: 'POST',
      postData: {},
      editurl: "",
      datatype: 'local',
      width: 'auto',
      height: 200,
      shrinkToFit: false,
      rownumbers: true,
      pager: '#transDetailsPager',
      rowNum: 60,
      rowList: [10, 20, 30, 60, 120],
      viewrecords: true,
      gridview: true,
      multiselect: true,
      cellEdit: true,
      cellurl: urls.trans.update_status,
      ajaxCellOptions: {
        async: false
      },
      colNames: ['Label', 'Max Len.', 'Context', 'Reference language', 'Translation', 'Status', 'TransId', 'Trans.Src', 'Last updated', 'Match'],
      colModel: [
        {
          name: 'key',
          index: 'key',
          width: 120,
          editable: false,
          stype: 'select',
          align: 'left',
          frozen: true
        }, {
          name: 'maxlen',
          index: 'maxLength',
          width: 60,
          editable: false,
          align: 'right',
          frozen: true,
          search: false
        }, {
          name: 'context',
          index: 'context.name',
          width: 80,
          align: 'left',
          frozen: true,
          search: false
        }, {
          name: 'reflang',
          index: 'reference',
          width: 200,
          align: 'left',
          frozen: true,
          search: false
        }, {
          name: 'translation',
          index: 'ct.translation',
          width: 200,
          align: 'left',
          edittype: 'textarea',
          editable: true,
          classes: 'editable-column',
          search: false
        }, {
          name: 'transStatus',
          index: 'ct.status',
          width: 100,
          align: 'left',
          editable: true,
          classes: 'editable-column',
          search: true,
          edittype: 'select',
          editoptions: {
            value: "0:" + i18n.trans.nottranslated + ";1:" + i18n.trans.inprogress + ";2:" + i18n.trans.translated
          },
          formatter: 'select',
          stype: 'select',
          searchoptions: {
            value: ":" + c18n.all + ";0:" + i18n.trans.nottranslated + ";1:" + i18n.trans.inprogress + ";2:" + i18n.trans.translated
          }
        }, {
          name: 'transId',
          index: 'ct.id',
          width: 50,
          align: 'left',
          hidden: true,
          search: false
        }, {
          name: 'transtype',
          index: 'ct.translationType',
          width: 70,
          align: 'left',
          formatter: 'select',
          editoptions: {
            value: i18n.trans.typefilter
          },
          search: true,
          stype: 'select',
          searchoptions: {
            value: i18n.trans.typefilter
          }
        }, {
          name: 'lastUpdate',
          index: 'ct.lastUpdateTime',
          width: 100,
          align: 'left',
          search: false,
          formatter: 'date',
          formatoptions: {
            srcformat: 'ISO8601Long',
            newformat: 'Y-m-d H:i'
          }
        }, {
          name: 'action',
          index: 'action',
          width: 50,
          align: 'center',
          search: false,
          sortable: false,
          hidden: true,
          formatter: function(cellvalue, options, rowObject) {
            var ret;

            ret = "<div id='matchAct_" + rowObject[3] + "_" + rowObject[6] + "' style='display:inline-block' title=\"Match\" class=\"ui-state-default ui-corner-all\">";
            ret += "<span class=\"ui-icon ui-icon-search\"></span></div>";
            return ret;
          },
          unformat: function(cellvalue, options) {
            return "";
          }
        }
      ],
      gridComplete: function() {
        var grid;

        grid = $(this);
        return $('div[id^=matchAct]', this).click(function() {
          var ref, transId, _, _ref;

          _ref = this.id.split('_'), _ = _ref[0], ref = _ref[1], transId = _ref[2];
          grid.getRowData();
          return matchAction(ref, transId);
        }).on('mouseover', function() {
          return $(this).addClass('ui-state-hover');
        }).on('mouseout', function() {
          return $(this).removeClass('ui-state-hover');
        });
      },
      afterEditCell: function(rowid, cellname, val, iRow, iCol) {
        return lastEditedCell = {
          iRow: iRow,
          iCol: iCol,
          name: name,
          val: val
        };
      },
      beforeSubmitCell: function(rowid, cellname, value, iRow, iCol) {
        var ctid;

        ctid = $(this).getRowData(rowid).transId;
        if ('transStatus' === cellname) {
          $(this).setGridParam({
            'cellurl': urls.trans.update_status
          });
          return {
            type: 'trans',
            ctid: ctid
          };
        }
        if ('translation' === cellname) {
          $(this).setGridParam({
            'cellurl': urls.trans.update_translation
          });
          return {
            ctid: ctid
          };
        }
      },
      afterSubmitCell: function(serverresponse, rowid, cellname, value, iRow, iCol) {
        var dictList, json, showMsg;

        json = $.parseJSON(serverresponse.responseText);
        if ('translation' === cellname && 1 === json.status) {
          dictList = "<ul>\n  <li>" + (json.dicts.join('</li>\n  <li>')) + "</li>\n</ul>";
          showMsg = i18n.msgbox.updatetranslation.msg.format(dictList);
          delete json.dicts;
          delete json.message;
          delete json.status;
          $('#transmngTranslationUpdate').html(showMsg).data('param', json).dialog('open');
          return [true, json.message];
        }
        return [0 === json.status, json.message];
      },
      afterCreate: function(grid) {
        grid.setGridParam({
          'datatype': 'json'
        });
        grid.navGrid('#transDetailsPager', {
          edit: false,
          add: false,
          del: false,
          search: false,
          view: false
        });
        return grid.filterToolbar({
          stringResult: true,
          searchOnEnter: false
        });
      }
    });
    afterCreate = transDetailGrid.jqGrid('getGridParam', 'afterCreate');
    if (afterCreate) {
      afterCreate(transDetailGrid);
    }
    $('#makeDetailLabelTranslateStatus').button({
      icons: {
        primary: "ui-icon-triangle-1-n",
        secondary: "ui-icon-gear"
      }
    }).attr('privilegeName', util.urlname2Action(urls.trans.update_status)).click(function(e) {
      var menu;

      menu = $('#detailTranslationStatus').show().width($(this).width()).position({
        my: "left bottom",
        at: "left top",
        of: this
      });
      $(document).one("click", function() {
        return menu.hide();
      });
      return false;
    });
    $('#detailTranslationStatus').menu().hide().find("li").on('click', function(e) {
      var ctIds, detailGrid, ids;

      detailGrid = $("#transDetailGridList");
      ids = detailGrid.getGridParam('selarrrow').join(',');
      ctIds = $.map(ids, function(element, index) {
        return detailGrid.getRowData(element).transId;
      });
      if (!ids) {
        $.msgBox(c18n.selrow.format(c18n.label), null, {
          title: c18n.warning
        });
        return;
      }
      return $.post(urls.trans.update_status, {
        type: 'trans',
        transStatus: e.target.name,
        ctid: ctIds.join(','),
        id: ids
      }, function(json) {
        if (json.status !== 0) {
          $.msgBox(json.message, null, {
            title: c18n.warning
          });
          return;
        }
        detailGrid.trigger('reloadGrid');
        return $("#transGrid").trigger('reloadGrid');
      });
    });
    return {
      languageChanged: function(param) {
        var options, prop, url;

        transDetailGrid = $("#transDetailGridList");
        url = "rest/labels";
        prop = "key,maxLength,context.name,reference,ct.translation,ct.status,ct.id,ct.translationType,ct.lastUpdateTime";
        transDetailGrid.setGridParam({
          url: url,
          datatype: "json",
          postData: {
            dict: param.dict.id,
            language: param.language.id,
            format: 'grid',
            prop: prop
          }
        });
        options = transDetailGrid.getColProp('transStatus').searchoptions;
        options.defaultValue = param.searchStatus;
        transDetailGrid.setColProp('transStatus', {
          searchoptions: options
        });
        $('#gs_transStatus').val(param.searchStatus);
        return transDetailGrid[0].triggerToolbar();
      },
      saveLastEditedCell: function() {
        if (lastEditedCell) {
          transDetailGrid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol);
        }
        if (transDetailGrid.getChangedCells('dirty').length > 0) {
          return $("#transGrid").trigger('reloadGrid');
        }
      }
    };
  });

}).call(this);
