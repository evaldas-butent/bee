package com.butent.bee.client.modules.trade.acts;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.event.logical.ReadyEvent;
import com.butent.bee.client.event.logical.SelectionCountChangeEvent;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.ui.Relation;

public class TradeActInvoiceBuilder extends AbstractFormInterceptor implements
    SelectorEvent.Handler, SelectionCountChangeEvent.Handler {

  private static final String STYLE_COMMAND_PREFIX = TradeActKeeper.STYLE_PREFIX
      + "invoice-command-";

  private static final String STYLE_COMMAND_COMPOSE = STYLE_COMMAND_PREFIX + "compose";
  private static final String STYLE_COMMAND_SAVE = STYLE_COMMAND_PREFIX + "save";
  private static final String STYLE_COMMAND_DISABLED = STYLE_COMMAND_PREFIX + "disabled";

  private ChildGrid actGrid;

  private IdentifiableWidget commandCompose;
  private IdentifiableWidget commandSave;

  private static Filter eligibleAct() {
    CompoundFilter filter = Filter.or();

    for (TradeActKind kind : TradeActKind.values()) {
      if (kind.enableInvoices()) {
        filter.add(Filter.equals(COL_TA_KIND, kind));
      }
    }

    return filter;
  }

  TradeActInvoiceBuilder() {
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid) {
      actGrid = (ChildGrid) widget;

      actGrid.addReadyHandler(new ReadyEvent.Handler() {
        @Override
        public void onReady(ReadyEvent event) {
          LogUtils.getRootLogger().debug("ready", actGrid.getGridView() != null);
        }
      });

    } else if (widget instanceof DataSelector) {
      ((DataSelector) widget).addSelectorHandler(this);
    }

    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void configureRelation(String name, Relation relation) {
    if (COL_TA_COMPANY.equals(name)) {
      relation.setFilter(Filter.in(Data.getIdColumn(VIEW_COMPANIES),
          VIEW_TRADE_ACTS, COL_TA_COMPANY, eligibleAct()));
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new TradeActInvoiceBuilder();
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (event.isChanged() && event.getRelatedRow() != null && event.hasRelatedView(VIEW_COMPANIES)
        && actGrid != null) {
      actGrid.onParentRow(new ParentRowEvent(VIEW_COMPANIES, event.getRelatedRow(), true));
    }
  }

  @Override
  public void onLoad(FormView form) {
    if (commandCompose == null) {
      commandCompose = new Button(Localized.getConstants().taInvoiceCompose(), new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          doCompose();
        }
      });

      commandCompose.addStyleName(STYLE_COMMAND_COMPOSE);
      commandCompose.addStyleName(STYLE_COMMAND_DISABLED);

      form.getViewPresenter().getHeader().addCommandItem(commandCompose);
    }

    if (commandSave == null) {
      commandSave = new Button(Localized.getConstants().taInvoiceSave(), new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          doSave();
        }
      });

      commandSave.addStyleName(STYLE_COMMAND_SAVE);
      commandSave.addStyleName(STYLE_COMMAND_DISABLED);

      form.getViewPresenter().getHeader().addCommandItem(commandSave);
    }

    super.onLoad(form);
  }

  @Override
  public void onSelectionCountChange(SelectionCountChangeEvent event) {
  }

  private void doCompose() {
  }

  private void doSave() {
  }
}
