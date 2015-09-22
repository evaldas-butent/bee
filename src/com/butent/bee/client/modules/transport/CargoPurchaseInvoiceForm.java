package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.TabbedPages;
import com.butent.bee.client.modules.trade.InvoiceForm;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.ModificationEvent;
import com.butent.bee.shared.data.view.RowInfoList;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CargoPurchaseInvoiceForm extends InvoiceForm implements ClickHandler {

  private boolean done;

  private Long accumulation;
  private Button accumulationButton = new Button("Faktinės sąnaudos");

  public CargoPurchaseInvoiceForm() {
    super(null);
  }

  @Override
  public void beforeRefresh(FormView form, final IsRow row) {
    if (!done) {
      String caption;
      Widget child;

      if (DataUtils.isId(form.getLongValue(COL_SALE))) {
        caption = Localized.getConstants().trCreditInvoice();
        child = form.getWidgetByName(TransportConstants.VIEW_CARGO_PURCHASES);
      } else {
        caption = Localized.getConstants().trPurchaseInvoice();
        child = form.getWidgetByName(TransportConstants.VIEW_CARGO_SALES);

        accumulationButton.setVisible(false);
        accumulationButton.addClickHandler(this);
        form.getViewPresenter().getHeader().addCommandItem(accumulationButton);
      }
      form.getViewPresenter().getHeader().setCaption(caption);

      if (child != null) {
        Widget tabs = form.getWidgetByName(NameUtils.getClassName(TabbedPages.class));

        if (tabs != null && tabs instanceof TabbedPages) {
          int idx = ((TabbedPages) tabs).getContentIndex(child);

          if (!BeeConst.isUndef(idx)) {
            ((TabbedPages) tabs).removePage(idx);
          }
        }
      }
      done = true;
    }
    if (!DataUtils.isId(accumulation)) {
      Global.getParameter(TransportConstants.PRM_ACCUMULATION_OPERATION,
          new Consumer<String>() {
            @Override
            public void accept(String id) {
              accumulation = BeeUtils.toLongOrNull(id);
              showAction(row);
            }
          });
    } else {
      showAction(row);
    }
    super.beforeRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new CargoPurchaseInvoiceForm();
  }

  @Override
  public void onClick(ClickEvent clickEvent) {
    Relation relation = Relation.create(VIEW_PURCHASE_OPERATIONS,
        Collections.singletonList(COL_OPERATION_NAME));
    relation.disableNewRow();
    relation.disableEdit();

    final UnboundSelector selector = UnboundSelector.create(relation);
    Flow flow = new Flow();
    Horizontal panel = new Horizontal();
    panel.add(new Label(Localized.getConstants().trdOperation()));
    panel.add(selector);
    flow.add(panel);

    final List<Pair<IsRow, InputNumber>> values = new ArrayList<>();
    Widget items = getFormView().getWidgetByName(TBL_PURCHASE_ITEMS);
    final ChildGrid grid;
    final Integer price;
    final Integer oldPrice;

    if (items != null && items instanceof ChildGrid) {
      grid = (ChildGrid) items;
      HtmlTable table = new HtmlTable();
      int r = 0;
      int name = grid.getGridView().getDataIndex(ClassifierConstants.ALS_ITEM_NAME);
      int article = grid.getGridView().getDataIndex(COL_TRADE_ITEM_ARTICLE);
      price = grid.getGridView().getDataIndex(COL_TRADE_ITEM_PRICE);
      oldPrice = grid.getGridView().getDataIndex("Old" + COL_TRADE_ITEM_PRICE);

      for (IsRow item : grid.getGridView().getRowData()) {
        table.setText(r, 0, item.getString(name));
        table.setText(r, 1, item.getString(article));

        InputNumber number = new InputNumber();
        number.setValue(item.getString(price));
        table.setWidget(r, 2, number);
        values.add(Pair.of(item, number));
        r++;
      }
      flow.add(table);
    } else {
      grid = null;
      price = null;
      oldPrice = null;
    }
    Global.inputWidget(accumulationButton.getHtml(), flow, new InputCallback() {
      @Override
      public String getErrorMessage() {
        if (!DataUtils.isId(selector.getRelatedId())) {
          selector.setFocus(true);
          return Localized.getConstants().valueRequired();
        } else if (Objects.equals(selector.getRelatedId(), accumulation)) {
          selector.setFocus(true);
          return "Neteisinga operacija";
        }
        for (Pair<IsRow, InputNumber> pair : values) {
          if (pair.getB().isEmpty()) {
            pair.getB().setFocus(true);
            return Localized.getConstants().valueRequired();
          }
        }
        return super.getErrorMessage();
      }

      @Override
      public void onSuccess() {
        BeeRowSet rs = new BeeRowSet(grid.getPresenter().getViewName(),
            DataUtils.getColumns(grid.getPresenter().getDataColumns(),
                Arrays.asList(COL_TRADE_ITEM_PRICE, "Old" + COL_TRADE_ITEM_PRICE)));

        for (Pair<IsRow, InputNumber> pair : values) {
          IsRow row = pair.getA();
          String old = row.getString(price);
          BeeRow upd = new BeeRow(row.getId(), row.getVersion(),
              Arrays.asList(old, row.getString(oldPrice)));
          upd.preliminaryUpdate(0, pair.getB().getNormalizedValue());
          upd.preliminaryUpdate(1, old);
          rs.addRow(upd);
        }
        Queries.updateRows(rs, new RpcCallback<RowInfoList>() {
          @Override
          public void onSuccess(RowInfoList result) {
            IsRow activeRow = getActiveRow();
            Queries.updateAndFire(getViewName(), activeRow.getId(), activeRow.getVersion(),
                COL_TRADE_OPERATION, getStringValue(COL_TRADE_OPERATION),
                BeeUtils.toString(selector.getRelatedId()), ModificationEvent.Kind.UPDATE_ROW);

            grid.getPresenter().refresh(true, false);
          }
        });
      }
    });
  }

  private void showAction(IsRow row) {
    accumulationButton.setVisible(row != null && DataUtils.isId(accumulation)
        && !BeeUtils.isEmpty(row.getString(getDataIndex(COL_TRADE_EXPORTED)))
        && Objects.equals(row.getLong(getDataIndex(COL_TRADE_OPERATION)), accumulation));
  }
}
