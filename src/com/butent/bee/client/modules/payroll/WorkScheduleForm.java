package com.butent.bee.client.modules.payroll;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.payroll.PayrollConstants.ObjectStatus;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

class WorkScheduleForm extends AbstractFormInterceptor implements SelectorEvent.Handler {

  private static final BeeLogger logger = LogUtils.getLogger(WorkScheduleForm.class);

  private static final String STYLE_PREFIX = PayrollKeeper.STYLE_PREFIX + "wsf-";

  private static final String STYLE_OBJECT_WIDGET = STYLE_PREFIX + "object-widget";
  private static final String STYLE_OBJECT_ACTIVE = STYLE_PREFIX + "object-active";

  private UnboundSelector objectSelector;
  private Flow objectPanel;
  private Flow schedulePanel;

  WorkScheduleForm() {
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof UnboundSelector) {
      objectSelector = (UnboundSelector) widget;
      objectSelector.addSelectorHandler(this);

    } else if (BeeUtils.same(name, "Objects")) {
      objectPanel = (Flow) widget;

    } else if (BeeUtils.same(name, "Schedule")) {
      schedulePanel = (Flow) widget;
    }
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    switch (action) {
      case REFRESH:
        getObjects();

        if (schedulePanel != null && !schedulePanel.isEmpty()) {
          schedulePanel.clear();
        }
        if (objectSelector != null) {
          objectSelector.clearValue();
        }
        return false;

      case PRINT:
        WorkScheduleWidget widget = UiHelper.getChild(schedulePanel, LocationSchedule.class);
        if (widget == null) {
          return true;
        } else {
          Printer.print(widget);
          return false;
        }

      default:
        return super.beforeAction(action, presenter);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new WorkScheduleForm();
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (event.isChanged() && DataUtils.hasId(event.getRelatedRow())) {
      selectObject(event.getRelatedRow().getId());
    }
  }

  @Override
  public void onLoad(FormView form) {
    getObjects();
  }

  private Widget findObjectWidget(long id) {
    for (Widget widget : objectPanel) {
      if (DomUtils.getDataIndexLong(widget.getElement()) == id) {
        return widget;
      }
    }
    return null;
  }

  private void getObjects() {
    Filter filter = Filter.and(BeeKeeper.getUser().getFilter(COL_LOCATION_MANAGER),
        Filter.equals(COL_LOCATION_STATUS, ObjectStatus.ACTIVE));

    Queries.getRowSet(VIEW_LOCATIONS, null, filter, new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        renderObjects(result);
      }
    });
  }

  private Widget renderObject(long id, String name, String company) {
    Label widget = new Label(name);
    widget.addStyleName(STYLE_OBJECT_WIDGET);

    if (!BeeUtils.isEmpty(company)) {
      widget.setTitle(company);
    }

    DomUtils.setDataIndex(widget.getElement(), id);

    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        long objId = DomUtils.getDataIndexLong(EventUtils.getEventTargetElement(event));
        if (DataUtils.isId(objId)) {
          selectObject(objId);

          if (objectSelector != null) {
            objectSelector.setValue(objId, false);
          }
        }
      }
    });

    return widget;
  }

  private void renderObjects(BeeRowSet rowSet) {
    if (objectPanel == null) {
      logger.severe(NameUtils.getName(this), "object panel not found");
      return;
    }

    if (!objectPanel.isEmpty()) {
      objectPanel.clear();
    }

    if (!DataUtils.isEmpty(rowSet)) {
      int nameIndex = rowSet.getColumnIndex(COL_LOCATION_NAME);
      int companyIndex = rowSet.getColumnIndex(ClassifierConstants.ALS_COMPANY_NAME);

      for (BeeRow row : rowSet) {
        objectPanel.add(renderObject(row.getId(),
            row.getString(nameIndex), row.getString(companyIndex)));
      }
    }
  }

  private void renderSchedule(long objectId) {
    if (schedulePanel == null) {
      logger.severe(NameUtils.getName(this), "schedule panel not found");

    } else {
      if (!schedulePanel.isEmpty()) {
        schedulePanel.clear();
      }

      WorkScheduleWidget widget = new LocationSchedule(objectId);
      schedulePanel.add(widget);

      widget.refresh();
    }
  }

  private static boolean isSelected(Widget widget) {
    return widget != null && widget.getElement().hasClassName(STYLE_OBJECT_ACTIVE);
  }

  private void selectObject(long id) {
    Widget objectWidget = findObjectWidget(id);
    if (isSelected(objectWidget)) {
      return;
    }

    for (Widget widget : objectPanel) {
      if (isSelected(widget)) {
        widget.removeStyleName(STYLE_OBJECT_ACTIVE);
      }
    }

    if (objectWidget != null) {
      objectWidget.addStyleName(STYLE_OBJECT_ACTIVE);
    }

    renderSchedule(id);
  }
}
