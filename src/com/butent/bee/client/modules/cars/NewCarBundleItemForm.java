package com.butent.bee.client.modules.cars;

import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.cars.CarsConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.COL_ITEM;
import static com.butent.bee.shared.modules.transport.TransportConstants.COL_MODEL;

import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Objects;

public class NewCarBundleItemForm extends AbstractFormInterceptor {

  private TabBar tabBar;
  private UnboundSelector itemSelector;
  private UnboundSelector jobSelector;
  private Widget duration;
  private InputBoolean parent;

  private Long parentId;

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {
    if (Objects.equals(editableWidget.getColumnId(), COL_DURATION)) {
      duration = widget.asWidget();
    }
    super.afterCreateEditableWidget(editableWidget, widget);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (widget instanceof UnboundSelector) {
      if (((UnboundSelector) widget).hasRelatedView(VIEW_CAR_ITEMS)) {
        itemSelector = (UnboundSelector) widget;

      } else if (((UnboundSelector) widget).hasRelatedView(TBL_CAR_JOBS)) {
        jobSelector = (UnboundSelector) widget;
        jobSelector.addSelectorHandler(this::onJobSelection);

      }
    } else if (widget instanceof TabBar) {
      tabBar = (TabBar) widget;
      tabBar.addSelectionHandler(ev -> refresh());

    } else if (widget instanceof InputBoolean && Objects.equals(name, COL_PARENT)) {
      parent = (InputBoolean) widget;
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public FormInterceptor getInstance() {
    return new NewCarBundleItemForm();
  }

  private void onJobSelection(SelectorEvent event) {
    if (event.isOpened()) {
      FormView parentForm = ViewHelper.getForm(getGridView());
      if (Objects.isNull(parentForm)) {
        return;
      }
      Filter filter = Filter.isNull(COL_MODEL);

      if (DataUtils.isId(parentForm.getLongValue(COL_MODEL))) {
        filter = Filter.or(filter, Filter.equals(COL_MODEL, parentForm.getLongValue(COL_MODEL)));
      }
      event.getSelector().setAdditionalFilter(filter);

    } else if (event.isChangePending()) {
      String srcView = event.getRelatedViewName();
      IsRow srcRow = event.getRelatedRow();

      if (srcRow == null) {
        return;
      }
      IsRow target = getActiveRow();
      if (target == null) {
        return;
      }
      target.setValue(getDataIndex(COL_DURATION), BeeUtils.nvl(Data.getString(srcView, srcRow,
          COL_MODEL + COL_DURATION), Data.getString(srcView, srcRow, COL_DURATION)));

      target.setValue(getDataIndex(COL_PRICE), BeeUtils.nvl(Data.getString(srcView, srcRow,
          COL_MODEL + COL_PRICE), Data.getString(srcView, srcRow, COL_PRICE)));

      target.setValue(getDataIndex(COL_CURRENCY), BeeUtils.nvl(Data.getString(srcView, srcRow,
          COL_MODEL + COL_CURRENCY), Data.getString(srcView, srcRow, COL_CURRENCY)));
      target.setValue(getDataIndex(ALS_CURRENCY_NAME), BeeUtils.nvl(Data.getString(srcView, srcRow,
          COL_MODEL + ALS_CURRENCY_NAME), Data.getString(srcView, srcRow, ALS_CURRENCY_NAME)));

      getFormView().refresh();
    }
  }

  @Override
  public void onReadyForInsert(HasHandlers listener, ReadyForInsertEvent event) {
    UnboundSelector selector = jobVisible() ? jobSelector : itemSelector;

    if (!DataUtils.isId(selector.getRelatedId())) {
      selector.setFocus(true);
      notifyRequired("");
      event.consume();
      return;
    }
    event.getColumns().add(Data.getColumn(getViewName(), COL_ITEM));
    event.getValues().add(selector.getValue());

    if (!jobVisible()) {
      if (parent.isChecked()) {
        event.getColumns().add(Data.getColumn(getViewName(), COL_PARENT));
        event.getValues().add(BeeUtils.toString(parentId));
      }
      event.update(COL_DURATION, null);
    }
    super.onReadyForInsert(listener, event);
  }

  @Override
  public void onStartNewRow(FormView form, IsRow row) {
    itemSelector.clearValue();
    jobSelector.clearValue();
    setParent();
    tabBar.selectTab(0);
    super.onStartNewRow(form, row);
  }

  private void setParent() {
    GridView grid = getGridView();
    IsRow gridRow = grid.getGrid().getActiveRow();
    String viewName = grid.getViewName();

    if (Objects.nonNull(gridRow) && Data.isNull(viewName, gridRow, COL_PARENT)
        && !Data.isNull(viewName, gridRow, COL_JOB)) {

      parent.setChecked(true);

      if (parent.getCheckBox() instanceof CheckBox) {
        ((CheckBox) parent.getCheckBox()).setText(BeeUtils.join(": ",
            Localized.dictionary().parent(),
            Data.getString(viewName, gridRow, ClassifierConstants.ALS_ITEM_NAME)));
      }
      parentId = gridRow.getId();
    } else {
      parent.clearValue();
      parentId = null;
    }
  }

  private boolean jobVisible() {
    return Objects.equals(tabBar.getSelectedTab(), 1);
  }

  private void refresh() {
    StyleUtils.setVisible(DomUtils.getParentRow(jobSelector.getElement(), false), jobVisible());
    StyleUtils.setVisible(DomUtils.getParentRow(duration.getElement(), false), jobVisible());

    StyleUtils.setVisible(DomUtils.getParentRow(itemSelector.getElement(), false), !jobVisible());

    StyleUtils.setVisible(DomUtils.getParentRow(parent.getElement(), false),
        DataUtils.isId(parentId) && !jobVisible());
  }
}
