package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import com.google.gwt.xml.client.impl.DOMParseException;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.administration.AdministrationConstants.COL_CURRENCY;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.COL_TRADE_ITEM_PRICE;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.trade.InvoiceForm;
import com.butent.bee.client.output.ReportUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class CustomCargoInvoiceForm extends InvoiceForm {

  public CustomCargoInvoiceForm() {
    super(null);
  }

  public void getFilteredData(BeeRowSet beeRowSet, Consumer<BeeRowSet> consumer) {
    Relation relation = Relation.create(TBL_ITEMS, Arrays.asList(COL_ITEM_NAME, COL_ITEM_ARTICLE));
    relation.disableNewRow();

    final UnboundSelector selector = UnboundSelector.create(relation);
    final CheckBox enableGrouping = new CheckBox("Tik pirminiai");
    enableGrouping.setChecked(false);
    enableGrouping.addClickHandler(evt -> {
      if (enableGrouping.isChecked() && selector != null) {
        selector.setEnabled(false);
        selector.clearValue();
      } else if (selector != null) {
        selector.setEnabled(true);
      }
    });

    Flow flow = new Flow(StyleUtils.NAME_FLEX_BOX_VERTICAL);
    flow.add(selector);
    flow.add(enableGrouping);

    SimpleRowSet data = convertToSimpleRowSet(beeRowSet);
    Global.inputWidget(Localized.dictionary().itemOrService(), flow,
        new InputCallback() {
          @Override
          public void onCancel() {
            onSuccess();
          }

          @Override
          public void onSuccess() {
            SimpleRowSet rs;

            if (selector.getRelatedRow() != null) {
              rs = new SimpleRowSet(data.getColumnNames());

              String[] group = new String[] {
                  COL_CURRENCY, COL_CURRENCY_RATE + COL_CURRENCY,
                  COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC};

              Map<String, Multimap<String, String>> map = new HashMap<>();
              Map<String, Double> amounts = new HashMap<>();

              for (SimpleRowSet.SimpleRow simpleRow : data) {
                String key = "";

                for (String fld : group) {
                  if (data.hasColumn(fld)) {
                    key += fld + simpleRow.getValue(fld);
                  }
                }
                if (!map.containsKey(key)) {
                  Multimap<String, String> m = TreeMultimap.create();
                  map.put(key, m);
                }
                Multimap<String, String> valueMap = map.get(key);
                double qty = 0;
                double prc = 0;

                for (String fld : data.getColumnNames()) {
                  switch (fld) {
                    case COL_TRADE_ITEM_QUANTITY:
                      qty = BeeUtils.unbox(simpleRow.getDouble(fld));
                      break;

                    case COL_TRADE_ITEM_PRICE:
                      prc = BeeUtils.unbox(simpleRow.getDouble(fld));
                      break;

                    case COL_TRADE_ITEM_NOTE:
                      Document xml;

                      try {
                        xml = XMLParser.parse(simpleRow.getValue(COL_TRADE_ITEM_NOTE));
                      } catch (DOMParseException ex) {
                        xml = null;
                      }
                      if (xml != null) {
                        boolean primary = BeeUtils.isEmpty(XmlUtils
                            .getChildrenText(xml.getDocumentElement(),
                                "Parent" + COL_ASSESSMENT));

                        for (Element el : XmlUtils
                            .getChildrenElements(xml.getDocumentElement())) {
                          String name = el.getNodeName();

                          if (primary || !BeeUtils.inListSame(name, COL_ASSESSMENT,
                              COL_NUMBER, COL_ORDER_NOTES)) {
                            valueMap.put(name, XmlUtils.getText(el));
                          }
                        }
                      }
                      break;

                    default:
                      String viewName = selector.getOracle().getViewName();
                      String value;

                      if (!ArrayUtils.containsSame(group, fld)
                          && Data.containsColumn(viewName, fld)) {
                        value = Data.getString(viewName, selector.getRelatedRow(), fld);
                      } else {
                        value = simpleRow.getValue(fld);
                      }
                      if (!BeeUtils.isEmpty(value)) {
                        valueMap.put(fld, value);
                      }
                      break;
                  }
                }
                amounts.put(key, BeeUtils.unbox(amounts.get(key)) + qty * prc);
              }
              for (Map.Entry<String, Multimap<String, String>> entry : map.entrySet()) {
                SimpleRowSet.SimpleRow newRow = rs.addEmptyRow();
                Multimap<String, String> values = entry.getValue();
                Map<String, String> info = new HashMap<>();

                for (String fld : values.keySet()) {
                  if (data.hasColumn(fld)) {
                    newRow.setValue(fld, BeeUtils.joinItems(values.get(fld)));
                  } else {
                    info.put(fld, BeeUtils.joinItems(values.get(fld)));
                  }
                }
                if (!BeeUtils.isEmpty(info)) {
                  newRow.setValue(COL_TRADE_ITEM_NOTE,
                      XmlUtils.createString("CargoInfo", info));
                }
                newRow.setValue(COL_TRADE_ITEM_QUANTITY, "1");
                newRow.setValue(COL_TRADE_ITEM_PRICE,
                    BeeUtils.toString(amounts.get(entry.getKey()), 2));
              }
            } else if (enableGrouping.isChecked()) {
              rs = groupByPrimaryAssessments(data);
            } else {
              rs = data;
            }

            consumer.accept(convertToBeeRowSet(rs));
          }
        });
  }

  @Override
  protected void print(String report) {
    getReportParameters(parameters -> {
      String[] arr = BeeUtils.split(report, BeeConst.CHAR_UNDER);
      String rep = arr[0];

      if (Objects.equals("CargoInvoiceShort", rep)) {
        getReportData(data -> getFilteredData(data[0], rowSet -> {
          parameters.put("XmlData", Codec.beeSerialize(getDataFromXml(rowSet)));
          ReportUtils.showReport(report, getReportCallback(), parameters, rowSet);
        }));
      } else {
        getReportData(data -> {
          parameters.put("XmlData", Codec.beeSerialize(getDataFromXml(data[0])));
          ReportUtils.showReport(report, getReportCallback(), parameters, data);
        });
      }
    });
  }

  private BeeRowSet convertToBeeRowSet(SimpleRowSet data) {
    BeeRowSet rowSet = new BeeRowSet(VIEW_SALE_ITEMS, Data.getColumns(VIEW_SALE_ITEMS));

    for (SimpleRowSet.SimpleRow row : data) {
      rowSet.addRow(new BeeRow(DataUtils.NEW_ROW_ID, row.getValues()));
    }

    return rowSet;
  }

  private static SimpleRowSet convertToSimpleRowSet(BeeRowSet data) {
    List<BeeColumn> rsCols = data.getColumns();
    int cc = rsCols.size();
    List<String> nameCols = Arrays.asList(ALS_ITEM_NAME, ALS_ITEM_NAME + 2, ALS_ITEM_NAME + 3);
    String[] columns = new String[cc];

    for (int i = 0; i < cc; i++) {
      if (BeeUtils.containsSame(nameCols, rsCols.get(i).getId())) {
        columns[i] = rsCols.get(i).getId().replaceAll(COL_ITEM, "");
        continue;
      }
      columns[i] = rsCols.get(i).getId();
    }
    SimpleRowSet rowSet = new SimpleRowSet(columns);

    for (BeeRow row : data) {
      rowSet.addRow(row.getValues().toArray(new String[0]));
    }

    return rowSet;
  }

  private static Map<String, String> getDataFromXml(BeeRowSet rowSet) {
    Map<String, Set<String>> map = new HashMap<>();
    Map<String, String> result = new HashMap<>();
    int noteIdx = Data.getColumnIndex(VIEW_SALE_ITEMS, COL_NOTE);

    for (BeeRow row : rowSet) {
      String note = row.getString(noteIdx);

      if (!BeeUtils.isEmpty(note)) {
        Document doc = XmlUtils.parse(note);
        NodeList children = doc.getElementsByTagName("*");
        if (children != null && children.getLength() > 0) {
          for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            String name = node.getNodeName();
            if (BeeUtils.inList(name, "LoadingDate", "UnloadingDate", "Vehicle",
                "Documents", "OrderNo", "Number")) {
              if (map.containsKey(name)) {
                Set<String> tmpSet = map.get(name);
                tmpSet.add(XmlUtils.getText((Element) node));
                map.put(name, tmpSet);
              } else {
                Set<String> tmpSet = new HashSet<>();
                tmpSet.add(XmlUtils.getText((Element) node));
                map.put(name, tmpSet);
              }
            }
          }
        }
      }
    }

    for (String key : map.keySet()) {
      result.put(key, BeeUtils.joinItems(map.get(key)));
    }

    return result;
  }

  private static SimpleRowSet groupByPrimaryAssessments(SimpleRowSet data) {
    SimpleRowSet result = new SimpleRowSet(data.getColumnNames());
    Map<Long, List<SimpleRowSet.SimpleRow>> assessments = Maps.newLinkedHashMap();
    Map<Long, List<SimpleRowSet.SimpleRow>> childAssessments = Maps.newLinkedHashMap();

    for (SimpleRowSet.SimpleRow simpleRow : data) {
      boolean primary = false;
      Long assessmentId = null;
      Long parentId = null;

      Document xml;

      try {
        xml = XMLParser.parse(simpleRow.getValue(COL_TRADE_ITEM_NOTE));
      } catch (DOMParseException ex) {
        xml = null;
      }
      if (xml != null) {
        for (Element el : XmlUtils
            .getChildrenElements(xml.getDocumentElement())) {
          String name = el.getNodeName();

          if (BeeUtils.equalsTrim(name, COL_ASSESSMENT)) {
            assessmentId = BeeUtils.toLongOrNull(XmlUtils.getText(el));
          } else if (BeeUtils.equalsTrim(name, "Parent" + COL_ASSESSMENT)) {
            parentId = BeeUtils.toLongOrNull(XmlUtils.getText(el));
          }
        }

        primary = !DataUtils.isId(parentId);
      }

      if (!DataUtils.isId(assessmentId) && !DataUtils.isId(parentId)) {
        continue;
      }

      if (primary) {

        if (!assessments.containsKey(assessmentId)) {
          assessments.put(assessmentId, Lists.newArrayList(simpleRow));
        } else {
          List<SimpleRowSet.SimpleRow> r =
              insertByOrderOrMerge(assessments.get(assessmentId), simpleRow);
          assessments.put(assessmentId, r);
        }
      } else {
        if (!childAssessments.containsKey(parentId)) {
          childAssessments.put(parentId, Lists.newArrayList(simpleRow));
        } else {
          childAssessments.get(parentId).add(simpleRow);
        }
      }
    }

    for (Long key : childAssessments.keySet()) {
      for (SimpleRowSet.SimpleRow row : childAssessments.get(key)) {
        List<SimpleRowSet.SimpleRow> r =
            insertByOrderOrMerge(assessments.get(key), row);
        assessments.put(key, r);
      }
    }

    for (Long key : assessments.keySet()) {
      for (SimpleRowSet.SimpleRow row : assessments.get(key)) {
        result.addRow(row.getValues());
      }
    }

    if (result.isEmpty()) {
      return data; // collectWidgetInfo throws error if row set is empty.
    }
    return result;
  }

  private static List<SimpleRowSet.SimpleRow> insertByOrderOrMerge(List<SimpleRowSet.SimpleRow> a,
      SimpleRowSet.SimpleRow b) {
    if (a == null) {
      List<SimpleRowSet.SimpleRow> emptylist = Lists.newArrayList();
      return insertByOrderOrMerge(emptylist, b);
    }

    for (SimpleRowSet.SimpleRow c : a) {
      SimpleRowSet.SimpleRow merged = maybeItemsMerge(c, b);

      if (merged != null) {
        int idx = a.indexOf(c);
        a.set(idx, merged);
        return a;
      }
    }

    a.add(b);

    return a;

  }

  private static boolean isEqualAssessmentItems(SimpleRowSet.SimpleRow a,
      SimpleRowSet.SimpleRow b) {
    Assert.notNull(a);
    Assert.notNull(b);

    return BeeUtils.equalsTrim(a.getValue(COL_TRADE_VAT_PLUS), b.getValue(COL_TRADE_VAT_PLUS))
        && BeeUtils.equalsTrim(a.getValue(COL_TRADE_VAT), b.getValue(COL_TRADE_VAT))
        && BeeUtils.equalsTrim(a.getValue(COL_TRADE_VAT_PERC), b.getValue(COL_TRADE_VAT_PERC));

  }

  private static SimpleRowSet.SimpleRow maybeItemsMerge(SimpleRowSet.SimpleRow a,
      SimpleRowSet.SimpleRow b) {
    if (isEqualAssessmentItems(a, b)) {
      SimpleRowSet resRs = new SimpleRowSet(a.getColumnNames());
      resRs.addRow(a.getValues());
      SimpleRowSet.SimpleRow res = resRs.getRow(resRs.getNumberOfRows() - 1);

      double qnt = BeeUtils.unbox(a.getDouble(COL_TRADE_ITEM_QUANTITY));
      double price = BeeUtils.unbox(a.getDouble(COL_TRADE_ITEM_PRICE)) * qnt;

      price +=
          BeeUtils.unbox(b.getDouble(COL_TRADE_ITEM_QUANTITY))
              * BeeUtils.unbox(b.getDouble(COL_TRADE_ITEM_PRICE));

      res.setValue(COL_TRADE_ITEM_QUANTITY, "1");
      res.setValue(COL_TRADE_ITEM_PRICE, BeeUtils.toString(price));

      return res;
    }

    return null;
  }
}