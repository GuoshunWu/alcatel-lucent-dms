// Generated by CoffeeScript 1.4.0
(function() {
  var loadScript;

  String.prototype.trim = function() {
    return this.replace(/(^\s*)|(\s*$)/g, "");
  };

  loadScript = function(src, doc, async, charset, flag) {
    var head, script;
    if (doc == null) {
      doc = window.document;
    }
    if (async == null) {
      async = true;
    }
    if (charset == null) {
      charset = 'utf-8';
    }
    if (flag == null) {
      flag = 'nothing';
    }
    head = doc.getElementsByTagName('head')[0];
    script = doc.createElement('script');
    script.type = 'text/javascript';
    script.charset = charset;
    script.async = async;
    script.src = src;
    return head.appendChild(script);
  };

  jQuery(function($) {
    var progress;
    $("#accessIframe").button().click(function() {
      var iBody;
      if (frames.length === 0) {
        console.log("There is no frame in this page.");
        return false;
      }
      false;
      iBody = frames[0].document;
      console.log($(iBody).text().trim());
      return false;
    });
    progress = function(msg, sep) {
      if (!sep) {
        sep = ";";
      }
      return $(msg.split(sep)).each(function(index, elem) {
        if ($.isNumeric(elem)) {
          return $("#progressbar").progressbar("value", parseFloat(elem));
        }
      });
    };
    $("#pushStart").button().on('click', {
      pushForm: $('#pushForm')
    }, function(e) {
      window[$("input[name='callback']").val()] = function(data) {
        console.log(data);
        progress(data.msg);
        $('#content').html("" + (new Date(data.stamp).toLocaleString()) + "<br/>Msg: " + data.msg);
        if (/^done/.test(data.msg)) {
          $("#pushStart").button("enable");
          return delete window[$("input[name='callback']").val()];
        }
      };
      $("#progressbar").progressbar("value", 0).show().position({
        my: 'center',
        at: 'center',
        of: window
      });
      $("#pushStart").button("disable");
      return e.data.pushForm.submit();
    });
    return $("#progressbar").draggable({
      grid: [50, 20],
      opacity: 0.35
    }).progressbar({
      max: 100,
      create: function(e, ui) {
        this.label = $('div.progressbar-label', this);
        return $(this).position({
          my: 'center',
          at: 'center',
          of: window
        });
      },
      change: function(e, ui) {
        return this.label.html(($(this).progressbar("value").toPrecision(4)) + "%");
      },
      complete: function(e, ui) {
        return $(this).hide();
      }
    }).hide();
  });

}).call(this);