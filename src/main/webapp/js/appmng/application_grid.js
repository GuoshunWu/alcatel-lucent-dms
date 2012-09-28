// Generated by CoffeeScript 1.3.3
(function() {

  define(['require', 'appmng/apptree', 'appmng/product_panel'], function(require, apptree, prodpnl) {
    var $, URL, appGrid, dialogs, i18n, localIds;
    $ = require('jqgrid');
    i18n = require('i18n!nls/appmng');
    dialogs = require('appmng/dialogs');
    URL = {
      get_application_by_product_id: 'rest/applications'
    };
    localIds = {
      app_grid: '#applicationGridList'
    };
    appGrid = $(localIds.app_grid).jqGrid({
      datatype: 'json',
      url: 'json/appgrid.json',
      editurl: "app/create-or-add-application",
      cellurl: 'app/change-application-version',
      cellsubmit: 'remote',
      cellEdit: true,
      ajaxSelectOptions: 'json/selecttest.json',
      width: 700,
      height: 350,
      pager: '#pager',
      rowNum: 10,
      rowList: [10, 20, 30],
      sortname: 'name',
      sortorder: 'asc',
      viewrecords: true,
      gridview: true,
      altRows: true,
      caption: 'Applications for Product',
      colNames: ['ID', 'Application', 'Version', 'Dict. Num.'],
      colModel: [
        {
          name: 'id',
          index: 'id',
          width: 55,
          align: 'center',
          editable: false,
          hidden: true
        }, {
          name: 'name',
          index: 'name',
          width: 100,
          editable: false,
          align: 'center'
        }, {
          name: 'version',
          index: 'version',
          width: 90,
          editable: true,
          align: 'center',
          edittype: 'select',
          editoptions: {
            value: {}
          }
        }, {
          name: 'dictNum',
          index: 'dictNum',
          width: 80,
          editable: false,
          align: 'center'
        }
      ],
      afterEditCell: function(id, name, val, iRow, iCol) {
        if (name === 'version') {
          return $.ajax({
            url: "rest/applications/appssamebase/" + id,
            async: false,
            dataType: 'json',
            success: function(json) {
              return $("#" + iRow + "_version", localIds.app_grid).append($(json).map(function() {
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
        return {
          productId: prodpnl.getSelectedProduct().id,
          newAppId: value
        };
      },
      afterSubmitCell: function(serverresponse, rowid, cellname, value, iRow, iCol) {
        var jsonFromServer;
        jsonFromServer = eval("(" + serverresponse.responseText + ")");
        return [jsonFromServer.status === 0, jsonFromServer.message];
      }
    });
    appGrid.jqGrid('navGrid', '#pager', {
      edit: false,
      add: false,
      del: true,
      search: false,
      view: false
    }, {}, {}, {
      reloadAfterSubmit: false,
      url: 'app/remove-application',
      beforeShowForm: function(form) {
        var permanent;
        permanent = $('#permanentDeleteSignId', form);
        if (permanent.length === 0) {
          $("<tr><td>" + i18n.grid.permanenttext + "<td><input align='left' type='checkbox' id='permanentDeleteSignId'>").appendTo($("tbody", form));
        }
        return permanent != null ? permanent.removeAttr('checked') : void 0;
      },
      onclickSubmit: function(params, posdata) {
        var product;
        product = prodpnl.getSelectedProduct();
        return {
          productId: product.id,
          permanent: Boolean($('#permanentDeleteSignId').attr("checked"))
        };
      },
      afterSubmit: function(response, postdata) {
        var jsonFromServer;
        jsonFromServer = eval("(" + response.responseText + ")");
        if (jsonFromServer.id) {
          apptree.delApplictionBaseFromProductBase(jsonFromServer.id);
        }
        return [0 === jsonFromServer.status, jsonFromServer.message];
      }
    });
    appGrid.navButtonAdd('#pager', {
      caption: "",
      buttonicon: "ui-icon-plus",
      position: "first",
      onClickButton: function() {
        return dialogs.newOrAddApplication.dialog("open");
      }
    });
    return {
      id: localIds,
      productChanged: function(product) {
        var productBase, url;
        url = "" + URL.get_application_by_product_id + "?prod=" + product.id + "&format=grid&prop=id,name,version,dictNum";
        appGrid.setGridParam({
          url: url,
          datatype: "json"
        }).trigger("reloadGrid");
        productBase = require('appmng/apptree').getSelected();
        return appGrid.setCaption("Applications for Product " + productBase.text + " version " + product.version);
      }
    };
  });

}).call(this);
