/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 12-9-4
 * Time: 下午6:16
 * To change this template use File | Settings | File Templates.
 */

(function ($) {
    $.widget("ui.combobox", {
        options:{
            change:null,
            selected:null
        },
        _init:function () {
            var self = this;
        },
        _create:function () {
            var self = this;
            var select = this.element.hide();
            self.valid = false;
            var selected = select.children(":selected");
            var value = selected.val() ? selected.text() : "";
            var wrapper = this.wrapper = $("<span>").addClass("ui-combobox").insertAfter(select);


            var input = self.input = $("<input id='cboInput'>")
                .appendTo(wrapper)
                .val(value).addClass("ui-state-default ui-combobox-input")
                .autocomplete({
                    delay:0,
                    minLength:0,
                    source:function (request, response) {
                        var matcher = new RegExp($.ui.autocomplete.escapeRegex(request.term), "i");
                        response(select.children("option").map(function () {
                            var text = $(this).text();
                            if (this.value && ( !request.term || matcher.test(text) ))
                                return {
                                    label:text.replace(
                                        new RegExp(
                                            "(?![^&;]+;)(?!<[^<>]*)(" +
                                                $.ui.autocomplete.escapeRegex(request.term) +
                                                ")(?![^<>]*>)(?![^&;]+;)", "gi"
                                        ), "<strong>$1</strong>"),
                                    value:text,
                                    option:this
                                };
                        }));
                    },
                    select:function (event, ui) {
                        self._trigger("selected", event, {value:ui.item.option.value, text:ui.item.option.text});
                        self.valid = true;
                    },
                    change:function (event, ui) {

                        if (!ui.item) {
                            var matcher = new RegExp("^" + $.ui.autocomplete.escapeRegex($(this).val()) + "$", "i");
                            var valid = false;
                            select.children("option").each(function () {
                                if ($(this).text().match(matcher)) {
                                    this.selected = valid = self.valid = true;
                                    return false;
                                }
                            });
                            self._trigger("change", {value:null, text:$(this).val()});

                            if (!valid) {
                                // remove invalid value, as it didn't match anything
//                                                $(this).val("");
//                                                select.val("");
//                                                input.data("autocomplete").term = "";
                                self.valid = false;
                                return false;
                            }
                        }
                        self._trigger("change", event, {value:ui.item.option.value, text:ui.item.option.text});
                    }
                })
                .addClass("ui-widget ui-widget-content ui-corner-left");
            input.data("autocomplete")._renderItem = function (ul, item) {
                return $("<li></li>").data("item.autocomplete", item)
                    .append("<a>" + item.label + "</a>")
                    .appendTo(ul);
            };

            $("<a>")
                .attr("tabIndex", -1)
                .attr("title", "Show All Items")
                .appendTo(wrapper)
                .button({
                    icons:{
                        primary:"ui-icon-triangle-1-s"
                    },
                    text:false
                })
                .removeClass("ui-corner-all")
                .addClass("ui-corner-right ui-combobox-toggle")
                .click(function () {
                    // close if already visible
                    if (input.autocomplete("widget").is(":visible")) {
                        input.autocomplete("close");
                        return;
                    }

                    // work around a bug (likely same cause as #5265)
                    $(this).blur();

                    // pass empty string as value to search for, displaying all results
                    input.autocomplete("search", "");
                    input.focus();
                });
        },

        val:function (value) {
            if (value) {
                this.input.val(value);
            }
            var select = this.element;
            var selected = select.children(":selected");


            if (this.valid) {
                return {value:selected.val(), text:selected.text()}
            }
            return {value:null, text:this.input.val()}
        },
        destroy:function () {
            this.wrapper.remove();
            this.element.show();
            $.Widget.prototype.destroy.call(this);
        }
    });
})(jQuery);