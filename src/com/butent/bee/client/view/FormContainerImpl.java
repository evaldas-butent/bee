package com.butent.bee.client.view;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.view.add.AddEndEvent;
import com.butent.bee.client.view.add.AddStartEvent;
import com.butent.bee.client.view.form.FormImpl;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.navigation.PagerView;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.event.ActiveRowChangeEvent;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

/**
 * Handles such visible components of forms as header and footer.
 */

public class FormContainerImpl extends Split implements FormContainerView, HasNavigation,
    HasSearch, ActiveRowChangeEvent.Handler, AddStartEvent.Handler, AddEndEvent.Handler {

  public static String newRowCaption = "New Row";

  private Presenter viewPresenter = null;

  private String headerId = null;
  private String footerId = null;
  private String commandId = null;

  private int headerHeight = 22;
  private int footerHeight = 32;
  private int commandHeight = 36;

  private boolean hasSearch = false;

  private Evaluator rowMessage = null;

  private boolean enabled = true;

  private String currentCaption = null;

  private boolean hasData = false;
  private int initialRowCount = BeeConst.UNDEF;

  public FormContainerImpl() {
    this(-1);
  }

  public FormContainerImpl(int splitterSize) {
    super(splitterSize);
  }

  public FormContainerImpl(String style) {
    this(style, -1);
  }

  public FormContainerImpl(String style, int splitterSize) {
    super(style, splitterSize);
  }

  public void bind() {
    if (hasHeader()) {
      getContent().getDisplay().addLoadingStateChangeHandler(getHeader());
    }
    if (hasFooter()) {
      getContent().getDisplay().addSelectionCountChangeHandler(getFooter());
    }

    if (getRowMessage() != null) {
      getContent().getDisplay().addActiveRowChangeHandler(this);
    }

    getContent().addAddStartHandler(this);
    getContent().addAddEndHandler(this);
  }

  public void create(FormDescription formDescription, List<BeeColumn> dataColumns, int rowCount) {
    Assert.notNull(formDescription);

    setHasData(!BeeUtils.isEmpty(dataColumns));
    setInitialRowCount(rowCount);

    setHasSearch(hasData() && rowCount >= formDescription.getSearchThreshold());

    DataHeaderView header = new DataHeaderImpl();
    header.create(formDescription.getCaption(), hasData(), formDescription.isReadOnly(), false);

    FormView content = new FormImpl();
    content.create(formDescription, dataColumns);

    DataFooterView footer;
    if (hasData()) {
      footer = new DataFooterImpl();
      footer.create(rowCount, BeeConst.UNDEF, true, false, hasSearch());
    } else {
      footer = null;
    }

    addNorth(header.asWidget(), getHeaderHeight());
    setHeaderId(header.getWidgetId());

    if (footer != null) {
      addSouth(footer.asWidget(), getFooterHeight());
      setFooterId(footer.getWidgetId());
    }
    
    if (hasData()) {
      BeeImage confirm = new BeeImage(Global.getImages().ok(), new BeeCommand() {
        @Override
        public void execute() {
          getContent().prepareForInsert();
        }
      });

      BeeImage cancel = new BeeImage(Global.getImages().cancel(), new BeeCommand() {
        @Override
        public void execute() {
          getContent().finishNewRow(null);
        }
      });
    
      Absolute panel = new Absolute();
      panel.add(confirm);
      panel.add(cancel);
    
      StyleUtils.setLeft(confirm, 10);
      StyleUtils.setRight(cancel, 10);
      StyleUtils.makeAbsolute(confirm);
      StyleUtils.makeAbsolute(cancel);
    
      addSouth(panel, 0);
      setCommandId(panel.getId());
      panel.setVisible(false);
    }
    
    add(content.asWidget(), ScrollBars.BOTH);

    if (hasData()) {
      Calculation rmc = formDescription.getRowMessage();
      if (rmc != null) {
        setRowMessage(Evaluator.create(rmc, null, dataColumns));
      }
    }
  }

  public int getCommandHeight() {
    return commandHeight;
  }
  
  public Widget getCommandPanel() {
    if (BeeUtils.isEmpty(getCommandId())) {
      return null;
    }
    for (Widget widget : getChildren()) {
      if (BeeUtils.same(DomUtils.getId(widget), getCommandId())) {
        return widget;
      }
    }
    return null;
  }

  public FormView getContent() {
    if (getCenter() == null) {
      return null;
    }
    return (FormView) getCenter();
  }

  public DataFooterView getFooter() {
    if (BeeUtils.isEmpty(getFooterId())) {
      return null;
    }
    for (Widget widget : getChildren()) {
      if (widget instanceof DataFooterView) {
        return (DataFooterView) widget;
      }
    }
    return null;
  }

  public int getFooterHeight() {
    return footerHeight;
  }

  public DataHeaderView getHeader() {
    if (BeeUtils.isEmpty(getHeaderId())) {
      return null;
    }
    for (Widget widget : getChildren()) {
      if (widget instanceof DataHeaderView) {
        return (DataHeaderView) widget;
      }
    }
    return null;
  }

  public int getHeaderHeight() {
    return headerHeight;
  }

  @Override
  public String getIdPrefix() {
    return "form-container";
  }

  public Collection<PagerView> getPagers() {
    if (hasData()) {
      return ViewHelper.getPagers(this);
    } else {
      return null;
    }
  }

  public Collection<SearchView> getSearchers() {
    if (hasSearch()) {
      return ViewHelper.getSearchers(this);
    } else {
      return null;
    }
  }

  public Presenter getViewPresenter() {
    return viewPresenter;
  }

  public String getWidgetId() {
    return getId();
  }

  public boolean hasFooter() {
    return !BeeUtils.isEmpty(getFooterId());
  }

  public boolean hasHeader() {
    return !BeeUtils.isEmpty(getHeaderId());
  }

  public boolean isEnabled() {
    return enabled;
  }
  
  public void onActiveRowChange(ActiveRowChangeEvent event) {
    if (event == null || event.getRowValue() == null || getRowMessage() == null) {
      return;
    }
    getRowMessage().update(event.getRowValue());
    String message = getRowMessage().evaluate();

    if (!BeeUtils.isEmpty(message)) {
      getHeader().setCaption(message);
    }
  }

  public void onAddEnd(AddEndEvent event) {
    if (hasHeader()) {
      getHeader().setCaption(getCurrentCaption());
      getHeader().setEnabled(true);
    }
    if (hasFooter()) {
      setWidgetSize(getFooter().asWidget(), getFooterHeight());
      getFooter().asWidget().setVisible(true);
      getFooter().setEnabled(true);
    }

    showNewRowCommands(false);
  }
  
  public void onAddStart(AddStartEvent event) {
    if (hasHeader()) {
      getHeader().setEnabled(false);
      setCurrentCaption(getHeader().getCaption());
      getHeader().setCaption(newRowCaption);
    }
    if (hasFooter()) {
      getFooter().setEnabled(false);
      getFooter().asWidget().setVisible(false);
      setWidgetSize(getFooter().asWidget(), 0);
    }
    
    showNewRowCommands(true);
  }

  public void setCommandHeight(int commandHeight) {
    this.commandHeight = commandHeight;
  }

  public void setEnabled(boolean enabled) {
    if (enabled == isEnabled()) {
      return;
    }
    this.enabled = enabled;
    DomUtils.enableChildren(this, enabled);
  }
  
  public void setFooterHeight(int footerHeight) {
    this.footerHeight = footerHeight;
  }

  public void setHeaderHeight(int headerHeight) {
    this.headerHeight = headerHeight;
  }

  public void setViewPresenter(Presenter viewPresenter) {
    this.viewPresenter = viewPresenter;
    for (Widget child : getChildren()) {
      if (child instanceof View) {
        ((View) child).setViewPresenter(viewPresenter);
      }
    }
  }

  public void start(int rowCount) {
    if (getContent() != null && hasData() && rowCount >= 0) {
      getContent().start(rowCount);

      Collection<PagerView> pagers = getPagers();
      if (pagers != null) {
        for (PagerView pager : pagers) {
          pager.start(getContent().getDisplay());
        }
      }
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    start(getInitialRowCount());
  }

  @Override
  protected void onUnload() {
    if (getViewPresenter() != null) {
      getViewPresenter().onViewUnload();
    }
    super.onUnload();
  }

  private String getCommandId() {
    return commandId;
  }

  private String getCurrentCaption() {
    return currentCaption;
  }

  private String getFooterId() {
    return footerId;
  }

  private String getHeaderId() {
    return headerId;
  }

  private int getInitialRowCount() {
    return initialRowCount;
  }

  private Evaluator getRowMessage() {
    return rowMessage;
  }

  private boolean hasData() {
    return hasData;
  }

  private boolean hasSearch() {
    return hasSearch;
  }

  private void setCommandId(String commandId) {
    this.commandId = commandId;
  }

  private void setCurrentCaption(String currentCaption) {
    this.currentCaption = currentCaption;
  }

  private void setFooterId(String footerId) {
    this.footerId = footerId;
  }

  private void setHasData(boolean hasData) {
    this.hasData = hasData;
  }

  private void setHasSearch(boolean hasSearch) {
    this.hasSearch = hasSearch;
  }

  private void setHeaderId(String headerId) {
    this.headerId = headerId;
  }

  private void setInitialRowCount(int initialRowCount) {
    this.initialRowCount = initialRowCount;
  }

  private void setRowMessage(Evaluator rowMessage) {
    this.rowMessage = rowMessage;
  }
  
  private void showNewRowCommands(boolean show) {
    Widget widget = getCommandPanel();
    if (widget != null) {
      setWidgetSize(widget, show ? getCommandHeight() : 0);
      widget.setVisible(show);
    }
  }
}
