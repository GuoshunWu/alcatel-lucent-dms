// Generated by CoffeeScript 1.5.0

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
        del: getURL('remove-product-base', 'app/')
      },
      app: {
        create: getURL('create-application-base', 'app/'),
        del: getURL('remove-application-base', 'app/')
      }
    };
  });

}).call(this);
