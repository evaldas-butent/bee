package com.butent.bee.client.modules.classifiers;

import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.COL_TRADE_ITEM_QUANTITY;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.modules.orders.OrdersKeeper;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.values.Display;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;
import java.util.List;

public class ItemComponentForm extends AbstractFormInterceptor {

  private Long itemId;

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {
    if (BeeUtils.same(editableWidget.getColumnId(), COL_ITEM_COMPONENT)) {
      ((DataSelector) widget).addSelectorHandler(event -> {
        if (event.isOpened()) {
          Long parentId = ViewHelper.getParentRowId(getFormView().asWidget(), VIEW_ITEMS);
          if (DataUtils.isId(parentId)) {
            Filter filter = Filter.isNot(Filter.compareId(parentId));
            event.getSelector().setAdditionalFilter(filter);
          }
        }

        if (event.isChanged()) {
          BeeRow row = event.getRelatedRow();

          if (row != null) {
            setItemId(row.getId());
            Integer count = row.getPropertyInteger(PROP_ITEM_COMPONENT);

            styleWidgets(BeeUtils.isPositive(count));
          }
        }
      });
    }
    super.afterCreateEditableWidget(editableWidget, widget);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {
    if (BeeUtils.same(name, "Plus")) {

      ((FaLabel) widget).addClickHandler(clickEvent -> {
        if (DataUtils.isId(getItemId())) {
          Queries.getRowSet(VIEW_ITEM_COMPONENTS, Arrays.asList(COL_ITEM_NAME, COL_ITEM_ARTICLE,
              COL_TRADE_ITEM_QUANTITY), Filter.equals(COL_ITEM, getItemId()),
              new Queries.RowSetCallback() {
                @Override
                public void onSuccess(BeeRowSet result) {
                  Global.showModalGrid(Localized.dictionary().components(), result, Action.EXPORT,
                      event -> OrdersKeeper.export(result, Localized.dictionary().components()),
                      StyleUtils.NAME_INFO_TABLE);
                }
              });
        }
      });
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    setItemId(null);
    styleWidgets(false);

    super.beforeRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new ItemComponentForm();
  }

  @Override
  public void onReadyForInsert(HasHandlers listener, ReadyForInsertEvent event) {
    event.consume();
    ParameterList params = ClassifierKeeper.createArgs(SVC_ADD_COMPONENTS);
    List<String> values = event.getValues();

    params.addDataItem(COL_ITEM, values.get(0));
    params.addDataItem(COL_ITEM_COMPONENT, values.get(1));
    params.addDataItem(TradeConstants.COL_TRADE_ITEM_QUANTITY, values.get(2));

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (!response.hasErrors()) {
          event.getCallback().onSuccess(null);
          DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_ITEM_COMPONENTS);

          IsRow row = ViewHelper.getParentRow(getFormView().asWidget(), VIEW_ITEMS);
          if (row != null) {
            Queries.getRow(VIEW_ITEMS, row.getId(), new RowCallback() {
              @Override
              public void onSuccess(BeeRow result) {
                RowUpdateEvent.fire(BeeKeeper.getBus(), VIEW_ITEMS, result);
              }
            });
          }
        }
      }
    });
  }

  private Long getItemId() {
    return itemId;
  }

  private void setItemId(Long itemId) {
    this.itemId = itemId;
  }

  private void styleWidgets(boolean change) {
    Widget plus = getFormView().getWidgetByName("Plus");
    Widget label = getFormView().getWidgetByName("ComponentLabel");

    if (plus != null) {
      if (change) {
        StyleUtils.setDisplay(plus, Display.INLINE);
      } else {
        StyleUtils.setDisplay(plus, Display.NONE);
      }
    }

    if (label != null) {
      if (change) {
        label.getElement().setInnerText(Localized.dictionary().complect());
      } else {
        label.getElement().setInnerText(Localized.dictionary().item());
      }
    }
  }
}