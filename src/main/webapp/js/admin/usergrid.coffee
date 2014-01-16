define [
  'jqgrid'
  'dms-util'
  'dms-urls'

  'i18n!nls/admin'
  'i18n!nls/common'
], ($, util, urls, i18n)->

  grid = $('#userGrid').jqGrid(
    url: urls.users
    datatype: 'json', mtype: 'post'
    postData: {prop: 'loginName,name,email,lastLoginTime, status, role, onLine,loginCounter', format: 'grid'}
    pager: '#userGridPager', rowNum: 30, rowList: [15, 30, 60]
    multiselect: true
    cellEdit: true, cellurl: urls.user.update, afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
      jsonFromServer = $.parseJSON serverresponse.responseText
      [jsonFromServer.status == 0, jsonFromServer.message]
    editurl: urls.user.update
    loadtext: 'Loading, please wait...', caption: i18n.usergrid.caption
    autowidth: true
    viewrecords: true, gridview: true, multiselect: true
    sortname: 'lastLoginTime', sortorder: 'desc'
    height: '100%'
    colNames: ['Login name', 'Name', 'Email', 'Last login time', 'Enabled', 'Role', 'IsOnLine', 'Login Counter']
    colModel: [
      {name: 'loginName', index: 'loginName', width: 100, align: 'left'}
      {name: 'name', index: 'name', width: 100, align: 'left'}
      {name: 'email', index: 'email', width: 150, align: 'left'}
      {name: 'lastLoginTime', index: 'lastLoginTime',formatter:'date',formatoptions:{srcformat: 'ISO8601Long', newformat: 'ISO8601Long'}, width: 150, align: 'left'}
      {name: 'userStatus', index: 'status', width: 50, align: 'left',editable:true, classes: 'editable-column'
      edittype: 'select' , editoptions: {value: "0:Disabled;1:Enabled"}, formatter: 'select'
      }
      {name: 'role', index: 'role', width: 200, align: 'left',editable:true, classes: 'editable-column', edittype: 'select'
      editoptions: {value: i18n.usergrid.roleeditoptions}
      formatter: 'select'
      }
      {name: 'onLine', index: 'onLine', sortable: false, width: 50, align: 'center', formatter: (cellvalue)->
        picName = if cellvalue.toLowerCase() == 'true'then 'online' else 'offline'
        "<span>#{picName}&nbsp;<img src='images/#{picName}16.png'></span>"
      unformat:(cellvalue, options, cell)-> -1 isnt cellvalue.indexOf("online") + ""
      }
      {name: 'loginCounter', index: 'loginCounter', width: 60, align: 'right'}
    ]
    beforeSubmitCell:(rowid,celname,value,iRow,iCol)->
      'loginName':rowid

    afterSubmitCell:(serverresponse, rowid, cellname, value, iRow, iCol)->
      json = $.parseJSON(serverresponse.responseText)
      [json.status == 0, json.message]

  ).jqGrid('navGrid', '#userGridPager', {search: false, edit: false, add: false, view: false, del: false})

  grid.navButtonAdd('#userGridPager', {id: "custom_add_#{grid.attr 'id'}", caption: "", buttonicon: "ui-icon-plus", position: "first", onClickButton: ()->
    $('#addUserDialog').dialog('open')
  })
