require.config
  baseUrl: 'js/lib'
  paths:
#    jquery: 'jquery-1.7.2.min'
#    jquery: 'jquery-1.10.1.min'
    jquery: '//code.jquery.com/jquery-1.10.1'
    "jquery.migrate": 'jquery-migrate-1.2.1.min'
#    jqueryui: 'jquery-ui-1.8.22.custom.min'
    jqueryui: 'jquery-ui-1.10.3.custom.min'
#    jqueryui: 'jquery-ui'

    formvalidate: 'formValidator-4.0.1.min'
    formvalreg: 'formValidatorRegex'
    jqform: 'jquery.form'
    jqgrid: 'jquery.jqGrid.min'
    jqtree: 'jquery.jstree'
    jqlayout: 'jquery.layout-latest.min'
    jqmsgbox: 'jquery.msgBox.v1'
    blockui: 'jquery.blockUI.min'
    jqupload: 'jsfileuploader/jquery.fileupload'
    iframetransport: 'jsfileuploader/jquery.iframe-transport'
    hchart: 'highcharts'
    hchart_exp: 'exporting'

    globallayout: '../globallayout'
    ptree: '../producttree'

    # modules
    appmng: '../appmng'
    transmng: '../transmng'
    taskmng: '../taskmng'
    ctxmng: '../ctxmng'

    login: '../login'
    admin: '../admin'
    nls: '../nls'
    main: '../main'

  shim:
    'jquery.migrate':
      deps: ['jquery']
      exports: 'jQuery'
    'jquerym':
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
    'jqupload':
      deps: ['jqueryui','jquery.migrate','iframetransport']
      exports: 'jQuery'
    'i18n/grid.locale-en':
      deps: ['jquery']
      exports: 'jQuery'
    'ui.multiselect':
      deps: ['jquery']
      exports: 'jQuery'
    'jqgrid':
      deps: ['jqueryui','jquery.migrate', if param? then param.i18ngridfile else 'i18n/grid.locale-en']
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
  urlArgs: "bust=#{if param? then param.buildNumber else '1'}"

  waitSeconds: 60