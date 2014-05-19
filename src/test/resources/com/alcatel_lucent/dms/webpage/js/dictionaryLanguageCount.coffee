###
  This script is used in TestImportDictionary.getDictionaryLanguageCount
  for client dictionary language count
  must be called by executeAsyncScript

  @param arguments[0] the dictionary name to count
  @param arguments[1] timeOut to wait for client get result
###

callback = arguments[arguments.length - 1]
if(arguments.length < 2)
  callback null
  return
dictName = arguments[0]
timeOut = arguments[1]

# call the external util function
langId = getDictActionIds(dictName).langId
unless langId
  callback null
  return

$('#' + langId, '#dictionaryGridList').click()

setTimeout(()->
  langNum = $('#languageSettingGrid').getRowData().length
  callback(langNum)
  # close dialog
  $('#languageSettingsDialog').dialog 'close'
, timeOut)
