require.config
  baseUrl: 'js/lib'
  paths:
    jquery: 'jquery-1.11.0.min'
#    jquery: '//code.jquery.com/jquery-1.10.1'
    "jquery.migrate": 'jquery-migrate-1.2.1.min'
    jqueryui: 'jquery-ui-1.10.3.custom.min'
    'edialog': 'jquery.dialogextend.min'
#    jqueryui: 'jquery-ui'
    qunit: 'qunit-1.13.0'
    formvalidate: 'formValidator-4.0.1.min'
    formvalreg: 'formValidatorRegex'
    jqform: 'jquery.form'
    jqgrid: 'jqgridlib/jquery.jqGrid.min'
    jqgridI18n: if param? then param.i18ngridfile else 'jqgridlib/i18n/grid.locale-en'
#    jqgrid: 'jquery.jqGrid.min'
    jqtree: 'jquery.jstree'
#    jqtree: 'jstree3.0'
    jqlayout: 'jquery.layout-latest.min'
    jqmsgbox: 'jquery.msgBox.v1'
    blockui: 'jquery.blockUI.min'
    jqupload: 'jsfileuploader/jquery.fileupload'
    select2: 'select2/select2.min'
    multiselect: 'jquery.multiselect.min'
#    multiselect: 'jquery.multiple.select'

    iframetransport: 'jsfileuploader/jquery.iframe-transport'
    hchart: 'highcharts'
    hchart_exp: 'exporting'

    testcases: 'dmstestcases'

    requireLib: 'require'

    globallayout: '../globallayout'
    ptree: '../producttree'

    # modules
    appmng: '../appmng'
    transmng: '../transmng'
    taskmng: '../taskmng'
    ctxmng: '../ctxmng'
    commons: '../commons'

    webtests: '../webtests'

    login: '../login'
    admin: '../admin'
    nls: '../nls'
    main: '../main'

  shim:
    'qunit':
      exports: 'QUnit'
      init: ()->
        QUnit.config.autoload =  QUnit.config.autostart = false

    'select2':
      deps: ['jquery']
      exports: 'jQuery'
    'edialog':
      deps: ['jqueryui']
      exports: 'jQuery'
    'jquery.migrate':
      deps: ['jquery']
      exports: 'jQuery'
    'hchart':
      deps: ['jquery']
      exports: 'jQuery'
    'hchart_exp':
      deps: ['hchart']
      exports: 'jQuery'
    'formvalidate':
      deps: ['jquery']
      exports: 'jQuery'
    'formvalreg':
      'jqvalidate':
        deps: ['jquery']
        exports: 'jQuery'
    'jqform':
      deps: ['jquery']
      exports: 'jQuery'
    'jqueryui':
      deps: ['jquery']
      exports: 'jQuery'
    'i18n/grid.locale-en':
      deps: ['jquery']
      exports: 'jQuery'
    'ui.multiselect':
      deps: ['jqueryui']
      exports: 'jQuery'
    'multiselect':
      deps: ['jquery']
      exports: 'jQuery'
    'jqgridI18n':
      deps : ['jqueryui','jquery.migrate']
      exports: 'jQuery'
    'jqgrid':
      deps: ['jqgridI18n']
      exports: 'jQuery'
    'jqtree':
      deps: ['jquery']
      exports: 'jQuery'
    'jqlayout':
      deps: ['jqueryui']
      exports: 'jQuery'
    'jqmsgbox':
      deps: ['jqueryui']
      exports: 'jQuery'
    'blockui':
      deps: ['jquery']
      exports: 'jQuery'
    'themeswitchertool':
      deps: ['jquery']
      exports: 'jQuery'

  #Set the config for the i18n
  config:
    i18n:
      locale: if param? then param.locale else 'en_us'
  urlArgs: "bust=#{if param? then param.buildNumber else new Date().getTime()}"

  waitSeconds: 60
