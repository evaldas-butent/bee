package com.butent.bee.client;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Label;
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

  private final class NewsPanel extends Flow {
    
    private NewsPanel() {
      super(STYLE_PREFIX + "panel");

      FaLabel refreshWidget = new FaLabel(FontAwesome.REFRESH);
      refreshWidget.setTitle(Localized.getConstants().actionRefresh());
      refreshWidget.addStyleName(STYLE_PREFIX + "refresh");

      refreshWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          refresh();
        }
      });

      add(refreshWidget);
    }
    
    private void addSubscriptionPanel(SubscriptionPanel subscriptionPanel) {
      int count = getWidgetCount();
      insert(subscriptionPanel, count - 1);
    }
    
    private void clearSubscriptions() {
      while (getWidgetCount() > 1) {
        remove(0);
      }
    }
  }

  private final class SubscriptionPanel extends Flow {
    
    private final Feed feed;
    
    private SubscriptionPanel(Subscription subscription) {
      super(STYLE_SUBSCRIPTION_PREFIX + "panel");
      this.feed = subscription.getFeed();

      Label subscriptionLabel = new Label(subscription.getLabel());
      subscriptionLabel.addStyleName(STYLE_PREFIX + "feed");
      add(subscriptionLabel);

      for (final Headline headline : subscription.getHeadlines()) {
        Label headlineLabel = new Label(headline.getCaption());
        headlineLabel.addStyleName(STYLE_PREFIX + "headline");

        headlineLabel.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            RowEditor.openRow(feed.getHeadlineView(), headline.getId(), false, null);
          }
        });

        add(headlineLabel);
        
        if (!BeeUtils.isEmpty(headline.getSubtitle())) {
          Label subtitle = new Label(headline.getSubtitle());
          subtitle.addStyleName(STYLE_PREFIX + "subtitle");
          
          add(subtitle);
        }
      }
    }
    
  }
  
  private static final BeeLogger logger = LogUtils.getLogger(NewsAggregator.class);

  private static final String STYLE_PREFIX = "bee-News-";
  private static final String STYLE_SUBSCRIPTION_PREFIX = STYLE_PREFIX + "subscription-";

  private final NewsPanel newsPanel = new NewsPanel();

  private final List<Subscription> subscriptions = Lists.newArrayList();

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
      if (!subscriptions.isEmpty()) {
        subscriptions.clear();
      }
      newsPanel.clearSubscriptions();

      for (String s : arr) {
        final Subscription subscription = Subscription.restore(s);
        subscriptions.add(subscription);

        if (!subscription.isEmpty()) {
          SubscriptionPanel subscriptionPanel = new SubscriptionPanel(subscription);
          newsPanel.addSubscriptionPanel(subscriptionPanel);
        }
      }

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
    BeeKeeper.getRpc().makeGetRequest(Service.GET_NEWS, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (!response.hasErrors() && response.hasResponse()) {
          loadSubscriptions(response.getResponseAsString());
        }
      }
    });
  }
}
