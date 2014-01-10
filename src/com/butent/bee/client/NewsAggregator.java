package com.butent.bee.client;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.Badge;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.news.Headline;
import com.butent.bee.shared.news.NewsConstants;
import com.butent.bee.shared.news.Subscription;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class NewsAggregator {

  private final class HeadlinePanel extends Flow {

    private static final String STYLE_HEADLINE_PREFIX = STYLE_PREFIX + "headline-";
    private static final String STYLE_NEW = STYLE_HEADLINE_PREFIX + "new";
    private static final String STYLE_UPD = STYLE_HEADLINE_PREFIX + "upd";

    private final long dataId;

    private HeadlinePanel(Headline headline) {
      super(STYLE_HEADLINE_PREFIX + "panel");

      this.dataId = headline.getId();

      Flow header = new Flow(STYLE_HEADLINE_PREFIX + "header");

      CustomDiv typeWidget = new CustomDiv(headline.isNew() ? STYLE_NEW : STYLE_UPD);
      header.add(typeWidget);

      Label captionWidget = new Label(headline.getCaption());
      captionWidget.addStyleName(STYLE_HEADLINE_PREFIX + "caption");

      captionWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          showHeadline(HeadlinePanel.this);
        }
      });

      header.add(captionWidget);

      Label closeWidget = new Label(String.valueOf(BeeConst.CHAR_TIMES));
      closeWidget.addStyleName(STYLE_HEADLINE_PREFIX + "close");

      closeWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          closeHeadline(HeadlinePanel.this);
        }
      });

      header.add(closeWidget);

      add(header);

      if (!BeeUtils.isEmpty(headline.getSubtitle())) {
        Label subtitle = new Label(headline.getSubtitle());
        subtitle.addStyleName(STYLE_HEADLINE_PREFIX + "subtitle");

        add(subtitle);
      }
    }

    private long getDataId() {
      return dataId;
    }
    
    private Feed getFeed() {
      for (Widget parent = getParent(); parent != null;  parent = parent.getParent()) {
        if (parent instanceof SubscriptionPanel) {
          return ((SubscriptionPanel) parent).getFeed();
        }
      }
      return null;
    }
  }

  private final class NewsPanel extends Flow {

    private static final String STYLE_LOADING = "bee-News-loading";
    private static final String STYLE_NOT_LOADING = "bee-News-not-loading";

    private final FaLabel loadingWidget;

    private final Flow content;

    private NewsPanel() {
      super(STYLE_PREFIX + "panel");

      Flow header = new Flow(STYLE_PREFIX + "header");

      FaLabel refreshWidget = new FaLabel(FontAwesome.REFRESH);
      refreshWidget.setTitle(Localized.getConstants().actionRefresh());
      refreshWidget.addStyleName(STYLE_PREFIX + "refresh");

      refreshWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          refresh();
        }
      });

      header.add(refreshWidget);

      FaLabel settingsWidget = new FaLabel(FontAwesome.GEAR);
      settingsWidget.setTitle(Localized.getConstants().actionConfigure());
      settingsWidget.addStyleName(STYLE_PREFIX + "settings");

      settingsWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          GridOptions gridOptions = GridOptions.forCurrentUserFilter(NewsConstants.COL_UF_USER);
          GridFactory.openGrid(NewsConstants.GRID_USER_FEEDS, gridOptions);
        }
      });

      header.add(settingsWidget);

      this.loadingWidget = new FaLabel(FontAwesome.SPINNER);
      loadingWidget.addStyleName(STYLE_NOT_LOADING);

      header.add(loadingWidget);

      add(header);

      this.content = new Flow(STYLE_PREFIX + "content");
      add(content);
    }

    private void addSubscriptionPanel(SubscriptionPanel subscriptionPanel) {
      content.add(subscriptionPanel);
    }

    private void clearSubscriptions() {
      if (!content.isEmpty()) {
        content.clear();
      }
    }

    private void endRefresh() {
      loadingWidget.addStyleName(STYLE_NOT_LOADING);
      loadingWidget.removeStyleName(STYLE_LOADING);
    }

    private void startRefresh() {
      loadingWidget.addStyleName(STYLE_LOADING);
      loadingWidget.removeStyleName(STYLE_NOT_LOADING);
    }
  }

  private final class SubscriptionPanel extends Flow {

    private static final String STYLE_SUBSCRIPTION_PREFIX = STYLE_PREFIX + "subscription-";
    private static final String STYLE_OPEN = STYLE_SUBSCRIPTION_PREFIX + "open";
    private static final String STYLE_CLOSED = STYLE_SUBSCRIPTION_PREFIX + "closed";

    private final Feed feed;

    private final FaLabel disclosure;
    private final Badge newSize;
    private final Badge updSize;

    private final Flow content;

    private boolean open;

    private SubscriptionPanel(Subscription subscription, boolean open) {
      super(STYLE_SUBSCRIPTION_PREFIX + "panel");
      addStyleName(open ? STYLE_OPEN : STYLE_CLOSED);

      this.feed = subscription.getFeed();
      this.open = open;

      Flow header = new Flow(STYLE_SUBSCRIPTION_PREFIX + "header");

      this.disclosure = new FaLabel(open ? FontAwesome.CARET_DOWN : FontAwesome.CARET_RIGHT);
      disclosure.addStyleName(STYLE_SUBSCRIPTION_PREFIX + "disclosure");

      disclosure.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          toggleOpen();
        }
      });

      header.add(disclosure);

      Label feedLabel = new Label(subscription.getLabel());
      feedLabel.addStyleName(STYLE_SUBSCRIPTION_PREFIX + "label");

      feedLabel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          toggleOpen();
        }
      });

      header.add(feedLabel);

      FaLabel show = new FaLabel(FontAwesome.TABLE);
      show.addStyleName(STYLE_SUBSCRIPTION_PREFIX + "show");

      show.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          showSubsciption();
        }
      });

      header.add(show);

      this.newSize = new Badge(subscription.countNew(), STYLE_SUBSCRIPTION_PREFIX + "new-size");
      header.add(newSize);

      this.updSize = new Badge(subscription.countUpdated(), STYLE_SUBSCRIPTION_PREFIX + "upd-size");
      header.add(updSize);

      add(header);

      this.content = new Flow(STYLE_SUBSCRIPTION_PREFIX + "content");

      for (Headline headline : subscription.getHeadlines()) {
        HeadlinePanel headlinePanel = new HeadlinePanel(headline);
        content.add(headlinePanel);
      }

      add(content);
    }

    private Feed getFeed() {
      return feed;
    }

    private boolean isOpen() {
      return open;
    }

    private void setOpen(boolean open) {
      this.open = open;
    }

    private void toggleOpen() {
      setOpen(!isOpen());

      addStyleName(isOpen() ? STYLE_OPEN : STYLE_CLOSED);
      disclosure.setChar(isOpen() ? FontAwesome.CARET_DOWN : FontAwesome.CARET_RIGHT);
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(NewsAggregator.class);

  private static final String STYLE_PREFIX = "bee-News-";

  private static void showHeadline(HeadlinePanel headlinePanel) {
    Feed feed = headlinePanel.getFeed();
    if (feed != null) {
      RowEditor.openRow(feed.getHeadlineView(), headlinePanel.getDataId(), false, null);
    }
  }

  private final List<Subscription> subscriptions = Lists.newArrayList();

  private final NewsPanel newsPanel = new NewsPanel();

  private Badge sizeBadge;

  NewsAggregator() {
  }

  public int countNews() {
    int count = 0;
    for (Subscription subscription : subscriptions) {
      count += subscription.size();
    }
    return count;
  }

  public IdentifiableWidget getNewsPanel() {
    return newsPanel;
  }

  public boolean hasNews() {
    for (Subscription subscription : subscriptions) {
      if (!subscription.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  public void loadSubscriptions(String serialized) {
    String[] arr = Codec.beeDeserializeCollection(serialized);

    if (ArrayUtils.isEmpty(arr)) {
      logger.severe("cannot deserialize subscriptions");

    } else {
      clear(false);

      for (String s : arr) {
        final Subscription subscription = Subscription.restore(s);
        subscriptions.add(subscription);

        if (!subscription.isEmpty()) {
          SubscriptionPanel subscriptionPanel = new SubscriptionPanel(subscription, false);
          newsPanel.addSubscriptionPanel(subscriptionPanel);
        }
      }

      updateHeader();
      logger.info("subscriptions", subscriptions.size(), countNews());
    }
  }

  public void onAccess(String viewName, long rowId) {
    String table = Data.getViewTable(viewName);

    if (NewsConstants.hasUsageTable(table)) {
      ParameterList parameters = BeeKeeper.getRpc().createParameters(Service.ACCESS);
      parameters.addQueryItem(Service.VAR_TABLE, table);
      parameters.addQueryItem(Service.VAR_ID, rowId);

      BeeKeeper.getRpc().makeRequest(parameters);
    }
  }

  public void refresh() {
    newsPanel.startRefresh();

    BeeKeeper.getRpc().makeGetRequest(Service.GET_NEWS, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (!response.hasErrors()) {
          if (response.hasResponse()) {
            loadSubscriptions(response.getResponseAsString());
          } else {
            clear(true);
          }
        }

        newsPanel.endRefresh();
      }
    });
  }

  private void clear(boolean clearSizeBadge) {
    if (!subscriptions.isEmpty()) {
      subscriptions.clear();
    }
    newsPanel.clearSubscriptions();

    if (clearSizeBadge && getSizeBadge() != null) {
      getSizeBadge().setValue(0);
    }
  }

  private void closeHeadline(HeadlinePanel headlinePanel) {
    Feed feed = headlinePanel.getFeed();
    if (feed != null) {
      onAccess(feed.getHeadlineView(), headlinePanel.getDataId());
    }
  }

  private Badge getSizeBadge() {
    return sizeBadge;
  }

  private void setSizeBadge(Badge sizeBadge) {
    this.sizeBadge = sizeBadge;
  }

  private void showSubsciption() {

  }

  private void updateHeader() {
    Flow header = BeeKeeper.getScreen().getDomainHeader(Domain.NEWS, null);
    if (header == null) {
      return;
    }

    int size = countNews();

    if (getSizeBadge() == null) {
      Badge badge = new Badge(size, STYLE_PREFIX + "size");

      header.add(badge);
      setSizeBadge(badge);

    } else {
      getSizeBadge().update(size);
    }
  }
}
