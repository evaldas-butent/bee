package com.butent.bee.client.view;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.view.client.SelectionModel;

import java.util.List;

public interface DataView<T> {

  <H extends EventHandler> HandlerRegistration addHandler(final H handler, GwtEvent.Type<H> type);

  void render(SafeHtmlBuilder sb, List<T> values, int start,
      SelectionModel<? super T> selectionModel);

  void replaceAllChildren(List<T> values, SafeHtml html, boolean stealFocus);

  void replaceChildren(List<T> values, int start, SafeHtml html, boolean stealFocus);

  void resetFocus();

  void setKeyboardSelected(int index, boolean selected, boolean stealFocus);

  void setLoadingState(LoadingState state);
}
