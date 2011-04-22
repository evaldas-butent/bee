package com.butent.bee.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.view.navigation.PageResizer;
import com.butent.bee.client.view.navigation.PagerView;
import com.butent.bee.client.view.navigation.SimplePager;
import com.butent.bee.client.view.search.SearchBox;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

public class DataFooterImpl extends Absolute implements DataFooterView, HasNavigation, HasSearch {
  
  public interface Resources extends ClientBundle {
    @Source("DataFooterImpl.css")
    Style footerStyle();
  }

  public interface Style extends CssResource {
    String container();

    int horizontalMargin();
    
    String pageResizer();

    int resizerWidth();
    
    String search();
    
    String simplePager();
    
    int spacing();
    
    int top();
  }
  
  private static Resources defaultResources = null;
  private static Style defaultStyle = null;
  
  private static Resources getDefaultResources() {
    if (defaultResources == null) {
      defaultResources = GWT.create(Resources.class);
    }
    return defaultResources; 
  }
  
  private static Style getDefaultStyle() {
    if (defaultStyle == null) {
      defaultStyle = getDefaultResources().footerStyle();
      defaultStyle.ensureInjected();
    }
    return defaultStyle;
  }
  
  private Presenter viewPresenter = null;

  private String pagerId = null;
  private String resizerId = null;
  private String searchId = null;
  
  private boolean adjusted = false;
  
  public DataFooterImpl() {
    super();
    addStyleName(StyleUtils.WINDOW_FOOTER);
  }

  public void create(int rowCount, int pageSize, boolean addPaging, boolean addSearch) {
    Style style = getDefaultStyle();
    addStyleName(style.container());
    
    int top = style.top();
    int spacing = style.spacing();
    int margin = style.horizontalMargin();
    
    int pagerWidth = 256;
    int resizerWidth = style.resizerWidth();
    
    int left = margin;
    
    if (addPaging) {
      SimplePager pager = new SimplePager(rowCount);
      pager.addStyleName(style.simplePager());
      add(pager, left, top);
      left += pagerWidth + spacing;
      pagerId = pager.getWidgetId();
      
      PageResizer pageResizer = new PageResizer(pageSize);
      pageResizer.addStyleName(style.pageResizer());
      add(pageResizer, left, top);
      left += resizerWidth + spacing;
      resizerId = pageResizer.getWidgetId();
    }

    if (addSearch) {
      SearchBox search = new SearchBox();
      search.addStyleName(style.search());
      add(search, left, top);
      searchId = search.getWidgetId();
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

  public String getWidgetId() {
    return getId();
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

    Style style = getDefaultStyle();
    int spacing = style.spacing();
    int resizerWidth = style.resizerWidth();
    int margin = style.horizontalMargin();
    
    int left = margin + pagerWidth + spacing;
    
    if (!BeeUtils.isEmpty(resizerId)) {
      StyleUtils.setLeft(DomUtils.getChild(this, resizerId), left);
      left += resizerWidth + spacing;
    }
    if (!BeeUtils.isEmpty(searchId)) {
      StyleUtils.setLeft(DomUtils.getChild(this, searchId), left);
    }
  }
}
