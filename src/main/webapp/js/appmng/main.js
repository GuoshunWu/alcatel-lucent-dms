// Generated by CoffeeScript 1.5.0
(function() {
  var dependencies;

  dependencies = ['jqgrid', 'dms-util', 'appmng/product_panel', 'appmng/application_panel', 'appmng/layout'];

  define(dependencies, function($, util, productpnl, apppnl, layout) {
    var init, nodeSelectHandler, onShow, ready;
    nodeSelectHandler = function(node, nodeInfo) {
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
    };
    onShow = function() {};
    init = function() {
      return layout.showWelcomePanel();
    };
    ready = function(param) {
      return typeof console !== "undefined" && console !== null ? console.debug("appmng panel ready...") : void 0;
    };
    init();
    ready(this);
    return {
      onShow: onShow,
      nodeSelect: nodeSelectHandler
    };
  });

}).call(this);
