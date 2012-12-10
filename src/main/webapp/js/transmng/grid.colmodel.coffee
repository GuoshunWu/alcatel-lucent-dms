define ['jqgrid'], ($)->
  $.jgrid.extend {
  getId: ()->
    "##{@attr 'id'}"
  addColumns: (newColNames, newColModelEntrys, url, postData)->
    gridParam = @getGridParam()
    gridParam.colNames = $.grep gridParam.colNames, (val, key)-> "" != val
    gridParam.colModel = $.grep gridParam.colModel, (val, key)-> "rn" != val.name

    $.merge gridParam.colModel, newColModelEntrys
    $.merge gridParam.colNames, newColNames

    @reloadAll(url, postData)

  reloadAll: (url, postData)->
    return if !url
    gridParam = @getGridParam()
    $(gridParam.colModel).each (index, colModel)->colModel.classes = 'editable-column' if colModel.editable

    @GridUnload @getId()
    gridParam.url = url
    gridParam.postData = postData if postData
    #    console.log "recreate grid, gridpostData="
    #    console.log  gridParam.postData
    delete gridParam.selarrrow
    #    delete gridParam.selrow
    newGrid = $(@getId()).jqGrid gridParam
#    save search tool bar status before recreate the grid
    searchValues=
      app: $('#gs_application').val()
      encoding: $('#gs_encoding').val()
      format: $('#gs_format').val()
    console.log searchValues

    @getGridParam('afterCreate') newGrid


  addTaskLanguage: (language, url, postData)->
    cols = ['T', 'N', 'I']
    level = $("input:radio[name='viewOption'][checked]").val()
    colModels = $(cols).map(
      (index, elem)->
        model = {
        name: "#{language.name}.#{@}",
        sortable: false, index: "s(#{language.id})[#{index}]",
        width: 40, align: 'right',
        search: false, editable: false
        }
        if elem == 'T'
          model.classes = 'language-group-border'
        if level != 'application'
          model.formatter = 'showlink'
          model.formatoptions = {baseLinkUrl: '#', addParam: encodeURI("&languageId=#{language.id}&languageName=#{model.name}")}
        model
    ).get()
    @getGridParam('groupHeaders').push {startColumnName: "#{language.name}.T", numberOfColumns: cols.length, titleText: "<bold>#{language.name}</bold>"}
    @addColumns cols, colModels, url, postData

  updateTaskLanguage: (languages, url, postData)->
  #  get all the columns which are not Languages
    return false if $.isEmptyObject languages
    cols = ['T', 'N', 'I']
    gridParam = @getGridParam()
    gridParam.colNames = $.grep gridParam.colNames, (val, key)-> !(val  in cols)
    gridParam.colModel = $.grep gridParam.colModel, (val, key)-> !/.+\.[TIN]/g.test val.name

    if !$.isArray languages
      @addTaskLanguage languages, url, postData, 0
      return
    if 0 == languages.length
      @reloadAll url, postData
      return

    $(languages).each (index, language)=>
      if index < languages.length - 1
        @addTaskLanguage(language)
      else
        @addTaskLanguage(language, url, postData)
  }

