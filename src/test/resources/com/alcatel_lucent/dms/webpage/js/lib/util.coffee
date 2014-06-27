###
  Obtain given dictName action ids in dictionaryGridList grid
###
getDictActionIds = (dictName)->
  dictRows = (dictRow for dictRow in $('#dictionaryGridList').getRowData() when dictRow.name is dictName)
  return {} unless dictRows.length
  actionStr = dictRows[0].action
  [strId, langId] = ($(atag).attr 'id' for atag in actionStr.split(/(?:&nbsp;)+/))
  strId: strId, langId: langId