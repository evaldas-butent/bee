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

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.Autocomplete;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.event.logical.AutocompleteEvent;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
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

public class DocumentTemplateForm extends AbstractFormInterceptor implements ClickHandler {

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
  public void afterInsertRow(IsRow result) {
    save(result);
  }

  @Override
  public void beforeRefresh(final FormView form, final IsRow row) {
    criteriaHistory.clear();
    criteria.clear();
    ids.clear();
    groupId = null;
    render();
    Long dataId = form.getDataLong(COL_DOCUMENT_DATA);

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

  @Override
  public FormInterceptor getInstance() {
    return new DocumentTemplateForm();
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
  public void onReadyForInsert(HasHandlers listener, ReadyForInsertEvent event) {
    boolean exists = false;

    for (BeeColumn column : event.getColumns()) {
      if (BeeUtils.same(column.getId(), COL_DOCUMENT_CONTENT)) {
        exists = true;
        break;
      }
    }
    if (!exists) {
      event.getColumns().add(Data.getColumn(getViewName(), COL_DOCUMENT_CONTENT));
      event.getValues().add(null);
    }
  }

  @Override
  public void onSaveChanges(HasHandlers listener, SaveChangesEvent event) {
    save(event.getNewRow());
  }

  private Autocomplete createAutocomplete(String viewName, String column, String value) {
    Autocomplete input = Autocomplete.create(Relation.create(viewName,
        Lists.newArrayList(column)), true);

    input.addAutocompleteHandler(new AutocompleteFilter(null, value));
    return input;
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

  private boolean save(IsRow row) {
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
    final long templateId = row.getId();
    final String dataId = row.getString(getFormView().getDataIndex(COL_DOCUMENT_DATA));

    final ScheduledCommand scheduler = new ScheduledCommand() {
      @Override
      public void execute() {
        holder.set(holder.get() - 1);

        if (!BeeUtils.isPositive(holder.get())) {
          Queries.getRow(getViewName(), templateId, new RowUpdateCallback(getViewName()) {
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
        Queries.insert(TBL_CRITERIA_GROUPS,
            Data.getColumns(TBL_CRITERIA_GROUPS, Lists.newArrayList(COL_DOCUMENT_DATA)),
            Lists.newArrayList(dataId), null, new RowCallback() {
              @Override
              public void onSuccess(BeeRow result) {
                consumer.accept(result.getId());
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
