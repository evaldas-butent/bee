package com.butent.bee.client.modules.transport;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.ui.CssUnit;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class CargoRequestForm extends AbstractFormInterceptor {

  private static class SaveCallback extends RowUpdateCallback {

    private final FormView formView;

    public SaveCallback(FormView formView) {
      super(formView.getViewName());
      this.formView = formView;
    }

    @Override
    public void onSuccess(BeeRow result) {
      super.onSuccess(result);
      formView.updateRow(result, false);
    }
  }

  private IsRow currentRow;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, COL_ORDER_NO) && widget instanceof HasClickHandlers) {
      ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          RowEditor.openRow(TBL_ORDERS, currentRow.getLong(getFormView().getDataIndex(COL_ORDER)),
              false, null);
        }
      });
    }
  }

  @Override
  public void afterRefresh(final FormView form, IsRow row) {
    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    Long author = currentRow.getLong(form.getDataIndex("Creator"));
    Long manager = currentRow.getLong(form.getDataIndex("Manager"));

    boolean enabled = (author == null && manager == null)
        || Objects.equal(author, BeeKeeper.getUser().getUserId())
        || Objects.equal(manager, BeeKeeper.getUser().getUserId());

    if (enabled) {
      if (!DataUtils.isId(currentRow.getLong(form.getDataIndex(COL_ORDER)))) {
        header.addCommandItem(new Button(Localized.getConstants().trCargoRequestReturnToOrder(),
            new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            requestToOrders();
          }
        }));
      }
      boolean finished =
          currentRow.getDateTime(form.getDataIndex(CrmConstants.COL_REQUEST_FINISHED)) != null;

      if (finished) {
        header.addCommandItem(new Button(Localized.getConstants().trCargoRequestReturn(),
            new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            restoreRequest();
          }
        }));
        enabled = false;

      } else {
        header.addCommandItem(new Button(Localized.getConstants().trCargoRequestFinish(),
            new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            finishRequest();
          }
        }));
      }
    }
    form.setEnabled(enabled);
  }

  @Override
  public FormInterceptor getInstance() {
    return new CargoRequestForm();
  }

  @Override
  public void onSetActiveRow(IsRow row) {
    currentRow = row;
  }

  private void finishRequest() {
    final FormView form = getFormView();

    Global.inputString(Localized.getConstants().trRequestFinishAction(), Localized.getConstants()
        .trRequestFinishReason(), new StringCallback(true) {
      @Override
      public void onSuccess(String value) {
        List<BeeColumn> columns = Lists.newArrayList(DataUtils
            .getColumn(CrmConstants.COL_REQUEST_FINISHED, form.getDataColumns()));

        List<String> oldValues = Lists.newArrayList(currentRow
            .getString(form.getDataIndex(CrmConstants.COL_REQUEST_FINISHED)));

        List<String> newValues = Lists.newArrayList(BeeUtils.toString(new DateTime().getTime()));

        columns.add(DataUtils.getColumn(CrmConstants.COL_REQUEST_RESULT, form.getDataColumns()));
        oldValues.add(currentRow.getString(form.getDataIndex(CrmConstants.COL_REQUEST_RESULT)));
        newValues.add(value);

        Queries.update(form.getViewName(), currentRow.getId(), currentRow.getId(),
            columns, oldValues, newValues, form.getChildrenForUpdate(), new SaveCallback(form));
      }
    }, null, BeeConst.UNDEF, 300, CssUnit.PX);
  }

  private void requestToOrders() {
    Global.confirm(Localized.getConstants().trCargoRequestCreateTransportationOrderQuestion(),
        new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        FormView form = getFormView();
        List<BeeColumn> columns = Lists.newArrayList();
        List<String> oldValues = Lists.newArrayList();
        List<String> newValues = Lists.newArrayList();

        for (String col : new String[] {"Route", "Customer", "CustomerPerson", "Manager"}) {
          columns.add(DataUtils.getColumn("Order" + col, form.getDataColumns()));
          oldValues.add(currentRow.getString(form.getDataIndex("Order" + col)));
          newValues.add(currentRow.getString(form.getDataIndex(col)));
        }
        if (currentRow.getString(form.getDataIndex(CrmConstants.COL_REQUEST_FINISHED)) == null) {
          columns.add(DataUtils.getColumn(CrmConstants.COL_REQUEST_FINISHED,
              form.getDataColumns()));
          oldValues.add(null);
          newValues.add(BeeUtils.toString(new DateTime().getTime()));
        }
        Queries.update(form.getViewName(), currentRow.getId(), currentRow.getId(),
            columns, oldValues, newValues, form.getChildrenForUpdate(), new SaveCallback(form));
      }
    });
  }

  private void restoreRequest() {
    Global.confirm(Localized.getConstants().trCargoRequestsSetActiveRequestQuestion(),
        new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        FormView form = getFormView();

        List<BeeColumn> columns = Lists.newArrayList(DataUtils
            .getColumn(CrmConstants.COL_REQUEST_FINISHED, form.getDataColumns()));

        List<String> oldValues = Lists.newArrayList(currentRow
            .getString(form.getDataIndex(CrmConstants.COL_REQUEST_FINISHED)));

        List<String> newValues = Lists.newArrayList((String) null);

        Queries.update(form.getViewName(), currentRow.getId(), currentRow.getId(),
            columns, oldValues, newValues, form.getChildrenForUpdate(), new SaveCallback(form));
      }
    });
  }
}
