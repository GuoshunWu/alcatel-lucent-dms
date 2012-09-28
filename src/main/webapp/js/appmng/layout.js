// Generated by CoffeeScript 1.3.3
(function() {

  define(['jqlayout', 'module'], function($, module) {
    var PANEL_PREFIX, dmsPanels, ids, pageLayout, showCenterPanel;
    PANEL_PREFIX = 'DMS';
    ids = {
      container: {
        page: 'optional-container',
        center: 'ui_center'
      },
      panel: {
        welcome: PANEL_PREFIX + "_welcomePanel",
        product: PANEL_PREFIX + "_productPanel",
        application: PANEL_PREFIX + "_applicationPanel"
      }
    };
    $('#pageNavigator').val(window.location.pathname);
    pageLayout = $("#" + ids.container.page).layout({
      resizable: true,
      closable: true
    });
    dmsPanels = $("#" + ids.container.center + " div[id^=" + PANEL_PREFIX + "]");
    dmsPanels.addClass("ui-layout-content ui-corner-bottom");
    dmsPanels.css({
      paddingBottom: '1em',
      borderTop: 0
    });
    $(".header-footer").hover((function() {
      return $(this).addClass("ui-state-hover");
    }), function() {
      return $(this).removeClass("ui-state-hover");
    });
    showCenterPanel = function(panelId) {
      return dmsPanels.each(function(index, panel) {
        if (panel.id === panelId) {
          return $(panel).show();
        } else {
          return $(panel).hide();
        }
      });
    };
    showCenterPanel(ids.panel.welcome);
    return {
      showProductPanel: function() {
        return showCenterPanel(ids.panel.product);
      },
      showApplicationPanel: function() {
        return showCenterPanel(ids.panel.application);
      }
    };
  });

}).call(this);
