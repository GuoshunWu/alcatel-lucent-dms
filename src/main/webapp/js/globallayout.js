// Generated by CoffeeScript 1.5.0
(function() {
  var __indexOf = [].indexOf || function(item) { for (var i = 0, l = this.length; i < l; i++) { if (i in this && this[i] === item) return i; } return -1; };

  define(function(require) {
    var autoSizeGrids, glayout, init, ready;
    require('jqlayout');
    autoSizeGrids = ['applicationGridList', 'dictionaryGridList', 'transGrid', 'taskGrid'];
    ready = function(param) {
      return typeof console !== "undefined" && console !== null ? console.debug("global layout ready...") : void 0;
    };
    init = function() {
      var layout, westSelector;
      layout = $('#global-container').layout({
        onresize: function(name, element, state, options, layoutname) {
          return $('table.ui-jqgrid-btable').each(function(index, grid) {
            var _ref;
            if ('center' === name && (_ref = grid.id, __indexOf.call(autoSizeGrids, _ref) >= 0)) {
              return $(grid).setGridWidth(element.width() - 50, false);
            }
          });
        },
        defaults: {
          size: 'auto',
          minSize: 50,
          buttonClass: "button",
          togglerClass: "toggler",
          togglerLength_open: 35,
          togglerLength_closed: 35,
          hideTogglerOnSlide: true,
          resizable: true
        },
        north: {
          minSize: 35,
          togglerLength_closed: -1,
          resizable: false
        },
        west: {
          size: 250,
          spacing_closed: 21,
          togglerLength_closed: 21,
          togglerAlign_closed: "top",
          togglerLength_open: 0,
          slideTrigger_open: "click",
          togglerTip_open: "Close This Pane",
          togglerTip_closed: "Open This Pane",
          resizerTip: "Resize This Pane",
          fxName: 'slide',
          fxSpeed_open: 750,
          fxSpeed_close: 1500,
          fxSettings_open: {
            easing: "easeOutBounce"
          }
        }
      });
      westSelector = "#global-container > div.ui-layout-west";
      $("<span />").addClass("pin-button").prependTo(westSelector);
      layout.addPinBtn("" + westSelector + " .pin-button", "west");
      $("<span />").attr("id", "west-closer").prependTo(westSelector);
      layout.addCloseBtn("#west-closer", "west");
      return layout;
    };
    glayout = init();
    ready(this);
    return {
      layout: glayout
    };
  });

}).call(this);
