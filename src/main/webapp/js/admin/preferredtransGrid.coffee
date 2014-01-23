define [
  'jqgrid'
  'blockui'
  'jqmsgbox'
  'jqueryui'

  'i18n!nls/common'
  'i18n!nls/admin'
  'dms-util'
  'dms-urls'

], ($, blockui, msgbox, ui, c18n,i18n, util, urls)->
  gridId = 'preferredTranslationGrid'
  hGridId = "##{gridId}"

  createPreferredTranslationDlgId = 'createPreferredTranslationDialog'
  hCreatePreferredTranslationDlgId = "##{createPreferredTranslationDlgId}"

  #Create add PreferredTranslation dialog

  createPreferredTranslationDialog = $(hCreatePreferredTranslationDlgId).dialog(
    autoOpen: false
    title: i18n.preferredtranslation.create.title
    create: ()->
      btnPanel = $(@).next('div.ui-dialog-buttonpane')
      # keep button width consistent
      $('button[role=button]', btnPanel).width '63px'
    open: ()->
      $('#preferredTranslationErrorMsgContainer', @).hide()
      $('#preferredTranslationTranslation', @).val ""
      $('#preferredTranslationReference', @).val ""
      $('#preferredTranslationComment', @).val ""

      $.ajax {url: urls.languages, async: false, data: {prop: 'id,name'}, dataType: 'json', success: (languages)=>
        $('#preferredTranslationLanguage', @).empty().append util.json2Options(languages, false, "name")
      }


    width: 490, minWidth: 200, height: 300, minHeight: 200

    buttons:[
      {text: c18n.ok, click: (e)->
        unless $('#preferredTranslationReference', @).val()
          $("#errorMsg", @).text("Reference is required.")
          $('#preferredTranslationErrorMsgContainer', @).show()
          return

        unless $('#preferredTranslationTranslation', @).val()
          $("#errorMsg", @).text("Translation is required.")
          $('#preferredTranslationErrorMsgContainer', @).show()
          return
        $(@).dialog 'close'

        postData = {
          oper: 'add'
          reference: $('#preferredTranslationReference', @).val(),
          translation: $('#preferredTranslationTranslation', @).val()
          comment: $('#preferredTranslationComment', @).val()
          languageId: $('#preferredTranslationLanguage', @).val()
        }

        $.post urls.preferredTranslation.update, postData, (json)->
          ($.msgBox json.message, null, {title: c18n.error, width: 300, height: 'auto'};return) unless json.status == 0
          $(hGridId).trigger 'reloadGrid'

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
    url: urls.preferredTranslations

    datatype: 'json'
    mtype: 'post'
    postData: {format:'grid', prop: 'reference,translation,comment,language.name,createTime,creator.name'}
    cellurl: urls.preferredTranslation.update, cellEdit: true

    afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
      jsonFromServer = $.parseJSON serverresponse.responseText
      #setTimeout (->grid.trigger 'reloadGrid'), 10
      [jsonFromServer.status == 0, jsonFromServer.message]
    beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
    editurl: urls.preferredTranslation.update

    pager: "#{hGridId}Pager"
    rowNum: 30, rowList: [15, 30, 60]
    sortname: 'reference',  sortorder: 'asc'
    viewrecords: true, gridview: true, multiselect: true

    caption: c18n.preferredTrans.caption
    autowidth: true
    height: '100%'
    colNames: ['Reference','Translation', 'Comment', 'Language','Create Time', 'Creator']
    colModel: [
      {name: 'reference', index: 'reference', editable: true, classes: 'editable-column', width: 20 , align: 'left',editrules: {required: true}}
      {name: 'translation', index: 'translation', editable: true, classes: 'editable-column', width: 20, align: 'left',editrules: {required: true}}
      {name: 'comment', index: 'comment', editable: true, classes: 'editable-column', align: 'left', width: 30, editrules: {required: true}}
      {name: 'language.name', index: 'language.name', editable: false, width: 10, align: 'left'}
      {name: 'createTime', index: 'createTime', align: 'left', formatter: 'date', width: 10, formatoptions: {srcformat: 'ISO8601Long', newformat: 'ISO8601Long'}}
      {name: 'creator.name', index: 'creator.name', width: 10, align: 'left'}
    ]

    gridComplete: ->
      grid = $(@)
  )
  .navGrid("#{hGridId}Pager", {add: false, search: false, edit: false}, {},{},deleteOptions)
  .navButtonAdd("#{hGridId}Pager", {id: "custom_add_#{hGridId}", caption: "", buttonicon: "ui-icon-plus", position: "first", onClickButton: ()->
        createPreferredTranslationDialog.dialog 'open'
      })

  grid: grid



