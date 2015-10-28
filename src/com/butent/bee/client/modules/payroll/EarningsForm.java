package com.butent.bee.client.modules.payroll;

import com.google.common.base.Splitter;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Badge;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Map;
import java.util.TreeMap;

class EarningsForm extends AbstractFormInterceptor {

  private static final BeeLogger logger = LogUtils.getLogger(EarningsForm.class);

  private static String formatYm(YearMonth ym) {
    return BeeUtils.joinWords(ym.getYear(), Format.renderMonthFullStandalone(ym).toLowerCase());
  }

  private Flow monthsPanel;

  EarningsForm() {
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, "Months") && widget instanceof Flow) {
      monthsPanel = (Flow) widget;
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new EarningsForm();
  }

  @Override
  public void onLoad(FormView form) {
    getMonths();
  }

  private static Long getManager() {
    return BeeKeeper.getUser().getUserId();
  }

  private void getMonths() {
    ParameterList params = PayrollKeeper.createArgs(SVC_GET_SCHEDULED_MONTHS);

    Long manager = getManager();
    if (DataUtils.isId(manager)) {
      params.addQueryItem(COL_LOCATION_MANAGER, manager);
    }

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Map<YearMonth, Integer> months = new TreeMap<>();

        if (response.hasResponse()) {
          for (String s : Splitter.on(BeeConst.CHAR_COMMA).split(response.getResponseAsString())) {
            YearMonth ym = YearMonth.parse(BeeUtils.getPrefix(s, BeeConst.CHAR_EQ));
            Integer cnt = BeeUtils.toIntOrNull(BeeUtils.getSuffix(s, BeeConst.CHAR_EQ));

            if (ym != null && BeeUtils.isPositive(cnt)) {
              months.put(ym, cnt);
            }
          }
        }

        renderMonths(months);
      }
    });
  }

  private static Widget renderMonth(YearMonth ym, int count) {
    Flow panel = new Flow();

    Label label = new Label(formatYm(ym));
    panel.add(label);

    Badge badge = new Badge(count);
    panel.add(badge);

    return panel;
  }

  private void renderMonths(Map<YearMonth, Integer> months) {
    if (monthsPanel == null) {
      logger.severe(NameUtils.getName(this), "months panel not found");
      return;
    }

    if (!monthsPanel.isEmpty()) {
      monthsPanel.clear();
    }

    if (!BeeUtils.isEmpty(months)) {
      for (Map.Entry<YearMonth, Integer> entry : months.entrySet()) {
        monthsPanel.add(renderMonth(entry.getKey(), entry.getValue()));
      }
    }
  }
}
