package com.butent.bee.client.composite;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;

import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.HandlesRendering;
import com.butent.bee.client.render.RendererFactory;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Launchable;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.Relation;

import java.util.List;

public class UnboundSelector extends DataSelector implements HandlesRendering, Launchable {

  public static UnboundSelector create(Relation relation) {
    Assert.notNull(relation);
    return create(relation, relation.getChoiceColumns());
  }

  public static UnboundSelector create(Relation relation, AbstractCellRenderer renderer) {
    Assert.notNull(relation);
    Assert.notNull(renderer);

    UnboundSelector selector = new UnboundSelector(relation);
    selector.setRenderer(renderer);

    return selector;
  }

  public static UnboundSelector create(Relation relation, List<String> renderColumns) {
    Assert.notNull(relation);
    return create(relation, RendererFactory.createRenderer(relation.getViewName(), renderColumns));
  }

  public static UnboundSelector create(String viewName, List<String> columns) {
    Assert.notEmpty(viewName);
    Assert.notEmpty(columns);
    return create(Relation.create(viewName, columns));
  }

  private AbstractCellRenderer renderer = null;
  private String renderedValue = null;

  private boolean handledByForm = false;

  public UnboundSelector(Relation relation) {
    super(relation, true);
  }

  @Override
  public void clearValue() {
    super.clearValue();
    setRenderedValue(null);
  }

  @Override
  public String getIdPrefix() {
    return "unbound";
  }

  @Override
  public AbstractCellRenderer getRenderer() {
    return renderer;
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.UNBOUND_SELECTOR;
  }

  @Override
  public void launch() {
    setHandledByForm(true);
  }

  @Override
  public void render(IsRow row) {
    if (getRenderer() != null) {
      if (row == null) {
        setRenderedValue(null);
      } else {
        setRenderedValue(getRenderer().render(row));
      }
    }
  }

  @Override
  public void setRenderer(AbstractCellRenderer renderer) {
    this.renderer = renderer;
  }

  @Override
  public void setSelection(BeeRow row) {
    super.setSelection(row);
    render(row);
  }

  @Override
  protected void onLoad() {
    super.onLoad();

    if (!isHandledByForm()) {
      addFocusHandler(new FocusHandler() {
        @Override
        public void onFocus(FocusEvent event) {
          setEditing(true);
        }
      });
    }

    addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        updateDisplay(getRenderedValue());
        if (!isHandledByForm()) {
          setEditing(false);
        }
      }
    });

    addEditStopHandler(new EditStopEvent.Handler() {
      @Override
      public void onEditStop(EditStopEvent event) {
        if (getRenderer() != null) {
          if (event.isChanged()) {
            render(getRelatedRow());
          }
          updateDisplay(getRenderedValue());

          if (!isHandledByForm()) {
            UiHelper.moveFocus(getParent(), getElement(), true);
          }
        }
      }
    });
  }

  private String getRenderedValue() {
    return renderedValue;
  }

  private boolean isHandledByForm() {
    return handledByForm;
  }

  private void setHandledByForm(boolean handledByForm) {
    this.handledByForm = handledByForm;
  }

  private void setRenderedValue(String renderedValue) {
    this.renderedValue = renderedValue;
  }
}
