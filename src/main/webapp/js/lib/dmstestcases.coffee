define ['dms-urls', 'dms-util'], (urls, util)->
  window.testCreateApp = (testProductName = 'TestProduct', testAppName = 'TestApp')->
    console.log "================================Start auto create=============================="


    appTree = $('#appTree').on("search.jstree", (e, data)->
#      console.log "result=%o", data
      productsNode = ''
      if data.args[0] == testProductName
        if(data.rslt.nodes.length < 1)
          console.log "create " + testProductName
          data.inst.create($("li#-1[type='products']", '#appTree'), 'last', {data: testProductName, attr: {type: 'product', id: null}}, (->

            url = urls.product.create_version
            versionName = '1.0'
            productBaseId = util.getProductTreeInfo().id

#            create version for product
            $.post url, {version: versionName, id: productBaseId}, (json)->
            if (json.status != 0)
              $.msgBox json.message, null, {title: c18n.error, width: 300, height: 'auto'}
              return
            (require 'appmng/product_panel').addNewProduct {version: versionName, id: json.id}


#            data.inst.search testAppName
          ), false)
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





