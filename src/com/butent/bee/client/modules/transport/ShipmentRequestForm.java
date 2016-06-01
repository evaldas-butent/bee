package com.butent.bee.client.modules.transport;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.modules.administration.AdministrationUtils;
import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.modules.mail.NewMailMessage;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.utils.JsonUtils;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.client.validation.CellValidateEvent.Handler;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.i18n.SupportedLocale;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.io.Paths;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.modules.transport.TransportConstants.ShipmentRequestStatus;
import com.butent.bee.shared.modules.transport.TransportConstants.TextConstant;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class ShipmentRequestForm extends CargoPlaceUnboundForm {

  private final Dictionary loc = Localized.dictionary();

  private Button mailCommand = new Button(loc.trWriteEmail(), clickEvent -> onAnswer());

  private Button registerCommand = new Button(loc.register(), clickEvent -> onRegister());

  private Button confirmCommand = new Button(loc.trRequestStatusConfirmed(),
      clickEvent -> onConfirm());

  private Button blockCommand = new Button(loc.ipBlockCommand(), event -> onBlock());

  private Button lostCommand = new Button(loc.trRequestStatusLost(), clickEvent -> onLoss(true));

  private static final String NAME_VALUE_LABEL = "ValueLabel";
  private static final String NAME_INCOTERMS = "Incoterms";

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {
    if (BeeUtils.same(editableWidget.getColumnId(), COL_QUERY_FREIGHT_INSURANCE)) {

      editableWidget.addCellValidationHandler(new Handler() {

        @Override
        public Boolean validateCell(CellValidateEvent event) {
          styleRequiredField(event.getNewValue());

          return true;
        }
      });
    }
    super.afterCreateEditableWidget(editableWidget, widget);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (BeeUtils.same(name, NAME_INCOTERMS) && widget instanceof FaLabel) {
      ((FaLabel) widget).addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

          String locale = Localized.dictionary().languageTag();
          String suffix;

          switch (locale) {
            case "ru":
              suffix = "_ru.png";
              break;

            case "lt":
              suffix = "_lt.png";
              break;

            default:
              suffix = "_en.png";
          }

          Image image = new Image(Paths.buildPath(Paths.IMAGE_DIR, NAME_INCOTERMS + suffix));
          StyleUtils.setWidth(image, BeeKeeper.getScreen().getWidth() * 0.8, CssUnit.PX);
          StyleUtils.setHeight(image, BeeKeeper.getScreen().getHeight() * 0.5, CssUnit.PX);

          Global.showModalWidget(image);
        }
      });
    }

    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    HeaderView header = getHeaderView();

    if (header == null) {
      return;
    }
    header.clearCommandPanel();

    renderOrderId();

    Widget widget = form.getWidgetByName(COL_REGISTRATION_REGISTER);

    if (widget != null) {
      widget.setVisible(!isRegistered());

    }
    widget = form.getWidgetByName(COL_COMPANY_PERSON);

    if (widget != null) {
      widget.setVisible(isRegistered());
    }
    if (DataUtils.isNewRow(row)) {
      return;
    }
    header.setCaption(isRegistered() ? loc.trRequest() : loc.trRequestUnregistered());

    Integer status = row.getInteger(form.getDataIndex(COL_QUERY_STATUS));

    if (!isSelfService() && !ShipmentRequestStatus.LOST.is(status)) {
      header.addCommandItem(mailCommand);

      if (!ShipmentRequestStatus.CONFIRMED.is(status)) {
        if (!isRegistered()) {
          header.addCommandItem(registerCommand);
        } else if (!ShipmentRequestStatus.REJECTED.is(status)) {
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
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action == Action.PRINT) {
      if (!isRegistered() || isSelfService()
          || ShipmentRequestStatus.LOST.is(getIntegerValue(COL_QUERY_STATUS))) {
        return false;
      }
    } else if (action == Action.SAVE) {
      IsRow row = getActiveRow();

      if (BeeUtils.unbox(row.getBoolean(getDataIndex(COL_QUERY_FREIGHT_INSURANCE)))) {
        String value = row.getString(getDataIndex(COL_CARGO_VALUE));
        if (BeeUtils.isEmpty(value)) {
          getFormView().notifySevere(BeeUtils.join(" ", Localized.dictionary().valuation(),
              Localized.dictionary().valueRequired()));
          return false;
        }

        Long currency = row.getLong(getDataIndex(COL_CARGO_VALUE_CURRENCY));
        if (!DataUtils.isId(currency)) {
          getFormView().notifySevere(BeeUtils.join(" ", Localized.dictionary().currency(),
              Localized.dictionary().valueRequired()));
          return false;
        }
      }
    }
    return super.beforeAction(action, presenter);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    if (!DataUtils.isNewRow(row)) {
      Integer status = row.getInteger(form.getDataIndex(COL_QUERY_STATUS));

      form.setEnabled(!ShipmentRequestStatus.LOST.is(status)
          && !ShipmentRequestStatus.CONFIRMED.is(status)
          && (!isSelfService() || ShipmentRequestStatus.NEW.is(status)));
    }
    styleRequiredField(row.getString(getDataIndex(COL_QUERY_FREIGHT_INSURANCE)));

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

      if (!isSelfService()) {
        onLoss(true);
      }
    }
    return super.onStartEdit(form, row, focusCommand);
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    newRow.setValue(form.getDataIndex(COL_COMPANY_PERSON),
        BeeKeeper.getUser().getUserData().getCompanyPerson());

    SelfServiceUtils.setDefaultPerson(form, newRow, COL_COMPANY_PERSON);
    SelfServiceUtils.setDefaultExpeditionType(form, newRow, COL_QUERY_EXPEDITION);
    SelfServiceUtils.setDefaultShippingTerm(form, newRow, COL_SHIPPING_TERM);

    newRow.setValue(form.getDataIndex(COL_USER_LOCALE),
        SupportedLocale.getByLanguage(SupportedLocale.normalizeLanguage(loc.languageTag()))
            .ordinal());

    super.onStartNewRow(form, oldRow, newRow);
  }

  @Override
  protected Consumer<FileInfo> getReportCallback() {
    Consumer<FileInfo> callback = null;

    if (!ShipmentRequestStatus.CONFIRMED.is(getIntegerValue(COL_QUERY_STATUS))) {
      callback = fileInfo -> Queries.getRowSet(VIEW_TEXT_CONSTANTS, null,
          Filter.equals(COL_TEXT_CONSTANT, TextConstant.CONTRACT_MAIL_CONTENT),
          new Queries.RowSetCallback() {
            @Override
            public void onSuccess(BeeRowSet result) {
              String text;
              String localizedContent = Localized.column(COL_TEXT_CONTENT,
                  EnumUtils.getEnumByIndex(SupportedLocale.class, getIntegerValue(COL_USER_LOCALE))
                      .getLanguage());

              if (DataUtils.isEmpty(result)) {
                text = TextConstant.CONTRACT_MAIL_CONTENT.getDefaultContent();
              } else if (BeeConst.isUndef(DataUtils.getColumnIndex(localizedContent,
                  result.getColumns()))) {
                text = result.getString(0, COL_TEXT_CONTENT);
              } else {
                text = BeeUtils.notEmpty(result.getString(0, localizedContent),
                    result.getString(0, COL_TEXT_CONTENT));
              }
              sendMail(ShipmentRequestStatus.CONTRACT_SENT, null, BeeUtils.isEmpty(text)
                  ? null : text.replace("{contract_path}",
                  "rest/transport/confirm/" + getActiveRowId()), Collections.singleton(fileInfo));
            }
          });
    }
    return callback;
  }

  @Override
  protected void getReportData(Consumer<BeeRowSet[]> dataConsumer) {
    SelfServiceUtils.getCargos(Filter.compareId(getLongValue(COL_CARGO)),
        cargoInfo -> dataConsumer.accept(new BeeRowSet[] {cargoInfo}));
  }

  @Override
  protected void getReportParameters(Consumer<Map<String, String>> parametersConsumer) {
    Map<String, Long> companies = new HashMap<>();
    companies.put(COL_CUSTOMER, getLongValue(COL_COMPANY));
    companies.put(COL_COMPANY, BeeKeeper.getUser().getCompany());

    super.getReportParameters(defaultParameters ->
        ClassifierUtils.getCompaniesInfo(companies, companiesInfo -> {
          defaultParameters.putAll(companiesInfo);
          parametersConsumer.accept(defaultParameters);
        }));
  }

  @Override
  protected String[] getReports() {
    String[] reports = super.getReports();

    if (!ArrayUtils.isEmpty(reports) && BeeUtils.unbox(getBooleanValue(COL_EXPEDITION_LOGISTICS))) {
      for (int i = 0; i < reports.length; i++) {
        reports[i] = COL_EXPEDITION_LOGISTICS + reports[i];
      }
    }
    return reports;
  }

  private static void checkOrphans(Map<String, String> relations, Map<String, String> views,
      Map<String, Filter> data, Function<String, String> producer) {

    for (Map.Entry<String, String> entry : relations.entrySet()) {
      String col = entry.getKey();
      String value = producer.apply(col);

      if (!BeeUtils.isEmpty(value)) {
        String viewName = entry.getValue();
        String colName = views.get(viewName);

        Filter flt = Filter.and(Filter.compareWithValue(colName, Operator.STARTS,
            Value.getValue(value)), Filter.compareWithValue(colName, Operator.ENDS,
            Value.getValue(value)));

        if (!data.containsKey(viewName)) {
          data.put(viewName, Filter.or());
        }
        data.put(viewName, ((CompoundFilter) data.get(viewName)).add(flt));
      }
    }
  }

  private void doConfirm(List<String> messages, Runnable onConfirm) {
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
    messages.add(loc.trCommandCreateNewOrder());

    Global.confirm(logistics ? loc.trLogistics() : loc.transport(), Icon.QUESTION, messages, () -> {
      if (logistics && department.isNull()) {
        notifyRequired(loc.department());
        return;
      }
      if (onConfirm != null) {
        onConfirm.run();
      }
      Queries.insert(VIEW_ORDERS, Data.getColumns(VIEW_ORDERS,
          Arrays.asList(COL_CUSTOMER, COL_CUSTOMER + COL_PERSON, COL_ORDER_MANAGER,
              COL_EXPEDITION, COL_SHIPPING_TERM, COL_ORDER_NOTES, COL_VEHICLE, COL_DRIVER,
              COL_ORDER_NO)),
          Arrays.asList(row.getString(form.getDataIndex(COL_COMPANY)),
              row.getString(form.getDataIndex(COL_COMPANY_PERSON)), BeeUtils.toString(manager),
              row.getString(form.getDataIndex(COL_EXPEDITION)),
              row.getString(form.getDataIndex(COL_SHIPPING_TERM)),
              row.getString(form.getDataIndex(COL_ORDER_NOTES)),
              row.getString(form.getDataIndex(COL_VEHICLE)),
              row.getString(form.getDataIndex(COL_DRIVER)),
              "SLF-" + row.getId()),
          null, new RowInsertCallback(VIEW_ORDERS) {
            @Override
            public void onSuccess(BeeRow order) {
              super.onSuccess(order);

              row.setValue(form.getDataIndex(COL_QUERY_STATUS),
                  ShipmentRequestStatus.CONFIRMED.ordinal());

              if (logistics) {
                Long cargo = row.getLong(form.getDataIndex(COL_CARGO));

                Queries.update(VIEW_ORDER_CARGO, cargo, COL_ORDER, Value.getValue(order.getId()),
                    new Queries.IntCallback() {
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
                                    DataUtils.getUpdated(form.getViewName(), form.getDataColumns(),
                                        oldRow, row, null));
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

  private void doRegister(List<String> messages) {
    FormView form = getFormView();
    BeeRow oldRow = DataUtils.cloneRow(form.getOldRow());
    BeeRow row = DataUtils.cloneRow(form.getActiveRow());

    messages.add(loc.trCommandCreateNewUser());

    String login = row.getString(form.getDataIndex(COL_QUERY_CUSTOMER_EMAIL));
    String password;

    if (BeeUtils.unbox(row.getBoolean(form.getDataIndex("Customer" + COL_REGISTRATION_REGISTER)))
        && !BeeUtils.isEmpty(login)) {
      password = BeeUtils.randomString(6);

      messages.add("Login: " + login);
      messages.add("Password: " + password);
    } else {
      password = null;
    }
    Global.confirm(loc.register(), Icon.QUESTION, messages,
        Localized.dictionary().actionCreate(), Localized.dictionary().actionCancel(), () -> {
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
            personInfo.put(COL_PHONE, row.getString(form.getDataIndex(COL_QUERY_CUSTOMER_PHONE)));
            personInfo.put(ALS_EMAIL_ID, login);
            personInfo.put(COL_POSITION,
                row.getString(form.getDataIndex(COL_QUERY_CUSTOMER_CONTACT_POSITION)));

            ClassifierUtils.createCompanyPerson(personInfo, (person) -> {
              row.setValue(form.getDataIndex(COL_COMPANY_PERSON), person);

              if (BeeUtils.isEmpty(row.getString(form.getDataIndex(COL_QUERY_MANAGER)))) {
                row.setValue(form.getDataIndex(COL_QUERY_MANAGER), BeeKeeper.getUser().getUserId());
              }
              if (!BeeUtils.isEmpty(password)) {
                ParameterList args = TransportHandler.createArgs(SVC_CREATE_USER);
                args.addDataItem(COL_LOGIN, login);
                args.addDataItem(COL_PASSWORD, password);
                args.addDataItem(COL_COMPANY_PERSON, person);
                args.addDataItem(COL_USER_LOCALE,
                    row.getInteger(form.getDataIndex(COL_USER_LOCALE)));
                args.addNotEmptyData(COL_EMAIL, login);

                BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
                  @Override
                  public void onResponse(ResponseObject response) {
                    response.notify(form);

                    if (!response.hasErrors()) {
                      SelfServiceUtils.update(form, DataUtils.getUpdated(form.getViewName(),
                          form.getDataColumns(), oldRow, row, null));
                    }
                  }
                });
              } else {
                SelfServiceUtils.update(form, DataUtils.getUpdated(form.getViewName(),
                    form.getDataColumns(), oldRow, row, null));
              }
            });
          });
        });
  }

  private boolean isRegistered() {
    return DataUtils.isId(getLongValue(COL_COMPANY_PERSON));
  }

  private static boolean isSelfService() {
    return Objects.equals(BeeKeeper.getScreen().getUserInterface(), UserInterface.SELF_SERVICE);
  }

  private void styleRequiredField(String value) {
    getFormView().getWidgetByName(NAME_VALUE_LABEL).setStyleName(StyleUtils.NAME_REQUIRED,
        !BeeUtils.isEmpty(value));
  }

  private void onAnswer() {
    sendMail(ShipmentRequestStatus.NEW.is(getIntegerValue(COL_QUERY_STATUS))
        ? ShipmentRequestStatus.ANSWERED : null, null, null, null);
  }

  private void onBlock() {
    AdministrationUtils.blockHost(loc.ipBlockCommand(), getStringValue(COL_QUERY_HOST),
        getFormView(), result -> onLoss(false));
  }

  private void onConfirm() {
    Map<String, String> views = new HashMap<>();
    views.put(VIEW_CITIES, COL_CITY_NAME);
    views.put(VIEW_COUNTRIES, COL_COUNTRY_NAME);

    Map<String, String> relations = new HashMap<>();
    relations.put(VAR_LOADING + COL_CITY, VIEW_CITIES);
    relations.put(VAR_LOADING + COL_COUNTRY, VIEW_COUNTRIES);
    relations.put(VAR_UNLOADING + COL_CITY, VIEW_CITIES);
    relations.put(VAR_UNLOADING + COL_COUNTRY, VIEW_COUNTRIES);

    Map<String, Filter> data = new HashMap<>();
    Multimap<Pair<String, Long>, Pair<String, Object>> updates = HashMultimap.create();

    checkOrphans(relations, views, data, col -> {
      UnboundSelector widget = getUnboundWidget(col);
      String value = null;

      if (widget != null) {
        value = widget.getValue();

        if (!BeeUtils.isEmpty(value)) {
          updates.put(Pair.of(getViewName(), getActiveRowId()), Pair.of(col, value));
        }
      }
      return value;
    });
    Widget grid = getFormView().getWidgetByName(TBL_CARGO_HANDLING);

    if (grid != null && grid instanceof ChildGrid) {
      for (IsRow row : ((ChildGrid) grid).getGridView().getRowData()) {
        String jsonString = row.getString(Data.getColumnIndex(TBL_CARGO_HANDLING,
            ALS_CARGO_HANDLING_NOTES));

        if (!BeeUtils.isEmpty(jsonString)) {
          JSONObject json = JsonUtils.parseObject(jsonString);

          checkOrphans(relations, views, data, col -> {
            String value = JsonUtils.getString(json, col);

            if (!BeeUtils.isEmpty(value)) {
              updates.put(Pair.of(TBL_CARGO_HANDLING, row.getId()), Pair.of(col, value));
            }
            return value;
          });
        }
      }
    }
    List<String> messages = new ArrayList<>();

    if (!BeeUtils.isEmpty(data)) {
      Queries.getData(data.keySet(), data, CachingPolicy.NONE, new Queries.DataCallback() {
        @Override
        public void onSuccess(Collection<BeeRowSet> result) {
          for (BeeRowSet rs : result) {
            for (Pair<String, Object> pair : updates.values()) {
              if (!BeeUtils.same(relations.get(pair.getA()), rs.getViewName())) {
                continue;
              }
              for (int i = 0; i < rs.getNumberOfRows(); i++) {
                if (BeeUtils.same(rs.getString(i, views.get(rs.getViewName())),
                    (String) pair.getB())) {
                  pair.setB(rs.getRow(i).getId());
                  break;
                }
              }
            }
          }
          Map<String, Map<String, Object>> missing = new HashMap<>();

          for (Pair<String, Long> key : updates.keySet()) {
            for (Pair<String, Object> pair : updates.get(key)) {
              String col = pair.getA();
              Object value = pair.getB();
              String relation = relations.get(col);

              if (value instanceof String) {
                if (!missing.containsKey(relation)) {
                  missing.put(relation, new HashMap<>());
                }
                missing.get(relation).put((String) value, null);
                messages.add(BeeUtils.join(": ",
                    Data.getColumnLabel(getViewName(), col), value));

                if (BeeUtils.same(relation, VIEW_CITIES)) {
                  for (Pair<String, Object> x : updates.get(key)) {
                    if (BeeUtils.same(x.getA(), col.replace(COL_CITY, COL_COUNTRY))) {
                      missing.get(relation).put((String) value, x.getB());
                      break;
                    }
                  }
                }
              }
            }
          }
          Runnable onSuccess = () -> {
            for (Pair<String, Long> key : updates.keySet()) {
              List<String> columns = new ArrayList<>();
              List<String> values = new ArrayList<>();

              for (Pair<String, Object> pair : updates.get(key)) {
                String col = pair.getA();
                Object value = pair.getB();

                columns.add(col);
                values.add((value instanceof String
                    ? missing.get(relations.get(col)).get(value) : value).toString());
              }
              Queries.update(key.getA(), Filter.compareId(key.getB()), columns, values, null);
            }
          };
          Runnable onConfirm;

          if (!BeeUtils.isEmpty(missing)) {
            List<Runnable> queue = new ArrayList<>();

            onConfirm = () -> {
              Runnable startup = null;

              for (String relation : missing.keySet()) {
                Runnable runnable = () -> {
                  Holder<Integer> cnt = Holder.of(missing.get(relation).size());

                  for (Map.Entry<String, Object> entry : missing.get(relation).entrySet()) {
                    List<BeeColumn> cols = new ArrayList<>();
                    List<String> vals = new ArrayList<>();

                    cols.add(Data.getColumn(relation, views.get(relation)));
                    vals.add(entry.getKey());

                    if (BeeUtils.same(relation, VIEW_CITIES)) {
                      Object val = entry.getValue();

                      if (val instanceof String) {
                        val = missing.get(VIEW_COUNTRIES).get(val);
                      }
                      cols.add(Data.getColumn(relation, COL_COUNTRY));
                      vals.add(val.toString());
                    }
                    Queries.insert(relation, cols, vals, null, new RowInsertCallback(relation) {
                      @Override
                      public void onSuccess(BeeRow r) {
                        super.onSuccess(r);
                        entry.setValue(r.getId());
                        cnt.set(cnt.get() - 1);

                        if (!BeeUtils.isPositive(cnt.get())) {
                          if (BeeUtils.isEmpty(queue)) {
                            onSuccess.run();
                          } else {
                            queue.remove(0).run();
                          }
                        }
                      }
                    });
                  }
                };
                if (BeeUtils.same(relation, VIEW_COUNTRIES)) {
                  startup = runnable;
                } else {
                  queue.add(runnable);
                }
              }
              (startup == null ? queue.remove(0) : startup).run();
            };
            messages.add(0, loc.errors());
          } else {
            onConfirm = onSuccess;
          }
          doConfirm(messages, onConfirm);
        }
      });
    } else {
      doConfirm(messages, null);
    }
  }

  private void onLoss(boolean required) {
    InputArea comment = new InputArea();
    comment.setWidth("100%");
    comment.setVisibleLines(4);

    UnboundSelector reason = UnboundSelector.create(TBL_LOSS_REASONS,
        Collections.singletonList(COL_LOSS_REASON_NAME));

    reason.addSelectorHandler(event -> {
      if (event.isChanged()) {
        reason.setOptions(event.getRelatedRow() != null
            ? Data.getString(event.getRelatedViewName(), event.getRelatedRow(),
            COL_LOSS_REASON_TEMPLATE) : null);
        comment.setValue(reason.getOptions());
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

  private void onRegister() {
    Map<String, String> views = new HashMap<>();
    views.put(VIEW_CITIES, COL_CITY_NAME);
    views.put(VIEW_COUNTRIES, COL_COUNTRY_NAME);
    views.put(VIEW_COMPANY_TYPES, COL_COMPANY_TYPE_NAME);
    views.put(VIEW_POSITIONS, COL_POSITION_NAME);

    Map<String, String> relations = new HashMap<>();
    relations.put("Customer" + COL_COUNTRY, VIEW_COUNTRIES);
    relations.put("Customer" + COL_CITY, VIEW_CITIES);
    relations.put("Customer" + COL_COMPANY_TYPE, VIEW_COMPANY_TYPES);
    relations.put(COL_QUERY_CUSTOMER_CONTACT_POSITION, VIEW_POSITIONS);

    Map<String, Pair<String, String>> values = new HashMap<>();
    Map<String, Filter> data = new HashMap<>();

    checkOrphans(relations, views, data, (col) -> {
      Widget widget = getFormView().getWidgetBySource(col);
      String value = null;

      if (widget != null && widget instanceof Editor) {
        value = ((Editor) widget).getValue();

        if (!BeeUtils.isEmpty(value)) {
          values.put(relations.get(col), Pair.of(col, value));
        }
      }
      return value;
    });
    List<String> messages = new ArrayList<>();

    if (!BeeUtils.isEmpty(data)) {
      Queries.getData(data.keySet(), data, CachingPolicy.NONE, new Queries.DataCallback() {
        @Override
        public void onSuccess(Collection<BeeRowSet> result) {
          for (BeeRowSet rs : result) {
            if (DataUtils.isEmpty(rs)) {
              Pair<String, String> pair = values.get(rs.getViewName());
              messages.add(BeeUtils.join(": ", Data.getColumnLabel(getViewName(), pair.getA()),
                  pair.getB()));
            }
          }
          if (!BeeUtils.isEmpty(messages)) {
            messages.add(0, loc.errors());
          }
          doRegister(messages);
        }
      });
    } else {
      doRegister(messages);
    }
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
