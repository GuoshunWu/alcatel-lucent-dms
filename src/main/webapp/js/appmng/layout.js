// Generated by CoffeeScript 1.5.0
(function() {

  define(['dms-util'], function(util) {
    var PANEL_PREFIX, appmngPnlGroup, panel;
    if (typeof console !== "undefined" && console !== null) {
      console.log("module appmng/layout loading.");
    }
    PANEL_PREFIX = 'DMS';
    panel = {
      welcome: "" + PANEL_PREFIX + "_welcomePanel",
      product: "" + PANEL_PREFIX + "_productPanel",
      application: "" + PANEL_PREFIX + "_applicationPanel"
    };
    appmngPnlGroup = new util.PanelGroup("div.dms_appmng_panel", "DMS_welcomePanel");
    return {
      showProductPanel: function() {
        return appmngPnlGroup.switchTo(panel.product, function() {
          var grid, parent;
          grid = $('#applicationGridList');
          parent = $('#applicationGrid_parent');
          return grid.setGridWidth(parent.width() - 10).setGridHeight(parent.height() - 85);
        });
      },
      showApplicationPanel: function() {
        return appmngPnlGroup.switchTo(panel.application, function() {
          var parent;
          parent = $('#dictionaryGridList_parent');
          return $('#dictionaryGridList').setGridWidth(parent.width() - 10).setGridHeight(parent.height() - 85);
        });
      },
      showWelcomePanel: function() {
        return appmngPnlGroup.switchTo(panel.welcome);
      },
      layout: appmngPnlGroup
    };
  });

}).call(this);
