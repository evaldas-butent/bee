package com.butent.bee.client.modules.ec;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants.EcOrderStatus;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

class EcOrderForm extends AbstractFormInterceptor {

  private static final String STYLE_LABEL = "label";
  private static final String STYLE_INPUT = "input";

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

  private void rejectOrder() {
    String stylePrefix = EcStyles.name("OrderRejection-");

    HtmlTable table = new HtmlTable();
    table.addStyleName(stylePrefix + "table");

    int row = 0;

    String styleName = stylePrefix + "reason-";
    Label label = new Label(Localized.getConstants().ecRejectionReason());
    label.addStyleName(StyleUtils.NAME_REQUIRED);
    table.setWidgetAndStyle(row, 0, label, styleName + STYLE_LABEL);

    final UnboundSelector selector = UnboundSelector.create(VIEW_REJECTION_REASONS,
        Lists.newArrayList(COL_REJECTION_REASON_NAME));
    table.setWidgetAndStyle(row, 1, selector, styleName + STYLE_INPUT);
    row++;

    styleName = stylePrefix + "comment-";
    label = new Label(Localized.getConstants().comment());
    table.setWidgetAndStyle(row, 0, label, styleName + STYLE_LABEL);

    final InputArea inputArea = new InputArea();

    Widget widget = getFormView().getWidgetBySource(COL_ORDER_MANAGER_COMMENT);
    if (widget instanceof InputArea) {
      String value = ((InputArea) widget).getValue();
      if (!BeeUtils.isEmpty(value)) {
        inputArea.setValue(value.trim());
      }
    }

    table.setWidgetAndStyle(row, 1, inputArea, styleName + STYLE_INPUT);
    row++;

    int col = 0;
    Button confirm = new Button(Localized.getConstants().ecOrderRejectConfirm());
    table.setWidgetAndStyle(row, col, confirm, styleName + "confirm");

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
              }
            });
      }
    });
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
              header.addCommandItem(EcOrderForm.this.send);
              header.addCommandItem(EcOrderForm.this.reject);
            } else {
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
}
