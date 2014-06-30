###
This javascript file checks for the brower/browser tab action.
It is based on the file menstioned by Daniel Melo.
Reference: http://stackoverflow.com/questions/1921941/close-kill-the-session-when-the-browser-or-tab-is-closed
###
define ['jqueryui'], ($)->
  window.validNavigation = false

  wireUpEvents = ->
    #
    # For a list of events that triggers onbeforeunload on IE
    # check http://msdn.microsoft.com/en-us/library/ms536907(VS.85).aspx

    # onbeforeunload for IE and chrome
    # check http://stackoverflow.com/questions/1802930/setting-onbeforeunload-on-body-element-in-chrome-and-ie-using-jquery
    window.onunload = null
    window.onbeforeunload = ()->
#      return undefined if window.validNavigation
      $.post '../scripts/jsonpservice.groovy', {'navigator': navigator.userAgent, 'time': new Date().getTime()}
      undefined

    # Attach the event keypress to exclude the F5 refresh
    $(document).keypress (e) ->
      window.validNavigation = e.which is 116 # F5 key Code is 116

    # Attach the event click for all links in the page
    $("a").click ->
      window.validNavigation = true

    # Attach the event submit for all forms in the page
    $("form").submit ->
      window.validNavigation = true

    # Attach the event click for all inputs in the page
    $("input[type=submit]").click ->
      window.validNavigation = true

  # Wire up the events as soon as the DOM tree is ready
  $(document).ready ->
    wireUpEvents()