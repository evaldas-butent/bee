package com.butent.bee.client.modules.transport;

import com.google.common.base.Strings;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.modules.administration.AdministrationUtils;
import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.modules.mail.NewMailMessage;
import com.butent.bee.client.output.ReportUtils;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.i18n.SupportedLocale;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class ShipmentRequestForm extends AbstractFormInterceptor {

  private final LocalizableConstants loc = Localized.getConstants();

  private Button mailCommand = new Button(loc.trWriteEmail(), new ClickHandler() {
    @Override
    public void onClick(ClickEvent clickEvent) {
      onAnswer();
    }
  });

  private Button registerCommand = new Button(loc.register(), new ClickHandler() {
    @Override
    public void onClick(ClickEvent clickEvent) {
      onRegister();
    }
  });

  private Button contractCommand = new Button(loc.trContract(), new ClickHandler() {
    @Override
    public void onClick(ClickEvent clickEvent) {
      sendContract(true);
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

    renderOrderId();

    boolean registered = DataUtils.isId(row.getLong(form.getDataIndex(COL_COMPANY_PERSON)));

    Widget widget = form.getWidgetByName(COL_REGISTRATION_REGISTER);

    if (widget != null) {
      widget.setVisible(!registered);
    }
    widget = form.getWidgetByName(COL_COMPANY_PERSON);

    if (widget != null) {
      widget.setVisible(registered);
    }
    if (DataUtils.isNewRow(row)) {
      return;
    }
    header.setCaption(registered ? loc.trRequest() : loc.trRequestUnregistered());

    Integer status = row.getInteger(form.getDataIndex(COL_QUERY_STATUS));

    if (!isSelfService() && !ShipmentRequestStatus.LOST.is(status)) {
      header.addCommandItem(mailCommand);

      if (!ShipmentRequestStatus.CONFIRMED.is(status)) {
        if (!registered) {
          header.addCommandItem(registerCommand);
        } else {
          header.addCommandItem(contractCommand);
          header.addCommandItem(confirmCommand);
        }
        header.addCommandItem(lostCommand);

        if (ShipmentRequestStatus.NEW.is(status)
            && !BeeUtils.isEmpty(getStringValue(COL_QUERY_HOST))
            && Data.isViewEditable(VIEW_IP_FILTERS)) {
          header.addCommandItem(blockCommand);
        }
      }
    }
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    if (!DataUtils.isNewRow(row)) {
      Integer status = row.getInteger(form.getDataIndex(COL_QUERY_STATUS));

      form.setEnabled(!ShipmentRequestStatus.LOST.is(status)
          && !ShipmentRequestStatus.CONFIRMED.is(status)
          && (!isSelfService() || ShipmentRequestStatus.NEW.is(status)));
    }
    super.beforeRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new ShipmentRequestForm();
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, Scheduler.ScheduledCommand focusCommand) {
    if (ShipmentRequestStatus.LOST.is(row.getInteger(form.getDataIndex(COL_QUERY_STATUS)))
        && BeeUtils.isEmpty(row.getString(form.getDataIndex(COL_QUERY_REASON)))) {

      onLoss(true);
    }
    return super.onStartEdit(form, row, focusCommand);
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    newRow.setValue(form.getDataIndex(COL_COMPANY_PERSON),
        BeeKeeper.getUser().getUserData().getCompanyPerson());

    SelfServiceUtils.setDefaultPerson(form, newRow, COL_COMPANY_PERSON);
    SelfServiceUtils.setDefaultExpeditionType(form, newRow, COL_QUERY_EXPEDITION);
    SelfServiceUtils.setDefaultShippingTerm(form, newRow, COL_CARGO_SHIPPING_TERM);

    newRow.setValue(form.getDataIndex(COL_USER_LOCALE),
        SupportedLocale.getByLanguage(SupportedLocale.normalizeLanguage(loc.languageTag()))
            .ordinal());

    super.onStartNewRow(form, oldRow, newRow);
  }

  private static boolean isSelfService() {
    return Objects.equals(BeeKeeper.getScreen().getUserInterface(), UserInterface.SELF_SERVICE);
  }

  private void onAnswer() {
    sendMail(ShipmentRequestStatus.NEW.is(getIntegerValue(COL_QUERY_STATUS))
        ? ShipmentRequestStatus.ANSWERED : null, null, null, null);
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
    FormView form = getFormView();
    BeeRow oldRow = DataUtils.cloneRow(form.getOldRow());
    BeeRow row = DataUtils.cloneRow(form.getActiveRow());

    Global.confirm(loc.register(), Icon.QUESTION, Arrays.asList("Lab", "dein"),
        Localized.getConstants().actionCreate(), Localized.getConstants().actionCancel(), () -> {
          Map<String, String> companyInfo = new HashMap<>();

          for (String col : new String[] {
              COL_COMPANY_TYPE, COL_COMPANY_NAME, COL_COMPANY_CODE, COL_COMPANY_VAT_CODE, COL_FAX,
              COL_COUNTRY, COL_CITY, COL_ADDRESS, COL_POST_INDEX, COL_NOTES}) {

            companyInfo.put(col, row.getString(form.getDataIndex("Customer" + col)));
          }
          ClassifierUtils.createCompany(companyInfo, (company) -> {
            Map<String, String> personInfo = new HashMap<>();
            personInfo.put(COL_COMPANY, BeeUtils.toString(company));

            String contact = row.getString(form.getDataIndex(COL_QUERY_CUSTOMER_CONTACT));

            if (!BeeUtils.isEmpty(contact)) {
              String[] arr = contact.split(BeeConst.STRING_SPACE, 2);
              personInfo.put(COL_FIRST_NAME, ArrayUtils.getQuietly(arr, 0));
              personInfo.put(COL_LAST_NAME, ArrayUtils.getQuietly(arr, 1));
            }
            personInfo.put(COL_PHONE, row.getString(form.getDataIndex("Customer" + COL_PHONE)));
            personInfo.put(ALS_EMAIL_ID, row.getString(form.getDataIndex("Customer" + COL_EMAIL)));
            personInfo.put(COL_POSITION,
                row.getString(form.getDataIndex(COL_QUERY_CUSTOMER_CONTACT_POSITION)));

            ClassifierUtils.createCompanyPerson(personInfo, (person) -> {
              row.setValue(getDataIndex(COL_COMPANY_PERSON), person);
              row.setValue(getDataIndex(COL_QUERY_MANAGER), BeeKeeper.getUser().getUserId());

              SelfServiceUtils.update(form, DataUtils.getUpdated(form.getViewName(),
                  form.getDataColumns(), oldRow, row, null));
            });
          });
        });
  }

  private void onConfirm() {
    boolean logistics = BeeUtils.unbox(getBooleanValue(COL_EXPEDITION_LOGISTICS));

    FormView form = getFormView();
    BeeRow oldRow = DataUtils.cloneRow(form.getOldRow());
    BeeRow row = DataUtils.cloneRow(form.getActiveRow());

    Long manager = row.getLong(form.getDataIndex(COL_QUERY_MANAGER));
    Holder<String> department = Holder.absent();

    if (!DataUtils.isId(manager)) {
      notifyRequired(loc.trRequestResponsibleManager());
      return;
    }
    if (logistics) {
      Queries.getValue(VIEW_ASSESSMENT_EXECUTORS, manager, COL_DEPARTMENT,
          new RpcCallback<String>() {
            @Override
            public void onSuccess(String result) {
              department.set(result);
            }
          });
    }
    Global.confirm(logistics ? loc.trLogistics() : loc.transport(), Icon.QUESTION,
        Collections.singletonList(loc.trCommandCreateNewOrder()), () -> {
          if (logistics && department.isNull()) {
            notifyRequired(loc.department());
            return;
          }
          Queries.insert(VIEW_ORDERS, Data.getColumns(VIEW_ORDERS,
                  Arrays.asList(COL_CUSTOMER, COL_CUSTOMER + COL_PERSON, COL_ORDER_MANAGER)),
              Arrays.asList(row.getString(form.getDataIndex(COL_COMPANY)),
                  row.getString(form.getDataIndex(COL_COMPANY_PERSON)), BeeUtils.toString(manager)),
              null, new RowInsertCallback(VIEW_ORDERS) {
                @Override
                public void onSuccess(BeeRow order) {
                  super.onSuccess(order);

                  row.setValue(form.getDataIndex(COL_QUERY_STATUS),
                      ShipmentRequestStatus.CONFIRMED.ordinal());

                  if (logistics) {
                    Long cargo = row.getLong(form.getDataIndex(COL_CARGO));

                    Queries.update(VIEW_ORDER_CARGO, cargo, COL_ORDER,
                        Value.getValue(order.getId()), new Queries.IntCallback() {
                          @Override
                          public void onSuccess(Integer upd) {
                            Queries.insert(VIEW_ASSESSMENTS, Data.getColumns(VIEW_ASSESSMENTS,
                                    Arrays.asList(COL_CARGO, COL_DEPARTMENT)),
                                Arrays.asList(BeeUtils.toString(cargo), department.get()), null,
                                new RowInsertCallback(VIEW_ASSESSMENTS) {
                                  @Override
                                  public void onSuccess(BeeRow assessment) {
                                    super.onSuccess(assessment);

                                    SelfServiceUtils.update(form,
                                        DataUtils.getUpdated(form.getViewName(),
                                            form.getDataColumns(), oldRow, row, null));
                                  }
                                });
                          }
                        });
                  } else {
                    row.setValue(getDataIndex(COL_ORDER), order.getId());

                    SelfServiceUtils.update(form, DataUtils.getUpdated(form.getViewName(),
                        form.getDataColumns(), oldRow, row, null));
                  }
                }
              });
        });
  }

  private void onLoss(boolean required) {
    InputArea comment = new InputArea();
    comment.setWidth("100%");
    comment.setVisibleLines(4);

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

    Global.inputWidget(ShipmentRequestStatus.LOST.getCaption(loc), layout, new InputCallback() {
      @Override
      public String getErrorMessage() {
        if (required && (BeeUtils.allEmpty(reason.getDisplayValue(), comment.getValue())
            || BeeUtils.allNotEmpty(reason.getDisplayValue(), comment.getValue())
            && Objects.equals(comment.getValue(), Strings.nullToEmpty(reason.getOptions())))) {

          comment.setFocus(true);
          return loc.valueRequired();
        }
        return InputCallback.super.getErrorMessage();
      }

      @Override
      public void onCancel() {
        if (required) {
          InputCallback.super.onCancel();
        } else {
          onSuccess();
        }
      }

      @Override
      public void onSuccess() {
        getActiveRow().setValue(getDataIndex(COL_QUERY_REASON),
            BeeUtils.join(BeeConst.STRING_EOL, reason.getDisplayValue(), comment.getValue()));
        SelfServiceUtils.updateStatus(getFormView(), COL_QUERY_STATUS, ShipmentRequestStatus.LOST);
      }
    });
  }

  private void renderOrderId() {
    Widget widget = getFormView().getWidgetByName(COL_ORDER_ID);

    if (widget != null && widget instanceof HasWidgets) {
      ((HasWidgets) widget).clear();

      Long assessment = getLongValue(COL_ASSESSMENT);
      Long order = getLongValue(COL_ORDER);

      String viewName = DataUtils.isId(assessment) ? VIEW_ASSESSMENTS
          : (DataUtils.isId(order) ? VIEW_ORDERS : null);

      if (!BeeUtils.isEmpty(viewName)) {
        Long id = BeeUtils.nvl(assessment, order);
        Label label = new Label(BeeUtils.joinWords(loc.trOrder(), id));
        label.addClickHandler((e) -> RowEditor.open(viewName, id, Opener.MODAL));
        ((HasWidgets) widget).add(label);
      }
    }
  }

  private void sendContract(boolean preview) {
    Map<String, String> params = new HashMap<>();

    for (BeeColumn column : getFormView().getDataColumns()) {
      params.put(column.getId(), getStringValue(column.getId()));
    }
    if (preview) {
      ReportUtils.showReport(REP_CONTRACT, params, null, () -> sendContract(false));
    } else {
      Long id = getActiveRowId();
      String localizedContent = Localized.column(COL_TEXT_CONTENT,
          EnumUtils.getEnumByIndex(SupportedLocale.class, getIntegerValue(COL_USER_LOCALE))
              .getLanguage());

      Queries.getRowSet(VIEW_TEXT_CONSTANTS, null, Filter.equals(COL_TEXT_CONSTANT,
          TextConstant.CONTRACT_MAIL_CONTENT), new Queries.RowSetCallback() {
        @Override
        public void onSuccess(BeeRowSet result) {
          String text;

          if (DataUtils.isEmpty(result)) {
            text = TextConstant.CONTRACT_MAIL_CONTENT.getDefaultContent();
          } else if (BeeConst.isUndef(DataUtils.getColumnIndex(localizedContent,
              result.getColumns()))) {
            text = result.getString(0, COL_TEXT_CONTENT);
          } else {
            text = BeeUtils.notEmpty(result.getString(0, localizedContent),
                result.getString(0, COL_TEXT_CONTENT));
          }
          ReportUtils.getReport(REP_CONTRACT, params, null, (fileInfo) ->
              sendMail(ShipmentRequestStatus.CONTRACT_SENT, null, BeeUtils.isEmpty(text)
                      ? null : text.replace("{contract_path}", "rest/transport/confirm/" + id),
                  Collections.singleton(fileInfo)));
        }
      });
    }
  }

  private void sendMail(ShipmentRequestStatus status, String subject, String content,
      Collection<FileInfo> attachments) {

    FormView form = getFormView();
    BeeRow row = DataUtils.cloneRow(form.getActiveRow());

    if (status != null) {
      row.setValue(form.getDataIndex(COL_QUERY_STATUS), status.ordinal());
    }
    BeeRowSet rs = DataUtils.getUpdated(form.getViewName(), form.getDataColumns(), form.getOldRow(),
        row, form.getChildrenForUpdate());

    NewMailMessage.create(BeeUtils.notEmpty(getStringValue(COL_PERSON + COL_EMAIL),
            getStringValue(COL_QUERY_CUSTOMER_EMAIL)), subject, content, attachments,
        (messageId, saveMode) -> {
          DataInfo info = Data.getDataInfo(VIEW_RELATIONS);

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
        });
  }
}
