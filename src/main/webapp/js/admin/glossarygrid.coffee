define [
  'jqgrid'
  'blockui'
  'jqmsgbox'
  'jqueryui'

  'i18n!nls/common'
  'i18n!nls/admin'
  'dms-util'
  'dms-urls'

], ($, blockui, msgbox, ui, c18n, i18n, util, urls)->
  gridId = 'glossaryGrid'
  hGridId = "##{gridId}"

  createGlossaryDlgId = 'createGlossaryDialog'
  hCreateGlossaryDlgId = "##{createGlossaryDlgId}"

  createGlossary = (glossary)->
    postData = $.extend glossary, {
      oper: 'add'
    }
    $.post urls.glossary.update, postData, (json)->
      ($.msgBox json.message, null, {title: c18n.error, width: 300, height: 'auto'};return) unless json.status == 0
      $(hGridId).trigger 'reloadGrid'

  #Create add glossary dialog
  createGlossaryDialog = $(hCreateGlossaryDlgId).dialog(
    autoOpen: false
    title: i18n.glossary.create.title
    create: ()->
      btnPanel = $(@).next('div.ui-dialog-buttonpane')
      # keep button width consistent
      $('button[role=button]', btnPanel).width '63px'
    open: ()->
      $('#glossaryErrorMsgContainer', @).hide()
      $('#glossaryTranslate', @).prop 'checked', false
      $('#glossaryText', @).val ""
      $('#glossaryDescription', @).val ""

    width: 490, minWidth: 200, height: 270, minHeight: 200
    resizable: false
    buttons:[
      {text: c18n.ok, click: (e)->
        unless $('#glossaryText', @).val()
          $("#errorMsg", @).text("Text is required.")
          $('#glossaryErrorMsgContainer', @).show()
          return

        createGlossary(
          text: $('#glossaryText', @).val(),
          translate: $('#glossaryTranslate', @).prop 'checked'
          description: $('#glossaryDescription', @).val()
        )

        $(@).dialog 'close'
      }
      {text: c18n.cancel, click: (e)->$(@).dialog 'close'}
    ]
  )


  deleteOptions = {
  msg: 'Delete selected text?'
  afterShowForm: (formid)->
    $(formid).parent().parent().position(my:'center', at: 'center', of: window)
  }

  grid = $(hGridId).jqGrid(
    url: urls.glossaries

    datatype: 'json'
    mtype: 'post'
    postData: {format:'grid', prop: 'text, translate, description, createTime,creator.name', 'idprop': 'text'}
    prmNames: id: 'text'
    cellurl: urls.glossary.update, cellEdit: true

    afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
      jsonFromServer = $.parseJSON serverresponse.responseText
      #setTimeout (->grid.trigger 'reloadGrid'), 10
      [jsonFromServer.status == 0, jsonFromServer.message]
    beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
      newText: value if 'text' == cellname

    editurl: urls.glossary.update

    pager: "#{hGridId}Pager"
    rowNum: 30, rowList: [15, 30, 60]
    sortname: 'text',  sortorder: 'asc'
    viewrecords: true, gridview: true, multiselect: true

    caption: c18n.glossary.caption
    autowidth: true
    height: '100%'
    colNames: ['Glossary text', 'Is Translate' ,'Description' ,'Create Time', 'Creator']
    colModel: [
      {name: 'text', index: 'text', width: 15, editable: true, classes: 'editable-column', align: 'left',editrules: {required: true}}
      {name: 'translate', index: 'translate', formatter:'select',edittype: 'select', editoptions: {value: 'true:yes;false:no'}, width: 5, editable: true, classes: 'editable-column', align: 'left',editrules: {required: true}}
      {name: 'description', width: 40, index: 'description', editable: true, classes: 'editable-column', align: 'left',editrules: {required: true}}
      {name: 'createTime',width: 10, index: 'createTime', align: 'left', formatter: 'date', formatoptions: {srcformat: 'ISO8601Long', newformat: 'ISO8601Long'}}
      {name: 'creator.name', width: 10, index: 'creator.name', align: 'left'}
    ]

    gridComplete: ->
      grid = $(@)
  )
  .navGrid("#{hGridId}Pager", {add: false, search: false, edit: false}, {},{},deleteOptions)
  .navButtonAdd("#{hGridId}Pager", {id: "custom_add_#{hGridId}", caption: "", buttonicon: "ui-icon-plus", position: "first", onClickButton: ()->
    createGlossaryDialog.dialog 'open'
  })



  grid: grid



