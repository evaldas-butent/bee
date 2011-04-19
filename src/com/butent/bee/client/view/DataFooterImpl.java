package com.butent.bee.client.view;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.view.client.HasRows;

import com.butent.bee.client.data.PageResizer;
import com.butent.bee.client.data.Pager;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.BeeLayoutPanel;
import com.butent.bee.client.presenter.Presenter;

public class DataFooterImpl extends BeeLayoutPanel implements DataFooterView {
  private Presenter presenter = null;
  private SearchView searchView = null;

  public DataFooterImpl() {
    super();
    addStyleName(StyleUtils.WINDOW_FOOTER);
  }

  public void create(HasRows display, int rowCount, int pageSize,
      boolean addPaging, boolean addSearch) {
    Pager pager = null;
    PageResizer pageResizer = null;
    SearchBox search = null;

    if (addPaging) {
      pager = new Pager(rowCount);
      pager.setDisplay(display);

      pageResizer = new PageResizer(pageSize);
      pageResizer.setDisplay(display);
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
      this.searchView = search;
    }
  }

  public SearchView getSearchView() {
    return searchView;
  }

  public Presenter getViewPresenter() {
    return presenter;
  }

  public void setViewPresenter(Presenter presenter) {
    this.presenter = presenter;
  }
}
