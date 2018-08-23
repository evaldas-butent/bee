package com.butent.bee.client.modules.service;

import com.butent.bee.client.widget.FaLabel;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.service.ServiceConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.COL_TA_OBJECT;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.composite.Autocomplete;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.composite.Relations;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.RowActionEvent;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.Latch;
import com.butent.bee.shared.css.values.FontWeight;
import com.butent.bee.shared.css.values.Overflow;
import com.butent.bee.shared.css.values.TextAlign;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class ServiceObjectForm extends MaintenanceExpanderForm implements ClickHandler,
    RowActionEvent.Handler, SelectorEvent.Handler, RowUpdateEvent.Handler, DataChangeEvent.Handler {

  private final GridInterceptor childInterceptor = new AbstractGridInterceptor() {

    @Override
    public GridInterceptor getInstance() {
      return null;
    }
  };

  private HasWidgets criteriaPanel;

  private final Map<String, String> criteriaHistory = new LinkedHashMap<>();
  private final Map<String, Editor> criteriaEditors = new LinkedHashMap<>();
  private final List<Long> criteriaList = new ArrayList<>();

  private final Map<String, Long> criteriaIds = new HashMap<>();

  private ChildGrid groupsGrid;
  private ChildGrid criteriaGrid;

  private DataSelector itemSelector;

  private Relations relations;

  private final List<HandlerRegistration> registry = new ArrayList<>();

  ServiceObjectForm() {
    super();
  }

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {
    if (widget instanceof DataSelector && editableWidget.hasSource(COL_TA_OBJECT)) {
      ((DataSelector) widget).addSelectorHandler(event -> {
        if (event.isOpened()) {
          String columnName = event.getSelector().getOptions();

          if (!BeeUtils.isEmpty(columnName)) {
            Long company = getLongValue(columnName);

            Filter filter;
            if (company == null) {
              filter = null;
            } else {
              filter = Filter.equals(COL_COMPANY, company);
            }
            event.getSelector().setAdditionalFilter(filter, filter != null);
          }
        }
      });
    } else if (widget instanceof DataSelector && editableWidget.hasSource(COL_ITEM)) {
      itemSelector = (DataSelector) widget;
    }
    super.afterCreateEditableWidget(editableWidget, widget);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    super.afterCreateWidget(name, widget, callback);

    if (BeeUtils.same(name, "MainCriteriaEditor")) {
      widget.asWidget().addDomHandler(this, ClickEvent.getType());

    } else if (widget instanceof HasWidgets && BeeUtils.same(name, "MainCriteriaContainer")) {
      criteriaPanel = (HasWidgets) widget;

    } else if (widget instanceof ChildGrid) {
      ChildGrid grid = (ChildGrid) widget;

      if (BeeUtils.same(name, VIEW_SERVICE_CRITERIA_GROUPS)) {
        groupsGrid = grid;
        grid.setGridInterceptor(childInterceptor);

      } else if (BeeUtils.same(name, VIEW_SERVICE_CRITERIA)) {
        criteriaGrid = grid;
        grid.setGridInterceptor(childInterceptor);

      } else if (BeeUtils.same(name, TBL_SERVICE_MAINTENANCE)) {
        grid.setGridInterceptor(new AbstractGridInterceptor() {
          @Override
          public GridInterceptor getInstance() {
            return null;
          }

          @Override
          public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
            FormView parentForm = ViewHelper.getForm(presenter.getMainView());
            if (parentForm != null && parentForm.getActiveRow() != null) {
              presenter.getGridView().ensureRelId(result -> {
                DataInfo objectDataInfo = Data.getDataInfo(parentForm.getViewName());
                DataInfo maintenanceDataInfo = Data.getDataInfo(getViewName());
                IsRow objectRow = parentForm.getActiveRow();

                BeeRow newRow = RowFactory.createEmptyRow(maintenanceDataInfo, true);
                newRow.setValue(maintenanceDataInfo.getColumnIndex(COL_SERVICE_OBJECT), result);
                newRow.setValue(maintenanceDataInfo.getColumnIndex(ALS_SERVICE_CATEGORY_NAME),
                    objectRow.getString(objectDataInfo.getColumnIndex(ALS_SERVICE_CATEGORY_NAME)));
                newRow.setValue(maintenanceDataInfo.getColumnIndex(COL_MODEL),
                    objectRow.getString(objectDataInfo.getColumnIndex(COL_MODEL)));
                newRow.setValue(maintenanceDataInfo.getColumnIndex(COL_SERIAL_NO),
                    objectRow.getString(objectDataInfo.getColumnIndex(COL_SERIAL_NO)));
                newRow.setValue(maintenanceDataInfo.getColumnIndex(COL_ARTICLE_NO),
                    objectRow.getString(objectDataInfo.getColumnIndex(COL_ARTICLE_NO)));

                ServiceUtils.fillContactValues(newRow, objectRow);
                ServiceUtils.fillCompanyValues(newRow, objectRow, parentForm.getViewName(),
                    COL_SERVICE_CUSTOMER, ALS_SERVICE_CUSTOMER_NAME, ALS_CUSTOMER_TYPE_NAME);
                ServiceUtils.fillContractorAndManufacturerValues(newRow, objectRow);

                RowFactory.createRow(maintenanceDataInfo, newRow, Opener.MODAL);
              });
              return false;
            } else {
              return true;
            }
          }
        });
      }
    } else if (widget instanceof DataSelector
        && BeeUtils.in(name, COL_SERVICE_CUSTOMER, ALS_CONTACT_PERSON)) {
      ((DataSelector) widget).addSelectorHandler(this);
    } else if (widget instanceof Relations) {
      relations = (Relations) widget;
    } else if (widget instanceof FaLabel && BeeUtils.same(name, "NewItem")) {
        ((FaLabel) widget).addClickHandler(clickEvent -> fillNewItemWithValues());
    }
  }

  @Override
  public void afterInsertRow(IsRow result, boolean forced) {
    if (!forced) {
      save(result);
    }
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    ServiceHelper.setGridEnabled(form, TBL_SERVICE_ITEMS, false);
  }

  @Override
  public void afterUpdateRow(IsRow result) {
    save(result);

    ParameterList params = ServiceKeeper.createArgs(SVC_UPDATE_SERVICE_MAINTENANCE_OBJECT);
    params.addDataItem(COL_SERVICE_OBJECT, result.getId());
    BeeKeeper.getRpc().makePostRequest(params, response -> {
      if (!response.isEmpty() && !response.hasErrors()) {
        Queries.getRow(TBL_SERVICE_MAINTENANCE, response.getResponseAsLong(), result1 -> {
          if (result1 != null) {
            RowUpdateEvent.fire(BeeKeeper.getBus(), TBL_SERVICE_MAINTENANCE, result1);
          }
        });
      }
    });
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    MultiSelector objectSelector = relations.getMultiSelector("CompanyObject");
    if (objectSelector != null) {
      Filter filter =
          Filter.equals(COL_COMPANY, Data.getLong(VIEW_SERVICE_OBJECTS, row, COL_SERVICE_CUSTOMER));
      objectSelector.setAdditionalFilter(filter);
    }

    super.beforeRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new ServiceObjectForm();
  }

  @Override
  public void onClick(ClickEvent event) {
    renderMainCriteria();
  }

  @Override
  public void onClose(List<String> messages, IsRow oldRow, IsRow newRow) {
    if (save(null)) {
      if (messages.size() == 1) {
        String msg = BeeUtils.joinItems(messages.get(0), Localized.dictionary().mainCriteria());
        messages.clear();
        messages.add(msg);

      } else {
        messages.add(BeeUtils.joinWords(Localized.dictionary().changedValues(),
            Localized.dictionary().mainCriteria()));
      }
    }
  }

  @Override
  public void onDataChange(DataChangeEvent event) {
    if (event.hasView(VIEW_COMPANY_PERSONS)
        && BeeUtils.isTrue(getActiveRow().getBoolean(getDataIndex(COL_COMPANY_TYPE_PERSON)))) {
      Long companyId = getActiveRow().getLong(getDataIndex(COL_SERVICE_CUSTOMER));
      Queries.getRow(VIEW_COMPANY_PERSONS, Filter.equals(COL_COMPANY, companyId), null,
          companyPersonRow -> {
            if (companyPersonRow != null && DataUtils.isId(companyPersonRow.getId())) {
              DataInfo targetDataInfo = Data.getDataInfo(getViewName());
              DataInfo sourceDataInfo = Data.getDataInfo(VIEW_COMPANY_PERSONS);

              getActiveRow().setValue(targetDataInfo.getColumnIndex(ALS_CONTACT_PERSON),
                  companyPersonRow.getId());
              RelationUtils.maybeUpdateColumn(targetDataInfo, ALS_CONTACT_PHONE, getActiveRow(),
                  sourceDataInfo, COL_PHONE, companyPersonRow);
              RelationUtils.maybeUpdateColumn(targetDataInfo, ALS_CONTACT_FIRST_NAME,
                  getActiveRow(), sourceDataInfo, COL_FIRST_NAME, companyPersonRow);
              RelationUtils.maybeUpdateColumn(targetDataInfo, ALS_CONTACT_LAST_NAME,
                  getActiveRow(), sourceDataInfo, COL_LAST_NAME, companyPersonRow);
              RelationUtils.maybeUpdateColumn(targetDataInfo, ALS_CONTACT_EMAIL, getActiveRow(),
                  sourceDataInfo, COL_EMAIL, companyPersonRow);
              RelationUtils.maybeUpdateColumn(targetDataInfo, ALS_CONTACT_ADDRESS, getActiveRow(),
                  sourceDataInfo, COL_ADDRESS, companyPersonRow);

              getFormView().refreshBySource(ALS_CONTACT_PERSON);
            }
          });
    }
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (event.isChanged()
        && BeeUtils.same(event.getRelatedViewName(), VIEW_COMPANIES)) {
      getActiveRow().clearCell(Data.getColumnIndex(getViewName(), ALS_CONTACT_PERSON));
      getActiveRow().clearCell(Data.getColumnIndex(getViewName(), ALS_CONTACT_FIRST_NAME));
      getActiveRow().clearCell(Data.getColumnIndex(getViewName(), ALS_CONTACT_LAST_NAME));
      getActiveRow().clearCell(Data.getColumnIndex(getViewName(), ALS_CONTACT_PHONE));
      getActiveRow().clearCell(Data.getColumnIndex(getViewName(), ALS_CONTACT_ADDRESS));
      getActiveRow().clearCell(Data.getColumnIndex(getViewName(), ALS_CONTACT_EMAIL));
      getFormView().refreshBySource(ALS_CONTACT_PERSON);

    } else if (event.isChanged()
        && BeeUtils.same(event.getRelatedViewName(), VIEW_COMPANY_PERSONS)) {
      Long objectCustomerId = getActiveRow().getLong(getDataIndex(COL_SERVICE_CUSTOMER));

      if (event.getRelatedRow() != null && !DataUtils.isId(objectCustomerId)) {
        Long company = event.getRelatedRow().getLong(Data
            .getColumnIndex(event.getRelatedViewName(), COL_COMPANY));

        if (DataUtils.isId(company)) {
          getActiveRow().setValue(getDataIndex(COL_SERVICE_CUSTOMER), company);
          RelationUtils.updateRow(Data.getDataInfo(getViewName()), COL_SERVICE_CUSTOMER,
              getActiveRow(), Data.getDataInfo(event.getRelatedViewName()),
              event.getRelatedRow(), false);
          getFormView().refreshBySource(COL_SERVICE_CUSTOMER);
        }
      }
    }
  }

  @Override
  public void onLoad(FormView form) {
    EventUtils.clearRegistry(registry);
    registry.add(BeeKeeper.getBus().registerRowActionHandler(this));
    registry.add(BeeKeeper.getBus().registerRowUpdateHandler(this, false));
    registry.add(BeeKeeper.getBus().registerDataChangeHandler(this, false));
  }

  @Override
  public void onRowAction(RowActionEvent event) {
    if (event != null && event.isCreateRow() && event.hasRow()
        && event.hasAnyView(TaskConstants.VIEW_TASKS, TaskConstants.VIEW_RECURRING_TASKS)
        && DomUtils.isOrHasChild(getFormView().asWidget(), event.getOptions())
        && getActiveRow() != null) {

      String address = getStringValue(COL_SERVICE_ADDRESS);

      if (!BeeUtils.isEmpty(address)) {
        Data.setValue(event.getViewName(), event.getRow(), TaskConstants.COL_SUMMARY, address);
      }

      Long customer = getLongValue(COL_SERVICE_CUSTOMER);

      if (DataUtils.isId(customer)) {
        RelationUtils.copyWithDescendants(
            Data.getDataInfo(getViewName()), COL_SERVICE_CUSTOMER, getActiveRow(),
            Data.getDataInfo(event.getViewName()), COL_COMPANY, event.getRow());
      }
    }
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    if (getFormView() == null) {
      return;
    }

    if (getActiveRow() == null) {
      return;
    }

    FormView form = getFormView();

    if (event.hasView(getViewName()) && Objects.equals(event.getRowId(), getActiveRowId())) {
      form.updateRow(event.getRow(), true);
    }
  }

  @Override
  public void onSaveChanges(HasHandlers listener, SaveChangesEvent event) {
    if (event.isEmpty()) {
      save(getActiveRow());
    }
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, ScheduledCommand focusCommand) {
    requery(row);
    return true;
  }

  @Override
  public void onStartNewRow(FormView form, IsRow newRow) {
    requery(newRow);
  }

  @Override
  public void onUnload(FormView form) {
    EventUtils.clearRegistry(registry);
  }

  private static Autocomplete createAutocomplete(String viewName, String column) {

    return Autocomplete.create(Relation.create(viewName,
        Lists.newArrayList(column)), true);
  }

  private void fillNewItemWithValues() {

    FormView form = getFormView();
    String category = form.getStringValue(ALS_SERVICE_CATEGORY_NAME);

    Queries.getRowSet(VIEW_ITEM_CATEGORY_TREE, null, Filter.equals(COL_CATEGORY_NAME, category), null, result -> {
      if (result.isEmpty()) {
        form.notifySevere("Kategorija \"" + category + "\" nerasta prekių kategorijų medyje");
      } else {
        BeeRow newRow = RowFactory.createEmptyRow(Data.getDataInfo(VIEW_ITEMS));

        String itemName = BeeUtils.joinItems(form.getStringValue(COL_MODEL),
          form.getStringValue(COL_SERVICE_BODY_NO));
        String itemInButenta = form.getStringValue("ItemInButenta");
        String address = form.getStringValue(COL_ADDRESS);

        Data.setValue(VIEW_ITEMS, newRow, COL_ITEM_NAME, itemName);
        Data.setValue(VIEW_ITEMS, newRow, COL_ITEM_EXTERNAL_CODE, itemInButenta);
        Data.setValue(VIEW_ITEMS, newRow, COL_ITEM_ARTICLE, address);

        for (Entry<String, Editor> entry : criteriaEditors.entrySet()) {

          Editor editor = entry.getValue();

          if (editor != null) {
            String value = editor.getNormalizedValue();

            if (entry.getKey().trim().toLowerCase().equals("svoris") && BeeUtils.isDouble(value.trim())) {
              Data.setValue(VIEW_ITEMS, newRow, COL_ITEM_WEIGHT, BeeUtils.toDouble(value.trim()));
            } else if (entry.getKey().trim().toLowerCase().equals("nuomos kaina") && BeeUtils.isDouble(value.trim())) {
              Data.setValue(VIEW_ITEMS, newRow, COL_ITEM_RENTAL_PRICE, BeeUtils.toDouble(value.trim()));
            } else if (entry.getKey().trim().toLowerCase().equals("pardavimo kaina") && BeeUtils.isDouble(value.trim())) {
              Data.setValue(VIEW_ITEMS, newRow, COL_ITEM_PRICE, BeeUtils.toDouble(value.trim()));
            }
          }
        }

        newRow.setProperty(COL_CATEGORY, result.getRow(0).getId());

        RowFactory.createRelatedRow(COL_ITEM, newRow, itemSelector, Opener.MODAL);
      }
    });
  }

  private void render() {
    if (criteriaPanel == null) {
      return;
    }
    criteriaPanel.clear();

    if (criteriaEditors.size() > 0) {
      int h = 0;
      FlowPanel labelContainer = new FlowPanel();
      labelContainer.getElement().getStyle().setMarginRight(5, Unit.PX);
      criteriaPanel.add(labelContainer);

      FlowPanel inputContainer = new FlowPanel();
      inputContainer.addStyleName(StyleUtils.NAME_FLEXIBLE);
      criteriaPanel.add(inputContainer);

      for (Entry<String, Editor> entry : criteriaEditors.entrySet()) {
        Label label = new Label(entry.getKey());
        StyleUtils.setTextAlign(label.getElement(), TextAlign.RIGHT);
        SimplePanel labelDiv = new SimplePanel(label);
        labelContainer.add(labelDiv);

        Widget editor = entry.getValue().asWidget();
        StyleUtils.fullWidth(editor);
        SimplePanel editorDiv = new SimplePanel(editor);
        inputContainer.add(editorDiv);

        if (!BeeUtils.isPositive(h)) {
          h = BeeUtils.max(labelDiv.getElement().getClientHeight(),
              editorDiv.getElement().getClientHeight());

          if (!BeeUtils.isPositive(h)) {
            h = 20;
          }
          h += 5;
        }

        StyleUtils.setHeight(labelDiv, h);
        StyleUtils.setHeight(editorDiv, h);
      }
    }
  }

  private void renderMainCriteria() {

    Queries.getRowSet(VIEW_SERVICE_MAIN_CRITERIA, null, null, result -> {

      if (result.isEmpty()) {
        return;
      }

      Dictionary d = Localized.dictionary();
      Latch rNo = new Latch(0);

      HtmlTable table = new HtmlTable(StyleUtils.NAME_INFO_TABLE);
      table.getRowFormatter().addStyleName(rNo.get(), StyleUtils.className(FontWeight.BOLD));
      table.getRowFormatter().addStyleName(rNo.get(), StyleUtils.className(TextAlign.CENTER));

      CheckBox checkAll = new CheckBox();
      Map<Long, CheckBox> checkBoxMap = new HashMap<>();

      checkAll.addValueChangeHandler(valueChangeEvent -> {
        for (CheckBox cb : checkBoxMap.values()) {
          cb.setValue(valueChangeEvent.getValue());
        }
      });

      int c = 0;
      table.setWidget(rNo.get(), c++, checkAll);
      table.setText(rNo.get(), c++, d.captionId());
      table.setText(rNo.get(), c++, d.ordinal());
      table.setText(rNo.get(), c++, d.criterionName());

      for (BeeRow row : result) {
        rNo.increment();
        int cNo = 0;

        CheckBox cb = new CheckBox();

        cb.setValue(criteriaList.contains(row.getId()));
        checkBoxMap.put(row.getId(), cb);

        table.setWidget(rNo.get(), cNo++, cb);
        table.setText(rNo.get(), cNo++, BeeUtils.toString(row.getId()));
        table.setText(rNo.get(), cNo++, row.getString(0));
        table.setText(rNo.get(), cNo++, row.getString(1));
      }

      Flow container = new Flow();
      StyleUtils.setHeight(container, 400);
      StyleUtils.setOverflow(container, StyleUtils.ScrollBars.VERTICAL, Overflow.AUTO);
      container.add(table);

      Global.inputWidget(d.mainCriteria(), container, () -> {

        BeeRowSet rowSet = new BeeRowSet(VIEW_SERVICE_OBJECT_MAIN_CRITERIA,
            Data.getColumns(VIEW_SERVICE_OBJECT_MAIN_CRITERIA,
                Arrays.asList(COL_SERVICE_OBJECT, COL_SERVICE_MAIN_CRITERIA)));

        List<Long> delete = new ArrayList<>();

        int actions = 0;

        for (Entry<Long, CheckBox> entry : checkBoxMap.entrySet()) {
          if (entry.getValue().isChecked() && !criteriaList.contains(entry.getKey())) {
            BeeRow row = rowSet.addEmptyRow();
            row.setValue(0, getActiveRowId());
            row.setValue(1, entry.getKey());

          } else if (!entry.getValue().isChecked() && criteriaList.contains(entry.getKey())) {
            delete.add(entry.getKey());
          }
        }

        if (delete.size() > 0) {
          actions++;
        }
        if (rowSet.getNumberOfRows() > 0) {
          actions++;
        }

        insertDeleteData(rowSet, delete, actions);
      });
    });
  }

  private void insertDeleteData(BeeRowSet rowSet, List<Long> delete, int actions) {
    if (actions == 0) {
      return;
    }

    Runnable runnable = new Runnable() {
      int index;

      @Override
      public void run() {
        if (Objects.equals(actions, ++index)) {
          requery(getActiveRow());
        }
      }
    };

    if (delete.size() > 0) {
      Queries.delete(VIEW_SERVICE_OBJECT_MAIN_CRITERIA,
          Filter.and(Filter.equals(COL_SERVICE_OBJECT, getActiveRowId()),
              Filter.any(COL_SERVICE_MAIN_CRITERIA, delete)), result -> runnable.run());
    }

    if (rowSet.getNumberOfRows() > 0) {
      Queries.insertRows(rowSet, result -> runnable.run());
    }
  }

  private void requery(IsRow row) {
    criteriaHistory.clear();
    criteriaEditors.clear();
    criteriaIds.clear();
    criteriaList.clear();

    render();

    Long objId = row.getId();
    if (!DataUtils.isId(objId)) {
      return;
    }

    Filter filter = Filter.equals(COL_SERVICE_OBJECT, objId);

    Queries.getRowSet(VIEW_SERVICE_OBJECT_MAIN_CRITERIA, null, filter, result -> {
      if (result.getNumberOfRows() > 0) {

        for (BeeRow crit : result) {
          String name = DataUtils.getString(result, crit, COL_SERVICE_MAIN_CRITERIA + "Name");

          if (!BeeUtils.isEmpty(name)) {
            String value = DataUtils.getString(result, crit, COL_SERVICE_CRITERION_VALUE);

            criteriaList.add(DataUtils.getLong(result, crit, COL_SERVICE_MAIN_CRITERIA));

            Autocomplete box = createAutocomplete(VIEW_SERVICE_OBJECT_MAIN_CRITERIA,
                COL_SERVICE_CRITERION_VALUE);

            box.setAdditionalFilter(Filter.equals(COL_SERVICE_MAIN_CRITERIA,
                DataUtils.getLong(result, crit, COL_SERVICE_MAIN_CRITERIA)));

            box.setValue(value);

            criteriaHistory.put(name, value);
            criteriaEditors.put(name, box);

            criteriaIds.put(name, crit.getId());
          }
        }

        render();
      }
    });
  }

  private boolean save(final IsRow row) {
    Map<Long, String> changedValues = new HashMap<>();

    final Holder<Integer> holder = Holder.of(0);

    for (String crit : criteriaEditors.keySet()) {
      String value = criteriaEditors.get(crit).getValue();
      value = BeeUtils.isEmpty(value) ? null : value;
      Long id = criteriaIds.get(crit);

      if (!criteriaHistory.containsKey(crit) || !Objects.equals(value, criteriaHistory.get(crit))) {
        if (DataUtils.isId(id)) {
          changedValues.put(id, value);
        }
        holder.set(holder.get() + 1);
      }
    }

    if (row == null) {
      return BeeUtils.isPositive(holder.get());
    }

    final ScheduledCommand scheduler = new ScheduledCommand() {
      @Override
      public void execute() {
        holder.set(holder.get() - 1);

        if (!BeeUtils.isPositive(holder.get())) {
          Queries.getRow(getViewName(), row.getId(), new RowUpdateCallback(getViewName()) {
            @Override
            public void onSuccess(BeeRow result) {
              super.onSuccess(result);

              GridView gridView = getGridView();
              if (gridView != null) {
                gridView.getGrid().refresh();
              }
            }
          });
        }
      }
    };

    if (!BeeUtils.isEmpty(changedValues)) {
      for (Entry<Long, String> entry : changedValues.entrySet()) {
        Queries.update(VIEW_SERVICE_OBJECT_MAIN_CRITERIA, Filter.compareId(entry.getKey()),
            COL_SERVICE_CRITERION_VALUE, new TextValue(entry.getValue()),
            result -> scheduler.execute());
      }
    }

    return true;
  }
}
