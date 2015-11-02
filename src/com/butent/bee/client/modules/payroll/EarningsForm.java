package com.butent.bee.client.modules.payroll;

import com.google.common.base.Splitter;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Selectors;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
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
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class EarningsForm extends AbstractFormInterceptor {

  private static final BeeLogger logger = LogUtils.getLogger(EarningsForm.class);

  private static final String STYLE_PREFIX = PayrollKeeper.STYLE_PREFIX + "earn-";

  private static final String STYLE_MONTH_WIDGET = STYLE_PREFIX + "month-widget";
  private static final String STYLE_MONTH_ACTIVE = STYLE_PREFIX + "month-active";
  private static final String STYLE_MONTH_LABEL = STYLE_PREFIX + "month-label";
  private static final String STYLE_MONTH_BADGE = STYLE_PREFIX + "month-badge";

  private static final String KEY_YM = "ym";

  private static YearMonth parseMonth(Element element) {
    return YearMonth.parse(DomUtils.getDataProperty(element, KEY_YM));
  }

  private final ObjectEarningsGrid gridInterceptor = new ObjectEarningsGrid();

  private Flow monthsPanel;

  EarningsForm() {
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, "Months") && widget instanceof Flow) {
      monthsPanel = (Flow) widget;
    } else if (BeeUtils.same(name, GRID_OBJECT_EARNINGS) && widget instanceof GridPanel) {
      ((GridPanel) widget).setGridInterceptor(gridInterceptor);
    }
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    switch (action) {
      case REFRESH:
        final YearMonth selectedMonth = getSelectedMonth();

        getMonths(new Callback<List<YearMonth>>() {
          @Override
          public void onSuccess(List<YearMonth> result) {
            if (!BeeUtils.isEmpty(result)) {
              YearMonth ym;

              if (selectedMonth != null && result.contains(selectedMonth)) {
                ym = selectedMonth;
              } else {
                ym = BeeUtils.getLast(result);
              }

              selectMonth(ym);
            }
          }
        });

        return false;

      default:
        return super.beforeAction(action, presenter);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new EarningsForm();
  }

  @Override
  public void onLoad(FormView form) {
    getMonths(new Callback<List<YearMonth>>() {
      @Override
      public void onSuccess(List<YearMonth> result) {
        if (!BeeUtils.isEmpty(result)) {
          selectMonth(BeeUtils.getLast(result));
        }
      }
    });
  }

  private static Long getManager() {
    return BeeKeeper.getUser().getUserId();
  }

  private Element getMonthElement(YearMonth ym) {
    if (monthsPanel == null || ym == null) {
      return null;
    } else {
      return Selectors.getElementByDataProperty(monthsPanel, KEY_YM, ym.serialize());
    }
  }

  private void getMonths(final Callback<List<YearMonth>> callback) {
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
        callback.onSuccess(new ArrayList<>(months.keySet()));
      }
    });
  }

  private YearMonth getSelectedMonth() {
    if (monthsPanel == null) {
      return null;
    } else {
      Element element = Selectors.getElementByClassName(monthsPanel, STYLE_MONTH_ACTIVE);
      return (element == null) ? null : parseMonth(element);
    }
  }

  private Widget renderMonth(YearMonth ym, int count) {
    Flow panel = new Flow(STYLE_MONTH_WIDGET);
    DomUtils.setDataProperty(panel.getElement(), KEY_YM, ym.serialize());

    Label label = new Label(PayrollHelper.format(ym));
    label.addStyleName(STYLE_MONTH_LABEL);
    panel.add(label);

    Badge badge = new Badge(count, STYLE_MONTH_BADGE);
    panel.add(badge);

    panel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        selectMonth(parseMonth(EventUtils.getSourceElement(event)));
      }
    });

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

  private boolean selectMonth(YearMonth ym) {
    if (monthsPanel == null || ym == null) {
      return false;
    }

    YearMonth selectedYm = getSelectedMonth();
    if (selectedYm != null && ym.equals(selectedYm)) {
      return false;
    }

    Element element = getMonthElement(ym);
    if (element == null) {
      return false;
    }

    if (selectedYm != null) {
      Element old = getMonthElement(selectedYm);
      if (old != null) {
        old.removeClassName(STYLE_MONTH_ACTIVE);
      }
    }

    element.addClassName(STYLE_MONTH_ACTIVE);
    if (DomUtils.isInView(monthsPanel)) {
      element.scrollIntoView();
    }

    gridInterceptor.selectMonth(ym);

    return true;
  }
}
