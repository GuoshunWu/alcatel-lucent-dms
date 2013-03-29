// Generated by CoffeeScript 1.6.2
(function() {
  jQuery(function($) {
    var base, long_polling, randomStr;

    base = '/dms';
    randomStr = function(length, alphbet) {
      var ch, rstr, _i, _len;

      if (length == null) {
        length = 10;
      }
      if (alphbet == null) {
        alphbet = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz';
      }
      rstr = '';
      for (_i = 0, _len = alphbet.length; _i < _len; _i++) {
        ch = alphbet[_i];
        rstr += alphbet[Math.floor(Math.random() * alphbet.length)];
        length--;
        if (0 === length) {
          break;
        }
      }
      return rstr;
    };
    /*
      generate a progress bar
    */

    window.genProgressBar = function(autoDispaly, autoRemoveWhenCompleted) {
      var pbContainer, randStr;

      if (autoDispaly == null) {
        autoDispaly = true;
      }
      if (autoRemoveWhenCompleted == null) {
        autoRemoveWhenCompleted = true;
      }
      randStr = randomStr(5);
      pbContainer = $("<div id=\"pb_container_" + randStr + "\"  class=\"progressbar-container\">\n<div class=\"progressbar-msg\">\nLoading...\n</div>\n<div id=\"progressbar_" + randStr + "\" class=\"progressbar\">\n<div class=\"progressbar-label\">0.00%</div>\n</div>\n</div>").appendTo(document.body).draggable({
        create: function() {
          return $("#progressbar_" + randStr, this).progressbar({
            max: 100,
            create: function(e, ui) {
              this.label = $('div.progressbar-label', this);
              return this.msg = $('div.progressbar-msg', pbContainer);
            },
            change: function(e, ui) {
              if ($(this).is(":data(msg)")) {
                this.msg.html($(this).data('msg'));
              }
              return this.label.html("" + ($(this).progressbar('value').toPrecision(4)) + "%");
            },
            complete: function(e, ui) {
              if (autoRemoveWhenCompleted) {
                return pbContainer.remove();
              }
            }
          });
        }
      }).hide();
      if (autoDispaly) {
        pbContainer.show().position({
          my: 'center',
          at: 'center',
          of: window
        });
      }
      return $("#progressbar_" + randStr, pbContainer);
    };
    long_polling = function(url, postData, callback, pb) {
      var pollingInterval, reTryAjax;

      if (!postData || !postData.pqCmd) {
        postData.pqCmd = 'start';
      }
      if (postData.pqCmd === "start") {
        postData.freq = ($("#eFreq").val() ? parseInt($("#eFreq").val()) : 2000);
        postData.speed = ($("#speed").val() ? parseInt($("#speed").val()) : 1000);
      }
      if (postData.freq < 2) {
        postData.freq = 2;
      }
      if (postData.speed < 2) {
        postData.speed = 2;
      }
      pollingInterval = $("#pollingFreq").val() ? parseInt($("#pollingFreq").val()) : 1000;
      reTryAjax = function(retryTimes, retryCounter) {
        if (retryTimes == null) {
          retryTimes = Number.MAX_VALUE;
        }
        if (retryCounter == null) {
          retryCounter = 0;
        }
        return $.ajax(url, {
          cache: false,
          data: postData,
          type: 'post',
          dataType: "json"
        }).done(function(data, textStatus, jqXHR) {
          if ('error' === data.event.cmd) {
            $.msgBox(event.msg, null, {
              title: c18n.error
            });
            return;
          }
          if ('done' === data.event.cmd) {
            if (typeof callback === "function") {
              callback(data);
            }
            return;
          }
          if (pb) {
            pb.toggleClass('progressbar-indeterminate', -1 === data.event.percent);
            pb.data('msg', data.event.msg);
            pb.progressbar('value', data.event.percent);
          } else {
            if (typeof callback === "function") {
              callback(data);
            }
          }
          return setTimeout((function() {
            return long_polling(url, {
              pqCmd: 'process',
              pqId: data.pqId
            }, callback, pb);
          }), pollingInterval);
        }).fail(function(jqXHR, textStatus, errorThrown) {
          if ('timeout' !== textStatus) {
            return;
          }
          if (retryTimes > 0) {
            return setTimeout((function() {
              return reTryAjax(--retryTimes, ++retryCounter);
            }), pollingInterval);
          } else {

          }
        });
      };
      return reTryAjax(10);
    };
    return $("#startAction").button().click(function(e) {
      var pb;

      pb = genProgressBar();
      return long_polling("../scripts/cp.groovy", {}, function(event) {
        return pb.parent().remove();
      }, pb);
    });
  });

}).call(this);
