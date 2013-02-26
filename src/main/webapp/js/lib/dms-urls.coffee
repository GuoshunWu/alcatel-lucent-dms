###
Created by IntelliJ IDEA.
User: Guoshun Wu
Date: -8-
Time: 下午7:
###
define (require)->
  $ = require 'jquery'
  getURL = (url, prefix = '', params = {})->
    url = prefix + url
    return url if $.isEmptyObject params

    url = "#{url}?"
    isFirst = true
    for k, v of params
      url += '&' unless isFirst
      url += "#{k}=#{decodeURIComponent(v)}"
      isFirst = false
    url

  test: getURL('test', 'test/', name: 'aaa', id: 32)
  ### URLs for product tree. ###
  navigateTree: getURL('rest/products', '', format: 'tree')
  product:
    create: getURL('create-product', 'app/')
    del: getURL('remove-product-base', 'app/')
    versions: getURL('products/version','rest/')
  app:
    create: getURL('create-application-base', 'app/')
    del: getURL('remove-application-base', 'app/')

  #  rest urls
  prod_versions:getURL('products/version','rest/')
  # application base id in the url, example: applications/apps/id
  app_versions: getURL("applications/apps/",'rest/')

  apps: getURL('applications', 'rest/')
  dicts: getURL('dict', 'rest/')
  tasks: getURL('tasks', 'rest/')

  languages: getURL('languages','rest/')


