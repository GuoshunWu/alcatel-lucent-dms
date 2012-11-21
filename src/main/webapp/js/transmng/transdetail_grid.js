// Generated by CoffeeScript 1.3.3
(function() {

  define(['jqgrid', 'util', 'require', 'i18n!nls/transmng', 'i18n!nls/common'], function($, util, require, i18n, c18n) {
    var lastEditedCell, transDetailGrid;
    lastEditedCell = null;
    transDetailGrid = $("#transDetailGridList").jqGrid({
      url: 'json/transdetailgrid.json',
      mtype: 'POST',
      postData: {},
      editurl: "",
      datatype: 'json',
      width: 'auto',
      height: 200,
      shrinkToFit: false,
      rownumbers: true,
      loadonce: false,
      pager: '#transDetailsPager',
      rowNum: 60,
      rowList: [10, 20, 30, 60, 120],
      sortname: 'key',
      sortorder: 'asc',
      viewrecords: true,
      gridview: true,
      multiselect: true,
      cellEdit: true,
      cellurl: 'trans/update-status',
      ajaxCellOptions: {
        async: false
      },
      colNames: ['Label', 'Max Length', 'Context', 'Reference language', 'Translation', 'Status'],
      colModel: [
        {
          name: 'key',
          index: 'key',
          width: 100,
          editable: false,
          stype: 'select',
          align: 'left',
          frozen: true
        }, {
          name: 'maxlen',
          index: 'maxLength',
          width: 90,
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
          width: 150,
          align: 'left',
          frozen: true,
          search: false
        }, {
          name: 'trans',
          index: 'ct.translation',
          width: 150,
          align: 'left',
          search: false
        }, {
          name: 'transStatus',
          index: 'ct.status',
          width: 150,
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
        }
      ],
      afterEditCell: function(rowid, cellname, val, iRow, iCol) {
        return lastEditedCell = {
          iRow: iRow,
          iCol: iCol,
          name: name,
          val: val
        };
      },
      beforeSubmitCell: function(rowid, cellname, value, iRow, iCol) {
        return {
          type: 'trans'
        };
      },
      afterSubmitCell: function(serverresponse, rowid, cellname, value, iRow, iCol) {
        var jsonFromServer;
        jsonFromServer = eval('(' + serverresponse.responseText + ')');
        return [0 === jsonFromServer.status, jsonFromServer.message];
      },
      afterCreate: function(grid) {
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
    transDetailGrid.getGridParam('afterCreate')(transDetailGrid);
    ($("#translationDetailDialog [id^=detailTrans]").button().click(function() {
      var detailGrid, selectedRowIds;
      detailGrid = $("#transDetailGridList");
      selectedRowIds = detailGrid.getGridParam('selarrrow').join(',');
      return $.post('trans/update-status', {
        type: 'trans',
        transStatus: this.value,
        id: selectedRowIds
      }, function(json) {
        if (json.status !== 0) {
          alert(json.message);
          return;
        }
        return detailGrid.trigger('reloadGrid');
      });
    })).parent().buttonset();
    return {
      languageChanged: function(param) {
        var options, prop, url;
        transDetailGrid = $("#transDetailGridList");
        url = "rest/labels";
        prop = "key,maxLength,context.name,reference,ct.translation,ct.status";
        transDetailGrid.setGridParam({
          url: url,
          datatype: "json",
          postData: {
            dict: param.dict.id,
            language: param.language.id,
            format: 'grid',
            prop: prop,
            idprop: 'ct.id'
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
