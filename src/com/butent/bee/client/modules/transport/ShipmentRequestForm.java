package com.butent.bee.client.modules.transport;

import com.google.common.base.Strings;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.modules.administration.AdministrationUtils;
import com.butent.bee.client.modules.mail.NewMailMessage;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BiConsumer;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.modules.transport.TransportConstants.*;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

class ShipmentRequestForm extends AbstractFormInterceptor {

  private final LocalizableConstants loc = Localized.getConstants();

  private Button mailCommand = new Button(loc.trWriteEmail(), new ClickHandler() {
    @Override
    public void onClick(ClickEvent clickEvent) {
      sendMail();
    }
  });

  private Button registerCommand = new Button(loc.register(), new ClickHandler() {
    @Override
    public void onClick(ClickEvent clickEvent) {
      onRegister();
    }
  });

  private Button confirmCommand = new Button(loc.trRequestStatusConfirmed(), new ClickHandler() {
    @Override
    public void onClick(ClickEvent clickEvent) {
      onConfirm();
    }
  });

  private Button blockCommand = new Button(loc.ipBlockCommand(), new ClickHandler() {
    @Override
    public void onClick(ClickEvent event) {
      onBlock();
    }
  });

  private Button lostCommand = new Button(loc.trRequestStatusLost(), new ClickHandler() {
    @Override
    public void onClick(ClickEvent clickEvent) {
      onLoss(true);
    }
  });

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    HeaderView header = getHeaderView();
    if (header == null) {
      return;
    }
    header.clearCommandPanel();

    if (!DataUtils.hasId(row)) {
      return;
    }
    CargoRequestStatus status = EnumUtils.getEnumByIndex(CargoRequestStatus.class,
        row.getInteger(form.getDataIndex(COL_QUERY_STATUS)));

    if (status != CargoRequestStatus.LOST) {
      header.addCommandItem(mailCommand);

      if (!DataUtils.isId(row.getLong(form.getDataIndex(ClassifierConstants.COL_COMPANY_PERSON)))) {
        header.addCommandItem(registerCommand);
      } else if (status != CargoRequestStatus.CONFIRMED) {
        header.addCommandItem(confirmCommand);
      }
      if (status != CargoRequestStatus.CONFIRMED) {
        header.addCommandItem(lostCommand);

        if (status == CargoRequestStatus.NEW
            && !BeeUtils.isEmpty(getStringValue(COL_QUERY_HOST))
            && Data.isViewEditable(AdministrationConstants.VIEW_IP_FILTERS)) {
          header.addCommandItem(blockCommand);
        }
      }
    }
    form.setEnabled(status != CargoRequestStatus.LOST);
  }

  @Override
  public FormInterceptor getInstance() {
    return new ShipmentRequestForm();
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, Scheduler.ScheduledCommand focusCommand) {
    if (EnumUtils.getEnumByIndex(CargoRequestStatus.class,
        row.getInteger(form.getDataIndex(COL_QUERY_STATUS))) == CargoRequestStatus.LOST
        && BeeUtils.isEmpty(row.getString(form.getDataIndex(COL_QUERY_REASON)))) {

      onLoss(true);
    }
    return super.onStartEdit(form, row, focusCommand);
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    SelfServiceUtils.setDefaultExpeditionType(form, newRow, COL_QUERY_EXPEDITION);
    SelfServiceUtils.setDefaultShippingTerm(form, newRow, COL_CARGO_SHIPPING_TERM);
    super.onStartNewRow(form, oldRow, newRow);
  }

  private void onBlock() {
    AdministrationUtils.blockHost(loc.ipBlockCommand(), getStringValue(COL_QUERY_HOST),
        getFormView(), new Callback<String>() {
          @Override
          public void onSuccess(String result) {
            onLoss(false);
          }
        });
  }

  private void onRegister() {
  }

  private void onConfirm() {
  }

  private void onLoss(boolean required) {
    InputArea comment = new InputArea();
    comment.setWidth("100%");
    comment.setVisibleLines(3);

    UnboundSelector reason = UnboundSelector.create(TBL_LOSS_REASONS,
        Collections.singletonList(COL_LOSS_REASON_NAME));

    reason.addSelectorHandler(new SelectorEvent.Handler() {
      @Override
      public void onDataSelector(SelectorEvent event) {
        if (event.isChanged()) {
          reason.setOptions(event.getRelatedRow() != null
              ? Data.getString(event.getRelatedViewName(), event.getRelatedRow(),
              COL_LOSS_REASON_TEMPLATE) : null);
          comment.setValue(reason.getOptions());
        }
      }
    });
    HtmlTable layout = new HtmlTable();
    layout.setText(0, 0, loc.reason());
    layout.setWidget(0, 1, reason);
    layout.getCellFormatter().setColSpan(1, 0, 2);
    layout.setText(1, 0, loc.comment());
    layout.getCellFormatter().setColSpan(2, 0, 2);
    layout.setWidget(2, 0, comment);

    Global.inputWidget(CargoRequestStatus.LOST.getCaption(loc), layout, new InputCallback() {
      @Override
      public String getErrorMessage() {
        if (required && (BeeUtils.allEmpty(reason.getDisplayValue(), comment.getValue())
            || BeeUtils.allNotEmpty(reason.getDisplayValue(), comment.getValue())
            && Objects.equals(comment.getValue(), Strings.nullToEmpty(reason.getOptions())))) {

          comment.setFocus(true);
          return loc.valueRequired();
        }
        return super.getErrorMessage();
      }

      @Override
      public void onCancel() {
        if (required) {
          super.onCancel();
        } else {
          onSuccess();
        }
      }

      @Override
      public void onSuccess() {
        getActiveRow().setValue(getDataIndex(COL_QUERY_REASON),
            BeeUtils.join(BeeConst.STRING_EOL, reason.getDisplayValue(), comment.getValue()));
        SelfServiceUtils.updateStatus(getFormView(), COL_QUERY_STATUS, CargoRequestStatus.LOST);
      }
    });
  }

  private void sendMail() {
    FormView form = getFormView();
    BeeRow row = DataUtils.cloneRow(form.getActiveRow());
    row.setValue(getDataIndex(COL_QUERY_STATUS), CargoRequestStatus.ANSWERED.ordinal());

    BeeRowSet rs = DataUtils.getUpdated(form.getViewName(), form.getDataColumns(), form.getOldRow(),
        row, form.getChildrenForUpdate());

    NewMailMessage.create(getStringValue(COL_QUERY_CUSTOMER_EMAIL), null, null, null,
        new BiConsumer<Long, Boolean>() {
          @Override
          public void accept(Long messageId, Boolean saveMode) {
            DataInfo info = Data.getDataInfo(AdministrationConstants.VIEW_RELATIONS);

            Queries.insert(info.getViewName(),
                Arrays.asList(info.getColumn(COL_SHIPMENT_REQUEST),
                    info.getColumn(MailConstants.COL_MESSAGE)),
                Arrays.asList(BeeUtils.toString(row.getId()), BeeUtils.toString(messageId)), null,
                new RowInsertCallback(info.getViewName()) {
                  @Override
                  public void onSuccess(BeeRow result) {
                    Data.onTableChange(info.getTableName(), DataChangeEvent.RESET_REFRESH);
                    super.onSuccess(result);
                  }
                });
            SelfServiceUtils.update(form, rs);
          }
        });
  }
}
