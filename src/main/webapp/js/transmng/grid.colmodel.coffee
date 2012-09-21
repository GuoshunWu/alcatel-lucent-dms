define ['jqgrid'], ($)->
  $.jgrid.extend {
  getId: ()->
    "##{@attr 'id'}"
  addColumns: (newColNames, newColModelEntrys, url,postData)->
    gridParam = @getGridParam()
    gridParam.colNames = $.grep gridParam.colNames, (val, key)-> ""!=val
    gridParam.colModel = $.grep gridParam.colModel, (val, key)-> "rn" != val.name

    $.merge gridParam.colModel , newColModelEntrys
    $.merge gridParam.colNames , newColNames

    @reloadAll(url,postData)

  reloadAll: (url,postData)->
    return if !url
    gridParam = @getGridParam()
    @GridUnload @getId()
    gridParam.url = url
    gridParam.postData=postData if postData
#    console.log "recreate grid, gridpostData="
#    console.log  gridParam.postData
    newGrid = $(@getId()).jqGrid gridParam
    @getGridParam('afterCreate') newGrid

  addTaskLanguage: (language, url, postData)->
    cols = ['T', 'N', 'I']
    colModels = ($(cols).map ()-> {name: "#{language}.#{@}", index: "#{language}.#{@}",width: 20,editable: false,search:false, align: 'center'}).get()

    @getGridParam('groupHeaders').push {startColumnName: "#{language}.T", numberOfColumns: 3, titleText: "<bold>#{language}</bold>"}
    @addColumns cols, colModels, url, postData

  updateTaskLanguage: (languages, url,postData)->
  #  get all the columns which are not Languages
    return false if $.isEmptyObject languages
    cols = ['T', 'N', 'I']
    gridParam = @getGridParam()
    gridParam.colNames = $.grep gridParam.colNames, (val, key)-> val not in cols
    gridParam.colModel = $.grep gridParam.colModel, (val, key)-> not /.+\.[TIN]/g.test val.name

    if not $.isArray languages
      @addTaskLanguage languages, url, postData
      return
    if 0==languages.length
      @reloadAll url,postData
      return

    $(languages).each (index, language)=>
      return false if index == languages.length - 1
      @addTaskLanguage(language)
    @addTaskLanguage(languages[languages.length-1], url, postData)
  }

