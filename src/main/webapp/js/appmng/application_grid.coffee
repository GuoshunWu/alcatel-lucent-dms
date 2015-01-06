define [
  'i18n!nls/appmng'
  'dms-urls'
  'dms-util'
  'jqgrid'
],(
  i18n
  urls
  util
  $
)->
  #  console?.log "module appmng/application_grid loading."

  gridId = 'applicationGridList'
  hGridId = '#' + gridId
  pagerId =  gridId + '_' + 'Pager'
  hPagerId = '#' + pagerId

  appGrid = $(hGridId).after("<div id='#{pagerId}'>").jqGrid(
    datatype: 'local', mtype: 'post', url: urls.apps
    editurl: "app/create-or-add-application"
    cellurl: 'app/change-application-version'
    cellactionurl: 'app/add-application'
    cellsubmit: 'remote', cellEdit: true
    widht: 700
    height: '100%'
    pager: hPagerId, rowNum: 20, rowList: [10, 20, 30], multiselect: true
    viewrecords: true
#    gridview: true

    sortname: 'name', sortorder: 'asc'
    caption: i18n.grid.appsforprod
    colNames: ['ID', 'Application', 'Version', 'Dict. Num.']
    colModel: [
      {name: 'id', index: 'id', width: 55, align: 'center', editable: false, hidden: true}
      {name: 'name', index: 'name', width: 100, editable: false, align: 'left'}
      {name: 'version', index: 'version', width: 90, editable: true, classes: 'editable-column', align: 'left', edittype: 'select', editoptions: {value: {}}}
      {name: 'dictNum', index: 'dictNum', width: 80, editable: false, align: 'right'}
    ]
    afterEditCell: (id, name, val, iRow, iCol)->
      if name == 'version'
        $.ajax {url: "rest/applications/appssamebase/#{id}", async: false, dataType: 'json', success: (json)->
          $("##{iRow}_version", '#applicationGridList').append util.json2Options json, val
        }
    beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
      prodpnl = require 'appmng/product_panel'
      {productId: prodpnl.getSelectedProduct().id, newAppId: value}

    afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
      jsonFromServer = eval "(#{serverresponse.responseText})"
      [jsonFromServer.status == 0, jsonFromServer.message]
  ).setGridParam(datatype: 'json')
  .jqGrid('navGrid', hPagerId, {edit: false, add: false, del: true, search: false, view: false}, {}, {}, {
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

  appGrid.navButtonAdd(hPagerId, {id: "custom_add_#{appGrid.attr 'id'}", caption: "", buttonicon: "ui-icon-plus", position: "first", onClickButton: ()->
    $("#addApplicationDialog").dialog "open"
  })

  productChanged: (param)->
    appGrid.setCaption "Applications for Product #{param.base.text} version #{param.product.version}"
    postData = {prod: param.product.id, format: 'grid', prop: 'id,name,version,dictNum'}
    appGrid.setGridParam(postData: postData).trigger("reloadGrid")


