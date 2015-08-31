package com.butent.bee.client.modules.transport;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
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
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.event.InputEvent;
import com.butent.bee.client.event.InputHandler;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MessageBuilder extends FaLabel implements ClickHandler {

  private class MessageDialog extends DialogBox implements ClickHandler {

    private final HtmlTable driverPanel = new HtmlTable();
    private final Label characterCount = new Label();
    private final InputArea messagePanel = new InputArea();
    private Map<String, Collection<String>> templates = new LinkedHashMap<>();
    private BeeRowSet cargoInfo;
    private final Collection<Long> selectedIds = new HashSet<>();
    private final TabBar tabs = new TabBar(Orientation.HORIZONTAL);

    protected MessageDialog(Set<Long> driverIds, Set<Long> cargoIds, Set<Long> ids) {
      super(Localized.getConstants().message());

      setAnimationEnabled(true);
      setHideOnEscape(true);
      addDefaultCloseBox();

      Flow panel = new Flow();
      panel.add(driverPanel);

      Horizontal hor = new Horizontal();
      hor.add(new Button(Localized.getConstants().template(), this));
      hor.add(tabs);
      tabs.addSelectionHandler(new SelectionHandler<Integer>() {
        @Override
        public void onSelection(SelectionEvent<Integer> selectionEvent) {
          renderMessage();
        }
      });
      panel.add(hor);

      characterCount.getElement().getStyle().setTextAlign(Style.TextAlign.RIGHT);
      panel.add(characterCount);

      messagePanel.setVisibleLines(15);
      messagePanel.setWidth("100%");
      messagePanel.addInputHandler(new InputHandler() {
        @Override
        public void onInput(InputEvent event) {
          updateCharacterCount();
        }
      });
      panel.add(messagePanel);

      SimplePanel simple = new SimplePanel(new Button(Localized.getConstants().send(),
          new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg) {
              sendMessage();
            }
          }));
      simple.getElement().getStyle().setTextAlign(Style.TextAlign.CENTER);
      panel.add(simple);

      setWidget(panel);

      Global.getParameter(PRM_MESSAGE_TEMPLATE, new Consumer<String>() {
        @Override
        public void accept(String parameter) {
          for (Map.Entry<String, String> entry : Codec.deserializeMap(parameter).entrySet()) {
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
        }
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
      setCargoInfo(Data.createRowSet(TBL_ORDER_CARGO));

      if (!BeeUtils.isEmpty(cargoIds)) {
        Queries.getRowSet(TBL_ORDER_CARGO, null, Filter.idIn(cargoIds),
            new Queries.RowSetCallback() {
              @Override
              public void onSuccess(BeeRowSet result) {
                setCargoInfo(result);
              }
            });
      }
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
      Global.inputCollection(Localized.getConstants().template(),
          caption, false, template, new Consumer<Collection<String>>() {
            @Override
            public void accept(final Collection<String> input) {
              if (BeeUtils.isEmpty(input)) {
                templates.remove(caption);
                renderTemplates(null);
              } else {
                Global.inputString(Localized.getConstants().name(), null, new StringCallback() {
                  @Override
                  public void onSuccess(String name) {
                    templates.put(name, input);
                    renderTemplates(name);
                  }
                }, null, caption);
              }
            }
          }, new Function<String, Editor>() {
            @Override
            public Editor apply(String value) {
              final InputText editor = new InputText();
              editor.setWidth("30em");

              if (!BeeUtils.isEmpty(value)) {
                editor.setValue(value);
              } else {
                final List<String> caps = new ArrayList<>();
                final List<String> keys = new ArrayList<>();

                for (BeeColumn col : cargoInfo.getColumns()) {
                  if (col.getType() != ValueType.LONG) {
                    caps.add(Localized.getLabel(col));
                    keys.add(col.getId());
                  }
                }
                Global.choice(Localized.getConstants().value(), null, caps, new ChoiceCallback() {
                  @Override
                  public void onSuccess(int item) {
                    editor.setValue(caps.get(item) + ": {" + keys.get(item) + "}");
                  }
                });
              }
              return editor;
            }
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
                String value = null;
                int idx = cargoInfo.getColumnIndex(chunk.substring(1, chunk.length() - 1));

                if (idx != BeeConst.UNDEF) {
                  switch (cargoInfo.getColumn(idx).getType()) {
                    case BOOLEAN:
                      value = BeeUtils.unbox(cargoInfo.getBoolean(i, idx))
                          ? Localized.getConstants().yes() : Localized.getConstants().no();
                      break;

                    case DATE:
                      JustDate date = cargoInfo.getDate(i, idx);

                      if (date != null) {
                        value = date.toString();
                      }
                      break;

                    case DATE_TIME:
                      DateTime dateTime = cargoInfo.getDateTime(i, idx);

                      if (dateTime != null) {
                        value = dateTime.toCompactString();
                      }
                      break;

                    default:
                      value = cargoInfo.getString(i, idx);
                      break;
                  }
                } else {
                  value = chunk;
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
      Global.setParameter(PRM_MESSAGE_TEMPLATE, Codec.beeSerialize(templates));
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
        errors.add(Localized.getConstants().mobile());
      }
      final String msg = messagePanel.getValue();

      if (BeeUtils.isEmpty(msg)) {
        errors.add(Localized.getConstants().message());
      }
      if (!BeeUtils.isEmpty(errors)) {
        Global.showError(Localized.getConstants().noData(), errors);
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

    private void setCargoInfo(BeeRowSet cargo) {
      this.cargoInfo = cargo;
      renderMessage();
    }

    private void setDrivers(Map<String, String> drivers) {
      driverPanel.clear();
      drivers.put(Localized.getConstants().mobile(), null);
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

    setTitle(Localized.getConstants().message());
    addClickHandler(this);

    this.gridView = Assert.notNull(gridView);
    Assert.state(BeeUtils.same(Data.getViewTable(gridView.getViewName()), TBL_CARGO_TRIPS));
  }

  @Override
  public void onClick(ClickEvent clickEvent) {
    final Set<Long> ids = new HashSet<>();

    for (RowInfo rowInfo : gridView.getSelectedRows(GridView.SelectedRows.ALL)) {
      ids.add(rowInfo.getId());
    }
    if (BeeUtils.isEmpty(ids) && gridView.getActiveRow() != null) {
      ids.add(gridView.getActiveRow().getId());
    }
    if (BeeUtils.isEmpty(ids)) {
      gridView.notifyWarning(Localized.getConstants().selectAtLeastOneRow());
      return;
    }
    Set<Long> tripIds = new HashSet<>();
    final Set<Long> cargoIds = new HashSet<>();

    for (IsRow row : gridView.getRowData()) {
      if (ids.contains(row.getId())) {
        tripIds.add(row.getLong(gridView.getDataIndex(COL_TRIP)));
        cargoIds.add(row.getLong(gridView.getDataIndex(COL_CARGO)));
      }
    }
    Queries.getRowSet(TBL_TRIP_DRIVERS, Lists.newArrayList(COL_DRIVER),
        Filter.any(COL_TRIP, tripIds), new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            new MessageDialog(result.getDistinctLongs(0), cargoIds, ids);
          }
        });
  }
}
