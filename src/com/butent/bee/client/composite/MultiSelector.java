package com.butent.bee.client.composite;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.HandlesRendering;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MultiSelector extends DataSelector implements HandlesRendering {
  
  private class ChoiceWidget extends Flow {
    
    private final long rowId;

    private ChoiceWidget(long rowId, String caption) {
      super();
      this.rowId = rowId;
      
      addStyleName(STYLE_CHOICE);
      
      InlineLabel label = new InlineLabel(caption);
      label.addStyleName(STYLE_LABEL);

      add(label);
      
      BeeImage close = new BeeImage(Global.getImages().closeSmall());
      close.addStyleName(STYLE_CLOSE);
      close.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          MultiSelector.this.removeChoice(getRowId());
        }
      });
      
      add(close);
    }

    @Override
    public String getIdPrefix() {
      return "choice";
    }

    private long getRowId() {
      return rowId;
    }
  }

  private static final String STYLE_PREFIX = "bee-MultiSelector-";
  private static final String STYLE_CONTAINER = STYLE_PREFIX + "container";
  private static final String STYLE_CONTAINER_ACTIVE = STYLE_CONTAINER + "-active";
  private static final String STYLE_CHOICE = STYLE_PREFIX + "choice";
  private static final String STYLE_LABEL = STYLE_PREFIX + "label";
  private static final String STYLE_CLOSE = STYLE_PREFIX + "close";
  private static final String STYLE_INPUT = STYLE_PREFIX + "input";
  
  private final String rowProperty;
  
  private AbstractCellRenderer renderer;
  
  private final Map<Long, String> cache = Maps.newHashMap();
  
  private String oldValue = null;
  
  public MultiSelector(Relation relation, boolean embedded, String rowProperty) {
    super(relation, embedded);
    this.rowProperty = rowProperty;
  }

  @Override
  public String getIdPrefix() {
    return "multi";
  }

  @Override
  public AbstractCellRenderer getRenderer() {
    return renderer;
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.MULTI_SELECTOR;
  }

  @Override
  public boolean isOrHasPartner(Node node) {
    return super.isOrHasPartner(node) || getElement().isOrHasChild(node);
  }

  @Override
  public void render(IsRow row) {
    String value = (row == null) ? null : row.getProperty(rowProperty);
    setOldValue(value);
    setValue(value);

    if (BeeUtils.isEmpty(value)) {
      clearChoices();
      setAdditionalFilter(null);
      return;
    }
    
    final List<Long> choices = DataUtils.parseIdList(value);
    if (choices.isEmpty()) {
      clearChoices();
      setAdditionalFilter(null);
      return;
    }
    
    if (cache.keySet().containsAll(choices)) {
      renderChoices(choices);
      return;
    }
    
    Set<Long> notCached = Sets.newHashSet(choices);
    notCached.removeAll(cache.keySet());
    
    getData(notCached, new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        if (result != null) {
          for (BeeRow r : result.getRows()) {
            cache.put(r.getId(), getRenderer().render(r));
          }
        }
        renderChoices(choices);
      }
    });
  }

  @Override
  public void setRenderer(AbstractCellRenderer renderer) {
    this.renderer = renderer;
  }
  
  @Override
  public void setSelection(BeeRow row) {
    hideSelector();
    clearDisplay();
    reset();
    
    if (row != null) {
      String label = renderer.render(row);
      cache.put(row.getId(), label);

      InsertPanel container = getContainer();
      container.insert(new ChoiceWidget(row.getId(), label), container.getWidgetCount() - 1);
      
      updateValue();
      setFocus(true);
      
      SelectorEvent.fire(this, State.CHANGED);
    }
  }
  
  @Override
  protected void exit(boolean hideSelector, State state, Integer keyCode, boolean hasModifiers) {
    State st = BeeUtils.same(getOldValue(), getValue()) ? state : State.CHANGED;
    super.exit(hideSelector, st, keyCode, hasModifiers);
  }

  @Override
  protected void init(InputWidget inputWidget, boolean embed) {
    final Flow container = new Flow();
    container.addStyleName(STYLE_CONTAINER);
    
    inputWidget.addStyleName(STYLE_INPUT);
    container.add(inputWidget);
    
    inputWidget.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        container.addStyleName(STYLE_CONTAINER_ACTIVE);
      }
    });
    inputWidget.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        container.removeStyleName(STYLE_CONTAINER_ACTIVE);
      }
    });
    
    inputWidget.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE) {
          onBackSpace();
        }
      }
    });
    
    initWidget(container);
  }
  
  private void clearChoices() {
    InsertPanel container = getContainer();
    int count = container.getWidgetCount();
    for (int i = 0; i < count - 1; i++) {
      container.remove(0);
    }
  }

  private InsertPanel getContainer() {
    return (InsertPanel) getWidget();
  }
  
  private void getData(Collection<Long> choices, Queries.RowSetCallback callback) {
    int size = choices.size();
    
    BeeRowSet cached = getOracle().getViewData();
    if (cached != null && cached.getNumberOfRows() >= size) {
      BeeRowSet result = new BeeRowSet(cached.getViewName(), cached.getColumns());
      
      for (BeeRow row : cached.getRows()) {
        if (choices.contains(row.getId())) {
          result.addRow(row);
          if (result.getNumberOfRows() == size) {
            callback.onSuccess(result);
            return;
          }
        }
      }
    }

    Queries.getRowSet(getOracle().getViewName(), null, Filter.idIn(choices),
        getOracle().getViewOrder(), callback);
  }

  private String getOldValue() {
    return oldValue;
  }
  
  private void onBackSpace() {
    if (getInput().getCursorPos() == 0 && !BeeUtils.isEmpty(getValue())) {
      InsertPanel container = getContainer();
      int count = container.getWidgetCount();

      if (count > 1) {
        Widget child = container.getWidget(count - 2);
        if (child instanceof ChoiceWidget) {
          hideSelector();
          removeChoice(((ChoiceWidget) child).getRowId());
        }
      }
    }
  }
  
  private void removeChoice(long rowId) {
    InsertPanel container = getContainer();
    int count = container.getWidgetCount();

    for (int i = 0; i < count - 1; i++) {
      Widget child = container.getWidget(i);
      if (child instanceof ChoiceWidget && ((ChoiceWidget) child).getRowId() == rowId) {
        container.remove(i);
        SelectorEvent.fire(this, State.CHANGED);
        break;
      }
    }
    
    updateValue();
    setFocus(true);
  }
  
  private void renderChoices(List<Long> choices) {
    clearChoices();
    InsertPanel container = getContainer();

    for (Long rowId : choices) {
      container.insert(new ChoiceWidget(rowId, cache.get(rowId)), container.getWidgetCount() - 1);
    }
    
    setAdditionalFilter(Filter.idNotIn(choices));
  }

  private void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }
  
  private void updateValue() {
    List<Long> choices = Lists.newArrayList();
    InsertPanel container = getContainer();
    int count = container.getWidgetCount();

    for (int i = 0; i < count - 1; i++) {
      Widget child = container.getWidget(i);
      if (child instanceof ChoiceWidget) {
        choices.add(((ChoiceWidget) child).getRowId());
      }
    }
    
    setValue(DataUtils.buildIdList(choices));
    setAdditionalFilter(Filter.idNotIn(choices));
  }
}
