package com.butent.bee.client.view.form.interceptor;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.dialog.ModalGrid;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.output.Printable;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HasStringValue;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public abstract class ReportInterceptor extends AbstractFormInterceptor implements Printable {

  private static BeeLogger logger = LogUtils.getLogger(ReportInterceptor.class);

  private static final String NAME_DATA_CONTAINER = "DataContainer";

  private static final NumberFormat percentFormat = Format.getNumberFormat("0.0");
  private static final NumberFormat quantityFormat = Format.getNumberFormat("#,###");
  private static final NumberFormat amountFormat = Format.getNumberFormat("#,##0.00");
  
  protected static void drillDown(String gridName, String caption, Filter filter, boolean modal) {
    GridOptions gridOptions = GridOptions.forCaptionAndFilter(caption, filter);

    PresenterCallback presenterCallback;
    if (modal) {
      presenterCallback = ModalGrid.opener(80, CssUnit.PCT, 60, CssUnit.PCT);
    } else {
      presenterCallback = PresenterCallback.SHOW_IN_NEW_TAB;
    }

    GridFactory.openGrid(gridName, null, gridOptions, presenterCallback);
  }
  
  protected static boolean drillModal(NativeEvent event) {
    return Popup.getActivePopup() != null || !EventUtils.hasModifierKey(event);
  }

  protected static String renderAmount(Double x) {
    if (BeeUtils.isDouble(x) && !BeeUtils.isZero(x)) {
      return amountFormat.format(x);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }
  
  protected static String renderPercent(int x, int y) {
    if (x > 0 && y > 0) {
      return percentFormat.format(x * 100d / y);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  protected static String renderPercent(Double p) {
    if (BeeUtils.isDouble(p) && !BeeUtils.isZero(p)) {
      return percentFormat.format(p);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  protected static String renderQuantity(int x) {
    if (x > 0) {
      return quantityFormat.format(x);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  private static void widgetNotFound(String name) {
    logger.severe("widget not found", name);
  }

  protected ReportInterceptor() {
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    switch (action) {
      case REFRESH:
      case FILTER:
        doReport();
        return false;

      case REMOVE_FILTER:
        clearFilter();
        return false;

      case PRINT:
        if (hasReport()) {
          Printer.print(this);
        }
        return false;

      default:
        return super.beforeAction(action, presenter);
    }
  }

  @Override
  public String getCaption() {
    return getFormView().getCaption();
  }

  @Override
  public Element getPrintElement() {
    if (hasReport()) {
      return getDataContainer().getWidget(0).getElement();
    } else {
      return null;
    }
  }

  @Override
  public boolean onPrint(Element source, Element target) {
    return true;
  }

  protected boolean checkRange(DateTime start, DateTime end) {
    if (start != null && end != null && TimeUtils.isMore(start, end)) {
      getFormView().notifyWarning(Localized.getConstants().invalidRange(),
          TimeUtils.renderPeriod(start, end));
      return false;
    } else {
      return true; 
    }
  }

  protected void clearEditor(String name) {
    Widget widget = getFormView().getWidgetByName(name);
    if (widget instanceof Editor) {
      ((Editor) widget).clearValue();
    } else {
      widgetNotFound(name);
    }
  }

  protected abstract void clearFilter();

  protected abstract void doReport();

  protected HasIndexedWidgets getDataContainer() {
    Widget widget = getFormView().getWidgetByName(NAME_DATA_CONTAINER);
    if (widget instanceof HasIndexedWidgets) {
      return (HasIndexedWidgets) widget;
    } else {
      widgetNotFound(NAME_DATA_CONTAINER);
      return null;
    }
  }

  protected DateTime getDateTime(String name) {
    Widget widget = getFormView().getWidgetByName(name);
    if (widget instanceof InputDateTime) {
      return ((InputDateTime) widget).getDateTime();
    } else {
      widgetNotFound(name);
      return null;
    }
  }

  protected String getEditorValue(String name) {
    Widget widget = getFormView().getWidgetByName(name);
    if (widget instanceof HasStringValue) {
      return ((HasStringValue) widget).getValue();
    } else {
      widgetNotFound(name);
      return null;
    }
  }

  protected String getFilterLabel(String name) {
    Widget widget = getFormView().getWidgetByName(name);

    if (widget instanceof MultiSelector) {
      MultiSelector selector = (MultiSelector) widget;
      List<Long> ids = DataUtils.parseIdList(selector.getValue());

      if (ids.isEmpty()) {
        return null;
      } else {
        List<String> labels = Lists.newArrayList();
        for (Long id : ids) {
          labels.add(selector.getRowLabel(id));
        }

        return BeeUtils.joinItems(labels);
      }

    } else {
      widgetNotFound(name);
      return null;
    }
  }

  protected abstract String getStorageKeyPrefix();

  protected String storageKey(String name, long user) {
    return getStorageKeyPrefix() + name + user;
  }

  private boolean hasReport() {
    HasIndexedWidgets container = getDataContainer();
    return container != null && !container.isEmpty();
  }
}
