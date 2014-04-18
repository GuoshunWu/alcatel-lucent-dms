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





