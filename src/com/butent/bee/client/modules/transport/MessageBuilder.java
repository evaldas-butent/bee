package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.regexp.shared.SplitResult;
import com.google.gwt.user.client.ui.SimplePanel;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MessageBuilder extends FaLabel implements ClickHandler {

  private final class MessageDialog extends DialogBox implements ClickHandler {

    private final HtmlTable driverPanel = new HtmlTable();
    private final Label characterCount = new Label();
    private final InputArea messagePanel = new InputArea();
    private final Map<String, Collection<String>> templates = new LinkedHashMap<>();

    private BeeRowSet tripInfo;
    private BeeRowSet cargoInfo;
    private final Map<Long, Map<String, String>> additionalPlaces = new HashMap<>();
    private final Map<Long, Map<String, String>> revisedPlaces = new HashMap<>();

    private final Collection<Long> selectedIds = new HashSet<>();
    private final TabBar tabs = new TabBar(Orientation.HORIZONTAL);

    private MessageDialog(BeeRowSet trip, Set<Long> driverIds, Set<Long> cargoIds, Set<Long> ids) {
      super(Localized.dictionary().message());

      setAnimationEnabled(true);
      setHideOnEscape(true);
      addDefaultCloseBox();

      Flow panel = new Flow();
      panel.add(driverPanel);

      Horizontal hor = new Horizontal();
      hor.add(new Button(Localized.dictionary().template(), this));
      hor.add(tabs);
      tabs.addSelectionHandler(selectionEvent -> renderMessage());
      panel.add(hor);

      characterCount.getElement().getStyle().setTextAlign(Style.TextAlign.RIGHT);
      panel.add(characterCount);

      messagePanel.setVisibleLines(15);
      messagePanel.setWidth("100%");
      messagePanel.addInputHandler(event -> updateCharacterCount());
      panel.add(messagePanel);

      SimplePanel simple = new SimplePanel(new Button(Localized.dictionary().send(),
          handler -> sendMessage()));
      simple.getElement().getStyle().setTextAlign(Style.TextAlign.CENTER);
      panel.add(simple);

      setWidget(panel);

      Global.getParameter(PRM_MESSAGE_TEMPLATE, parameter -> {
        for (Map.Entry<String, String> entry : Codec.deserializeLinkedHashMap(
            parameter).entrySet()) {
          String[] items;

          try {
            items = Codec.beeDeserializeCollection(entry.getValue());
          } catch (Exception e) {
            items = null;
          }
          if (!ArrayUtils.isEmpty(items)) {
            templates.put(entry.getKey(), Arrays.asList(items));
          }
        }
        renderTemplates(null);
      });
      Queries.getRowSet(TBL_DRIVERS,
          Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME, COL_MOBILE),
          BeeUtils.isEmpty(driverIds) ? Filter.isFalse() : Filter.idIn(driverIds),
          new Queries.RowSetCallback() {
            @Override
            public void onSuccess(BeeRowSet result) {
              Map<String, String> drivers = new HashMap<>();

              for (int i = 0; i < result.getNumberOfRows(); i++) {
                drivers.put(BeeUtils.joinWords(result.getString(i, COL_FIRST_NAME),
                    result.getString(i, COL_LAST_NAME)), result.getString(i, COL_MOBILE));
              }
              setDrivers(drivers);
            }
          });
      tripInfo = trip;
      setCargoInfo(Data.createRowSet(TBL_ORDER_CARGO), Data.createRowSet(VIEW_CARGO_HANDLING));

      Queries.getRowSet(TBL_ORDER_CARGO, null, Filter.idIn(cargoIds), new Queries.RowSetCallback() {
        @Override
        public void onSuccess(BeeRowSet cargo) {
          Queries.getRowSet(VIEW_CARGO_HANDLING, null, Filter.or(Filter.any(COL_CARGO_TRIP, ids),
              Filter.any(COL_CARGO, cargoIds)), new Queries.RowSetCallback() {
            @Override
            public void onSuccess(BeeRowSet handling) {
              if (!DataUtils.isEmpty(handling)) {
                Queries.getRowSet(VIEW_CARGO_TRIPS, Collections.singletonList(COL_CARGO),
                    Filter.idIn(handling.getDistinctLongs(handling.getColumnIndex(COL_CARGO_TRIP))),
                    new Queries.RowSetCallback() {
                      @Override
                      public void onSuccess(BeeRowSet result) {
                        for (BeeRow row : handling) {
                          Long id = row.getLong(handling.getColumnIndex(COL_CARGO_TRIP));

                          if (DataUtils.isId(id) && !BeeConst.isUndef(result.getRowIndex(id))) {
                            row.setValue(handling.getColumnIndex(COL_CARGO),
                                result.getStringByRowId(id, COL_CARGO));
                          }
                        }
                        setCargoInfo(cargo, handling);
                      }
                    });
              } else {
                setCargoInfo(cargo, handling);
              }
            }
          });
        }
      });
      selectedIds.addAll(ids);
    }

    @Override
    public void onClick(ClickEvent arg) {
      final String caption;
      Collection<String> template;

      if (BeeUtils.isPositive(tabs.getItemCount())) {
        caption = ((Label) tabs.getSelectedWidget()).getText();
        template = templates.get(caption);
      } else {
        caption = null;
        template = null;
      }
      Global.inputCollection(Localized.dictionary().template(),
          caption, false, template, input -> {
            if (BeeUtils.isEmpty(input)) {
              templates.remove(caption);
              renderTemplates(null);
            } else {
              Global.inputString(Localized.dictionary().name(), null, new StringCallback() {
                @Override
                public void onSuccess(String name) {
                  templates.put(name, input);
                  renderTemplates(name);
                }
              }, null, caption);
            }
          }, value -> {
            final InputText editor = new InputText();
            editor.setWidth("30em");

            if (!BeeUtils.isEmpty(value)) {
              editor.setValue(value);
            } else {
              final List<String> caps = new ArrayList<>();
              final List<String> keys = new ArrayList<>();

              for (BeeRowSet rowSet : new BeeRowSet[] {cargoInfo, tripInfo}) {
                for (BeeColumn col : rowSet.getColumns()) {
                  if (col.getType() != ValueType.LONG) {
                    caps.add(Localized.getLabel(col));
                    keys.add(col.getId());
                  }
                }
              }
              Global.choice(Localized.dictionary().value(), null, caps,
                  item -> editor.setValue(caps.get(item) + ": {" + keys.get(item) + "}"));
            }
            return editor;
          });
    }

    private void renderMessage() {
      StringBuilder sb = new StringBuilder();

      if (!BeeConst.isUndef(tabs.getSelectedTab()) && !DataUtils.isEmpty(cargoInfo)) {
        RegExp r = RegExp.compile("\\{\\w+\\}", "g");

        for (int i = 0; i < cargoInfo.getNumberOfRows(); i++) {
          if (sb.length() > 0) {
            sb.append("\n");
          }
          for (String rowExpr : templates.get(((Label) tabs.getSelectedWidget()).getText())) {
            SplitResult s = r.split(rowExpr);

            for (int j = 0; j < s.length(); j++) {
              sb.append(s.get(j));

              MatchResult m = r.exec(rowExpr);

              if (m != null) {
                String chunk = m.getGroup(0);
                String col = chunk.substring(1, chunk.length() - 1);
                int idx = DataUtils.getColumnIndex(col, cargoInfo.getColumns());
                String value;

                if (BeeConst.isUndef(idx)) {
                  idx = tripInfo.getColumnIndex(col);

                  if (BeeConst.isUndef(idx)) {
                    value = chunk;
                  } else {
                    value = parseValue(tripInfo, 0, idx);
                  }
                } else {
                  Long cargoId = cargoInfo.getRow(i).getId();

                  if (revisedPlaces.containsKey(cargoId)
                      && revisedPlaces.get(cargoId).containsKey(col)) {
                    value = revisedPlaces.get(cargoId).get(col);
                  } else {
                    value = parseValue(cargoInfo, i, idx);

                    if (additionalPlaces.containsKey(cargoId)) {
                      value = BeeUtils.joinItems(value, additionalPlaces.get(cargoId).get(col));
                    }
                  }
                }
                sb.append(BeeUtils.nvl(value, ""));
              }
            }
            sb.append("\n");
          }
        }
      }
      messagePanel.setValue(sb.toString());
      updateCharacterCount();
    }

    private void renderTemplates(String name) {
      tabs.clear();
      int idx = BeeConst.UNDEF;
      int c = 0;

      for (String s : templates.keySet()) {
        if (BeeUtils.same(s, name)) {
          idx = c;
        }
        tabs.addItem(s);
        c++;
      }
      if (BeeConst.isUndef(idx) && BeeUtils.isPositive(c)) {
        idx = 0;
      }
      if (!BeeConst.isUndef(idx)) {
        tabs.selectTab(idx);
      }
      Global.setParameter(PRM_MESSAGE_TEMPLATE, Codec.beeSerialize(templates), false);
    }

    private void sendMessage() {
      List<String> errors = new ArrayList<>();
      Set<String> phones = new HashSet<>();

      for (int i = 0; i < driverPanel.getRowCount(); i++) {
        if (((InputBoolean) driverPanel.getWidget(i, 0)).isChecked()) {
          String phone = ((InputText) driverPanel.getWidget(i, 1)).getValue();

          if (!BeeUtils.isEmpty(phone)) {
            phones.add(phone);
          }
        }
      }
      if (BeeUtils.isEmpty(phones)) {
        errors.add(Localized.dictionary().mobile());
      }
      final String msg = messagePanel.getValue();

      if (BeeUtils.isEmpty(msg)) {
        errors.add(Localized.dictionary().message());
      }
      if (!BeeUtils.isEmpty(errors)) {
        Global.showError(Localized.dictionary().noData(), errors);
        return;
      }
      ParameterList args = TransportHandler.createArgs(SVC_SEND_MESSAGE);
      args.addDataItem(COL_MOBILE, Codec.beeSerialize(phones));
      args.addDataItem(COL_DESCRIPTION, msg);

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          response.notify(gridView);

          if (response.hasErrors()) {
            return;
          }
          if (!BeeUtils.isEmpty(selectedIds)) {
            Queries.update(gridView.getViewName(), Filter.idIn(selectedIds), COL_CARGO_MESSAGE,
                Value.getValue(msg), new Queries.IntCallback() {
                  @Override
                  public void onSuccess(Integer result) {
                    DataChangeEvent.fireRefresh(BeeKeeper.getBus(), gridView.getViewName());
                  }
                });
          }
        }
      });
      close();
    }

    private void setCargoInfo(BeeRowSet cargo, BeeRowSet handling) {
      cargoInfo = cargo;
      additionalPlaces.clear();
      revisedPlaces.clear();
      int aCnt = 1;
      int rCnt = 0;

      for (int i = 0; i < handling.getNumberOfRows(); i++) {
        Long cargoId = handling.getLong(i, COL_CARGO);
        Map<String, String> map;
        int c;

        if (DataUtils.isId(handling.getLong(i, COL_CARGO_TRIP))) {
          if (!revisedPlaces.containsKey(cargoId)) {
            revisedPlaces.put(cargoId, new HashMap<>());
          }
          map = revisedPlaces.get(cargoId);
          c = ++rCnt;
        } else {
          if (!additionalPlaces.containsKey(cargoId)) {
            additionalPlaces.put(cargoId, new HashMap<>());
          }
          map = additionalPlaces.get(cargoId);
          c = ++aCnt;
        }
        for (int j = 0; j < handling.getNumberOfColumns(); j++) {
          String col = handling.getColumnId(j);
          String val = parseValue(handling, i, j);

          if (!BeeUtils.isEmpty(val)) {
            map.put(col, BeeUtils.joinItems(map.get(col),
                BeeUtils.joinWords(c == 1 ? "" : c + ".", val)));
          }
        }
      }
      renderMessage();
    }

    private void setDrivers(Map<String, String> drivers) {
      driverPanel.clear();
      drivers.put(Localized.dictionary().mobile(), null);
      int c = 0;

      for (Map.Entry<String, String> driver : drivers.entrySet()) {
        InputBoolean check = new InputBoolean(driver.getKey());
        check.setChecked(true);
        driverPanel.setWidget(c, 0, check);

        InputText phone = new InputText();
        phone.setValue(driver.getValue());
        driverPanel.setWidget(c, 1, phone);
        c++;
      }
      if (!isShowing()) {
        center();
      }
    }

    private void updateCharacterCount() {
      characterCount.setText(BeeUtils.toString(messagePanel.getValue().length()));
    }
  }

  private final GridView gridView;

  public MessageBuilder(GridView gridView) {
    super(FontAwesome.WHATSAPP);

    setTitle(Localized.dictionary().message());
    addClickHandler(this);

    this.gridView = Assert.notNull(gridView);
    Assert.state(BeeUtils.same(Data.getViewTable(gridView.getViewName()), TBL_CARGO_TRIPS));
  }

  @Override
  public void onClick(ClickEvent clickEvent) {
    Set<Long> ids = new HashSet<>();

    for (RowInfo rowInfo : gridView.getSelectedRows(GridView.SelectedRows.ALL)) {
      ids.add(rowInfo.getId());
    }
    if (BeeUtils.isEmpty(ids) && gridView.getActiveRow() != null) {
      ids.add(gridView.getActiveRow().getId());
    }
    Long tripId = null;
    Set<Long> cargoIds = new HashSet<>();

    for (IsRow row : gridView.getRowData()) {
      if (ids.contains(row.getId())) {
        if (DataUtils.isId(tripId)
            && !Objects.equals(tripId, row.getLong(gridView.getDataIndex(COL_TRIP)))) {
          tripId = null;
          break;
        }
        tripId = row.getLong(gridView.getDataIndex(COL_TRIP));
        cargoIds.add(row.getLong(gridView.getDataIndex(COL_CARGO)));
      }
    }
    if (!DataUtils.isId(tripId)) {
      gridView.notifyWarning(Localized.dictionary().selectAtLeastOneRow());
      return;
    }
    Queries.getRowSet(VIEW_TRIPS, null, Filter.compareId(tripId), new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet trip) {
        Queries.getDistinctLongs(TBL_TRIP_DRIVERS, COL_DRIVER,
            Filter.equals(COL_TRIP, trip.getRow(0).getId()), new RpcCallback<Set<Long>>() {
              @Override
              public void onSuccess(Set<Long> driverIds) {
                new MessageDialog(trip, driverIds, cargoIds, ids);
              }
            });
      }
    });
  }

  private static String parseValue(BeeRowSet rs, int row, int col) {
    String value = null;

    switch (rs.getColumn(col).getType()) {
      case BOOLEAN:
        value = BeeUtils.unbox(rs.getBoolean(row, col))
            ? Localized.dictionary().yes() : Localized.dictionary().no();
        break;

      case DATE:
        JustDate date = rs.getDate(row, col);

        if (date != null) {
          value = date.toString();
        }
        break;

      case DATE_TIME:
        DateTime dateTime = rs.getDateTime(row, col);

        if (dateTime != null) {
          value = dateTime.toCompactString();
        }
        break;

      default:
        value = rs.getString(row, col);
        break;
    }
    return value;
  }
}
