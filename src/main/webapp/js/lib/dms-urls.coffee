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
    generate_dict: getURL('generate-dict','app/')
    remove_version: getURL('remove-application', 'app/')
    deliver_dict: getURL('deliver-dict', 'app/')
    deliver_app_dict: getURL('deliver-app-dict', 'app/')
    process_dict: getURL('process-dict', 'app/')
    update_label_status: getURL('update-label-status', 'app/')
    download_app_dict: getURL('download-app-dict', 'app/')
  label:
    del: getURL('delete-label', 'app/')
    create: getURL('add-label', 'app/')
  task:
    apply:  getURL('apply-task', 'task/')
    close:  getURL('close-task', 'task/')
    generate_task_files: getURL('generate-task-files', 'task/')
    receive_task_files: getURL('receive-task-files', 'task/')

  trans:
    update_translation: getURL('update-translation', 'trans/')
    update_status: getURL('update-status', 'trans/')
  user:
    update: getURL('user', 'admin/')
  config:
    create_index: getURL('config','admin/')


  #  rest urls
  prod_versions:getURL('products/version','rest/')
  # application base id in the url, example: applications/apps/id
  app_versions: getURL("applications/apps/",'rest/')

  apps: getURL('applications', 'rest/')
  dicts: getURL('dict', 'rest/')
  tasks: getURL('tasks', 'rest/')

  labels_normal: getURL('labels', 'rest/')
  # lucene query
  labels: getURL('luceneLabels', 'rest/')

  languages: getURL('languages','rest/')

  translations: getURL('luceneTranslations', 'rest/')

  users: getURL('users', 'rest/')
  ldapuser: getURL('users/ldapUser', 'rest/')

  getURL: getURL



