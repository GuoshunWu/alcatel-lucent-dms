// Generated by CoffeeScript 1.5.0
(function() {

  define(['jqgrid', 'dms-util', 'dms-urls', 'i18n!nls/common', 'i18n!nls/appmng', 'appmng/langsetting_translation_grid'], function($, util, urls, c18n, i18n, ltgrid) {
    var dicGrid, lastEditedCell;
    lastEditedCell = null;
    dicGrid = $('#stringSettingsGrid').jqGrid({
      url: 'json/dummy.json',
      mtype: 'post',
      datatype: 'local',
      width: 880,
      height: 300,
      pager: '#stringSettingsPager',
      editurl: "",
      rowNum: 10,
      rowList: [10, 20, 30],
      sortorder: 'asc',
      viewrecords: true,
      gridview: true,
      multiselect: true,
      cellEdit: true,
      cellurl: 'app/update-label',
      colNames: ['Label', 'Reference Language', 'T', 'N', 'I', 'Max Length', 'Context', 'Description'],
      colModel: [
        {
          name: 'key',
          index: 'key',
          width: 100,
          editable: false,
          align: 'left'
        }, {
          name: 'reference',
          index: 'reference',
          width: 200,
          edittype: 'textarea',
          editable: false,
          align: 'left'
        }, {
          name: 't',
          index: 't',
          sortable: true,
          width: 15,
          align: 'right',
          formatter: 'showlink',
          formatoptions: {
            baseLinkUrl: '#',
            addParam: encodeURI("&status=2")
          }
        }, {
          name: 'n',
          index: 'n',
          formatter: 'showlink',
          sortable: true,
          width: 15,
          align: 'right',
          formatoptions: {
            baseLinkUrl: '#',
            addParam: encodeURI("&status=0")
          }
        }, {
          name: 'i',
          index: 'i',
          formatter: 'showlink',
          sortable: true,
          width: 15,
          align: 'right',
          formatoptions: {
            baseLinkUrl: '#',
            addParam: encodeURI("&status=1")
          }
        }, {
          name: 'maxLength',
          index: 'maxLength',
          width: 40,
          editable: true,
          classes: 'editable-column',
          align: 'right'
        }, {
          name: 'context',
          index: 'context.name',
          width: 40,
          editable: true,
          classes: 'editable-column',
          align: 'left',
          editrules: {
            required: true
          }
        }, {
          name: 'description',
          index: 'description',
          width: 60,
          editable: true,
          edittype: 'textarea',
          classes: 'editable-column',
          align: 'left'
        }
      ],
      gridComplete: function() {
        var grid;
        grid = $(this);
        $('a', this).each(function(index, a) {
          if ('0' === $(a).text()) {
            return $(a).before(' ').remove();
          }
        });
        return $('a', this).css('color', 'blue').click(function() {
          var param, rowData;
          param = util.getUrlParams(this.href);
          rowData = grid.getRowData(param.id);
          param.key = rowData.key;
          param.ref = rowData.reference;
          $('#stringSettingsTranslationDialog').data({
            param: param
          });
          return $('#stringSettingsTranslationDialog').dialog('open');
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
      afterSubmitCell: function(serverresponse, rowid, cellname, value, iRow, iCol) {
        var json;
        json = $.parseJSON(serverresponse.responseText);
        if ('reference' === cellname && 0 === json.status) {
          $(this).trigger('reloadGrid');
        }
        return [0 === json.status, json.message];
      },
      beforeSubmitCell: function(rowid, cellname, value, iRow, iCol) {}
    }).setGridParam({
      datatype: 'json'
    }).jqGrid('navGrid', '#stringSettingsPager', {
      edit: false,
      add: false,
      del: false,
      search: false,
      view: false
    }, {}, {}, {});
    dicGrid.navButtonAdd('#stringSettingsPager', {
      id: "custom_add_" + (dicGrid.attr('id')),
      caption: c18n.add,
      buttonicon: "ui-icon-plus",
      position: "last",
      onClickButton: function() {
        return $('#addLabelDialog').data('param', {
          dict: dicGrid.getGridParam('postData').dict
        }).dialog('open');
      }
    }).navButtonAdd('#stringSettingsPager', {
      id: "custom_del_" + (dicGrid.attr('id')),
      caption: c18n.del,
      buttonicon: "ui-icon-trash",
      position: "last",
      onClickButton: function() {
        var rowIds;
        if ((rowIds = $(this).getGridParam('selarrrow')).length === 0) {
          $.msgBox(c18n.selrow.format(c18n.label), null, {
            title: c18n.warning
          });
          return;
        }
        return dicGrid.jqGrid('delGridRow', rowIds, {
          msg: i18n.dialog["delete"].delmsg.format(c18n.label),
          url: urls.label.del
        });
      }
    }).setGroupHeaders({
      useColSpanStyle: true,
      groupHeaders: [
        {
          startColumnName: "t",
          numberOfColumns: 3,
          titleText: 'Status'
        }
      ]
    });
    return {
      saveLastEditedCell: function() {
        if (lastEditedCell) {
          return dicGrid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol);
        }
      }
    };
  });

}).call(this);
