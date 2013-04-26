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
    create_version: getURL('create-product-release', 'app/')
  app:
    create: getURL('create-application-base', 'app/')
    del: getURL('remove-application-base', 'app/')
    create_version: getURL('create-application', 'app/')
    remove_version: getURL('remove-application', 'app/')
    deliver_dict: getURL('deliver-dict', 'app/')
  label:
    del: getURL('delete-label', 'app/')
    create: getURL('add-label', 'app/')
  task:
    apply:  getURL('apply-task', 'task/')
    close:  getURL('close-task', 'task/')

  trans:
    update_translation: getURL('update-translation', 'trans/')
    update_status: getURL('update-status', 'trans/')
  user:
    update: getURL('user', 'admin/')


  #  rest urls
  prod_versions:getURL('products/version','rest/')
  # application base id in the url, example: applications/apps/id
  app_versions: getURL("applications/apps/",'rest/')

  apps: getURL('applications', 'rest/')
  dicts: getURL('dict', 'rest/')
  tasks: getURL('tasks', 'rest/')

  labels: getURL('labels', 'rest/')

  languages: getURL('languages','rest/')

  users: getURL('users', 'rest/')
  ldapuser: getURL('users/ldapUser', 'rest/')



