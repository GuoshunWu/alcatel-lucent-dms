define
  root:
    loadingpage: '<h1><img src="busy.gif" />Loading page please wait...</h1>'
    all: 'All'
    dictformats: window.param?.dictFormats
    transcontext: '[DEFAULT]:[DEFAULT];[EXCLUSION]:[EXCLUSION];[LABEL]:[LABEL];[DICT]:[DICT];[APP]:[APP];[PROD]:[PROD];Other:Other'
    dictencodings: 'ISO-8859-1:ISO-8859-1;UTF-8:UTF-8;UTF-16LE:UTF-16LE;UTF-16BE:UTF-16BE'
    transoptype: '1:DELIVER;2:RECEIVE;3:NEW;4:INPUT;5:CAPITALIZE;6:GLOSSARY;7:SUGGEST;8:STATUS'

    ###
            "7z", "Arj", "BZip2", "Cab", "Chm",
            "Cpio", "Deb", "Gzip", "Iso", "Lzh",
            "Lzma", "Nsis", "Rar", "Rpm", "Tar",
            "Udf", "Wim", "Xar", "Z", "Zip",
            "Tgz", "gz", "apk"
    ###
    supportedarchives: '.zip,.rar,.7z,.tgz,.Z,.gz,.cpio,.cab,.rpm,.tar,.gzip,.deb,.bzip2,.arj'


    history:
      'caption': "History for translation '{0}'"
      'nohistory': 'No translation history to display.'

    "import": 'Import'
    ok: 'OK'
    cancel: 'Cancel'
    "export": 'Export'
    "yes": 'Yes'
    del: 'Delete'
    add: 'Add'
    no: 'No'
    required: '{0} is required!'
    save: 'Save'
    close: 'Close'
    error: 'Error'
    warning: 'Warning'
    confirm: 'Confirm'
    message: 'Message'
    create: 'Create'
    prod: 'product'
    products: 'all products'
    app: 'application'
    dict: 'dictionary'
    label: 'label'
    language: 'language'
    confirmloadall: "Some dictionaries are not loaded in this page.\n<br/>Do you want to load and select all dictionaries?"
    languagerequired: 'Please select languages!'
    selrow: 'Please select {0} first'
    'selecttip': 'Please select'
    noversion: '{0} \"{1}\" has no version.'

    preferredTrans:
      caption: 'Reference Translation'
    glossary:
      caption: 'Glossaries'
    select:
      product:
        tip: 'Please select product'
        msg: 'Please select product'
        msgtitle: 'Select product'
      release:
        tip: 'Please select product release'
        msg: 'Please select product release!'
        msgtitle: 'Select product release'
      context:
        tip: 'Please select context'
    translation:
      values: ':All;0:Not translated;1:In progress;2:Translated'

    tipofday:
      previous: 'Previous Tip'
      next: 'Next Tip'
      showtip: 'Don\'t show tips next time'

  "zh-cn": true