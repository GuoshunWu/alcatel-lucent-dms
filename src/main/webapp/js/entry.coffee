###
  This is the global entry of the project js
###

require ['../js/config', '../js/lib/domReady'], (config, domReady)->domReady(->require ['main'])
