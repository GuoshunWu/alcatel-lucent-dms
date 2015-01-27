// Generated by IcedCoffeeScript 1.8.0-c

/*
This javascript file checks for the brower/browser tab action.
It is based on the file menstioned by Daniel Melo.
Reference: http://stackoverflow.com/questions/1921941/close-kill-the-session-when-the-browser-or-tab-is-closed
 */

(function() {
  define(['jqueryui'], function($) {
    var wireUpEvents;
    window.validNavigation = false;
    wireUpEvents = function() {
      window.onunload = null;
      window.onbeforeunload = function() {
        $.post('../scripts/jsonpservice.groovy', {
          'navigator': navigator.userAgent,
          'time': new Date().getTime()
        });
        return void 0;
      };
      $(document).keypress(function(e) {
        return window.validNavigation = e.which === 116;
      });
      $("a").click(function() {
        return window.validNavigation = true;
      });
      $("form").submit(function() {
        return window.validNavigation = true;
      });
      return $("input[type=submit]").click(function() {
        return window.validNavigation = true;
      });
    };
    return $(document).ready(function() {
      return wireUpEvents();
    });
  });

}).call(this);

//# sourceMappingURL=check_browser_close.js.map