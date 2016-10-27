package com.butent.bee.client.modules.finance;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.DropEvent;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Selectors;
import com.butent.bee.client.event.DndHelper;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.DndDiv;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.finance.TradeAccountsPrecedence;
import com.butent.bee.shared.modules.finance.TradeDimensionsPrecedence;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FinancePostingPrecedenceForm extends AbstractFormInterceptor {

  private enum Type {
    TRADE_ACCOUNTS(COL_TRADE_ACCOUNTS_PRECEDENCE) {
      @SuppressWarnings("unchecked")
      @Override
      <E extends Enum<?>> List<E> parse(String value) {
        return (List<E>) TradeAccountsPrecedence.parse(value);
      }
    },

    TRADE_DIMENSIONS(COL_TRADE_DIMENSIONS_PRECEDENCE) {
      @SuppressWarnings("unchecked")
      @Override
      <E extends Enum<?>> List<E> parse(String value) {
        return (List<E>) TradeDimensionsPrecedence.parse(value);
      }
    };

    private final String source;

    Type(String source) {
      this.source = source;
    }

    abstract <E extends Enum<?>> List<E> parse(String value);
  }

  private static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "fin-posting-precedence-";

  private static final String STYLE_TABLE = STYLE_PREFIX + "table";
  private static final String STYLE_LABEL = STYLE_PREFIX + "label";
  private static final String STYLE_DRAG = STYLE_PREFIX + "drag";

  private final Map<Type, Flow> containers = new HashMap<>();

  FinancePostingPrecedenceForm() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new FinancePostingPrecedenceForm();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (!BeeUtils.isEmpty(name) && widget instanceof Flow) {
      for (Type type : Type.values()) {
        if (BeeUtils.same(type.source, name)) {
          initContainer(type, (Flow) widget);
          break;
        }
      }
    }

    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    if (row != null) {
      containers.forEach((type, container) -> {
        String value = row.getString(getDataIndex(type.source));
        render(type, container, value);
      });
    }

    super.afterRefresh(form, row);
  }

  private void initContainer(final Type type, Flow panel) {
    containers.put(type, panel);

    DndHelper.makeTarget(panel, Collections.singleton(type.name()), null,
        DndHelper.ALWAYS_TARGET, (event, object) -> {
          if (object instanceof Integer) {
            onDrop(event, type, BeeUtils.unbox((Integer) object));
          }
        });
  }

  private void onDrop(DropEvent event, Type type, int sourceOrdinal) {
    int targetOrdinal = BeeConst.UNDEF;

    if (event != null && containers.containsKey(type) && sourceOrdinal >= 0) {
      int y = event.getNativeEvent().getClientY();

      List<Element> labelElements =
          Selectors.getElementsByClassName(containers.get(type).getElement(), STYLE_LABEL);

      for (Element labelElement : labelElements) {
        Element rowElement = DomUtils.getParentRow(labelElement, false);
        if (rowElement == null) {
          rowElement = labelElement;
        }

        if (y > rowElement.getAbsoluteTop() && y < rowElement.getAbsoluteBottom()) {
          targetOrdinal = DomUtils.getDataIndexInt(labelElement);
          break;
        }
      }
    }

    if (!BeeConst.isUndef(targetOrdinal) && sourceOrdinal != targetOrdinal) {
      LogUtils.getRootLogger().debug(sourceOrdinal, targetOrdinal);
    }
  }

  private static void render(Type type, Flow container, String value) {
    if (!container.isEmpty()) {
      container.clear();
    }

    List<Enum<?>> list = type.parse(value);

    if (!BeeUtils.isEmpty(list)) {
      Vertical table = new Vertical(STYLE_TABLE);

      for (Enum<?> e : list) {
        DndDiv widget = new DndDiv(STYLE_LABEL);
        widget.setText(EnumUtils.getCaption(e));

        DomUtils.setDataIndex(widget.getElement(), e.ordinal());
        DndHelper.makeSource(widget, type.name(), e.ordinal(), STYLE_DRAG);

        table.add(widget);
      }

      container.add(table);
    }
  }
}
