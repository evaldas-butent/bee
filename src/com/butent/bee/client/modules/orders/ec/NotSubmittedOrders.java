package com.butent.bee.client.modules.orders.ec;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.orders.ec.NotSubmittedOrdersInfo;
import com.butent.bee.shared.modules.orders.ec.OrdEcCart;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;

public class NotSubmittedOrders extends OrdEcView {

  private static final String STYLE_NAME = "notSubmittedOrder";
  private static final String STYLE_PREFIX_ORDER = EcStyles.name(STYLE_NAME, "order-");
  private static final String STYLE_PREFIX_ORDER_CART = STYLE_PREFIX_ORDER + "cart-";

  private static final String STYLE_SUFFIX_TABLE = "table";
  private static final String STYLE_SUFFIX_HEADER = "header";
  private static final String STYLE_SUFFIX_PANEL = "panel";
  private static final String STYLE_SUFFIX_DATA = "data";

  private static final String STYLE_SUFFIX_LABEL = "label";

  private static final String STYLE_ORDER_DATE = STYLE_PREFIX_ORDER + "date";
  private static final String STYLE_ORDER_NAME = STYLE_PREFIX_ORDER + "name";
  private static final String STYLE_ORDER_COMMENT = STYLE_PREFIX_ORDER + "comment";

  private static final int ORDER_DATE_COL = 0;
  private static final int ORDER_NAME_COL = 1;
  private static final int ORDER_COMMENT_COL = 2;

  private static void openShoppingCart(String cartName) {
    ParameterList params = OrdEcKeeper.createArgs(SVC_EC_OPEN_SHOPPING_CART);
    params.addDataItem(COL_SHOPPING_CART_NAME, cartName);

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {

      @Override
      public void onResponse(ResponseObject response) {
        OrdEcKeeper.dispatchMessages(response);

        if (!response.hasErrors()) {
          OrdEcKeeper.openShoppinCart(input -> {
            OrdEcCart cart = OrdEcKeeper.getCart();
            OrdEcShoppingCart widget = new OrdEcShoppingCart(cart);
            BeeKeeper.getScreen().show(widget);
          });
        }
      }
    });
  }

  private Widget renderInfo(List<NotSubmittedOrdersInfo> data) {

    if (BeeUtils.isEmpty(data)) {
      return null;
    }

    Flow panel = new Flow(STYLE_PREFIX_ORDER + STYLE_SUFFIX_PANEL);
    HtmlTable table = new HtmlTable(STYLE_PREFIX_ORDER + STYLE_SUFFIX_TABLE);
    int row = 0;

    table.setWidgetAndStyle(row, ORDER_DATE_COL, renderHeader(Localized.dictionary().date()),
        STYLE_ORDER_DATE + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, ORDER_NAME_COL, renderHeader(Localized.dictionary().name()),
        STYLE_ORDER_NAME + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.setWidgetAndStyle(row, ORDER_COMMENT_COL, renderHeader(Localized.dictionary()
        .comment()), STYLE_ORDER_COMMENT + BeeConst.STRING_MINUS + STYLE_SUFFIX_LABEL);

    table.getRowFormatter().addStyleName(row, STYLE_PREFIX_ORDER + STYLE_SUFFIX_HEADER);
    row++;

    Widget widget;

    for (final NotSubmittedOrdersInfo info : data) {
      if (info.getDate() != null) {
        widget = new Label(TimeUtils.renderCompact(info.getDate()));
        table.setWidgetAndStyle(row, ORDER_DATE_COL, widget, STYLE_ORDER_DATE);
      }

      Label nameWidget = new Label(info.getName());
      nameWidget.addClickHandler(event -> {
        String cartName = event.getRelativeElement().getInnerText();
        if (OrdEcKeeper.getCart().getItems().size() > 0) {
          List<String> options1 =
              Lists.newArrayList(Localized.dictionary().yes(), Localized.dictionary().no());

          Global.choice(null, Localized.dictionary().ordSaveShoppingCart(), options1,
              value -> {
                if (value == 0) {
                  OrdEcKeeper.saveOrder(null, NotSubmittedOrders.this,
                      input -> openShoppingCart(cartName));
                } else {
                  ParameterList params = OrdEcKeeper.createArgs(SVC_EC_CLEAN_SHOPPING_CART);
                  BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {

                    @Override
                    public void onResponse(ResponseObject response) {
                      if (!response.hasErrors()) {
                        OrdEcKeeper.resetCart();
                        OrdEcKeeper.closeView(NotSubmittedOrders.this);
                        openShoppingCart(cartName);
                      }
                    }
                  });
                }
              });
        } else {
          openShoppingCart(cartName);
        }
      });

      table.setWidgetAndStyle(row, ORDER_NAME_COL, nameWidget, STYLE_ORDER_NAME);

      if (!BeeUtils.isEmpty(info.getComment())) {
        widget = new Label(info.getComment());
        table.setWidgetAndStyle(row, ORDER_COMMENT_COL, widget, STYLE_ORDER_COMMENT);
      }

      table.getRowFormatter().addStyleName(row, STYLE_PREFIX_ORDER + STYLE_SUFFIX_DATA);
      row++;
    }

    panel.add(table);
    return panel;
  }

  private static Widget renderHeader(String text) {
    Label label = new Label(text);
    label.addStyleName(STYLE_PREFIX_ORDER_CART + STYLE_SUFFIX_LABEL);
    return label;
  }

  @Override
  protected void createUi() {
    clear();
    add(new Image(Global.getImages().loading()));

    BeeKeeper.getRpc().makeRequest(OrdEcKeeper.createArgs(SVC_EC_GET_NOT_SUBMITTED_ORDERS),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            OrdEcKeeper.dispatchMessages(response);

            if (!response.hasErrors()) {
              List<NotSubmittedOrdersInfo> carts = new ArrayList<>();

              if (response.getResponseAsString() != null) {

                String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());
                if (arr != null) {
                  for (String s : arr) {
                    carts.add(NotSubmittedOrdersInfo.restore(s));
                  }
                }
              }
              if (carts.size() > 0) {
                render(carts);
              } else {
                clear();
              }
            }
          }
        });
  }

  @Override
  protected String getPrimaryStyle() {
    return "notSubmittedOrders";
  }

  private void render(List<NotSubmittedOrdersInfo> carts) {
    if (!isEmpty()) {
      clear();
    }

    Widget infoWidget = renderInfo(carts);
    if (infoWidget != null) {
      add(infoWidget);
    }
  }
}