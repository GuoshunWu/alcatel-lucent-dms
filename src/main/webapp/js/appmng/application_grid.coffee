define ['require', 'appmng/apptree', 'appmng/product_panel'], (require, apptree, prodpnl)->
  $ = require 'jqgrid'
  i18n = require 'i18n!nls/appmng'
  dialogs = require 'appmng/dialogs'

  URL = {
  # get application in product by product id
  get_application_by_product_id: 'rest/applications'
  }

  localIds = {
  app_grid: '#applicationGridList'
  }

  appGrid = $(localIds.app_grid).jqGrid {
  datatype: 'json'
  url: 'json/appgrid.json'
  editurl: "app/create-or-add-application"
  cellurl: 'app/change-application-version'
  cellsubmit: 'remote', cellEdit: true
  ajaxSelectOptions: 'json/selecttest.json'
  width: 700, height: 350
  pager: '#pager', rowNum: 10, rowList: [10, 20, 30]
  sortname: 'name', sortorder: 'asc'
  viewrecords: true, gridview: true, altRows: true
  caption: 'Applications for Product'
  colNames: ['ID', 'Application', 'Version', 'Dict. Num.']

  colModel: [
    {name: 'id', index: 'id', width: 55, align: 'center', editable: false, hidden: true}
    {name: 'name', index: 'name', width: 100, editable: false, align: 'center'}
    {name: 'version', index: 'version', width: 90, editable: true, align: 'center', edittype: 'select', editoptions: {value: {}}}
    {name: 'dictNum', index: 'dictNum', width: 80, editable: false, align: 'center'}
  ],
  afterEditCell: (id, name, val, iRow, iCol)->
    if name == 'version'
      $.ajax {url: "rest/applications/appssamebase/#{id}", async: false, dataType: 'json', success: (json)->
        $("##{iRow}_version", localIds.app_grid).append $(json).map ()->opt = new Option(@version, @id);opt.selected = @version == val; opt
      }
  beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
    {productId: prodpnl.getSelectedProduct().id, newAppId: value}

  afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
    jsonFromServer = eval "(#{serverresponse.responseText})"
    [jsonFromServer.status == 0, jsonFromServer.message]
  }
  appGrid.jqGrid 'navGrid', '#pager', {edit: false, add: false, del: true, search: false, view: false}, {}, {}, {
  #  delete form properties
  reloadAfterSubmit: false, url: 'app/remove-application'
  beforeShowForm: (form)->
    permanent = $('#permanentDeleteSignId', form)
    $("<tr><td>#{i18n.grid.permanenttext}<td><input align='left' type='checkbox' id='permanentDeleteSignId'>")
    .appendTo $("tbody", form) if permanent.length == 0
    permanent?.removeAttr 'checked'
  onclickSubmit: (params, posdata)->
    product = prodpnl.getSelectedProduct()
    {productId: product.id, permanent: Boolean($('#permanentDeleteSignId').attr("checked"))}
  afterSubmit: (response, postdata)->
    jsonFromServer = eval "(#{response.responseText})"
    #remove appbase node from apptree.
    apptree.delApplictionBaseFromProductBase jsonFromServer.id if jsonFromServer.id
    #appbase is deleted
    [0 == jsonFromServer.status, jsonFromServer.message]
  }
  appGrid.navButtonAdd '#pager', { caption: "", buttonicon: "ui-icon-plus", position: "first"
  onClickButton: ()->
    dialogs.newOrAddApplication.dialog "open"
  }
  id: localIds
  productChanged: (product)->
    url = "#{URL.get_application_by_product_id}?prod=#{product.id}&format=grid&prop=id,name,version,dictNum"
    appGrid.setGridParam({url: url, datatype: "json"}).trigger("reloadGrid")
    productBase = require('appmng/apptree').getSelected()
    appGrid.setCaption "Applications for Product #{productBase.text} version #{product.version}"
