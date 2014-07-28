package com.butent.bee.client.modules.mail;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.DecisionCallback;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.event.logical.SelectorEvent.Handler;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.view.form.CloseCallback;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.RowChildren;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Relations extends HtmlTable implements ClickHandler {

  private static final String STORAGE = TBL_RELATIONS;

  private final String view;
  private final String column;

  final Multimap<String, Long> ids = HashMultimap.create();
  final Map<String, MultiSelector> widgetMap = new HashMap<>();
  final Map<MultiSelector, HandlerRegistration> registry = new HashMap<>();

  private Long id;
  private Handler handler;

  public Relations(String column, List<String> requiredRelations) {
    Assert.notEmpty(column);

    DataInfo info = Data.getDataInfo(STORAGE);

    this.column = column;
    this.view = info.getRelation(column);

    setWidth("100%");

    for (String col : info.getColumnNames(false)) {
      String relation = info.getRelation(col);

      if (!BeeKeeper.getUser().isDataVisible(relation) || BeeUtils.same(col, COL_RELATION)) {
        continue;
      }
      MultiSelector multi = null;

      if (BeeUtils.containsSame(requiredRelations, relation)) {
        multi = createMultiSelector(relation);
      }
      widgetMap.put(col, multi);
    }
  }

  @Override
  public void onClick(ClickEvent event) {
    event.stopPropagation();

    if (!DataUtils.isId(id)) {
      return;
    }
    setWidth("600px");
    refresh();

    Global.inputWidget(Localized.getConstants().relations(), this, new InputCallback() {
      @Override
      public void onAdd() {
        final Map<String, String> availableRelations = new HashMap<>();

        for (Entry<String, MultiSelector> entry : widgetMap.entrySet()) {
          String fld = entry.getKey();

          if (entry.getValue() == null) {
            availableRelations.put(fld, Data.getColumnRelation(STORAGE, fld));
          }
        }
        if (BeeUtils.isEmpty(availableRelations)) {
          Global.showInfo(Localized.getConstants().noData());
          return;
        }
        final ListBox listBox = new ListBox(true);

        for (Entry<String, String> entry : availableRelations.entrySet()) {
          listBox.addItem(Data.getViewCaption(entry.getValue()), entry.getKey());
        }
        if (availableRelations.size() > 30) {
          listBox.setVisibleItemCount(30);
        } else {
          listBox.setAllVisible();
        }
        Global.inputWidget(Localized.getConstants().newRelation(), listBox, new InputCallback() {
          @Override
          public void onSuccess() {
            for (int i = 0; i < listBox.getItemCount(); i++) {
              OptionElement optionElement = listBox.getOptionElement(i);

              if (optionElement.isSelected()) {
                String col = optionElement.getValue();
                widgetMap.put(col, createMultiSelector(availableRelations.get(col)));
              }
            }
          }
        });
      }

      @Override
      public void onClose(final CloseCallback closeCallback) {
        List<String> messages = new ArrayList<>();

        for (Entry<String, MultiSelector> entry : widgetMap.entrySet()) {
          MultiSelector multi = entry.getValue();

          if (multi != null && multi.isValueChanged()) {
            messages.add(Data.getViewCaption(Data.getColumnRelation(STORAGE, entry.getKey())));
          }
        }
        if (!BeeUtils.isEmpty(messages)) {
          Global.decide(Localized.getConstants().relations(),
              Lists.newArrayList(Localized.getConstants().changedValues() + BeeConst.CHAR_SPACE
                  + BeeUtils.joinItems(messages), Localized.getConstants().saveChanges()),
              new DecisionCallback() {
                @Override
                public void onConfirm() {
                  closeCallback.onSave();
                }

                @Override
                public void onDeny() {
                  closeCallback.onClose();
                }
              }, DialogConstants.DECISION_YES);
        } else {
          super.onClose(closeCallback);
        }
      }

      @Override
      public void onSuccess() {
        List<RowChildren> relations = new ArrayList<>();

        for (Entry<String, MultiSelector> entry : widgetMap.entrySet()) {
          MultiSelector multi = entry.getValue();

          if (multi != null && multi.isValueChanged()) {
            relations.add(RowChildren.create(STORAGE, column, id, entry.getKey(),
                DataUtils.buildIdList(multi.getIds())));
          }
        }
        if (!BeeUtils.isEmpty(relations)) {
          Queries.updateChildren(view, id, relations, new RowCallback() {
            @Override
            public void onSuccess(BeeRow result) {
              requery(id);
            }
          });
        }
      }
    }, null, null, EnumSet.of(Action.ADD));
  }

  public void requery(final Long parentId) {
    reset();

    if (!DataUtils.isId(parentId)) {
      return;
    }
    Queries.getRowSet(STORAGE, null, Filter.equals(column, parentId), new RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        for (int i = 0; i < result.getNumberOfColumns(); i++) {
          String col = result.getColumnId(i);

          if (BeeUtils.same(col, column)) {
            continue;
          }
          for (BeeRow beeRow : result) {
            Long relId = beeRow.getLong(i);

            if (DataUtils.isId(relId)) {
              ids.put(col, relId);
            }
          }
        }
        if (ids.containsKey(COL_RELATION)) {
          Queries.getRowSet(STORAGE, Lists.newArrayList(column),
              Filter.idIn(ids.get(COL_RELATION)), new RowSetCallback() {
                @Override
                public void onSuccess(BeeRowSet res) {
                  for (BeeRow beeRow : res) {
                    Long relId = beeRow.getLong(0);

                    if (DataUtils.isId(relId)) {
                      ids.put(column, relId);
                    }
                  }
                  id = parentId;
                }
              });
        } else {
          id = parentId;
        }
      }
    });
  }

  public void refresh() {
    for (String col : widgetMap.keySet()) {
      MultiSelector multi = widgetMap.get(col);

      if (ids.containsKey(col)) {
        if (multi == null) {
          multi = createMultiSelector(Data.getColumnRelation(STORAGE, col));
          widgetMap.put(col, multi);
        }
        multi.setIds(ids.get(col));

      } else if (multi != null) {
        multi.clearValue();
      }
    }
  }

  public void reset() {
    for (MultiSelector multi : widgetMap.values()) {
      if (multi != null) {
        multi.clearValue();
      }
    }
    ids.clear();
    id = null;
  }

  public Relations setSelectorHandler(Handler selectorHandler) {
    this.handler = selectorHandler;

    for (MultiSelector multi : widgetMap.values()) {
      if (multi != null) {
        registerSelectorHandler(multi);
      }
    }
    return this;
  }

  private MultiSelector createMultiSelector(String relation) {
    DataInfo dataInfo = Data.getDataInfo(relation);

    List<String> cols = DataUtils.parseColumns(dataInfo.getMainColumns(), dataInfo.getColumns());

    if (BeeUtils.isEmpty(cols)) {
      for (BeeColumn beeCol : Data.getColumns(relation)) {
        if (!beeCol.isNullable()) {
          cols.add(beeCol.getId());
        }
      }
    }
    MultiSelector multi = MultiSelector.autonomous(relation, cols);
    registerSelectorHandler(multi);
    multi.setWidth("100%");

    int c = getRowCount();
    setText(c, 0, Data.getViewCaption(relation));
    setWidget(c, 1, multi);

    return multi;
  }

  private void registerSelectorHandler(MultiSelector multi) {
    HandlerRegistration registration = registry.get(multi);

    if (registration != null) {
      registration.removeHandler();
    }
    if (handler != null) {
      registry.put(multi, multi.addSelectorHandler(handler));
    }
  }
}
