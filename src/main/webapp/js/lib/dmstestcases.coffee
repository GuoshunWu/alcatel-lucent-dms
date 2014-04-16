define ['dms-urls', 'dms-util'], (urls, util)->
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

  window.testCreateApp = (testProductName = 'TestProduct', testAppName = 'TestApp')->
    console.log "================================Start auto create=============================="

    ###
      product node is not found
    ###

    appTree = $('#appTree').on("search.jstree", (e, data)->
      if data.args[0] == testProductName
        if(data.rslt.nodes.length < 1)
          productsNode = $("li#-1[type='products'] > a", '#appTree')

          # test product not found, create new test product
          data.inst.create(productsNode, 'last', {data: testProductName, attr: {type: 'product', id: null}}, (->
            createdNode = $("li[type='product'].jstree-last > a", "#appTree")
            createdNode.trigger 'click'
            createVersionDialog = $("#newApplicationVersionDialog").dialog "open"
            $('#appVersionName', createVersionDialog).val "1.0"
            createVersionDialog.next("div.ui-dialog-buttonpane").find("button:contains('OK')").click()

#           data.inst.search testAppName
          ), true)
        else
          data.inst.search testAppName
      return

      if data.args[0] == testAppName
        if(data.rslt.nodes.length)
          $(data.rslt.nodes[0]).trigger 'click'
        else
          console.log "create " + testAppName
        data.inst.clear_search
    ).jstree('search', testProductName)





