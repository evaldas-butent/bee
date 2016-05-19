package com.butent.bee.client.layout;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.event.logical.HasSummaryChangeHandlers;
import com.butent.bee.client.event.logical.SummaryChangeEvent;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.Value;

public class SummaryProxy extends Simple implements HasSummaryChangeHandlers {

  private boolean summarize;

  public SummaryProxy() {
    super();
  }

  @Override
  public HandlerRegistration addSummaryChangeHandler(SummaryChangeEvent.Handler handler) {
    return addHandler(handler, SummaryChangeEvent.getType());
  }

  @Override
  public String getIdPrefix() {
    return "summary-proxy";
  }

  @Override
  public Value getSummary() {
    if (getWidget() instanceof HasSummaryChangeHandlers) {
      return ((HasSummaryChangeHandlers) getWidget()).getSummary();
    } else {
      return BooleanValue.FALSE;
    }
  }

  @Override
  public void setSummarize(boolean summarize) {
    this.summarize = summarize;
  }

  @Override
  public void setWidget(Widget w) {
    super.setWidget(w);

    if (w instanceof HasSummaryChangeHandlers) {
      ((HasSummaryChangeHandlers) w).addSummaryChangeHandler(new SummaryChangeEvent.Handler() {
        @Override
        public void onSummaryChange(SummaryChangeEvent event) {
          SummaryChangeEvent.maybeFire(SummaryProxy.this);
        }
      });
    }
  }

  @Override
  public boolean summarize() {
    return summarize;
  }
}
