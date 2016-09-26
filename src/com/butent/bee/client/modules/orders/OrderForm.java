package com.butent.bee.client.modules.orders;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.orders.OrdersConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.COL_CUSTOMER;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.ParentRowCreator;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.TabbedPages;
import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.modules.mail.NewMailMessage;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.validation.CellValidation;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BiConsumer;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.NumberValue;
import com.butent.bee.shared.data.view.RowInfoList;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.orders.Option;
import com.butent.bee.shared.modules.orders.Specification;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OrderForm extends PrintFormInterceptor implements Consumer<Specification> {

  private final Dictionary loc = Localized.dictionary();
  private Label warehouseLabel;

  private HasWidgets objectContainer;
  private Specification objectSpecification;

  @Override
  public void accept(Specification specification) {
    Map<Long, Integer> opts = new HashMap<>();

    for (Option option : specification.getOptions()) {
      opts.put(option.getId(), specification.getOptionPrice(option));
    }
    Queries.getRowSet(TBL_CONF_OPTIONS, Collections.singletonList(COL_ITEM),
        Filter.and(opts.isEmpty() ? Filter.isFalse() : Filter.idIn(opts.keySet()),
            Filter.notNull(COL_ITEM)), new RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet res) {
            Holder<Integer> price = Holder.of(specification.getPrice());
            List<BeeColumn> columns = Data.getColumns(VIEW_ORDER_ITEMS, Arrays.asList(COL_ORDER,
                COL_ITEM, TradeConstants.COL_TRADE_ITEM_QUANTITY, COL_ITEM_PRICE, COL_OBJECT));
            BeeRowSet items = new BeeRowSet(VIEW_ORDER_ITEMS, columns);

            for (BeeRow item : res) {
              BeeRow row = items.addEmptyRow();
              row.setValue(DataUtils.getColumnIndex(COL_ORDER, columns), getActiveRowId());
              row.setValue(DataUtils.getColumnIndex(COL_ITEM, columns), item.getLong(0));
              row.setValue(DataUtils.getColumnIndex(TradeConstants.COL_TRADE_ITEM_QUANTITY,
                  columns), 1);
              row.setValue(DataUtils.getColumnIndex(COL_ITEM_PRICE, columns),
                  opts.get(item.getId()));
              row.setValue(DataUtils.getColumnIndex(COL_OBJECT, columns), specification.getId());
              price.set(price.get() - opts.get(item.getId()));
            }
            Consumer<Integer> consumer = itemCount -> {
              BeeRowSet rs = DataUtils.getUpdated(getViewName(), getFormView().getDataColumns(),
                  getFormView().getOldRow(), getActiveRow(), getFormView().getChildrenForUpdate());

              if (DataUtils.isEmpty(rs)) {
                rs = new BeeRowSet(getViewName(), new ArrayList<>());
                rs.addRow(getActiveRowId(), getActiveRow().getVersion(), new ArrayList<>());
              } else {
                for (String col : new String[] {COL_DESCRIPTION, TransportConstants.COL_AMOUNT}) {
                  int idx = DataUtils.getColumnIndex(col, rs.getColumns());

                  if (!BeeConst.isUndef(idx)) {
                    rs.removeColumn(idx);
                  }
                }
              }
              Long oldObject = getLongValue(COL_OBJECT);
              int c = rs.getNumberOfColumns();
              rs.addColumns(Data.getColumns(getViewName(),
                  Arrays.asList(COL_OBJECT, TransportConstants.COL_AMOUNT)));

              rs.getRow(0).setValue(c, oldObject);
              rs.getRow(0).preliminaryUpdate(c, BeeUtils.toString(specification.getId()));
              rs.getRow(0).setValue(c + 1, getIntegerValue(TransportConstants.COL_AMOUNT));
              rs.getRow(0).preliminaryUpdate(c + 1, BeeUtils.toString(price.get()));

              Queries.updateRow(rs, new RowUpdateCallback(getViewName()) {
                @Override
                public void onSuccess(BeeRow result) {
                  if (DataUtils.isId(oldObject)) {
                    Queries.deleteRow(TBL_CONF_OBJECTS, oldObject, new IntCallback() {
                      @Override
                      public void onSuccess(Integer cnt) {
                        Data.onViewChange(VIEW_ORDER_ITEMS, DataChangeEvent.RESET_REFRESH);
                      }
                    });
                  } else if (BeeUtils.isPositive(itemCount)) {
                    Data.onViewChange(VIEW_ORDER_ITEMS, DataChangeEvent.RESET_REFRESH);
                  }
                  super.onSuccess(result);
                }
              });
            };
            if (!DataUtils.isEmpty(items)) {
              Queries.insertRows(items, new RpcCallback<RowInfoList>() {
                @Override
                public void onSuccess(RowInfoList result) {
                  consumer.accept(result.size());
                }
              });
            } else {
              consumer.accept(0);
            }
          }
        });
  }

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

    } else if (BeeUtils.same(name, NameUtils.getClassName(SpecificationBuilder.class))
        && widget instanceof HasClickHandlers) {
      ((HasClickHandlers) widget).addClickHandler(clickEvent -> createObject());

    } else if (BeeUtils.same(name, COL_OBJECT) && widget instanceof HasWidgets) {
      objectContainer = (HasWidgets) widget;
    }
  }

  @Override
  public void afterRefresh(final FormView form, final IsRow row) {
    refreshObject();

    Button prepare = new Button(loc.ordPrepare(), event -> {
      updateStatus(form, OrdersStatus.PREPARED);
      form.setEnabled(true);
      ((ListBox) form.getWidgetBySource(COL_ORDERS_STATUS)).setEditing(false);
      update();
    });

    Button cancel = new Button(loc.ordCancel(), event -> {
      Global.confirm(loc.ordAskCancel(), () -> {
        updateStatus(form, OrdersStatus.CANCELED);
        save(form);
      });
    });

    Button approve = new Button(loc.ordApprove(), event -> {
      Global.confirm(loc.ordAskApprove(), () -> {
        String id = row.getString(Data.getColumnIndex(VIEW_ORDERS, COL_WAREHOUSE));
        if (BeeUtils.isEmpty(id)) {
          form.notifySevere(Localized.dictionary().warehouse() + " "
              + Localized.dictionary().valueRequired());
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
      });
    });

    Button send = new Button(loc.send(), event -> {
      sendMail(form);
    });

    Button finish = new Button(loc.crmActionFinish(), event -> {
      Global.confirm(loc.ordAskFinish(), () -> checkIsFinish(form));
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
    if (DataUtils.isNewRow(row)) {
      if (parentGrid == null) {
        return;
      } else if (parentGrid.getGridName() == VIEW_ORDERS
          && Objects.equals(status, OrdersStatus.PREPARED.ordinal())) {
        updateStatus(form, OrdersStatus.APPROVED);
      }
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
          ? Localized.dictionary().newOrder() : Localized.dictionary().newOffer();

      UnboundSelector template = (UnboundSelector) form.getWidgetByName(COL_TEMPLATE);
      template.clearValue();
    } else {
      caption = isOrder
          ? Localized.dictionary().order() : Localized.dictionary().offer();
    }

    if (!BeeUtils.isEmpty(caption)) {
      header.setCaption(caption);
    }

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

    if (Objects.equals(form.getIntegerValue(COL_ORDERS_STATUS), OrdersStatus.FINISH.ordinal())) {
      form.setEnabled(false);
    }

    Widget child = form.getWidgetByName(VIEW_ORDER_CHILD_INVOICES);

    if (child != null) {
      Widget tabs = form.getWidgetByName(NameUtils.getClassName(TabbedPages.class));

      if (tabs != null && tabs instanceof TabbedPages) {
        int idx = ((TabbedPages) tabs).getContentIndex(child);

        if (!BeeConst.isUndef(idx)) {
          if (Objects.equals(OrdersStatus.APPROVED.ordinal(), row.getInteger(idxStatus))
              || Objects.equals(OrdersStatus.FINISH.ordinal(), row.getInteger(idxStatus))) {
            ((TabbedPages) tabs).enablePage(idx);
          } else {
            ((TabbedPages) tabs).disablePage(idx);
          }
        }
      }
    }
  }

  @Override
  protected void getReportData(Consumer<BeeRowSet[]> dataConsumer) {
    Queries.getRowSet(VIEW_ORDER_ITEMS, null, Filter.equals(COL_ORDER, getActiveRowId()),
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            dataConsumer.accept(new BeeRowSet[] {result});
          }
        });
  }

  @Override
  protected void getReportParameters(Consumer<Map<String, String>> parametersConsumer) {
    Map<String, Long> companies = new HashMap<>();
    companies.put(COL_CUSTOMER, getLongValue(COL_COMPANY));
    companies.put(TradeConstants.COL_TRADE_SUPPLIER, BeeKeeper.getUser().getCompany());

    super.getReportParameters(defaultParameters ->
        ClassifierUtils.getCompaniesInfo(companies, companiesInfo -> {
          if (objectSpecification != null) {
            defaultParameters.put(TBL_CONF_OBJECT_OPTIONS,
                objectSpecification.renderSummary(false).toString());

            for (int i = 0; i < objectSpecification.getBranchOptions().size(); i++) {
              defaultParameters.put(COL_PHOTO + i,
                  BeeUtils.toString(objectSpecification.getBranchOptions().get(i).getPhoto()));
            }
          }
          defaultParameters.putAll(companiesInfo);
          parametersConsumer.accept(defaultParameters);
        }));
  }

  @Override
  public void onSetActiveRow(IsRow row) {
    Long objectId = row.getLong(getDataIndex(COL_OBJECT));

    if (DataUtils.isId(objectId)) {
      ParameterList args = OrdersKeeper.createSvcArgs(SVC_GET_OBJECT);
      args.addDataItem(COL_OBJECT, objectId);

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          response.notify(getFormView());

          if (!response.hasErrors()) {
            objectSpecification = Specification.restore(response.getResponseAsString());
            refreshObject();
          }
        }
      });
    } else {
      objectSpecification = null;
    }
    super.onSetActiveRow(row);
  }

  @Override
  public void onLoad(FormView form) {
    form.addCellValidationHandler(COL_WAREHOUSE, event -> {
      CellValidation cv = event.getCellValidation();
      final String newValue = cv.getNewValue();
      String oldValue = cv.getOldValue();

      if (newValue != oldValue && oldValue != null && DataUtils.hasId(getActiveRow())
          && Objects.equals(getActiveRow().getInteger(Data.getColumnIndex(VIEW_ORDERS,
          COL_ORDERS_STATUS)), OrdersStatus.APPROVED.ordinal())) {

        Global.confirm(Localized.dictionary().ordAskChangeWarehouse() + " "
            + Localized.dictionary().saveChanges(), () -> {
          if (DataUtils.isId(newValue)) {

            Filter filter = Filter.equals(COL_ORDER, getActiveRowId());
            Queries.update(VIEW_ORDER_ITEMS, filter, COL_RESERVED_REMAINDER,
                new NumberValue(BeeConst.DOUBLE_ZERO), new IntCallback() {

                  @Override
                  public void onSuccess(Integer result) {
                    getActiveRow().setValue(Data.getColumnIndex(VIEW_ORDERS, COL_WAREHOUSE),
                        newValue);
                    update();
                  }
                });
          }
        });
        return false;
      } else if (!Objects.equals(getActiveRow().getInteger(Data.getColumnIndex(VIEW_ORDERS,
          COL_ORDERS_STATUS)), OrdersStatus.APPROVED.ordinal()) && newValue != oldValue
          && DataUtils.hasId(getActiveRow())) {

        getActiveRow().setValue(Data.getColumnIndex(VIEW_ORDERS, COL_WAREHOUSE), newValue);
        update();
        return false;
      }
      return true;
    });
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {

    Global.getRelationParameter(PRM_MANAGER_WAREHOUSE, new BiConsumer<Long, String>() {
      @Override
      public void accept(Long aLong, String s) {
        GridView parentGrid = getGridView();
        if (parentGrid == null) {
          return;
        }

        if (BeeUtils.isEmpty(newRow.getString(Data.getColumnIndex(VIEW_ORDERS, COL_SOURCE)))) {
          String gridName = GRID_COMPANY_ORDERS.equals(parentGrid.getGridName()) ? GRID_OFFERS
              : parentGrid.getGridName();

          newRow.setValue(Data.getColumnIndex(VIEW_ORDERS, COL_SOURCE), gridName);
        }

        if (GRID_OFFERS.equals(parentGrid.getGridName())) {
          int statusIdx = Data.getColumnIndex(VIEW_ORDERS, COL_ORDERS_STATUS);
          int endDateIdx = Data.getColumnIndex(VIEW_ORDERS, COL_END_DATE);

          if (Objects.equals(OrdersStatus.PREPARED.ordinal(), newRow.getInteger(statusIdx))) {
            DateTime now = TimeUtils.nowMillis();
            int year = now.getYear();
            int month = now.getMonth() + 3;

            if (month > 12) {
              year++;
              month = month - 12;
            }
            newRow.setValue(endDateIdx, new DateTime(year, month, now.getDom()));
          }
        }

        newRow.setValue(Data.getColumnIndex(VIEW_ORDERS, COL_WAREHOUSE), aLong);
        newRow.setValue(Data.getColumnIndex(VIEW_ORDERS, ALS_WAREHOUSE_CODE), s);

        getFormView().refreshBySource(COL_WAREHOUSE);

        OrderForm.super.onStartNewRow(form, oldRow, newRow);
      }
    });
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
          getFormView().notifySevere(Localized.dictionary().warehouse() + " "
              + Localized.dictionary().valueRequired());
          return false;
        }
      }

      if (!BeeUtils.isPositive(company)) {
        getFormView().notifySevere(Localized.dictionary().client() + " "
            + Localized.dictionary().valueRequired());
        return false;
      }
    }
    return super.beforeAction(action, presenter);
  }

  private void createObject() {
    if (DataUtils.isId(getActiveRowId())) {
      new SpecificationBuilder(objectSpecification, this);
    } else {
      FormView form = getFormView();

      if (form.getViewPresenter() instanceof ParentRowCreator) {
        ((ParentRowCreator) form.getViewPresenter())
            .createParentRow(form, result -> new SpecificationBuilder(null, OrderForm.this));
      }
    }
  }

  private void refreshObject() {
    if (objectContainer != null) {
      objectContainer.clear();

      if (objectSpecification != null) {
        for (Option option : objectSpecification.getBranchOptions()) {
          if (DataUtils.isId(option.getPhoto())) {
            Flow thumbnail = new Flow(SpecificationBuilder.STYLE_THUMBNAIL);
            thumbnail.addStyleName(StyleUtils.NAME_FLEXIBLE);
            thumbnail.add(new Image(FileUtils.getUrl(option.getPhoto())));
            objectContainer.add(thumbnail);
          }
        }
        objectContainer.add(objectSpecification.renderSummary(false));
      }
    }
  }

  private static void updateStatus(FormView form, OrdersStatus status) {
    form.getActiveRow().setValue(Data.getColumnIndex(VIEW_ORDERS, COL_ORDERS_STATUS),
        status.ordinal());
    form.refreshBySource(COL_ORDERS_STATUS);
  }

  private void update() {
    FormView form = getFormView();

    BeeRowSet rowSet =
        DataUtils.getUpdated(form.getViewName(), form.getDataColumns(), form.getOldRow(),
            getActiveRow(), form.getChildrenForUpdate());

    Queries.updateRow(rowSet, new RowCallback() {

      @Override
      public void onSuccess(BeeRow result) {
        RowUpdateEvent.fire(BeeKeeper.getBus(), form.getViewName(), result);
        form.refresh();
      }
    });
  }

  private static void save(final FormView form) {
    ScheduledCommand command = () -> form.getViewPresenter().handleAction(Action.SAVE);
    command.execute();
  }

  private static void sendMail(final FormView form) {
    NewMailMessage.create(BeeUtils.notEmpty(form.getStringValue(ALS_CONTACT_EMAIL),
        form.getStringValue(ALS_COMPANY_EMAIL)), null, null, null, (messageId, saveMode) -> {
      if (!saveMode && !Objects.equals(form.getIntegerValue(COL_ORDERS_STATUS),
          OrdersStatus.APPROVED.ordinal())) {
        updateStatus(form, OrdersStatus.SENT);
      }
    });
  }

  private static void checkIsFinish(final FormView form) {

    Filter filter = Filter.equals(COL_ORDER, form.getActiveRowId());
    Queries.getRowSet(VIEW_ORDER_ITEMS, null, filter, new RowSetCallback() {

      @Override
      public void onSuccess(BeeRowSet result) {
        int qtyIdx = Data.getColumnIndex(VIEW_ORDER_ITEMS, TradeConstants.COL_TRADE_ITEM_QUANTITY);

        if (result != null) {
          for (IsRow row : result) {
            Double completed = row.getPropertyDouble(PRP_COMPLETED_INVOICES);
            Double qty = row.getDouble(qtyIdx);

            if (BeeUtils.unbox(completed) <= 0 || !Objects.equals(completed, qty)) {
              form.notifySevere(Localized.dictionary().ordEmptyInvoice());
              return;
            }
          }

          Queries.update(VIEW_ORDER_ITEMS, Filter.equals(COL_ORDER, form.getActiveRowId()),
              COL_RESERVED_REMAINDER, new IntegerValue(0), new IntCallback() {

                @Override
                public void onSuccess(Integer count) {
                  updateStatus(form, OrdersStatus.FINISH);
                  int dateIdx = Data.getColumnIndex(VIEW_ORDERS, COL_END_DATE);
                  form.getActiveRow().setValue(dateIdx, TimeUtils.nowMinutes());

                  save(form);
                }
              });
        }
      }
    });
  }
}
