package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.VIEW_CARGO_CREDIT_INCOMES;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.event.DataChangeEvent.Effect;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.EnumSet;

public class CargoCreditInvoiceForm extends AbstractFormInterceptor
    implements ValueChangeHandler<String> {

  private ScheduledCommand refresher;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid) {
      ChildGrid grid = (ChildGrid) widget;

      if (BeeUtils.same(name, getTradeItemsName())) {
        grid.setGridInterceptor(new InvoiceItemsGrid(getRefresher()));

      } else if (BeeUtils.same(name, VIEW_CARGO_CREDIT_INCOMES)) {
        grid.setGridInterceptor(new AbstractGridInterceptor());
      }
    } else if (BeeUtils.same(name, COL_TRADE_VAT_INCL) && widget instanceof InputBoolean) {
      ((InputBoolean) widget).addValueChangeHandler(this);
    }
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action == Action.PRINT) {
      String print = getFormView().getProperty("reports");

      if (!BeeUtils.isEmpty(print)) {
        final String[] reports = BeeUtils.split(print, BeeConst.CHAR_COMMA);
        final String[] forms = new String[reports.length];

        final ChoiceCallback choice = new ChoiceCallback() {
          @Override
          public void onSuccess(int value) {
            RowEditor.openRow(reports[value], Data.getDataInfo(getFormView().getViewName()),
                getFormView().getActiveRow(), true, null, null, null,
                new PrintInvoiceInterceptor());
          }
        };
        if (reports.length > 1) {
          final Holder<Integer> counter = Holder.of(0);

          for (int i = 0; i < reports.length; i++) {
            final int idx = i;
            FormFactory.getFormDescription(reports[i], new Callback<FormDescription>() {
              @Override
              public void onFailure(String... reason) {
                super.onFailure(reason);
                forms[idx] = reports[idx] + " (missing)";
                process();
              }

              @Override
              public void onSuccess(FormDescription formDescription) {
                forms[idx] = BeeUtils.notEmpty(LocaleUtils
                    .maybeLocalize(formDescription.getCaption()), reports[idx]);
                process();
              }

              private void process() {
                counter.set(counter.get() + 1);

                if (counter.get() == reports.length) {
                  Global.choice(Localized.getConstants().trInvoice(),
                      Localized.getConstants().choosePrintingForm(), Lists.newArrayList(forms),
                      choice);
                }
              }
            });
          }
        } else {
          choice.onSuccess(0);
        }
        return false;
      }
    }
    return super.beforeAction(action, presenter);
  }

  @Override
  public FormInterceptor getInstance() {
    return new CargoCreditInvoiceForm();
  }

  @Override
  public void onValueChange(ValueChangeEvent<String> event) {
    Queries.update(getFormView().getViewName(),
        ComparisonFilter.compareId(getFormView().getActiveRow().getId()),
        COL_TRADE_VAT_INCL, new BooleanValue(BeeUtils.toBoolean(event.getValue())),
        new IntCallback() {
          @Override
          public void onSuccess(Integer result) {
            if (BeeUtils.isPositive(result)) {
              getRefresher().execute();
              Data.onTableChange(getTradeItemsName(), EnumSet.of(Effect.REFRESH));
            }
          }
        });
  }

  protected String getTradeItemsName() {
    return TBL_PURCHASE_ITEMS;
  }

  private ScheduledCommand getRefresher() {
    if (refresher == null) {
      refresher = new ScheduledCommand() {
        @Override
        public void execute() {
          final FormView form = getFormView();

          Queries.getRow(form.getViewName(), form.getActiveRow().getId(), new RowCallback() {
            @Override
            public void onSuccess(BeeRow result) {
              form.updateRow(result, false);
              BeeKeeper.getBus().fireEvent(new RowUpdateEvent(form.getViewName(), result));
            }
          });
        }
      };
    }
    return refresher;
  }
}
