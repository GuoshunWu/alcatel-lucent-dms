// Generated by CoffeeScript 1.6.2
(function() {
  define(['jqgrid', 'dms-util', 'dms-urls', 'i18n!nls/admin', 'i18n!nls/common'], function($, util, urls, i18n, c18n) {
    var afterSubmit, grid;

    afterSubmit = function(response, postdata) {
      var jsonFromServer;

      jsonFromServer = $.parseJSON(response.responseText);
      return [jsonFromServer.status === 0, jsonFromServer.message];
    };
    grid = $('#userGrid').jqGrid({
      url: urls.users,
      datatype: 'json',
      mtype: 'post',
      postData: {
        prop: 'loginName,name,email,lastLoginTime, status, role',
        format: 'grid'
      },
      pager: '#userGridPager',
      rowNum: 30,
      rowList: [15, 30, 60],
      multiselect: true,
      cellEdit: true,
      cellurl: urls.user.update,
      afterSubmitCell: function(serverresponse, rowid, cellname, value, iRow, iCol) {
        var jsonFromServer;

        jsonFromServer = $.parseJSON(serverresponse.responseText);
        return [jsonFromServer.status === 0, jsonFromServer.message];
      },
      editurl: urls.user.update,
      loadtext: 'Loading, please wait...',
      caption: i18n.usergrid.caption,
      autowidth: true,
      viewrecords: true,
      gridview: true,
      multiselect: true,
      sortname: 'lastLoginTime',
      sortorder: 'desc',
      height: '100%',
      colNames: ['Login name', 'Name', 'Email', 'Last login time', 'Enabled', 'Role'],
      colModel: [
        {
          name: 'loginName',
          index: 'loginName',
          width: 100,
          align: 'left'
        }, {
          name: 'name',
          index: 'name',
          width: 100,
          align: 'left'
        }, {
          name: 'email',
          index: 'email',
          width: 150,
          align: 'left'
        }, {
          name: 'lastLoginTime',
          index: 'lastLoginTime',
          width: 150,
          align: 'left'
        }, {
          name: 'userStatus',
          index: 'status',
          width: 50,
          align: 'left',
          editable: true,
          classes: 'editable-column',
          edittype: 'select',
          editoptions: {
            value: "0:Disabled;1:Enabled"
          },
          formatter: 'select'
        }, {
          name: 'role',
          index: 'role',
          width: 200,
          align: 'left',
          editable: true,
          classes: 'editable-column',
          edittype: 'select',
          editoptions: {
            value: i18n.usergrid.roleeditoptions
          },
          formatter: 'select'
        }
      ],
      beforeSubmitCell: function(rowid, celname, value, iRow, iCol) {
        return {
          'loginName': rowid
        };
      },
      afterSubmitCell: function(serverresponse, rowid, cellname, value, iRow, iCol) {
        var json;

        json = $.parseJSON(serverresponse.responseText);
        return [json.status === 0, json.message];
      }
    }).jqGrid('navGrid', '#userGridPager', {
      search: false,
      edit: false,
      add: false,
      view: false,
      del: false
    });
    return grid.navButtonAdd('#userGridPager', {
      id: "custom_add_" + (grid.attr('id')),
      caption: "",
      buttonicon: "ui-icon-plus",
      position: "first",
      onClickButton: function() {
        return $('#addUserDialog').dialog('open');
      }
    });
  });

}).call(this);
