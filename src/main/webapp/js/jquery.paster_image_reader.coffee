# Created by STRd6
# MIT License
# jquery.paste_image_reader.js.coffee
(($) ->
  # Make sure paste events get clipboard data
  $.event.fix = ((originalFix) ->
    (event) ->
      event = originalFix.apply(@, arguments)
      if event.type.indexOf('copy') == 0 || event.type.indexOf('paste') == 0
        console.log "event=", event
        event.clipboardData = event.originalEvent.clipboardData

      return event)($.event.fix)


  # Create the plugin
  # To use it: $("html").pasteImageReader callback
  matchTypes =
    image: /image.*/, text: /text.*/, file: /Files/
  $.fn.pasteImageReader = (options) ->
    if typeof options == "function"
      options =
        callback: options

    options = $.extend({}, $.fn.pasteImageReader.defaults, options)

    # Listen to paste events on each element in the selector
    $this=@
    @on 'paste', (event) ->
      clipboardData = event.clipboardData || window.clipboardData

      # Loop through all types the data can be pasted as until
      # we hit an image type
#      console.log "begin loop"
#      guess chrome and IE items
      items = clipboardData.items || clipboardData.files

      for i, type of clipboardData.types when (!options.matchType) or options.matchType and (type.match(options.matchType) or clipboardData.items[i].type.match(options.matchType))
        if type.match matchTypes.text
          options.callback.call($this, clipboardData.getData('text'))
          continue

        type = clipboardData.items[i].type
        file = clipboardData.items[i].getAsFile()
        reader = new FileReader()
        reader.onload =(evt)->
          data =
            dataURL: evt.target.result,
            file: file
            event: evt
          options.callback.call($this, data)
        reader.readAsDataURL file

      event.preventDefault()
      event.stopPropagation()

  $.fn.pasteImageReader.defaults =
    callback: $.noop
    matchType: /image.*/)(jQuery)