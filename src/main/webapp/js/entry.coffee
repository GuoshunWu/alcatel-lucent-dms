###
  This is the global entry of the project js

  There may be times when you do want to reference a script directly and not conform to the "baseUrl + paths" rules for finding it.
  If a module ID has one of the following characterstics, the ID will not be passed through the "baseUrl + paths" configuration,
  and just be treated like a regular URL that is relative to the document:
    Ends in ".js".
    Starts with a "/".
    Contains an URL protocol, like "http:" or "https:".
  In general though, it is best to use the baseUrl and "paths" config to set paths for module IDs. By doing so, it gives you more flexibility in renaming and configuring the paths to different locations for optimization builds.
###

require ['js/config.js', 'js/lib/domReady.js', 'js/lib/require.js'], (config, domReady, r)->domReady(->require ['main'])
