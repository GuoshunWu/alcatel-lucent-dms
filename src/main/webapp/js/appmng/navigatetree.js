// Generated by CoffeeScript 1.3.3
(function() {

  define(function(require) {
    var $, URL, appTree, apppnl, c18n, getNodeInfo, ids, layout, nodeCtxMenu, productpnl, removeNode, timeFunName, util;
    $ = require('jqueryui');
    util = require('util');
    require('jqtree');
    c18n = require('i18n!nls/common');
    require('jqmsgbox');
    layout = require('appmng/layout');
    productpnl = require('appmng/product_panel');
    apppnl = require('appmng/application_panel');
    ids = {
      navigateTree: 'appTree'
    };
    URL = {
      navigateTree: 'rest/products?format=tree',
      product: {
        create: 'app/create-product',
        del: 'app/remove-product-base'
      },
      app: {
        create: 'app/create-application-base',
        del: 'app/remove-application-base'
      }
    };
    appTree = null;
    getNodeInfo = function(node) {
      var info, parent, selectedNode;
      if (!appTree) {
        console.log("Error, appTree is null.");
        return null;
      }
      selectedNode = node ? node : appTree.get_selected();
      info = {
        id: selectedNode.attr('id'),
        text: appTree.get_text(selectedNode),
        type: selectedNode.attr('type')
      };
      parent = appTree._get_parent(selectedNode);
      if (parent === -1) {
        return info;
      }
      info.parent = getNodeInfo(parent);
      return info;
    };
    removeNode = function(node) {
      return $.post(URL[node.attr('type')].del, {
        id: node.attr('id')
      }, function(json) {
        if (json.status !== 0) {
          $.msgBox(json.message, null, {
            title: c18n.error,
            width: 300,
            height: 'auto'
          });
          return false;
        }
        return appTree != null ? appTree.remove(node) : void 0;
      });
    };
    nodeCtxMenu = {
      products: {
        createproduct: {
          label: 'New product',
          action: function(node) {
            return appTree != null ? appTree.create(node, 'last', {
              data: 'NewProduct',
              attr: {
                type: 'product',
                id: null
              }
            }, (function() {}), false) : void 0;
          },
          separator_before: true
        }
      },
      product: {
        createapp: {
          label: 'New application',
          action: function(node) {
            return appTree != null ? appTree.create(node, 'last', {
              data: 'NewApp',
              attr: {
                type: 'app',
                id: null
              }
            }, (function() {}), false) : void 0;
          }
        },
        del: {
          label: 'Delete product',
          action: removeNode,
          separator_before: true
        }
      },
      app: {
        del: {
          label: 'Delete application',
          action: removeNode
        }
      }
    };
    $.jstree._themes = "css/jstree/themes/";
    timeFunName = null;
    $.getJSON(URL.navigateTree, {}, function(treeInfo) {
      $("#" + ids.navigateTree).jstree({
        json_data: {
          data: treeInfo
        },
        ui: {
          select_limit: 1
        },
        themes: {},
        core: {
          initially_open: ["-1"]
        },
        contextmenu: {
          items: function(node) {
            return nodeCtxMenu[node.attr('type')];
          }
        },
        plugins: ["themes", "json_data", "ui", "core", "crrm", "contextmenu"]
      }).bind('create.jstree', function(event, data) {
        var name, node, pbId;
        appTree = data.inst;
        node = data.rslt.obj;
        name = data.rslt.name;
        if ('' === name) {
          $.jstree.rollback(data.rlbk);
          return;
        }
        if ('app' === node.attr('type')) {
          pbId = appTree._get_parent(node).attr('id');
        }
        return $.post(URL[node.attr('type')].create, {
          name: name,
          prod: pbId
        }, function(json) {
          if (json.status !== 0) {
            $.msgBox(json.message, null, {
              title: c18n.error,
              width: 300,
              height: 'auto'
            });
            $.jstree.rollback(data.rlbk);
            return false;
          }
          return data.rslt.obj.attr({
            id: json.id
          });
        });
      }).bind('loaded.jstree', function(event, data) {
        appTree = data.inst;
        if (param.currentSelected.productBaseId) {
          return appTree.select_node($("#appTree li [id=" + param.currentSelected.productBaseId + "][type=product]"));
        }
      }).bind("select_node.jstree", function(event, data) {
        clearTimeout(timeFunName);
        return timeFunName = setTimeout(function() {
          var node, nodeInfo;
          appTree = data.inst;
          node = data.rslt.obj;
          nodeInfo = getNodeInfo(node);
          switch (node.attr('type')) {
            case 'products':
              return layout.showWelcomePanel();
            case 'product':
              productpnl.refresh(nodeInfo);
              return layout.showProductPanel();
            case 'app':
              apppnl.refresh(nodeInfo);
              return layout.showApplicationPanel();
          }
        }, 300);
      }).bind('dblclick_node.jstree', function(event, data) {
        clearTimeout(timeFunName);
        return data.inst.toggle_node(data.rslt.obj);
      });
      $('#loading-container').fadeOut('slow', 'swing', function() {
        return $(this).remove();
      });
      return util.afterInitilized(this);
    });
    return {
      getNodeInfo: getNodeInfo
    };
  });

}).call(this);
