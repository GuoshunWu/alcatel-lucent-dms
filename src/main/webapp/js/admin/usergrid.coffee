define [
  'jqgrid'
  'dms-util'
  'i18n!nls/admin'
], ($, util, i18n)->

  afterSubmit = (response, postdata)->
    jsonFromServer = $.parseJSON response.responseText
    [jsonFromServer.status == 0, jsonFromServer.message]

  grid = $('#userGrid').jqGrid(
#    url: 'rest/users'
    url: 'json/usergrid.json'
    datatype: 'json', mtype: 'post'
    postData: {prop: 'loginName,name,email,lastLoginTime, status, role', format: 'grid'}
    pager: '#userGridPager', rowNum: 30, rowList: [15, 30, 60]
    multiselect: true
    cellEdit: true, cellurl: 'admin/user', afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
      jsonFromServer = $.parseJSON serverresponse.responseText
      [jsonFromServer.status == 0, jsonFromServer.message]
    editurl: 'admin/user'
    loadtext: 'Loading, please wait...', caption: i18n.usergrid.caption
    autowidth: true
    height: '100%'
    colNames: ['Login name', 'Name', 'Email', 'Last login time', 'Enabled', 'Role']
    colModel: [
      {name: 'loginName', index: 'loginName', width: 100, align: 'left'}
      {name: 'name', index: 'name', width: 100, align: 'left'}
      {name: 'email', index: 'email', width: 150, align: 'left'}
      {name: 'lastLoginTime', index: 'lastLoginTime', width: 150, align: 'left'}
      {name: 'enabled', index: 'status', width: 50, align: 'left',editable:true, classes: 'editable-column'
      edittype: 'select' , editoptions: {value: "0:Disabled;1:Enabled"}, formatter: 'select'
      }
      {name: 'role', index: 'role', width: 200, align: 'left',editable:true, classes: 'editable-column', edittype: 'select'
      editoptions: {value: "0:GUEST;1:APPLICATION_OWNER;2:TRANSLATION_MANAGER;3:APPLICATION_OWNER + TRANSLATION_MANAGER;4:ADMINISTRATOR"}
      formatter: 'select'
      }
    ]
  ).jqGrid('navGrid', '#userGridPager', {search: false, edit: false, add: false, view: false}
  )
