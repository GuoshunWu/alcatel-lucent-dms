###
Created by IntelliJ IDEA.
User: Guoshun Wu
###
define ['jqueryui'
  'jqgrid'
  "jqtree"
  "i18n!nls/common"
  "prototype-util",
  "common-util",
], (
  $
  jqgrid
  jqtree
  c18n
  protoType
  commonUtil
)->

  ###
    generate a progress bar
  ###
  genProgressBar = (autoDispaly = true, autoRemoveWhenCompleted = false)->
    randStr = commonUtil.randomStr(5)
    pbContainer=$("""
                  <div id="pb_container_#{randStr}"  class="progressbar-container">
                  <div class="progressbar-msg">
                  Loading...
                  </div>
                  <div id="progressbar_#{randStr}" class="progressbar">
                  <div class="progressbar-label">0.00%</div>
                  </div>
                  </div>
                  """).appendTo(document.body)
      .draggable(
        create: ()->
          $("#progressbar_#{randStr}", @).progressbar(
            max: 100
            create: (e, ui) ->
              @label = $('div.progressbar-label', @)
              @msg = $('div.progressbar-msg', pbContainer)
            change: (e, ui) ->
              @msg.html $(@).data('msg') if $(@).is(":data(msg)")
              @label.html "#{$(@).progressbar('value').toFixed(0)}%"
            complete: (e, ui) ->
              pbContainer.remove() if autoRemoveWhenCompleted
          )
      ).hide()
    pbContainer.show().position(my: 'center', at: 'center', of: window) if autoDispaly
    $("#progressbar_#{randStr}", pbContainer)


  ###

  ###
  ajaxStream = (url, postData, progress, finished)->
    xmlHttp = new $.ajaxSettings.xhr()
    xmlHttp.open "POST", url, true
    len = 0
    xmlHttp.onreadystatechange =()->
      return if xmlHttp.status != 200 || xmlHttp.readyState < 3
      if xmlHttp.readyState == 3
        progress? (xmlHttp.responseText.substr len), xmlHttp
      else if xmlHttp.readyState == 4
        finished? (xmlHttp.responseText.substr len), xmlHttp

      len = xmlHttp.responseText.length
    xmlHttp.send postData

  ###
  Long polling to update progress bar.


  @param url the url to get update informatin.
  @param postData data to be posted to server
  @callback callback when the progress complete.
  @pb the progress bar, if not null it will be auto updated until the progress complete and then invoke callback
     else callback will be invoked every time

  ###
  long_polling =(url, postData, callback, pb)->
    # call by terminal user
    postData.pqCmd = 'start' if !postData || !postData.pqCmd
#    console?.log "postData="

    # initlize the test parameters
    pollingInterval = if $("#pollingFreq").val() then parseInt($("#pollingFreq").val()) else 1000
    reTryAjax = (retryTimes = Number.MAX_VALUE, retryCounter = 0)->
      $.ajax(url,
        cache: false
        data: postData
        type: 'post'
        dataType: "json"
      ).done((data, textStatus, jqXHR) ->
        #        console?.log data
        if 'error' == data.event.cmd
          $.msgBox data.event.msg, null, {title: c18n.error}
          pb.parent().remove() if pb
          return

        if 'done' == data.event.cmd
          callback? data
          return

        if pb
          pb.toggleClass('progressbar-indeterminate', -1 == data.event.percent)
          pb.data 'msg', data.event.msg
          pb.get(0).msg.html data.event.msg
          pb.progressbar 'value', data.event.percent
        else
          callback? data

        setTimeout (->long_polling  url, {pqCmd: 'process', pqId: data.pqId}, callback, pb), pollingInterval
      ).fail((jqXHR, textStatus, errorThrown)->
        if 'timeout' != textStatus
          pb.parent().remove() if pb
          $.msgBox textStatus, null, {title: c18n.error}
          console?.log "error: #{textStatus}"
          return

        if(retryTimes > 0)
#          console?.log "Request #{textStatus}, I will retry in #{pollingInterval} milliseconds."
          setTimeout (->reTryAjax(--retryTimes, ++retryCounter)), pollingInterval
        else
          console?.log "I have retried #{retryCounter} times. There may be a network connection issue, please check network cable."
      )

    reTryAjax()

  getTreeNodeInfo = (node, treeSelecotr = '#appTree')->
    ptree=$.jstree._reference(treeSelecotr)
    return null if !ptree
    selectedNode = if node then node else ptree.get_selected()
    return null if 0 == selectedNode.length
    parent = ptree._get_parent(selectedNode)

    type = selectedNode.attr('type')
    type = 'prod' if type == 'product'

    id: selectedNode.attr('id')
    text: ptree.get_text(selectedNode)
    type: type
    parent: if parent != -1 then getTreeNodeInfo(parent) else parent

  newOption = (text, value, selected)->"<option #{if selected then 'selected ' else ''}value='#{value}'>#{text}</option>"
  urlname2Action = (urlname = '', suffix = 'Action')->urlname.split('/').pop().capitalize().split('-').join('') + suffix

  #  Ajax event for all pages
  sessionCheck = ()->
    $('#sessionTimeoutDialog').dialog(
      width: 320, modal: true
      autoOpen: false
      zIndex: 3999
      buttons: [
        {
        text: c18n.ok, click: (e)->
          $(@).dialog 'close'
          window.location = 'login/forward-to-https'
        }
      ]
    )
    $.ajaxSetup(
      converters :
        "text json": (jsonText)->
          json = jQuery.parseJSON(jsonText)
          $('#sessionTimeoutDialog').dialog 'open' if json.status and 203 == json.status
          json

#      statusCode:
#        203: ()->
#          console?.log "session expired."
#          $('#sessionTimeoutDialog').dialog 'open'
    )
#    $(document).on 'ajaxComplete', (e, xhr, settings)->
#    if 203 == xhr.status
#      $('#sessionTimeoutDialog').dialog 'open'


  checkGridPrivilege = (grid)->
#    console.log "check the privilege of grid '#{grid.id}'."
    gridParam = $(grid).jqGrid 'getGridParam'
    return unless gridParam
#    console.log($(grid).attr("id") + ", cellurl=" + gridParam.cellurl)
    forbiddenTab =
      cellurl: urlname2Action(gridParam.cellurl) in param.forbiddenPrivileges
      editurl: urlname2Action(gridParam.editurl) in param.forbiddenPrivileges
      cellactionurl: urlname2Action(gridParam.cellactionurl) in param.forbiddenPrivileges

    #    for the celledit
    if forbiddenTab.cellurl
      $.each gridParam.colModel, (idx, obj) ->
        if $.isPlainObject(obj) and obj.name and obj.editable
          obj.editable = false
          obj.classes = obj.classes.replace('editable-column', '') if obj.classes

    #    for the grid  navigatebar, ['view', 'search', 'refresh'] are readonly operation, enabled
    $.each ['add', 'edit', 'del', 'lock'], (index, value)->
      # for jqgrid predefined navigate buttons
      btnSelector = "##{value}_#{grid.id}"
      actButton = $ btnSelector

      if actButton.length > 0 and forbiddenTab.editurl
#        console?.log "Disable button #{actButton.attr('id')} due to forbidden privilege."
        actButton.addClass 'ui-state-disabled'

      # for custom buttons in navigate gird.
      btnSelector = "#custom_#{value}_#{grid.id}"
      actButton = $(btnSelector)

      if actButton.length > 0 and (forbiddenTab.editurl or forbiddenTab.cellactionurl)
#        console?.log "Disable button #{actButton.attr('id')} due to forbidden privilege."
        actButton.addClass 'ui-state-disabled'


    #   for the default cell action of the grid.
    if forbiddenTab.cellactionurl
      $.each gridParam.colModel, (idx, obj) ->
        if $.isPlainObject(obj) and obj.name == 'cellaction'
          obj.formatoptions.delbutton = false
          obj.formatoptions.editbutton = false
    #    for the custom cell action of the grid.
    tmpHandlers = gridParam.cellactionhandlers
    if tmpHandlers
      $.each tmpHandlers, (index, value)->delete tmpHandlers[index] if urlname2Action(value.url) in param.forbiddenPrivileges

  #    $(grid).trigger 'reloadGrid'
  checkAllGridPrivilege = (grids = $('table.ui-jqgrid-btable'), readonly = true)->$.each grids, (idx, grid)->checkGridPrivilege grid
  sessionCheck()


  # update search options value in grid model
  updateSearchOptionsValue : (url, colModel)->
    searchProps = $(colModel).map((idx, elem)->elem.name if elem.search).get()
    $.ajax(url, async: false, data: {prop: searchProps.join(", ")}).done((json, textStatus, jqXHR)->
      for model in colModel when model.search
        model.searchoptions =
          dataEvents: [
            {
              type: 'change'
              fn:((e)->
                #            console.log(e)
              )
            }
          ] if not model.searchoptions
        optionsValue = $(json).map((idx,elem)->
          #        value = if model.name isnt 'status' then elem[model.name] else ''
          value = elem[model.name]
          "#{value}:#{value}").get().unique().join(';')
        optionsValue = ":All;" + optionsValue if optionsValue
        model.searchoptions.value = optionsValue
    )

  # a grid edit default value to map
  string2Options = (stringValue)->
    opt = {}
    return opt unless stringValue
    for option in stringValue.split(";")
      entry = option .split ":"
      opt[entry[0]]=entry[1]
    opt

  setSearchSelect = (grid, colName=[], selectElements={})->
#    console.log "set search select, colName=%o, selectElements=%o", colName, selectElements
    return selectElements unless grid
    #if colName is array, set select one by one in it
    if $.isArray colName
      for name in colName
        setSearchSelect(grid,name, selectElements)
      return selectElements

    # single column name
    grid.jqGrid('setColProp', colName, {
      stype: 'select'
      searchoptions: {
#        clearSearch: false
        value: ":All"
#        attr: {mutiple: 'multiple', size: 2}
        dataInit:(elem)->
#          console.log "Column #{colName} select init, elem=%o.", elem
          selectElements[colName] = $(elem)
#          .multiselect()
#          .width(122)
#          ref: http://stackoverflow.com/questions/19395680/using-bootstrap-select2-with-jqgrid-form/19404013#19404013
#               http://stackoverflow.com/questions/5328072/can-jqgrid-support-dropdowns-in-the-toolbar-filter-fields
#          .select2(
#            dropdownCssClass: "ui-widget ui-jqdialog"
#          )

        dataEvents:[
          {type: "change", fn:(e)->
#            console.log "Column #{colName} select change event, elem=%o., seleElements=%o", e.target, selectElements[colName]
          }
        ]
      }
    })
    # console.log("column =%o, changed search options=%o", colName, grid.getColProp(colName).searchoptions)
    selectElements

  buildSearchSelectValues = (grid, colName, selectedValue = false)->
    # single column name
    colProps = grid.jqGrid('getColProp', colName)
    defaultSelectText = colProps.editoptions?.value
    mapValue = string2Options(defaultSelectText)
    uniqueValues = grid.jqGrid('getCol', colName).unique()

    "<option value=''>All</option>" + uniqueValues.map((elem)->
      display  = if mapValue[elem] then mapValue[elem] else elem
      isSelected = ""
      isSelected = "selected" if selectedValue and elem + "" == selectedValue
      "<option #{isSelected} value='#{elem}'>#{display}</option>"
    ).join("\n")

  ###
  Test here.
  ###
  #  a=[1,2,3]
  #  console.log a.insert 1,["a",'b']

  generateLanguageTable: (languages, tableId, colNum)->
    tableId = 'languageFilterTable' if !tableId
    colNum = 5 if !colNum
    rowCount = Math.ceil(languages.length / colNum)

    languageFilterTable = $("<table id='#{tableId}' align='center'width='100%' border='0'><tr valign='top' /></table>")
    outerTableFirstRow = $("tr:eq(0)", languageFilterTable)

    languageCells = $(languages).map ()->$("<td><input type='checkbox' checked value=\"#{@name}\" name='languages' id=#{@id} /><label for=#{@id}>#{@name}</label></td>").css('width', '180px')

    innerColTable = null
    languageCells.each (index)->
      if 0 == index % rowCount
        innerColTable = $("<table border='0'/>")
        outerTableFirstRow.append $("<td></td>").append innerColTable
      innerColTable.append $("<tr/>").append @

    checkedAll = $("<input type='checkbox'id='all_#{tableId}' checked><label for='all_#{tableId}'>All</label>").change ()->
      $(":checkbox[name='languages']", languageFilterTable).attr('checked', @checked)
    #    hr line
    languageFilterTable.append $('<tr/>').append $("<td colspan='#{colNum}'/>").append $("<hr width='100%'>")
    #    check all line
    languageFilterTable.append $('<tr/>').append $("<td colspan='#{colNum}'></td>").append checkedAll

  json2string: (jsonObj)->commonUtil.formatJonString JSON.stringify(jsonObj)
  getDictLanguagesByDictId: (id, callback)->$.getJSON 'rest/languages', {prop: 'id,name', dict: id}, (languages)=>callback languages
  # expires=date; Setting no expiration date on a cookie causes it to expire when the browser closes. If you set an expiration date in the future, the cookie is saved across browser sessions. If you set an expiration date in the past, the cookie is deleted. Use GMT format to specify the date.
  # domain=domainname; Setting the domain of the cookie allows pages on a domain made up of more than one server to share cookie information.
  #  path=path;Setting a path for the cookie allows the current document to share cookie information with other pages within the same domain—that is, if the path is set to /thispathname, all pages in /thispathname and all pages in subfolders of /thispathname can access the same cookie information.
  #  secure; Setting a cookie as secure; means the stored cookie information can be accessed only from a secure environment.
  getUrlParams: (href = window.location.href)->
    lastPos = unless href.endWith '#' then -1 else -2
    suffix = href[href.lastIndexOf('?') + 1..lastPos]
    params = {}
    ([k, v]=param.split('=');params[k] = decodeURIComponent(v) ) for param in suffix.split('&') if suffix
    params
  newOption: newOption
  ###
  convert a json array to a list of options.
  @params
  json: json array
  selectedValue: default selected option value

###
  json2Options: (json, selectedValue = ':last', textFieldName = "version", valueFieldName = "id", sep = '\n')->
    $(json).map(
      (index, elem)->
        #        selected = if !selectedValue then index == json.length - 1 else (String selectedValue) == (String @[valueFieldName])
        # :last indicate that the last option need to be selected
        return false unless jQuery.isArray(json)
        selectedValue = json.slice(-1)[0][valueFieldName] if ':last' == selectedValue
        selectedValue = json.slice(0)[0][valueFieldName] if ':first' == selectedValue
        selected = (String selectedValue) == (String @[valueFieldName])
        newOption @[textFieldName], @[valueFieldName], selected
    ).get().join(sep)

#    if ':last' == selectedValue

  afterInitialized: (context)->
    #   check all the grids' privilege
    checkAllGridPrivilege()
    # center progressbar
    $('div.progressbar').position(my: 'center', at: 'center',of: window)
    #    check all buttons' privilege
    $('[role=button][privilegeName]').each ()->
      forbidden = $(@).attr('privilegeName') in param.forbiddenPrivileges
      console.log "Button: #{@id} which privilegeName=#{$(@).attr('privilegeName')} forbidden: #{forbidden}."
      return true unless forbidden
      $(@).button 'disable'

    $('input[privilegeName],textarea[privilegeName]').each ()->
      forbidden = $(@).attr('privilegeName') in param.forbiddenPrivileges
      console.log "textarea or input: #{@id} which privilegeName=#{$(@).attr('privilegeName')} forbidden: #{forbidden}."
      return true unless forbidden
      $(@).prop 'disabled', true

  randomNum : commonUtil.randomNum
  ajaxStream: ajaxStream
  urlname2Action: urlname2Action
  createLayoutManager: (page = 'appmng.jsp')->createLayoutManager(page)

  getProductTreeInfo: getTreeNodeInfo
  genProgressBar:genProgressBar
  updateProgress: long_polling
  ###
    @param panels: the panel group selector
    @param currentPanel: the current panel selector
    @param onSwitch: the handler on panel switch
  ###
  PanelGroup: class PanelGroup
    constructor: (@panels, @currentPanel, @onSwitch = (oldpnl, newpnl)->)->
    switchTo: (panelId, callback)->

      $("#{@panels}").hide()
#      console?.log "switch to #{@panels}[id='#{panelId}']."
      oldPanel = @currentPanel
      @currentPanel = panelId
#      $("#{@panels}[id='#{panelId}']").fadeIn "slow", ()->callback() if $.isFunction(callback)
      $("#{@panels}[id='#{panelId}']").show 0, ()->callback() if $.isFunction(callback)

      @onSwitch oldPanel, @currentPanel if $.isFunction(@onSwitch) and oldPanel != @currentPanel

  adjustDialogAndInnerGridSize :(dialog, grid, adjust = {width: 100, height: 50}, adjustGrid = {width: 30, height: 240}) ->
    # resize size according to screen size
    jWindow = $(window)
    dialog.dialog( "option", "width", jWindow.width() - adjust.width)
    .dialog("option", "height", jWindow.height() - adjust.height)
    .dialog("option", "position", of : window)


    return if(!grid || !grid.length)
    # adjust grid
    grid.setGridWidth(jWindow.width() - adjust.width - adjustGrid.width)
    .setGridHeight(jWindow.height() - adjust.height - adjustGrid.height)

#    console.log("dialog %o width=%o, height=%o, grid=%o, grid width=%o, grid height=%o",
#      dialog.attr("id"), dialog.width(), dialog.height(),
#      grid.prop('id'), grid.getGridParam('width'), grid.getGridParam('height')
#    )

  setSearchSelect:setSearchSelect
  buildSearchSelectValues: buildSearchSelectValues

