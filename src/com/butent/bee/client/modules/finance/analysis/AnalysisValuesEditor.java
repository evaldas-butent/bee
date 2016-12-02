package com.butent.bee.client.modules.finance.analysis;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.HandlerRegistration;

import com.butent.bee.client.event.logical.SummaryChangeEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.view.edit.EditChangeHandler;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.modules.finance.analysis.AnalysisCellType;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class AnalysisValuesEditor extends Flow implements Editor {

  static final class Renderer extends AbstractCellRenderer {

    Renderer(CellSource cellSource) {
      super(cellSource);
    }

    @Override
    public String render(IsRow row) {
      String values = getString(row);
      if (BeeUtils.isEmpty(values)) {
        return null;
      }

      List<AnalysisCellType> cellTypes = AnalysisCellType.decode(values);
      if (BeeUtils.isEmpty(cellTypes)) {
        return null;
      }

      StringBuilder sb = new StringBuilder();

      for (AnalysisCellType cellType : cellTypes) {
        if (sb.length() > 0) {
          sb.append(BeeConst.STRING_SPACE);
        }

        sb.append(cellType.getAnalysisValueType().getAbbreviation());
        if (!BeeConst.isUndef(cellType.getScale())) {
          sb.append(BeeConst.STRING_POINT).append(cellType.getScale());
        }
      }

      return sb.toString();
    }
  }

  private static final String STYLE_PREFIX =
      BeeConst.CSS_CLASS_PREFIX + "fin-AnalysisValuesEditor-";

  private static final String STYLE_CONTAINER = STYLE_PREFIX + "container";

  private boolean nullable = true;

  private boolean editing;
  private boolean enabled = true;

  private boolean handlesTabulation;

  private boolean summarize;

  AnalysisValuesEditor() {
    super(STYLE_CONTAINER);
  }

  @Override
  public HandlerRegistration addBlurHandler(BlurHandler blurHandler) {
    return addDomHandler(blurHandler, BlurEvent.getType());
  }

  @Override
  public HandlerRegistration addEditChangeHandler(EditChangeHandler handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  @Override
  public HandlerRegistration addEditStopHandler(EditStopEvent.Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  @Override
  public HandlerRegistration addFocusHandler(FocusHandler handler) {
    return addDomHandler(handler, FocusEvent.getType());
  }

  @Override
  public HandlerRegistration addSummaryChangeHandler(SummaryChangeEvent.Handler handler) {
    return addHandler(handler, SummaryChangeEvent.getType());
  }

  @Override
  public void clearValue() {
  }

  @Override
  public EditorAction getDefaultFocusAction() {
    return null;
  }

  @Override
  public String getIdPrefix() {
    return "ave";
  }

  @Override
  public String getNormalizedValue() {
    return getValue();
  }

  @Override
  public Value getSummary() {
    return BooleanValue.of(!BeeUtils.isEmpty(getValue()));
  }

  @Override
  public int getTabIndex() {
    return getElement().getTabIndex();
  }

  @Override
  public String getValue() {
    return null;
  }

  @Override
  public FormWidget getWidgetType() {
    return null;
  }

  @Override
  public boolean handlesKey(int keyCode) {
    return false;
  }

  @Override
  public boolean handlesTabulation() {
    return handlesTabulation;
  }

  @Override
  public boolean isEditing() {
    return editing;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean isNullable() {
    return nullable;
  }

  @Override
  public boolean isOrHasPartner(Node node) {
    return node != null && getElement().isOrHasChild(node);
  }

  @Override
  public void normalizeDisplay(String normalizedValue) {
  }

  @Override
  public void render(String value) {
    setValue(value);
  }

  @Override
  public void setAccessKey(char key) {
  }

  @Override
  public void setEditing(boolean editing) {
    this.editing = editing;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void setFocus(boolean focused) {
    if (focused) {
      getElement().focus();
    } else {
      getElement().blur();
    }
  }

  @Override
  public void setHandlesTabulation(boolean handlesTabulation) {
    this.handlesTabulation = handlesTabulation;
  }

  @Override
  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  @Override
  public void setSummarize(boolean summarize) {
    this.summarize = summarize;
  }

  @Override
  public void setTabIndex(int index) {
    getElement().setTabIndex(index);
  }

  @Override
  public void setValue(String value) {
  }

  @Override
  public void startEdit(String oldValue, char charCode, EditorAction onEntry,
      Element sourceElement) {
  }

  @Override
  public boolean summarize() {
    return summarize;
  }

  @Override
  public List<String> validate(boolean checkForNull) {
    return BeeConst.EMPTY_IMMUTABLE_STRING_LIST;
  }

  @Override
  public List<String> validate(String normalizedValue, boolean checkForNull) {
    return BeeConst.EMPTY_IMMUTABLE_STRING_LIST;
  }
}
