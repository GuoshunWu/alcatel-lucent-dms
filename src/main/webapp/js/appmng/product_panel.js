// Generated by CoffeeScript 1.5.0
(function() {

  define(['require', 'jqmsgbox', 'util', 'i18n!nls/common', 'appmng/application_grid', 'appmng/dialogs'], function(require, msgbox, util, c18n, grid, dialogs) {
    var URL, productInfo,
      _this = this;
    URL = {
      get_product_by_base_id: 'rest/products/version'
    };
    $("#newVersion").button({
      text: false,
      label: '&nbsp;',
      icons: {
        primary: "ui-icon-plus"
      }
    }).attr('privilegeName', util.urlname2Action('app/create-product-release')).click(function() {
      return dialogs.newProductVersion.dialog("open");
    });
    $("#removeVersion").button({
      text: false,
      label: '&nbsp;',
      icons: {
        primary: "ui-icon-minus"
      }
    }).attr('privilegeName', util.urlname2Action('app/remove-product')).click(function() {
      var id;
      id = $("#selVersion").val();
      if (!id) {
        return;
      }
      $.post('app/remove-product', {
        id: id
      }, function(json) {
        if (json.status !== 0) {
          $.msgBox(json.message, null, {
            title: c18n.error
          });
        }
      });
      $("#selVersion option:selected").remove();
      return $('#selVersion').trigger('change');
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
          $('#selVersion').empty().append(util.json2Options(json));
          if (param.currentSelected.productId) {
            $('#selVersion').val(param.currentSelected.productId);
          }
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
        return $('#selVersion').append(util.newOption(product.version, product.id, true)).trigger('change');
      }
    };
  });

}).call(this);
