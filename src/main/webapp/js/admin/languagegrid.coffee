define ['jqgrid', 'dms-util', 'i18n!nls/admin'], ($, util, i18n)->
  afterSubmit = (response, postdata)->
    jsonFromServer = $.parseJSON response.responseText
    [jsonFromServer.status == 0, jsonFromServer.message]

  grid = $('#languageGrid').jqGrid(
    url: 'rest/languages', postData: {prop: 'name,defaultCharset', format: 'grid'}, datatype: 'json', mtype: 'post'
    pager: '#languagePager', rowNum: 15, rowList: [15, 30, 60]
    multiselect: true
    cellEdit: true, cellurl: 'admin/language'

    afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
      jsonFromServer = $.parseJSON serverresponse.responseText
      [jsonFromServer.status == 0, jsonFromServer.message]
    editurl: 'admin/language'
    loadtext: 'Loading, please wait...', caption: i18n.langgrid.caption
    autowidth: true
    height: '100%'
    colNames: ['Name', 'Default Charset']
    colModel: [
      {name: 'name', index: 'name', width: 100, classes: 'editable-column', editable: true, align: 'left', editrules: {required: true}
      }
      {name: 'defaultCharset', index: 'defaultCharset', width: 100, align: 'left', editable: true, classes: 'editable-column', edittype: 'select',
      editoptions:
        dataUrl: 'rest/charsets?prop=id,name'
        buildSelect: (response)->"<select>#{($($.parseJSON(response)).map (idx, elem)->"<option value=#{@id}>#{@name}</option>").get().join('\n')}</select>"
      editrules: {required: true}
      }
    ]
  ).jqGrid('navGrid', '#languagePager', {search: false, edit: false}, {}, {
    #      paramAdd
    mtype: 'post', afterSubmit: afterSubmit, closeAfterAdd: true
    afterShowForm: (form)->$("#editmod#{@id}").position {my: 'center', at: 'center', of: window}
    }, {
    #      paramDel
    mtype: 'post', afterSubmit: afterSubmit
    #    afterShowForm: (form)->$("#delhd#{@id}").position {my: 'center', at: 'center', of: window}
    }
  )
