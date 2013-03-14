// Generated by CoffeeScript 1.5.0
(function() {

  define(['require', 'jqueryui', 'admin/languagegrid', 'admin/charsetgrid'], function(require, $) {
    var init, ready;
    init = function() {
      var pheight, tabs;
      if (typeof console !== "undefined" && console !== null) {
        console.log("transmng panel init...");
      }
      $('#adminTabs').tabs({
        show: function(event, ui) {
          var pheight, pwidth;
          pheight = $(ui.panel).height();
          pwidth = $(ui.panel).width();
          return $('table.ui-jqgrid-btable', ui.panel).setGridHeight(pheight - 90).setGridWidth(pwidth - 20);
        }
      });
      tabs = $('#adminTabs');
      pheight = tabs.parent().height();
      tabs.tabs('option', 'pheight', pheight);
      $('div.ui-tabs-panel', tabs).height(pheight - 50);
      return tabs.tabs('select', 2);
    };
    ready = function() {
      return typeof console !== "undefined" && console !== null ? console.log("transmng panel ready...") : void 0;
    };
    init();
    return ready();
  });

}).call(this);
