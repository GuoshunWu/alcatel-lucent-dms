define ['dms-urls', 'dms-util'], (urls, util)->

  getDictActionIds = (dictName)->
    dictRows = (dictRow for dictRow in $('#dictionaryGridList').getRowData() when dictRow.name is dictName)
    return {} unless dictRows.length
    actionStr = dictRows[0].action
    [strId, langId] = ($(atag).attr 'id' for atag in actionStr.split(/(?:&nbsp;)+/))
    strId: strId, langId: langId

  window.wgsTest = ()->

    longRun = (msg, status, sucCallBack=((msg)->console.log "Success, hello "+ msg),failCallBack=(msg)->console.log "Fail, hello " + msg)->
      console.log "I am figuring out result..."
      setTimeout(->
        if status
          sucCallBack msg
        else
          failCallBack msg
      , 3000)

    wrapper=()->
      dnd = $.Deferred()
      longRun('Wgs', 1, sucFunc = ()->)
      dnd.promise()

    wrapper().done((result)->)


  ###

  ###
  window.testCreateApp = (dictName = 'dms-test.xlsx')->
    console.log "================================Start auto create=============================="
    # call the external util function
    actIds =  getDictActionIds(dictName)
    $('#' + actIds.strId, '#dictionaryGridList').click()

    labelKeys = ['DMSTEST2', 'DMSTEST3', 'DMSTEST4']
    setTimeout(()->
      result = {}
      stringSettingsGrid = $('#stringSettingsGrid')
      sstd = $('stringSettingsTranslationDialog')

      result[row.key] = context: row.context for row in stringSettingsGrid.getRowData() when row.key in labelKeys
        result[row.key] = context: row.context
        # check translation
        stringSettingsGrid.find("tr td[title='DMSTEST2'] ~ td[aria-describedby='stringSettingsGrid_t > a']").click()
        # waiting for translation load
        setTimeout( ->
          translations = $("#stringSettingsTranslationGrid").getRowData()
          result[row.key] = translations: translations
          sstd.dialog 'close'
        , 1000)

      $('#stringSettingsDialog').dialog 'close'
    , 2000)





