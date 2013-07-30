define
  root:
    select:
      product: 'Select product'
      application: 'Select application'
      version: 'Select version'
    grid:
      delappmsg: 'Please select application to delete.'
      permanenttext: 'Delete permanently'
      confirmdeldict: '''<table>
                      <tr><td>Following tasks are associated with the dictionaries being deleted:</td></tr>
                      <tr><td><pre>{0}</pre></td></tr>
                      <tr><td>The tasks will be deleted all together. Do you confirm it?</td></tr>
                      '''
      dictlistpreview: caption: 'Dictionaries in zip'
    dialog:
      addapplication:
        tip: 'There is no application to add.'
        'dummy': ''
      stringsettings:
        title: 'String Settings'
        unlock: 'Unlock'
        lock: 'Lock'
        add: 'Add'
        addandclose: 'Add & Close'
        'dummy': ''
      stringsettingstrans:
        'caption': "Label key: '{0}'<br/>Ref text: '{1}'"
        'dummy': ''
      languagesettings:
        title: 'Language Settings'
        'dummy': ''
      searchtext:
        caption: 'Text "{0}" found in {1} {2} version {3}'
        globalcaption: '{0} matches for "{1}"'
      history:
        'caption': "History for dictionary '{0}'"
      "delete":
        title: 'Delete'
        "delmsg": 'Delete selected {0}(s)?'
      dictlistpreview:
        title: 'Dictionaries Preview'
        "import": 'Import'
        check: 'There are some errors need to be fixed before import!'
#        success: 'Import dictionaries to {0} successful. <br/><br/>{1}'
        success: 'Import {0} label(s) in {1} dictionaries to {2} successful.'

      dictpreviewstringsettings:
        title: 'String Settings'
        'dummy': ''

      "addlanguage":
        "languagetip": "Please select language"
        "charsettip": "Please select charset"
        "coderequired": "Code is required"
        "successtip": "Add language {0} success"

      "customcontext":
        "namerequired": 'Context name is required.'
        "labeltip": 'Please select label(s)'

    browse: 'Import dictionaries'
    generating: 'Generating...'
    choosefile: 'Choose file'
    uploadingfile: 'Uploading: '
    uploadfinished: 'upload finished.'
  "zh-cn": true

