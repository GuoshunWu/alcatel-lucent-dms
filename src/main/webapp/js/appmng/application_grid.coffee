localIds = {
app_grid: '#applicationGridList'
}
$ ()->
  appGrid = $(localIds.app_grid).jqGrid ({
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
      select = $("##{iRow}_version", localIds.app_grid)
      url = "rest/applications/appssamebase/#{id}"
      $.getJSON url, {}, (json)->
        select.append $(json).map ()-> opt = new Option(this.version, this.id);opt.selected = (this.version == val);opt
  beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
    productSelect = $ "#{exports.product_panel.select_product_version_id}"
    changeSelect = $("##{iRow}_version", localIds.app_grid)
    {productId: productSelect.val(), newAppId: changeSelect.val()}
  afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
    jsonFromServer = eval('(' + serverresponse.responseText + ')')
    [0 == jsonFromServer.status, jsonFromServer.message]
  })
  appGrid.jqGrid('navGrid', '#pager', {edit: false, add: false, del: false, search: false, view: false})
  appGrid.navButtonAdd '#pager', { caption: "", buttonicon: "ui-icon-trash", position: "first"
  onClickButton: ()->
    appGrid = $(localIds.app_grid)
    gr = appGrid.jqGrid('getGridParam', 'selrow')
    if (null == gr)
      $.msgBox "Please Select Row to delete!", null, {title: 'Select Row', width: 300, height: 'auto' }
      false
    appGrid.jqGrid 'delGridRow', gr, { mtype: 'post', editData: [], recreateForm: false, modal: true, jqModal: true, reloadAfterSubmit: false
    url: 'app/remove-application'
    beforeShowForm: (form)->
      permanent = $('#permanentDeleteSignId', form)
      if (0 == permanent.length)
        $("<tr><td>Delete permanently</td><td><input align='left' type='checkbox' id='permanentDeleteSignId'></td></tr>").appendTo $("tbody", form)
      else
        permanent.removeAttr "checked"
    onclickSubmit: (params, posdata)->
      productSelect = $ "#{exports.product_panel.select_product_version_id}"
      {productId: productSelect.val(), permanent: Boolean($('#permanentDeleteSignId').attr("checked"))}
    afterSubmit: (response, postdata)->
      jsonFromServer = eval "(#{response.responseText})"
      log jsonFromServer
      if jsonFromServer.id #appbase is deleted
      #remove appbase node from apptree.
        appTree = $.jstree._reference("#appTree")
        appTree._get_children (appTree.get_selected()).each (index, app)-> appTree.delete_node(app) if app.id == jsonFromServer.id
      [0 == jsonFromServer.status, jsonFromServer.message]
    }
  }
  appGrid.navButtonAdd '#pager', { caption: "", buttonicon: "ui-icon-plus", position: "first"
  onClickButton: ()->
    $("##{ids.dialog.new_or_add_application}").dialog "open"
  }

exports.application_grid.application_grid_id = localIds.app_grid