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

  ### URLs for product tree. ###
  navigateTree: getURL('rest/products', '', format: 'tree')
  product:
    create: getURL('create-product', 'app/')
    del: getURL('remove-product-base', 'app/')
    create_version: getURL('create-product-release', 'app/')
  app:
    create: getURL('create-application-base', 'app/')
    add_app: getURL('create-or-add-application', 'app/')
    add_dict_language: getURL('add-dict-language', 'app/')
    remove_dict_language: getURL('remove-dict-language', 'app/')
    del: getURL('remove-application-base', 'app/')
    create_version: getURL('create-application', 'app/')
    generate_dict: getURL('generate-dict','app/')
    update_dict: getURL('update-dict','app/')
    change_dict_version: getURL('change-dict-version', 'app/')
    remove_dict: getURL('remove-dict', 'app/')
    remove_version: getURL('remove-application', 'app/')
    deliver_dict: getURL('deliver-dict', 'app/')
    deliver_update_dict: getURL('deliver-update-dict', 'app/')
    deliver_app_dict: getURL('deliver-app-dict', 'app/')
    process_dict: getURL('process-dict', 'app/')
    download_app_dict: getURL('download-app-dict', 'app/')
    capitalize: getURL('capitalize', 'app/')

  label:
    del: getURL('delete-label', 'app/')
    create: getURL('add-label', 'app/')
    update: getURL('update-label', 'app/')
    update_status: getURL('update-label-status', 'app/')
    update_ref_translations: getURL('update-label-ref-and-translations', 'app/')

  task:
    apply:  getURL('apply-task', 'task/')
    close:  getURL('close-task', 'task/')
    generate_task_files: getURL('generate-task-files', 'task/')
    receive_task_files: getURL('receive-task-files', 'task/')

  trans:
    update_translation: getURL('update-translation', 'trans/')
    update_status: getURL('update-status', 'trans/')
    generate_translation_details: getURL('generate-translation-details', 'trans/')
    export_translation_details: getURL('export-translation-details', 'trans/')
    import_translation_details: getURL('import-translation-details', 'trans/')

  context:
    merge: getURL('merge-context', 'context/')
    take_translations: getURL('take-translations', 'context/')
  charset:
    update: getURL('charset', 'admin/')
  glossary:
    update: getURL('glossary', 'admin/')
    apply: getURL('apply-glossaries', 'admin/')
  preferredTranslation:
    update: getURL('preferred-translation', 'admin/')
  preferredReference:
    create: getURL('create-preferred-reference', 'admin/')

  user:
    update: getURL('user', 'admin/')
  config:
    create_index: getURL('config','admin/')


  #  rest urls
  prod_versions:getURL('products/version','rest/')
  # application base id in the url, example: applications/apps/id
  app_versions: getURL("applications/apps/",'rest/')
  app_versions1: getURL("applications/version/",'rest/')

  apps: getURL('applications', 'rest/')
  dicts: getURL('dict', 'rest/')
  dict_validation: getURL('dictValidation', 'rest/')
  deliver_dict: getURL('delivery/dict', 'rest/')
  tasks: getURL('tasks', 'rest/')

  charsets: getURL('charsets', 'rest/')
  contexts: getURL('contexts', 'rest/')

  labels_normal: getURL('labels', 'rest/')
  # lucene query
  labels: getURL('luceneLabels', 'rest/')

  languages: getURL('languages','rest/')


  translations: getURL('luceneTranslations', 'rest/')
  getTranslations: getURL('label/translation', 'rest/')
  translation_histories: getURL('translationHistory', 'rest/')
  translation_check: getURL('translationCheck', 'rest/')
  app_translation_histories: getURL('appTranslationHistory', 'rest/')
  users: getURL('users', 'rest/')
  ldapuser: getURL('users/ldapUser', 'rest/')

  text:
    texts: getURL('texts', 'rest/')
    diff_texts: getURL('diff/texts', 'rest/')
    diff_text_translations: getURL('diff/text/translations', 'rest/')
    refs: getURL('text/refs', 'rest/')
    translations: getURL('text/translations', 'rest/')
  glossaries: getURL('glossaries', 'rest/')
  preferredTranslations: getURL('preferredTranslations', 'rest/')

  getURL: getURL



