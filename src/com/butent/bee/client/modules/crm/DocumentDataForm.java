package com.butent.bee.client.modules.crm;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.HasWidgets;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.Autocomplete;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.event.logical.AutocompleteEvent;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class DocumentDataForm extends AbstractFormInterceptor implements ClickHandler {

  private final class AutocompleteFilter implements AutocompleteEvent.Handler {

    private final String source;
    private final String criterion;

    private AutocompleteFilter(String source, String criterion) {
      this.source = source;
      this.criterion = criterion;
    }

    @Override
    public void onDataSelector(AutocompleteEvent event) {
      if (event.getState() == State.OPEN) {
        CompoundFilter flt = Filter.and();

        for (String name : new String[] {COL_DOCUMENT_CATEGORY, COL_DOCUMENT_DATA}) {
          Long id = getDataLong(name);

          if (DataUtils.isId(id)) {
            if (BeeUtils.same(name, COL_DOCUMENT_CATEGORY)) {
              flt.add(Filter.isEqual(name, Value.getValue(id)));
            } else {
              flt.add(Filter.isNotEqual(name, Value.getValue(id)));
            }
          }
        }
        if (BeeUtils.isEmpty(source)) {
          flt.add(Filter.isNull(COL_CRITERIA_GROUP_NAME));

          if (!BeeUtils.isEmpty(criterion)) {
            flt.add(Filter.isEqual(COL_CRITERION_NAME, Value.getValue(criterion)));
          }
        } else if (!BeeUtils.same(source, COL_CRITERIA_GROUP_NAME)) {
          if (groupsGrid != null) {
            flt.add(Filter.isEqual(COL_CRITERIA_GROUP_NAME,
                groupsGrid.getPresenter().getActiveRow().getValue(groupsGrid.getPresenter()
                    .getGridView().getDataIndex(COL_CRITERIA_GROUP_NAME))));
          }
          if (BeeUtils.same(source, COL_CRITERION_VALUE) && criteriaGrid != null) {
            flt.add(Filter.isEqual(COL_CRITERION_NAME,
                criteriaGrid.getPresenter().getActiveRow().getValue(criteriaGrid.getPresenter()
                    .getGridView().getDataIndex(COL_CRITERION_NAME))));
          }
        }
        event.getSelector().setAdditionalFilter(flt);
      }
    }
  }

  private HasWidgets panel;
  private Long groupId;
  private final Map<String, String> criteriaHistory = Maps.newLinkedHashMap();
  private final Map<String, Editor> criteria = Maps.newLinkedHashMap();
  private final Map<String, Long> ids = Maps.newHashMap();

  private ChildGrid groupsGrid;
  private ChildGrid criteriaGrid;

  private final GridInterceptor childInterceptor = new AbstractGridInterceptor() {
    @Override
    public void afterCreateEditor(String source, Editor editor, boolean embedded) {
      if (editor instanceof Autocomplete) {
        ((Autocomplete) editor).addAutocompleteHandler(new AutocompleteFilter(source, null));
      }
    }

    @Override
    public boolean ensureRelId(final IdCallback callback) {
      ensureDataId(null, callback);
      return true;
    }
  };

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, "MainCriteriaEditor")) {
      widget.asWidget().addDomHandler(this, ClickEvent.getType());

    } else if (widget instanceof HasWidgets && BeeUtils.same(name, "MainCriteriaContainer")) {
      panel = (HasWidgets) widget;

    } else if (widget instanceof ChildGrid) {
      ChildGrid grid = (ChildGrid) widget;

      if (BeeUtils.same(name, TBL_CRITERIA_GROUPS)) {
        groupsGrid = grid;
        grid.setGridInterceptor(childInterceptor);

      } else if (BeeUtils.same(name, TBL_CRITERIA)) {
        criteriaGrid = grid;
        grid.setGridInterceptor(childInterceptor);
      }
    }
  }

  @Override
  public void afterInsertRow(IsRow result, boolean forced) {
    if (!forced) {
      save(result);
    }
  }

  @Override
  public void afterUpdateRow(IsRow result) {
    save(result);
  }

  @Override
  public FormInterceptor getInstance() {
    return new DocumentDataForm();
  }

  @Override
  public void onClick(ClickEvent event) {
    LocalizableConstants loc = Localized.getConstants();

    Global.inputCollection(loc.mainCriteria(), loc.name(), true,
        criteria.keySet(), new Consumer<Collection<String>>() {
          @Override
          public void accept(Collection<String> collection) {
            Map<String, Editor> oldCriteria = Maps.newHashMap(criteria);
            criteria.clear();

            for (String crit : collection) {
              Editor input = oldCriteria.get(crit);

              if (input == null) {
                input = createAutocomplete("DistinctCriterionValues", COL_CRITERION_VALUE, crit);
              }
              criteria.put(crit, input);
            }
            render();
          }
        }, new Supplier<Editor>() {
          @Override
          public Editor get() {
            return createAutocomplete("DistinctCriteria", COL_CRITERION_NAME, null);
          }
        });
  }

  @Override
  public void onClose(List<String> messages, IsRow oldRow, IsRow newRow) {
    if (save(null)) {
      LocalizableConstants loc = Localized.getConstants();
      messages.add(BeeUtils.joinWords(loc.changedValues(), loc.mainCriteria()));
    }
  }

  @Override
  public void onSaveChanges(HasHandlers listener, SaveChangesEvent event) {
    if (BeeUtils.isEmpty(event.getColumns())) {
      save(getActiveRow());
    }
  }

  @Override
  public void onStart(FormView form) {
    if (getHeaderView() == null) {
      return;
    }
    final LocalizableConstants loc = Localized.getConstants();

    getHeaderView().addCommandItem(new Button(loc.documentNew(),
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            Global.inputString(loc.documentNew(), loc.documentName(),
                new StringCallback() {
                  @Override
                  public void onSuccess(final String value) {
                    DocumentHandler.copyDocumentData(getDataLong(COL_DOCUMENT_DATA),
                        new IdCallback() {
                          @Override
                          public void onSuccess(Long dataId) {
                            Queries.insert(TBL_DOCUMENTS,
                                Data.getColumns(TBL_DOCUMENTS,
                                    Lists.newArrayList(COL_DOCUMENT_CATEGORY,
                                        COL_DOCUMENT_NAME, COL_DOCUMENT_DATA)),
                                Lists.newArrayList(getDataValue(COL_DOCUMENT_CATEGORY), value,
                                    DataUtils.isId(dataId) ? BeeUtils.toString(dataId) : null),
                                null, new RowInsertCallback(TBL_DOCUMENTS, null) {
                                  @Override
                                  public void onSuccess(BeeRow result) {
                                    super.onSuccess(result);
                                    RowEditor.openRow(TBL_DOCUMENTS, result, true);
                                  }
                                });
                          }
                        });
                  }
                });
          }
        }));
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, ScheduledCommand focusCommand) {
    requery(row);
    return true;
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    requery(newRow);
  }

  protected String parseContent(String content) {
    String result = content;

    for (Entry<String, Editor> entry : criteria.entrySet()) {
      String criterion = entry.getKey();
      String value = entry.getValue().getNormalizedValue();

      result = result.replace("{" + criterion + "}", value);
    }
    return result;
  }

  private Autocomplete createAutocomplete(String viewName, String column, String value) {
    Autocomplete input = Autocomplete.create(Relation.create(viewName,
        Lists.newArrayList(column)), true);

    input.addAutocompleteHandler(new AutocompleteFilter(null, value));
    return input;
  }

  private void ensureDataId(IsRow row, final IdCallback callback) {
    final FormView form = getFormView();
    final BeeRow newRow = DataUtils.cloneRow(row == null ? form.getActiveRow() : row);
    final int idx = form.getDataIndex(COL_DOCUMENT_DATA);
    Long dataId = newRow.getLong(idx);

    if (DataUtils.isId(dataId)) {
      callback.onSuccess(dataId);
    } else {
      Queries.insert(TBL_DOCUMENT_DATA, Data.getColumns(TBL_DOCUMENT_DATA,
          Lists.newArrayList(COL_DOCUMENT_CONTENT)), Lists.newArrayList((String) null), null,
          new RowCallback() {
            @Override
            public void onSuccess(BeeRow result) {
              long id = result.getId();
              newRow.setValue(idx, id);

              RowUpdateEvent.fire(BeeKeeper.getBus(), form.getViewName(), newRow);
              callback.onSuccess(id);

              Queries.update(form.getViewName(), newRow.getId(), COL_DOCUMENT_DATA,
                  Value.getValue(id));
            }
          });
    }
  }

  private void render() {
    if (panel == null) {
      getHeaderView().clearCommandPanel();
      return;
    }
    panel.clear();

    if (criteria.size() > 0) {
      HtmlTable table = new HtmlTable();
      table.setColumnCellStyles(0, "text-align:right");
      int c = 0;

      for (Entry<String, Editor> entry : criteria.entrySet()) {
        table.setText(c, 0, entry.getKey());
        table.setWidget(c++, 1, entry.getValue().asWidget());
      }
      panel.add(table);
    }
  }

  private void requery(IsRow row) {
    criteriaHistory.clear();
    criteria.clear();
    ids.clear();
    groupId = null;
    render();
    Long dataId = row.getLong(getDataIndex(COL_DOCUMENT_DATA));

    if (!DataUtils.isId(dataId)) {
      return;
    }
    Queries.getRowSet(VIEW_MAIN_CRITERIA, null,
        Filter.isEqual(COL_DOCUMENT_DATA, Value.getValue(dataId)),
        new RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            if (result.getNumberOfRows() > 0) {
              groupId = result.getRow(0).getId();

              for (BeeRow crit : result.getRows()) {
                String name = Data.getString(VIEW_MAIN_CRITERIA, crit, COL_CRITERION_NAME);

                if (!BeeUtils.isEmpty(name)) {
                  String value = Data.getString(VIEW_MAIN_CRITERIA, crit, COL_CRITERION_VALUE);

                  Autocomplete box = createAutocomplete("DistinctCriterionValues",
                      COL_CRITERION_VALUE, name);

                  box.setValue(value);

                  criteriaHistory.put(name, value);
                  criteria.put(name, box);
                  ids.put(name, Data.getLong(VIEW_MAIN_CRITERIA, crit, "ID"));
                }
              }
              render();
            }
          }
        });
  }

  private boolean save(final IsRow row) {
    final Map<String, String> newValues = Maps.newLinkedHashMap();
    Map<Long, String> changedValues = Maps.newHashMap();
    CompoundFilter flt = Filter.or();
    final Holder<Integer> holder = Holder.of(0);

    for (String crit : criteria.keySet()) {
      String value = criteria.get(crit).getValue();
      value = BeeUtils.isEmpty(value) ? null : value;
      Long id = ids.get(crit);

      if (!criteriaHistory.containsKey(crit) || !Objects.equals(value, criteriaHistory.get(crit))) {
        if (DataUtils.isId(id)) {
          changedValues.put(id, value);
        } else {
          newValues.put(crit, value);
        }
        holder.set(holder.get() + 1);
      }
    }
    for (String crit : ids.keySet()) {
      if (!criteria.containsKey(crit)) {
        flt.add(Filter.compareId(ids.get(crit)));
      }
    }
    if (!flt.isEmpty()) {
      holder.set(holder.get() + 1);
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
              getGridView().getGrid().refresh();
            }
          });
        }
      }
    };
    if (!BeeUtils.isEmpty(newValues)) {
      final Consumer<Long> consumer = new Consumer<Long>() {
        @Override
        public void accept(Long id) {
          for (Entry<String, String> entry : newValues.entrySet()) {
            Queries.insert(TBL_CRITERIA, Data.getColumns(TBL_CRITERIA,
                Lists.newArrayList(COL_CRITERIA_GROUP, COL_CRITERION_NAME, COL_CRITERION_VALUE)),
                Lists.newArrayList(BeeUtils.toString(id), entry.getKey(), entry.getValue()), null,
                new RowCallback() {
                  @Override
                  public void onSuccess(BeeRow result) {
                    scheduler.execute();
                  }
                });
          }
        }
      };
      if (!DataUtils.isId(groupId)) {
        ensureDataId(row, new IdCallback() {
          @Override
          public void onSuccess(Long dataId) {
            Queries.insert(TBL_CRITERIA_GROUPS,
                Data.getColumns(TBL_CRITERIA_GROUPS, Lists.newArrayList(COL_DOCUMENT_DATA)),
                Lists.newArrayList(BeeUtils.toString(dataId)), null, new RowCallback() {
                  @Override
                  public void onSuccess(BeeRow result) {
                    consumer.accept(result.getId());
                  }
                });
          }
        });
      } else {
        consumer.accept(groupId);
      }
    }
    if (!BeeUtils.isEmpty(changedValues)) {
      for (Entry<Long, String> entry : changedValues.entrySet()) {
        Queries.update(TBL_CRITERIA, Filter.compareId(entry.getKey()),
            COL_CRITERION_VALUE, new TextValue(entry.getValue()), new IntCallback() {
              @Override
              public void onSuccess(Integer result) {
                scheduler.execute();
              }
            });
      }
    }
    if (!flt.isEmpty()) {
      Queries.delete(TBL_CRITERIA, flt, new IntCallback() {
        @Override
        public void onSuccess(Integer result) {
          scheduler.execute();
        }
      });
    }
    return true;
  }
}
