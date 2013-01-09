define ['jqgrid', 'i18n!nls/appmng', 'appmng/dialogs', 'util', 'require'], ($, i18n, dialogs, util, require)->
  URL = {
  # get application in product by product id
  get_application_by_product_id: 'rest/applications'
  }

  localIds = {
  app_grid: '#applicationGridList'
  }

  appGrid = $(localIds.app_grid).jqGrid(
    datatype: 'local'
    url: 'json/dummy.json'
    editurl: "app/create-or-add-application"
    cellurl: 'app/change-application-version'
    cellactionurl: 'app/add-application'
    cellsubmit: 'remote', cellEdit: true
    width: 700, height: 350
    pager: '#pager', rowNum: 10, rowList: [10, 20, 30], multiselect: true
    sortname: 'name', sortorder: 'asc'
    viewrecords: true, gridview: true, altRows: true
    caption: 'Applications for Product'
    colNames: ['ID', 'Application', 'Version', 'Dict. Num.']

    colModel: [
      {name: 'id', index: 'id', width: 55, align: 'center', editable: false, hidden: true}
      {name: 'name', index: 'name', width: 100, editable: false, align: 'left'}
      {name: 'version', index: 'version', width: 90, editable: true, classes: 'editable-column', align: 'left', edittype: 'select', editoptions: {value: {}}}
      {name: 'dictNum', index: 'dictNum', width: 80, editable: false, align: 'right'}
    ],
    afterEditCell: (id, name, val, iRow, iCol)->
      if name == 'version'
        $.ajax {url: "rest/applications/appssamebase/#{id}", async: false, dataType: 'json', success: (json)->
          $("##{iRow}_version", localIds.app_grid).append util.json2Options json, val
        }
    beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
      prodpnl = require 'appmng/product_panel'
      {productId: prodpnl.getSelectedProduct().id, newAppId: value}

    afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
      jsonFromServer = eval "(#{serverresponse.responseText})"
      [jsonFromServer.status == 0, jsonFromServer.message]
  ).setGridParam(datatype: 'json')
  .jqGrid('navGrid', '#pager', {edit: false, add: false, del: true, search: false, view: false}, {}, {}, {
    #  delete form properties
    reloadAfterSubmit: false, url: 'app/remove-application'
    beforeShowForm: (form)->
    #    permanent = $('#permanentDeleteSignId', form)
    #    $("<tr><td>#{i18n.grid.permanenttext}<td><input align='left' type='checkbox' id='permanentDeleteSignId'>")
    #    .appendTo $("tbody", form) if permanent.length == 0
    #    permanent?.removeAttr 'checked'
    onclickSubmit: (params, posdata)->
      prodpnl = require 'appmng/product_panel'
      product = prodpnl.getSelectedProduct()
      #    permanent = Boolean $('#permanentDeleteSignId').attr "checked"
      {productId: product.id}
    afterSubmit: (response, postdata)->
      jsonFromServer = eval "(#{response.responseText})"
      #remove appbase node from apptree.
      [0 == jsonFromServer.status, jsonFromServer.message]
    })

  appGrid.navButtonAdd('#pager', {id: "custom_add_#{appGrid.attr 'id'}", caption: "", buttonicon: "ui-icon-plus", position: "first", onClickButton: ()->
    dialogs.addApplication.dialog "open"
  })


  id: localIds
  productChanged: (param)->
    appGrid.setCaption "Applications for Product #{param.base.text} version #{param.product.version}"
    appGrid.setGridParam(url: URL.get_application_by_product_id, postData: {prod: param.product.id, format: 'grid', prop: 'id,name,version,dictNum'}
    ).trigger("reloadGrid")


