package com.butent.bee.client.modules.ec;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.SimpleCheckBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants.EcOrderStatus;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

class EcOrderForm extends AbstractFormInterceptor {

  private static final String STYLE_UNSUPPLIED = EcStyles.name("Order-UnsuppliedItems-");
  
  private static final String STYLE_UNSUPPLIED_SELECTION = STYLE_UNSUPPLIED + "selection";
  private static final String STYLE_UNSUPPLIED_DATE = STYLE_UNSUPPLIED + "date";
  private static final String STYLE_UNSUPPLIED_NAME = STYLE_UNSUPPLIED + "name";
  private static final String STYLE_UNSUPPLIED_BRAND = STYLE_UNSUPPLIED + "brand";
  private static final String STYLE_UNSUPPLIED_CODE = STYLE_UNSUPPLIED + "code";
  private static final String STYLE_UNSUPPLIED_QUANTITY = STYLE_UNSUPPLIED + "quantity";
  private static final String STYLE_UNSUPPLIED_PRICE = STYLE_UNSUPPLIED + "price";

  private static final String STYLE_UNSUPPLIED_ROW = STYLE_UNSUPPLIED + "row";
  private static final String STYLE_UNSUPPLIED_SELECTED = STYLE_UNSUPPLIED + "selected";

  private static final String STYLE_SUFFIX_LABEL = "-label";
  private static final String STYLE_SUFFIX_INPUT = "-input";
  
  private static void addToUnsuppliedItems(long orderId) {
    ParameterList params = EcKeeper.createArgs(SVC_ADD_TO_UNSUPPLIED_ITEMS);
    params.addQueryItem(COL_ORDER_ITEM_ORDER, orderId);

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        DataChangeEvent.fire(TBL_UNSUPPLIED_ITEMS, false);
      }
    });
  }

  private Button unsupplied;
  private Button send;
  private Button reject;

  EcOrderForm() {
    super();
  }

  @Override
  public FormInterceptor getInstance() {
    return new EcOrderForm();
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, ScheduledCommand focusCommand) {
    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    EcOrderStatus status = NameUtils.getEnumByIndex(EcOrderStatus.class,
        Data.getInteger(form.getViewName(), row, COL_ORDER_STATUS));

    if (status == EcOrderStatus.NEW) {
      if (this.unsupplied == null) {
        this.unsupplied = new Button(Localized.getConstants().ecUnsuppliedItems(),
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                getUnsuppliedItems();
              }
            });
      }
      header.addCommandItem(this.unsupplied);

      if (this.send == null) {
        this.send = new Button(Localized.getConstants().ecSendToERP(), new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            sendToErp();
          }
        });
      }
      header.addCommandItem(this.send);

      if (this.reject == null) {
        this.reject = new Button(Localized.getConstants().ecOrderRejectCommand(),
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                rejectOrder();
              }
            });
      }
      header.addCommandItem(this.reject);
    }

    form.setEnabled(status == EcOrderStatus.NEW);

    return true;
  }

  private void appendUnsuppliedItems(BeeRowSet rowSet, final Set<Long> selectedIds) {
    long orderId = getFormView().getActiveRow().getId();
    
    int articleIndex = rowSet.getColumnIndex(COL_UNSUPPLIED_ITEM_ARTICLE); 
    int qtyIndex = rowSet.getColumnIndex(COL_UNSUPPLIED_ITEM_QUANTITY); 
    int priceIndex = rowSet.getColumnIndex(COL_UNSUPPLIED_ITEM_PRICE); 
    int noteIndex = rowSet.getColumnIndex(COL_UNSUPPLIED_ITEM_NOTE);
    
    List<String> colNames = Lists.newArrayList(COL_ORDER_ITEM_ORDER, COL_ORDER_ITEM_ARTICLE,
        COL_ORDER_ITEM_QUANTITY_ORDERED, COL_ORDER_ITEM_QUANTITY_SUBMIT,
        COL_ORDER_ITEM_PRICE, COL_ORDER_ITEM_NOTE);
    List<BeeColumn> columns = Data.getColumns(VIEW_ORDER_ITEMS, colNames);
    
    final Holder<Integer> latch = Holder.of(0);
    RowInsertCallback insertCallback = new RowInsertCallback(VIEW_ORDER_ITEMS) {
      @Override
      public void onSuccess(BeeRow result) {
        super.onSuccess(result);
        
        latch.set(latch.get() + 1);
        if (BeeUtils.unbox(latch.get()) == selectedIds.size()) {
          DataChangeEvent.fire(VIEW_ORDER_ITEMS, false);
        }
      }
    };
    
    final Set<RowInfo> delete = Sets.newHashSet();
    
    for (Long id : selectedIds) {
      BeeRow row = rowSet.getRowById(id);
      
      List<String> values = Queries.asList(orderId, row.getString(articleIndex),
          0, row.getString(qtyIndex), row.getString(priceIndex), row.getString(noteIndex));
      Queries.insert(VIEW_ORDER_ITEMS, columns, values, null, insertCallback);
      
      delete.add(new RowInfo(row, true));
    }
    
    if (!delete.isEmpty()) {
      Queries.deleteRows(VIEW_UNSUPPLIED_ITEMS, delete, new Queries.IntCallback() {
        @Override
        public void onSuccess(Integer result) {
          DataChangeEvent.fire(VIEW_UNSUPPLIED_ITEMS, true);
        }
      });
    }
  }
  
  private void getUnsuppliedItems() {
    int index = getFormView().getDataIndex(COL_ORDER_CLIENT);
    Long client = getFormView().getActiveRow().getLong(index);
    Assert.notNull(client);
    
    Filter filter = ComparisonFilter.isEqual(COL_UNSUPPLIED_ITEM_CLIENT, new LongValue(client));
    Queries.getRowSet(VIEW_UNSUPPLIED_ITEMS, null, filter, new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        if (DataUtils.isEmpty(result)) {
          Global.showInfo(Localized.getConstants().ecUnsuppliedItemsNotFound());
        } else {
          showUnsuppliedItems(result);
        }
      }
    });
  }

  private void rejectOrder() {
    String stylePrefix = EcStyles.name("OrderRejection-");

    HtmlTable table = new HtmlTable();
    table.addStyleName(stylePrefix + "table");

    int row = 0;

    String styleName = stylePrefix + "reason";
    Label label = new Label(Localized.getConstants().ecRejectionReason());
    label.addStyleName(StyleUtils.NAME_REQUIRED);
    table.setWidgetAndStyle(row, 0, label, styleName + STYLE_SUFFIX_LABEL);

    final UnboundSelector selector = UnboundSelector.create(VIEW_REJECTION_REASONS,
        Lists.newArrayList(COL_REJECTION_REASON_NAME));
    table.setWidgetAndStyle(row, 1, selector, styleName + STYLE_SUFFIX_INPUT);
    row++;

    styleName = stylePrefix + "comment";
    label = new Label(Localized.getConstants().comment());
    table.setWidgetAndStyle(row, 0, label, styleName + STYLE_SUFFIX_LABEL);

    final InputArea inputArea = new InputArea();

    Widget widget = getFormView().getWidgetBySource(COL_ORDER_MANAGER_COMMENT);
    if (widget instanceof InputArea) {
      String value = ((InputArea) widget).getValue();
      if (!BeeUtils.isEmpty(value)) {
        inputArea.setValue(value.trim());
      }
    }

    table.setWidgetAndStyle(row, 1, inputArea, styleName + STYLE_SUFFIX_INPUT);
    row++;

    int col = 0;
    Button confirm = new Button(Localized.getConstants().ecOrderRejectConfirm());
    table.setWidgetAndStyle(row, col, confirm, stylePrefix + "confirm");

    table.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_CENTER);
    table.getCellFormatter().setColSpan(row, col, 2);

    final DialogBox dialog = DialogBox.create(Localized.getConstants().ecOrderRejectCaption(),
        stylePrefix + "dialog");
    dialog.setWidget(table);

    dialog.setAnimationEnabled(true);
    dialog.setHideOnEscape(true);

    dialog.center();
    selector.setFocus(true);

    confirm.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Long rrId = selector.getRelatedId();
        if (!DataUtils.isId(rrId)) {
          getFormView().notifySevere(Localized.getConstants().ecRejectionReasonRequired());
          selector.setFocus(true);
          return;
        }

        BeeRow orderRow = DataUtils.cloneRow(getFormView().getActiveRow());
        orderRow.setValue(getFormView().getDataIndex(COL_ORDER_STATUS),
            EcOrderStatus.REJECTED.ordinal());

        orderRow.setValue(getFormView().getDataIndex(COL_ORDER_REJECTION_REASON), rrId);

        int commentIndex = getFormView().getDataIndex(COL_ORDER_MANAGER_COMMENT);
        if (!BeeUtils.equalsTrim(orderRow.getString(commentIndex), inputArea.getValue())) {
          orderRow.setValue(commentIndex, inputArea.getValue());
        }

        Queries.update(getFormView().getViewName(), getFormView().getDataColumns(),
            getFormView().getOldRow(), orderRow, getFormView().getChildrenForUpdate(),
            new RowCallback() {
              @Override
              public void onFailure(String... reason) {
                getFormView().notifySevere(reason);
              }

              @Override
              public void onSuccess(BeeRow result) {
                getFormView().updateRow(result, false);
                getFormView().setEnabled(false);

                getFormView().getViewPresenter().getHeader().clearCommandPanel();

                dialog.close();

                BeeKeeper.getBus().fireEvent(new RowUpdateEvent(getFormView().getViewName(),
                    result));
                addToUnsuppliedItems(result.getId());
              }
            });
      }
    });
  }

  private void restoreCommandPanel(HeaderView header) {
    if (unsupplied != null) {
      header.addCommandItem(unsupplied);
    }
    if (send != null) {
      header.addCommandItem(send);
    }
    if (reject != null) {
      header.addCommandItem(reject);
    }
  }
  
  private void sendToErp() {
    Global.confirm(Localized.getConstants().ecSendToERPConfirm(), new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        final FormView form = getFormView();
        final HeaderView header = form.getViewPresenter().getHeader();
        final long rowId = form.getActiveRow().getId();

        header.clearCommandPanel();
        header.addCommandItem(new Image(Global.getImages().loading()));

        ParameterList args = EcKeeper.createArgs(SVC_SEND_TO_ERP);
        args.addDataItem(COL_ORDER_ITEM_ORDER, rowId);

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            header.clearCommandPanel();
            response.notify(getFormView());

            if (response.hasErrors()) {
              restoreCommandPanel(header);

            } else {
              DataChangeEvent.fire(VIEW_UNSUPPLIED_ITEMS, false);
              
              Queries.getRow(form.getViewName(), rowId, new RowCallback() {
                @Override
                public void onSuccess(BeeRow result) {
                  if (rowId == form.getActiveRow().getId()) {
                    form.updateRow(result, false);
                    form.setEnabled(false);
                  }
                  BeeKeeper.getBus().fireEvent(new RowUpdateEvent(form.getViewName(), result));
                }
              });
            }
          }
        });
      }
    });
  }
  
  private void showUnsuppliedItems(final BeeRowSet rowSet) {
    int dateIndex = rowSet.getColumnIndex(COL_UNSUPPLIED_ITEM_DATE);

    int nameIndex = rowSet.getColumnIndex(COL_TCD_ARTICLE_NAME);
    int brandIndex = rowSet.getColumnIndex(COL_TCD_BRAND_NAME);
    int codeIndex = rowSet.getColumnIndex(COL_TCD_ARTICLE_NR);

    int qtyIndex = rowSet.getColumnIndex(COL_UNSUPPLIED_ITEM_QUANTITY);
    int priceIndex = rowSet.getColumnIndex(COL_UNSUPPLIED_ITEM_PRICE);
    
    final Set<Long> selectedIds = Sets.newHashSet(); 
    
    Flow container = new Flow(STYLE_UNSUPPLIED + "container");

    final HtmlTable table = new HtmlTable();
    table.addStyleName(STYLE_UNSUPPLIED + "table");

    int row = 0;
    int col = 0;

    SimpleCheckBox selectAll = new SimpleCheckBox();
    selectAll.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        boolean select = BeeUtils.isTrue(event.getValue());

        selectedIds.clear();
        if (select) {
          for (BeeRow dataRow : rowSet.getRows()) {
            selectedIds.add(dataRow.getId());
          }
        }
        
        Collection<Widget> children = 
            UiHelper.getChildrenByStyleName(table, Sets.newHashSet(STYLE_UNSUPPLIED_SELECTION));
        for (Widget child : children) {
          if (child instanceof SimpleCheckBox) {
            ((SimpleCheckBox) child).setValue(select, true);
          }
        }
      }
    });
    table.setWidgetAndStyle(row, col++, selectAll, STYLE_UNSUPPLIED + "selectAll");
    
    Label dateLabel = new Label(Localized.getConstants().ecOrderDate());
    table.setWidgetAndStyle(row, col++, dateLabel, STYLE_UNSUPPLIED_DATE + STYLE_SUFFIX_LABEL);

    Label nameLabel = new Label(Localized.getConstants().ecItemName());
    table.setWidgetAndStyle(row, col++, nameLabel, STYLE_UNSUPPLIED_NAME + STYLE_SUFFIX_LABEL);

    Label brandLabel = new Label(Localized.getConstants().ecItemBrand());
    table.setWidgetAndStyle(row, col++, brandLabel, STYLE_UNSUPPLIED_BRAND + STYLE_SUFFIX_LABEL);

    Label codeLabel = new Label(Localized.getConstants().ecItemCode());
    table.setWidgetAndStyle(row, col++, codeLabel, STYLE_UNSUPPLIED_CODE + STYLE_SUFFIX_LABEL);

    Label qtyLabel = new Label(Localized.getConstants().ecItemQuantity());
    table.setWidgetAndStyle(row, col++, qtyLabel, STYLE_UNSUPPLIED_QUANTITY + STYLE_SUFFIX_LABEL);

    Label priceLabel = new Label(Localized.getConstants().ecItemPrice());
    table.setWidgetAndStyle(row, col++, priceLabel, STYLE_UNSUPPLIED_PRICE + STYLE_SUFFIX_LABEL);

    table.getRowFormatter().addStyleName(row, STYLE_UNSUPPLIED + "header");
    row++;
    
    for (BeeRow dataRow : rowSet.getRows()) {
      final long id = dataRow.getId();

      col = 0;
      
      final SimpleCheckBox selection = new SimpleCheckBox();
      selection.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          TableRowElement element = DomUtils.getParentRow(selection.getElement(), false);

          if (BeeUtils.isTrue(event.getValue())) {
            selectedIds.add(id);
            element.addClassName(STYLE_UNSUPPLIED_SELECTED);
          } else {
            selectedIds.remove(id);
            element.removeClassName(STYLE_UNSUPPLIED_SELECTED);
          }
        }
      });

      table.setWidgetAndStyle(row, col++, selection, STYLE_UNSUPPLIED_SELECTION);
      
      Label dateWidget = new Label(TimeUtils.renderCompact(dataRow.getDateTime(dateIndex)));
      table.setWidgetAndStyle(row, col++, dateWidget, STYLE_UNSUPPLIED_DATE);

      Label nameWidget = new Label(dataRow.getString(nameIndex));
      table.setWidgetAndStyle(row, col++, nameWidget, STYLE_UNSUPPLIED_NAME);

      Label brandWidget = new Label(dataRow.getString(brandIndex));
      table.setWidgetAndStyle(row, col++, brandWidget, STYLE_UNSUPPLIED_BRAND);

      Label codeWidget = new Label(dataRow.getString(codeIndex));
      table.setWidgetAndStyle(row, col++, codeWidget, STYLE_UNSUPPLIED_CODE);

      Label qtyWidget = new Label(dataRow.getString(qtyIndex));
      table.setWidgetAndStyle(row, col++, qtyWidget, STYLE_UNSUPPLIED_QUANTITY);
      
      int cents = EcUtils.toCents(dataRow.getDouble(priceIndex));
      Label priceWidget = new Label(EcUtils.renderCents(cents));
      table.setWidgetAndStyle(row, col++, priceWidget, STYLE_UNSUPPLIED_PRICE);
      
      table.getRowFormatter().addStyleName(row, STYLE_UNSUPPLIED_ROW);
      
      row++;
    }
    
    Simple wrapper = new Simple(table);
    wrapper.addStyleName(STYLE_UNSUPPLIED + "wrapper");
    
    container.add(wrapper);
    
    Button append = new Button(Localized.getConstants().ecUnsuppliedItemsAppend());
    append.addStyleName(STYLE_UNSUPPLIED + "append");
    container.add(append);

    final DialogBox dialog = DialogBox.create(Localized.getConstants().ecUnsuppliedItems(),
        STYLE_UNSUPPLIED + "dialog");
    dialog.setWidget(container);

    dialog.setAnimationEnabled(true);
    dialog.setHideOnEscape(true);

    dialog.center();
    
    append.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (selectedIds.isEmpty()) {
          Global.showInfo(Localized.getConstants().selectAtLeastOneRow());
        } else {
          appendUnsuppliedItems(rowSet, selectedIds);
          dialog.close();
        }
      }
    });
  }
}
