// Generated by CoffeeScript 1.6.2
/*
Created by IntelliJ IDEA.
User: Guoshun Wu
Date: -8-
Time: 下午7:
*/


(function() {
  define(function(require) {
    var $, getURL;

    $ = require('jquery');
    getURL = function(url, prefix, params) {
      var isFirst, k, v;

      if (prefix == null) {
        prefix = '';
      }
      if (params == null) {
        params = {};
      }
      url = prefix + url;
      if ($.isEmptyObject(params)) {
        return url;
      }
      url = "" + url + "?";
      isFirst = true;
      for (k in params) {
        v = params[k];
        if (!isFirst) {
          url += '&';
        }
        url += "" + k + "=" + (decodeURIComponent(v));
        isFirst = false;
      }
      return url;
    };
    return {
      test: getURL('test', 'test/', {
        name: 'aaa',
        id: 32
      }),
      /* URLs for product tree.
      */

      navigateTree: getURL('rest/products', '', {
        format: 'tree'
      }),
      product: {
        create: getURL('create-product', 'app/'),
        del: getURL('remove-product-base', 'app/'),
        create_version: getURL('create-product-release', 'app/')
      },
      app: {
        create: getURL('create-application-base', 'app/'),
        del: getURL('remove-application-base', 'app/'),
        create_version: getURL('create-application', 'app/'),
        remove_version: getURL('remove-application', 'app/'),
        deliver_dict: getURL('deliver-dict', 'app/'),
        update_label_status: getURL('update-label-status', 'app/')
      },
      label: {
        del: getURL('delete-label', 'app/'),
        create: getURL('add-label', 'app/')
      },
      task: {
        apply: getURL('apply-task', 'task/'),
        close: getURL('close-task', 'task/')
      },
      trans: {
        update_translation: getURL('update-translation', 'trans/'),
        update_status: getURL('update-status', 'trans/')
      },
      user: {
        update: getURL('user', 'admin/')
      },
      config: {
        create_index: getURL('config', 'admin/')
      },
      prod_versions: getURL('products/version', 'rest/'),
      app_versions: getURL("applications/apps/", 'rest/'),
      apps: getURL('applications', 'rest/'),
      dicts: getURL('dict', 'rest/'),
      tasks: getURL('tasks', 'rest/'),
      labels: getURL('luceneLabels', 'rest/'),
      languages: getURL('languages', 'rest/'),
      users: getURL('users', 'rest/'),
      ldapuser: getURL('users/ldapUser', 'rest/')
    };
  });

}).call(this);
