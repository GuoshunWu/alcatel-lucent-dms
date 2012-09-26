define ['jqgrid', 'require', 'util', 'appmng/dialogs', 'i18n!nls/appmng'], ($, require, util, dialogs, i18n)->
  localIds = {
  dic_grid: '#dictionaryGridList'
  }

  languageSetting = (rowData)->
    dialogs.langSettings.data "param", {dictId: rowData.id, refCode: rowData.langrefcode}
    dialogs.langSettings.dialog 'open'
  stringSetting = (rowData)->
  #    dialogs.stringsettings.data "param", {dictId: rowData.id, refCode: rowData.langrefcode}
    dialogs.stringSettings.dialog 'open'

  deleteRow = (rowid)->
    dicGrid = $(localIds.dic_grid)
    dicGrid.jqGrid 'delGridRow', rowid, { mtype: 'post', editData: [], recreateForm: false, modal: true, jqModal: true, reloadAfterSubmit: false
    url: 'http://127.0.0.1:2000'
    beforeShowForm: (form)->
      permanent = $('#permanentDeleteSignId', form)
      if (0 == permanent.length)
        $("<tr><td>#{i18n.grid.permanenttext}</td><td><input align='left' type='checkbox' id='permanentDeleteSignId'></td></tr>").appendTo $("tbody", form)
      else
        permanent.removeAttr "checked"
    onclickSubmit: (params, posdata)->
      {appId: $("#selAppVersion").val(), permanent: Boolean($('#permanentDeleteSignId').attr("checked"))}
    afterSubmit: (response, postdata)->
      jsonFromServer = eval "(#{response.responseText})"
      #appbase is deleted
      #remove dictionary base.
      [0 == jsonFromServer.status, jsonFromServer.message]
    }


  dicGrid = $(localIds.dic_grid).jqGrid {
  url: ''
  datatype: 'json'
  width: 1000
  height: 350
  pager: '#dictPager'
  editurl: "app/create-or-add-application"
  rowNum: 10
  rowList: [10, 20, 30]
  sortname: 'base.name'
  sortorder: 'asc'
  viewrecords: true, cellEdit: true, cellurl: 'http://127.0.0.1:2000'
  gridview: true
  caption: 'Dictionary for Application'
  colNames: ['LangRefCode', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Action']
  colModel: [
    {name: 'langrefcode', index: 'langrefcode', width: 55, align: 'center', hidden: true}
    {name: 'name', index: 'base.name', width: 200, editable: false, align: 'left'}
    {name: 'version', index: 'version', width: 25, editable: true, edittype: 'select', editoptions: {value: {}}, align: 'center'}
    {name: 'format', index: 'base.format', width: 60, editable: true, edittype: 'select',
    editoptions: {value: "DCT:DCT;Dictionary conf:Dictionary conf;Text properties:Text properties;XML labels:XML labels"}, align: 'center'}
    {name: 'encoding', index: 'base.encoding', width: 40, editable: true, edittype: 'select',
    editoptions: {value: 'ISO-8859-1:ISO-8859-1;UTF-8:UTF-8;UTF-16LE:UTF-16LE;UTF-16BE:UTF-16BE'}, align: 'center'}
    {name: 'labelNum', index: 'labelNum', width: 20, align: 'center'}
    {name: 'action', index: 'action', width: 43, editable: false, align: 'center'}
  ]
  beforeProcessing: (data, status, xhr)->
    actIndex = $(@).getGridParam('colNames').indexOf('Action')
    $(data.rows).each (index)->
      rowData = @
      @cell[actIndex] = ($(['S', 'L', 'X']).map ->"<A id='action_#{@}_#{rowData.id}_#{actIndex}' href=# >#{@}</A>").get().join('')
  afterEditCell: (id, name, val, iRow, iCol)->
  #    if name == 'version'
  #      console.log "name=#{name},id=#{id},val=#{val}"
  #      $.ajax {url: "rest/applications/appssamebase/#{id}", async: false, dataType: 'json', success: (json)->
  #        $("##{iRow}_version", localIds.app_grid).append $(json).map ()->opt = new Option(@version, @id);opt.selected = @version == val; opt
  #      }
  beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
  #    productpnl = require 'appmng/product_panel'
  #    product = productpnl.getSelectedProduct()
  #    changeSelect = $("##{iRow}_version", localIds.app_grid)
  #    #    add the product id and the application id changed to.
  #    {productId: product.id, newAppId: changeSelect.val()}

  afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
    jsonFromServer = eval "('#{serverresponse.responseText}')"
    [0 == jsonFromServer.status, jsonFromServer.message]
  gridComplete: ->
    $('a[id^=action_]', @).button {
    create: (e, ui)->
      [a, action, rowid, col]=@id.split('_')
      titles =
        S: i18n.dialog.stringsettings.title
        L: i18n.dialog.languagesettings.title
        X: i18n.dialog.delete.title
      @title = titles[action]
      @onclick = (e)->
        rowData = $('#dictionaryGridList').getRowData(rowid)
        delete rowData.action
        rowData.id = rowid
        switch action
          when 'S'
            stringSetting rowData
          when 'L'
            languageSetting rowData
          when 'X'
            deleteRow rowid
          else
            console.log 'Invalid action'
    }
  }
  dicGrid.jqGrid('navGrid', '#dictPager', {edit: false, add: true, del: false, search: false, view: false})

  appChanged: (app)->
    url = "rest/dict?app=#{app.id}&format=grid&prop=languageReferenceCode,base.name,version,base.format,base.encoding,labelNum"
    dicGrid.setGridParam({url: url, datatype: "json"}).trigger("reloadGrid")
    appBase = require('appmng/apptree').getSelected()
    dicGrid.setCaption "Dictionary for Application #{appBase.text} version #{app.version}"
