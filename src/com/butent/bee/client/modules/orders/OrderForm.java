package com.butent.bee.client.modules.orders;

import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.modules.mail.NewMailMessage;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.orders.OrdersConstants.OrdersStatus;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Set;

public class OrderForm extends AbstractFormInterceptor {

  private final LocalizableConstants loc = Localized.getConstants();

  @Override
  public FormInterceptor getInstance() {
    return new OrderForm();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (BeeUtils.same(name, TBL_ORDER_ITEMS)) {
      ((ChildGrid) widget).setGridInterceptor(new OrderItemsGrid());
    }
  }

  @Override
  public void afterRefresh(final FormView form, final IsRow row) {
    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    final int idxStatus = form.getDataIndex(COL_ORDERS_STATUS);

    if (BeeConst.isUndef(idxStatus)) {
      return;
    }

    int status = row.getInteger(idxStatus);

    if (BeeConst.isUndef(status)) {
      return;
    }

    GridView parentGrid = getGridView();
    if (parentGrid == null) {
      return;
    } else if (parentGrid.getGridName() == VIEW_ORDERS
        && status == OrdersStatus.PREPARED.ordinal()) {
      updateStatus(form, OrdersStatus.APPROVED);
    }

    boolean isOrder =
        (status == OrdersStatus.APPROVED.ordinal() || status == OrdersStatus.FINISH
            .ordinal()) ? true : false;
    String caption;

    if (DataUtils.isNewRow(row)) {
      caption = isOrder
          ? Localized.getConstants().newOrder() : Localized.getConstants().newOffer();

      UnboundSelector template = (UnboundSelector) form.getWidgetByName(COL_TEMPLATE);
      template.clearValue();
    } else {
      caption = isOrder
          ? Localized.getConstants().order() : Localized.getConstants().offer();
    }

    if (!BeeUtils.isEmpty(caption)) {
      header.setCaption(caption);
    }

    if (!isOrder && !DataUtils.isNewRow(row)) {

      status = row.getInteger(idxStatus);

      if (status == 1) {
        header.addCommandItem(new Button(loc.ordPrepare(), new ClickHandler() {
          @Override
          public void onClick(ClickEvent event1) {
            updateStatus(form, OrdersStatus.PREPARED);
          }
        }));
      } else if (status == 2) {
        header.addCommandItem(new Button(loc.ordCancel(), new ClickHandler() {
          @Override
          public void onClick(ClickEvent event2) {
            Global.confirm(loc.ordAskCancel(), new ConfirmationCallback() {
              @Override
              public void onConfirm() {
                updateStatus(form, OrdersStatus.CANCELED);
                save(form);
              }
            });
          }
        }));

        header.addCommandItem(new Button(loc.ordApprove(), new ClickHandler() {
          @Override
          public void onClick(ClickEvent event3) {
            Global.confirm(loc.ordAskApprove(), new ConfirmationCallback() {
              @Override
              public void onConfirm() {
                updateStatus(form, OrdersStatus.APPROVED);
                save(form);
              }
            });
          }
        }));

        header.addCommandItem(new Button(loc.send(), new ClickHandler() {
          @Override
          public void onClick(ClickEvent event4) {
            sendMail(form);
            updateStatus(form, OrdersStatus.SENT);
          }
        }));
      }

    } else if (isOrder && !DataUtils.isNewRow(row)) {
      header.addCommandItem(new Button(loc.crmActionFinish(), new ClickHandler() {
        @Override
        public void onClick(ClickEvent event5) {
          checkIsFinish(form);
        }
      }));
    }
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action.equals(Action.SAVE) && getFormView() != null && getActiveRow() != null) {
      final FormView form = getFormView();
      GridView parentGrid = getGridView();

      if (parentGrid != null) {
        form.getActiveRow().setValue(Data.getColumnIndex(VIEW_ORDERS, COL_SOURCE),
            parentGrid.getGridName());
      }
    }
    return super.beforeAction(action, presenter);
  }

  private static void updateStatus(FormView form, OrdersStatus status) {
    form.getActiveRow().setValue(Data.getColumnIndex(VIEW_ORDERS, COL_ORDERS_STATUS),
        status.ordinal());
    form.refreshBySource(COL_ORDERS_STATUS);
  }

  private static void save(final FormView form) {
    ScheduledCommand command = new ScheduledCommand() {

      @Override
      public void execute() {
        form.getViewPresenter().handleAction(Action.SAVE);
      }
    };
    command.execute();
  }

  private static void sendMail(FormView form) {
    Set<String> to = null;

    String addr = form.getStringValue(ALS_CONTACT_EMAIL);

    if (addr == null) {
      addr = form.getStringValue(ALS_COMPANY_EMAIL);
    }
    if (addr != null) {
      to = Sets.newHashSet(addr);
    }
    NewMailMessage.create(to, null, null, null, null, null, null, false);
  }

  private static void checkIsFinish(final FormView form) {

    Filter filter = Filter.equals(COL_ORDER, form.getActiveRowId());
    Queries.getRowSet(VIEW_ORDER_ITEMS, null, filter, new RowSetCallback() {

      @Override
      public void onSuccess(BeeRowSet result) {
        int invcIdx = Data.getColumnIndex(VIEW_ORDER_ITEMS, TradeConstants.COL_SALE);

        if (result != null) {
          for (IsRow row : result) {
            Long value = row.getLong(invcIdx);
            if (BeeUtils.unbox(value) <= 0) {
              form.notifySevere(Localized.getConstants().ordEmptyInvoice());
              return;
            }
          }
          updateStatus(form, OrdersStatus.FINISH);
          int dateIdx = Data.getColumnIndex(VIEW_ORDERS, COL_END_DATE);
          form.getActiveRow().setValue(dateIdx, TimeUtils.nowMinutes());
          form.refreshBySource(COL_END_DATE);
        }
      }
    });
  }
}
