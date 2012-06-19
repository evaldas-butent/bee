package com.butent.bee.client.view;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.view.navigation.PagerView;
import com.butent.bee.client.view.navigation.SimplePager;
import com.butent.bee.client.view.search.SearchBox;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.event.SelectionCountChangeEvent;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

/**
 * Implements styling and user command capture for data footers.
 */

public class FooterImpl extends Absolute implements FooterView, HasNavigation, HasSearch {

  private static final String STYLE_CONTAINER = "bee-FooterContainer";
  private static final String STYLE_PAGER = "bee-SimplePager";
  private static final String STYLE_SEARCH = "bee-FooterSearch";
  private static final String STYLE_SELECTION_COUNTER = "bee-SelectionCounter";

  private static final int HEIGHT = 32;

  private static final int TOP = 2;
  private static final int SPACING = 15;
  private static final int HORIZONTAL_MARGIN = 6;

  private static final int PAGER_WIDTH = 256;
  private static final int SELECTION_COUNTER_WIDTH = 32;

  private Presenter viewPresenter = null;

  private String pagerId = null;
  private String searchId = null;
  private String selectionCounterId = null;

  private boolean adjusted = false;

  private boolean enabled = true;

  public FooterImpl() {
    super();
    addStyleName(StyleUtils.WINDOW_FOOTER);
  }

  public void create(int rowCount, boolean addPaging, boolean showPageSize, boolean addSearch) {
    addStyleName(STYLE_CONTAINER);

    int left = HORIZONTAL_MARGIN;

    if (addPaging) {
      SimplePager pager = new SimplePager(rowCount, showPageSize);
      pager.addStyleName(STYLE_PAGER);
      add(pager, left, TOP);
      left += PAGER_WIDTH + SPACING;
      pagerId = pager.getWidgetId();
    }

    if (addSearch) {
      SearchBox search = new SearchBox();
      search.addStyleName(STYLE_SEARCH);
      add(search, left, TOP);
      StyleUtils.setRight(search, SPACING + SELECTION_COUNTER_WIDTH + HORIZONTAL_MARGIN);
      searchId = search.getWidgetId();
    }

    BeeLabel selectionCounter = new BeeLabel();
    selectionCounter.addStyleName(STYLE_SELECTION_COUNTER);
    add(selectionCounter);
    selectionCounterId = selectionCounter.getId();
  }

  public int getHeight() {
    return HEIGHT;
  }

  public Collection<PagerView> getPagers() {
    return ViewHelper.getPagers(this);
  }

  public Collection<SearchView> getSearchers() {
    return ViewHelper.getSearchers(this);
  }

  public Presenter getViewPresenter() {
    return viewPresenter;
  }

  public String getWidgetId() {
    return getId();
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void onSelectionCountChange(SelectionCountChangeEvent event) {
    Assert.notNull(event);
    if (selectionCounterId != null) {
      int cnt = event.getCount();
      String text = (cnt > 0) ? BeeUtils.toString(cnt) : BeeConst.STRING_EMPTY;
      DomUtils.setText(selectionCounterId, text);
    }
  }

  public void setEnabled(boolean enabled) {
    if (enabled == isEnabled()) {
      return;
    }
    this.enabled = enabled;
    DomUtils.enableChildren(this, enabled);
  }

  public void setViewPresenter(Presenter viewPresenter) {
    this.viewPresenter = viewPresenter;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    if (!adjusted) {
      adjusted = true;
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        public void execute() {
          adjust();
        }
      });
    }
  }

  private void adjust() {
    if (BeeUtils.isEmpty(pagerId)) {
      return;
    }

    int pagerWidth = DomUtils.getChildOffsetWidth(this, pagerId);
    if (pagerWidth <= 0) {
      return;
    }

    if (!BeeUtils.isEmpty(searchId)) {
      int left = HORIZONTAL_MARGIN + pagerWidth + SPACING;
      StyleUtils.setLeft(DomUtils.getChild(this, searchId), left);
    }
  }
}
