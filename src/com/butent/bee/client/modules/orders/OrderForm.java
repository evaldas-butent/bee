package com.butent.bee.client.modules.orders;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.orders.OrdersConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.COL_TA_MANAGER;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.modules.mail.NewMailMessage;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.client.validation.CellValidateEvent.Handler;
import com.butent.bee.client.validation.CellValidation;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BiConsumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.NumberValue;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Objects;

public class OrderForm extends AbstractFormInterceptor {

  private final Dictionary loc = Localized.getConstants();
  private Label warehouseLabel;

  @Override
  public FormInterceptor getInstance() {
    return new OrderForm();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (BeeUtils.same(name, TBL_ORDER_ITEMS)) {
      ((ChildGrid) widget).setGridInterceptor(new OrderItemsGrid());
    } else if (BeeUtils.same(name, COL_WAREHOUSE) && widget instanceof Label) {
      warehouseLabel = (Label) widget;
    }
  }

  @Override
  public void afterRefresh(final FormView form, final IsRow row) {
    Button prepare = new Button(loc.ordPrepare(), new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        updateStatus(form, OrdersStatus.PREPARED);
        form.setEnabled(true);
        ((ListBox) form.getWidgetBySource(COL_ORDERS_STATUS)).setEditing(false);
        RowUpdateEvent.fire(BeeKeeper.getBus(), VIEW_ORDERS, (BeeRow) row);
        form.refresh();
      }
    });

    Button cancel = new Button(loc.ordCancel(), new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Global.confirm(loc.ordAskCancel(), new ConfirmationCallback() {
          @Override
          public void onConfirm() {
            updateStatus(form, OrdersStatus.CANCELED);
            save(form);
          }
        });
      }
    });

    Button approve = new Button(loc.ordApprove(), new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Global.confirm(loc.ordAskApprove(), new ConfirmationCallback() {
          @Override
          public void onConfirm() {
            String id = row.getString(Data.getColumnIndex(VIEW_ORDERS, COL_WAREHOUSE));
            if (BeeUtils.isEmpty(id)) {
              form.notifySevere(Localized.getConstants().warehouse() + " "
                  + Localized.getConstants().valueRequired());
              return;
            }

            ParameterList params = OrdersKeeper.createSvcArgs(SVC_FILL_RESERVED_REMAINDERS);
            params.addDataItem(COL_ORDER, row.getId());
            params.addDataItem(COL_WAREHOUSE, form.getLongValue(COL_WAREHOUSE));

            BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {

              @Override
              public void onResponse(ResponseObject response) {
                if (!response.hasErrors()) {
                  updateStatus(form, OrdersStatus.APPROVED);
                  save(form);
                }
              }
            });
          }
        });
      }
    });

    Button send = new Button(loc.send(), new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        sendMail(form);
      }
    });

    Button finish = new Button(loc.crmActionFinish(), new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Global.confirm(loc.ordAskFinish(), new ConfirmationCallback() {

          @Override
          public void onConfirm() {
            checkIsFinish(form);
          }
        });
      }
    });

    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    final int idxStatus = form.getDataIndex(COL_ORDERS_STATUS);

    if (BeeConst.isUndef(idxStatus)) {
      return;
    }

    Integer status = row.getInteger(idxStatus);

    if (status == null) {
      return;
    }

    GridView parentGrid = getGridView();
    if (parentGrid == null) {
      return;
    } else if (parentGrid.getGridName() == VIEW_ORDERS
        && Objects.equals(status, OrdersStatus.PREPARED.ordinal())) {
      updateStatus(form, OrdersStatus.APPROVED);
    }

    if (!Objects.equals(row.getInteger(idxStatus), OrdersStatus.APPROVED.ordinal())) {
      warehouseLabel.setStyleName(StyleUtils.NAME_REQUIRED, false);
    } else {
      warehouseLabel.setStyleName(StyleUtils.NAME_REQUIRED, true);
    }

    boolean isOrder =
        Objects.equals(row.getInteger(idxStatus), OrdersStatus.APPROVED.ordinal())
            || Objects.equals(row.getInteger(idxStatus), OrdersStatus.FINISH.ordinal());

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

    if (isManager(row)) {
      if (!isOrder && !DataUtils.isNewRow(row)) {

        status = row.getInteger(idxStatus);

        if (Objects.equals(status, OrdersStatus.CANCELED.ordinal())) {
          header.addCommandItem(prepare);
          form.setEnabled(false);
        } else if (Objects.equals(status, OrdersStatus.PREPARED.ordinal())) {
          header.addCommandItem(cancel);
          header.addCommandItem(send);
          header.addCommandItem(approve);
        } else if (Objects.equals(status, OrdersStatus.SENT.ordinal())) {
          header.addCommandItem(cancel);
          header.addCommandItem(approve);
        }
      } else if (Objects.equals(status, OrdersStatus.APPROVED.ordinal())
          && !DataUtils.isNewRow(row)) {
        header.addCommandItem(send);
        header.addCommandItem(finish);
      }
    }

    if (Objects.equals(form.getIntegerValue(COL_ORDERS_STATUS), OrdersStatus.FINISH.ordinal())) {
      form.setEnabled(false);
    }

    if (BeeUtils.isEmpty(row
        .getString(Data.getColumnIndex(VIEW_ORDERS, COL_SOURCE)))) {
      row.setValue(Data.getColumnIndex(VIEW_ORDERS, COL_SOURCE),
          parentGrid.getGridName());
      form.getOldRow().setValue(Data.getColumnIndex(VIEW_ORDERS, COL_SOURCE),
          parentGrid.getGridName());
    }
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {

    if (action.equals(Action.SAVE)) {
      int statusIdx = Data.getColumnIndex(VIEW_ORDERS, COL_ORDERS_STATUS);
      Long warehouse = getActiveRow().getLong(Data.getColumnIndex(VIEW_ORDERS, COL_WAREHOUSE));
      Long company = getActiveRow().getLong(Data.getColumnIndex(VIEW_ORDERS, COL_COMPANY));
      Integer status = getActiveRow().getInteger(statusIdx);

      if (Objects.equals(status, OrdersStatus.APPROVED.ordinal())) {
        if (!BeeUtils.isPositive(warehouse)) {
          getFormView().notifySevere(Localized.getConstants().warehouse() + " "
              + Localized.getConstants().valueRequired());
          return false;
        }
      }

      if (!BeeUtils.isPositive(company)) {
        getFormView().notifySevere(Localized.getConstants().client() + " "
            + Localized.getConstants().valueRequired());
        return false;
      }
    }
    return true;
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {

    if (!isManager(row)) {
      form.setEnabled(false);
    }
  }

  @Override
  public void onLoad(final FormView form) {
    form.addCellValidationHandler(COL_WAREHOUSE, new Handler() {

      @Override
      public Boolean validateCell(CellValidateEvent event) {
        CellValidation cv = event.getCellValidation();
        final String newValue = cv.getNewValue();
        String oldValue = cv.getOldValue();

        if (newValue != oldValue
            && oldValue != null
            && DataUtils.hasId(getActiveRow())
            && Objects.equals(getActiveRow().getInteger(Data.getColumnIndex(VIEW_ORDERS,
            COL_ORDERS_STATUS)), OrdersStatus.APPROVED.ordinal())) {

          Global.confirm(Localized.getConstants().ordAskChangeWarehouse() + " "
              + Localized.getConstants().saveChanges(), new ConfirmationCallback() {

            @Override
            public void onConfirm() {
              if (DataUtils.isId(newValue)) {

                Filter filter =
                    Filter.equals(COL_ORDER, getActiveRowId());
                Queries.update(VIEW_ORDER_ITEMS, filter, COL_RESERVED_REMAINDER,
                    new NumberValue(BeeConst.DOUBLE_ZERO), new IntCallback() {

                      @Override
                      public void onSuccess(Integer result) {

                        if (BeeUtils.isPositive(result)) {
                          int idxColId = form.getDataIndex(COL_WAREHOUSE);
                          List<BeeColumn> cols =
                              Data.getColumns(form.getViewName(),
                                  Lists.newArrayList(COL_WAREHOUSE));
                          List<String> newValues = Lists.newArrayList(newValue);
                          List<String> oldValues =
                              Lists.newArrayList(form.getOldRow().getString(idxColId));

                          Queries.update(VIEW_ORDERS, getActiveRowId(), form.getOldRow()
                              .getVersion(), cols, oldValues, newValues, null, new RowCallback() {

                            @Override
                            public void onSuccess(BeeRow row) {
                              RowUpdateEvent.fire(BeeKeeper.getBus(), form.getViewName(), row);
                              form.refresh();
                            }
                          });
                        }
                      }
                    });
              }
            }
          });
          return false;
        }
        return true;
      }
    });
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

  private static void sendMail(final FormView form) {
    NewMailMessage.create(BeeUtils.notEmpty(form.getStringValue(ALS_CONTACT_EMAIL),
        form.getStringValue(ALS_COMPANY_EMAIL)), null, null, null, new BiConsumer<Long, Boolean>() {
      @Override
      public void accept(Long messageId, Boolean saveMode) {
        if (!saveMode && !Objects.equals(form.getIntegerValue(COL_ORDERS_STATUS),
            OrdersStatus.APPROVED.ordinal())) {
          updateStatus(form, OrdersStatus.SENT);
        }
      }
    });
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
          save(form);
        }
      }
    });
  }

  public static boolean isManager(IsRow row) {
    if (row == null) {
      return false;
    }

    int managerIdx = Data.getColumnIndex(VIEW_ORDERS, COL_TA_MANAGER);
    Long managerId = row.getLong(managerIdx);

    return Objects.equals(managerId, BeeKeeper.getUser().getUserId());
  }
}
