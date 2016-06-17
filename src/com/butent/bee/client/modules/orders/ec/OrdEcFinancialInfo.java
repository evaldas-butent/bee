package com.butent.bee.client.modules.orders.ec;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcConstants.EcOrderStatus;
import com.butent.bee.shared.modules.ec.EcFinInfo;
import com.butent.bee.shared.modules.ec.EcOrder;
import com.butent.bee.shared.modules.ec.EcUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class OrdEcFinancialInfo extends OrdEcView {

  private static final String STYLE_NAME = "finInfo";

  private static final String STYLE_PREFIX_FIN = EcStyles.name(STYLE_NAME, "fin-");

  private static final String STYLE_SUFFIX_TABLE = "table";

  private static final String STYLE_SUFFIX_LABEL = "label";
  private static final String STYLE_SUFFIX_VALUE = "value";

  private static Widget renderFin(EcFinInfo finInfo) {
    if (finInfo == null) {
      return null;
    }

    HtmlTable table = new HtmlTable(STYLE_PREFIX_FIN + STYLE_SUFFIX_TABLE);
    int row = 0;
    int col = 0;

    String stylePrefix = STYLE_PREFIX_FIN + "limit";
    Widget label = renderFinLabel(Localized.dictionary().ecCreditLimit());
    table.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    Widget value = renderFinAmount(finInfo.getCreditLimit());
    table.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_FIN + "days";
    label = renderFinLabel(Localized.dictionary().ecDaysForPayment());
    table.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = new CustomDiv(STYLE_PREFIX_FIN + STYLE_SUFFIX_VALUE);
    value.getElement().setInnerText(BeeUtils.toString(BeeUtils.unbox(finInfo.getDaysForPayment())));
    table.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    row++;
    col = 0;

    stylePrefix = STYLE_PREFIX_FIN + "orders";
    label = renderFinLabel(Localized.dictionary().ecTotalOrdered());
    table.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    double total = 0;
    for (EcOrder order : finInfo.getOrders()) {
      if (EcOrderStatus.in(order.getStatus(), EcOrderStatus.NEW, EcOrderStatus.ACTIVE)) {
        total += order.getAmount();
      }
    }

    value = renderFinAmount(total);
    table.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_FIN + "taken";
    label = renderFinLabel(Localized.dictionary().ecTotalTaken());
    table.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderFinAmount(finInfo.getTotalTaken());
    table.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    row++;
    col = 0;

    stylePrefix = STYLE_PREFIX_FIN + "debt";
    label = renderFinLabel(Localized.dictionary().ecDebt());
    table.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderFinAmount(finInfo.getDebt());
    table.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    stylePrefix = STYLE_PREFIX_FIN + "maxed";
    label = renderFinLabel(Localized.dictionary().ecMaxedOut());
    table.setWidgetAndStyle(row, col++, label, stylePrefix + STYLE_SUFFIX_LABEL);

    value = renderFinAmount(finInfo.getMaxedOut());
    table.setWidgetAndStyle(row, col++, value, stylePrefix + STYLE_SUFFIX_VALUE);

    return table;
  }

  private static Widget renderFinAmount(Double amount) {
    CustomDiv widget = new CustomDiv(STYLE_PREFIX_FIN + STYLE_SUFFIX_VALUE);

    int cents = (amount == null) ? 0 : BeeUtils.round(amount * 100);
    String text = (cents == 0) ? BeeConst.STRING_ZERO
        : BeeUtils.joinWords(EcUtils.formatCents(cents), EcConstants.CURRENCY);

    widget.setHtml(text);
    return widget;
  }

  private static Widget renderFinLabel(String text) {
    Label label = new Label(text);
    label.addStyleName(STYLE_PREFIX_FIN + STYLE_SUFFIX_LABEL);
    return label;
  }

  OrdEcFinancialInfo() {
    super();
  }

  @Override
  protected void createUi() {
    clear();
    add(new Image(Global.getImages().loading()));

    BeeKeeper.getRpc().makeRequest(OrdEcKeeper.createArgs(SVC_FINANCIAL_INFORMATION),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            OrdEcKeeper.dispatchMessages(response);
            if (response.hasResponse(EcFinInfo.class)) {
              render(EcFinInfo.restore(response.getResponseAsString()));
            }
          }
        });
  }

  @Override
  protected String getPrimaryStyle() {
    return STYLE_NAME;
  }

  private void render(EcFinInfo finInfo) {
    if (!isEmpty()) {
      clear();
    }

    Widget finWidget = renderFin(finInfo);
    if (finWidget != null) {
      add(finWidget);
    }
  }
}