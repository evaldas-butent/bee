package com.butent.bee.client.view;

import com.google.gwt.dom.client.Style.Unit;

import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.BeeLayoutPanel;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.view.navigation.PageResizer;
import com.butent.bee.client.view.navigation.PagerView;
import com.butent.bee.client.view.navigation.SimplePager;
import com.butent.bee.client.view.search.SearchBox;
import com.butent.bee.client.view.search.SearchView;

import java.util.Collection;

public class DataFooterImpl extends BeeLayoutPanel implements DataFooterView, HasNavigation,
    HasSearch {
  private Presenter viewPresenter = null;

  public DataFooterImpl() {
    super();
    addStyleName(StyleUtils.WINDOW_FOOTER);
  }

  public void create(int rowCount, int pageSize, boolean addPaging, boolean addSearch) {
    SimplePager pager = null;
    PageResizer pageResizer = null;
    SearchBox search = null;

    if (addPaging) {
      pager = new SimplePager(rowCount);
      pageResizer = new PageResizer(pageSize);
    }
    if (addSearch) {
      search = new SearchBox();
    }

    int x = 0;
    int y = 2;
    int w;

    if (pager != null) {
      x += 4;
      w = 256;
      addLeftWidthTop(pager, x, w, y);
      x += w;
    }
    if (pageResizer != null) {
      w = 64;
      addLeftWidthTop(pageResizer, x, w, y);
      x += w;
    }

    if (search != null) {
      addLeftTop(search.asWidget(), x, y + 1);
      setWidgetLeftRight(search, x, Unit.PX, 16, Unit.PX);
    }
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

  public void setViewPresenter(Presenter viewPresenter) {
    this.viewPresenter = viewPresenter;
  }
}
