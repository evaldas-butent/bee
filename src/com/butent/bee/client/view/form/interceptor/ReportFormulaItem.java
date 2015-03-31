package com.butent.bee.client.view.form.interceptor;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.output.Report;
import com.butent.bee.client.output.ReportItem;
import com.butent.bee.client.output.ReportNumericItem;
import com.butent.bee.client.output.ReportValue;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReportFormulaItem extends ReportNumericItem {

  private static final String EXPRESSION = "EXPRESSION";

  private List<Pair<String, ReportItem>> expression = new ArrayList<>();
  private List<Pair<String, ReportItem>> temporaryExpression = new ArrayList<>();

  public ReportFormulaItem(String caption) {
    super(BeeUtils.randomString(10), caption);
  }

  @Override
  public void deserialize(String data) {
    Map<String, String> map = Codec.deserializeMap(data);

    if (!BeeUtils.isEmpty(map)) {
      for (String itemData : Codec.beeDeserializeCollection(map.get(EXPRESSION))) {
        Pair<String, String> pair = Pair.restore(itemData);
        addItem(expression, pair.getA(), ReportItem.restore(pair.getB()));
      }
      super.deserialize(map.get(Service.VAR_DATA));
    }
  }

  public ReportFormulaItem divide(ReportItem item) {
    return addItem(expression, BeeConst.STRING_SLASH, item);
  }

  @Override
  public ReportValue evaluate(SimpleRow row) {
    List<BigDecimal> values = new ArrayList<>();
    BigDecimal previous = null;

    for (Pair<String, ReportItem> pair : expression) {
      BigDecimal value = BeeUtils.nvl(BeeUtils.toDecimalOrNull(pair.getB().evaluate(row)
          .getValue()), BigDecimal.ZERO);

      if (previous == null) {
        previous = value;
      } else {
        switch (pair.getA()) {
          case BeeConst.STRING_PLUS:
            values.add(previous);
            previous = value;
            break;
          case BeeConst.STRING_MINUS:
            values.add(previous);
            previous = value.negate();
            break;
          case BeeConst.STRING_ASTERISK:
            previous = previous.multiply(value, new MathContext(5));
            break;
          case BeeConst.STRING_SLASH:
            if (BeeUtils.isZero(value.doubleValue())) {
              return ReportValue.empty();
            }
            previous = previous.divide(value, new MathContext(5));
            break;
        }
      }
    }
    if (previous != null) {
      values.add(previous);
    }
    BigDecimal result = BigDecimal.ZERO;

    for (BigDecimal value : values) {
      result = result.add(value);
    }
    return ReportValue.of(result.setScale(getPrecision(), RoundingMode.HALF_UP).toPlainString());
  }

  @Override
  public String getCaption() {
    String cap = super.getCaption();

    if (Objects.equals(cap, getExpression())) {
      cap = BeeConst.STRING_EMPTY;
    }
    return cap;
  }

  @Override
  public Widget getExpressionWidget(Report report) {
    Flow container = new Flow(getStyle() + "-expression");
    temporaryExpression.clear();
    temporaryExpression.addAll(expression);
    render(container, report);
    return container;
  }

  @Override
  public String getFormatedCaption() {
    String cap = getCaption();

    if (BeeUtils.isEmpty(cap)) {
      StringBuilder display = new StringBuilder();

      for (Pair<String, ReportItem> pair : expression) {
        if (display.length() > 0) {
          display.append(pair.getA());
        }
        display.append(BeeUtils.embrace(pair.getB().getFormatedCaption()));
      }
      cap = display.toString();
    }
    return cap;
  }

  public ReportFormulaItem minus(ReportItem item) {
    return addItem(expression, BeeConst.STRING_MINUS, item);
  }

  public ReportFormulaItem multiply(ReportItem item) {
    return addItem(expression, BeeConst.STRING_ASTERISK, item);
  }

  public ReportFormulaItem plus(ReportItem item) {
    return addItem(expression, BeeConst.STRING_PLUS, item);
  }

  @Override
  public String saveOptions() {
    if (BeeUtils.isEmpty(temporaryExpression)) {
      return Localized.getMessages().dataNotAvailable(Localized.getConstants().expression());
    }
    expression.clear();
    expression.addAll(temporaryExpression);
    return super.saveOptions();
  }

  @Override
  protected String serialize(String data) {
    Map<String, Object> map = new HashMap<>();
    map.put(Service.VAR_DATA, data);
    map.put(EXPRESSION, expression);
    return super.serialize(Codec.beeSerialize(map));
  }

  private ReportFormulaItem addItem(List<Pair<String, ReportItem>> list, String op,
      ReportItem item) {
    if (item != null) {
      list.add(Pair.of(BeeUtils.notEmpty(op, BeeConst.STRING_PLUS), item));
    }
    return this;
  }

  private void render(final Flow container, final Report report) {
    container.clear();

    for (int i = 0; i < temporaryExpression.size(); i++) {
      final Pair<String, ReportItem> pair = temporaryExpression.get(i);

      if (i > 0) {
        Label sep = new Label(pair.getA());
        sep.addStyleName(getStyle() + "-operator");
        sep.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            final List<String> options = Arrays.asList(BeeConst.STRING_PLUS, BeeConst.STRING_MINUS,
                BeeConst.STRING_ASTERISK, BeeConst.STRING_SLASH);

            Global.choice(Localized.getConstants().operator(), null, options, new ChoiceCallback() {
              @Override
              public void onSuccess(int value) {
                pair.setA(options.get(value));
                render(container, report);
              }
            });
          }
        });
        container.add(sep);
      }
      container.add(ReportItem.renderDnd(pair.getB(), temporaryExpression, i, report, null,
          new Runnable() {
            @Override
            public void run() {
              render(container, report);
            }
          }));
    }
    FaLabel add = new FaLabel(FontAwesome.PLUS, true);
    add.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        final List<ReportItem> items = new ArrayList<>();
        List<String> options = new ArrayList<>();

        for (ReportItem item : report.getItems()) {
          if (item instanceof ReportNumericItem) {
            items.add(item);
            options.add(item.getCaption());
          }
        }
        options.add(Localized.getConstants().formula() + "...");
        options.add(Localized.getConstants().constant() + "...");

        Global.choice(null, null, options, new ChoiceCallback() {
          @Override
          public void onSuccess(int value) {
            if (BeeUtils.isIndex(items, value)) {
              addItem(temporaryExpression, null, BeeUtils.getQuietly(items, value));
              render(container, report);
            } else {
              final ReportItem item = value == items.size()
                  ? new ReportFormulaItem(null) : new ReportConstantItem(null, null);

              item.edit(report, null, new Runnable() {
                @Override
                public void run() {
                  addItem(temporaryExpression, null, item);
                  render(container, report);
                }
              });
            }
          }
        });
      }
    });
    container.add(add);
  }
}
