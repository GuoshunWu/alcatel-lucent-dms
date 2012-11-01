// Generated by CoffeeScript 1.3.3
(function() {

  define(function(require) {
    var $, URL, c18n, dialogs, grid, productInfo,
      _this = this;
    $ = require('jquery');
    grid = require('appmng/application_grid');
    dialogs = require('appmng/dialogs');
    require('jqmsgbox');
    c18n = require('i18n!nls/common');
    URL = {
      get_product_by_base_id: '/rest/products/version'
    };
    $("#newVersion").button({
      text: false,
      label: '&nbsp;',
      icons: {
        primary: "ui-icon-plus"
      }
    }).click(function() {
      return dialogs.newProductVersion.dialog("open");
    });
    $("#removeVersion").button({
      text: false,
      label: '&nbsp;',
      icons: {
        primary: "ui-icon-minus"
      }
    }).click(function() {
      var id;
      id = $("#selVersion").val();
      if (!id) {
        return;
      }
      return $.post('/app/remove-product', {
        id: id
      }, function(json) {
        if (json.status !== 0) {
          $.msgBox(json.message, null, {
            title: c18n.error
          });
          return;
        }
        $("#selVersion option:selected").remove();
        return $('#selVersion').trigger('change');
      });
    });
    productInfo = {};
    $('#selVersion').change(function() {
      var product;
      product = {
        version: $(this).find("option:selected").text(),
        id: $(this).val()
      };
      if (!product.id) {
        product.id = -1;
      }
      productInfo.product = product;
      return grid.productChanged(productInfo);
    });
    return {
      refresh: function(info) {
        productInfo.base = {
          id: info.id,
          text: info.text
        };
        $('#dispProductName').html(productInfo.base.text);
        return $.getJSON(URL.get_product_by_base_id, {
          base: productInfo.base.id,
          prop: 'id,version'
        }, function(json) {
          $('#selVersion').empty().append($(json).map(function() {
            return new Option(this.version, this.id);
          }));
          $("#selVersion option:last").attr('selected', true);
          return $('#selVersion').trigger('change');
        });
      },
      getSelectedProduct: function() {
        return {
          version: $("#selVersion option:selected").text(),
          id: $('#selVersion').val()
        };
      },
      getProductSelectOptions: function() {
        return $('#selVersion').children('option').clone(true);
      },
      addNewProduct: function(product) {
        var newOption;
        newOption = new Option(product.version, product.id);
        newOption.selected = true;
        return $('#selVersion').append(newOption).trigger('change');
      }
    };
  });

}).call(this);
