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
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.news.Headline;
import com.butent.bee.shared.data.news.NewsUtils;
import com.butent.bee.shared.data.news.Subscription;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class NewsAggregator {

  private static final class NewsPanel extends Flow {

    private NewsPanel() {
      super(STYLE_PREFIX + "panel");
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(NewsAggregator.class);

  private static final String STYLE_PREFIX = "bee-News-";

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
      if (!newsPanel.isEmpty()) {
        newsPanel.clear();
      }

      for (String s : arr) {
        final Subscription subscription = Subscription.restore(s);
        subscriptions.add(subscription);

        if (!subscription.isEmpty()) {
          Label subscriptionLabel = new Label(subscription.getLabel());
          subscriptionLabel.addStyleName(STYLE_PREFIX + "feed");
          newsPanel.add(subscriptionLabel);

          for (final Headline headline : subscription.getHeadlines()) {
            Label headlineLabel = new Label(headline.getCaption());
            headlineLabel.addStyleName(STYLE_PREFIX + "headline");

            headlineLabel.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                RowEditor.openRow(subscription.getFeed().getHeadlineView(), headline.getId(),
                    false,
                    null);
              }
            });

            newsPanel.add(headlineLabel);
          }
        }
      }

      logger.info("subscriptions", subscriptions.size(), countNews());
    }
  }

  public void onAccess(String viewName, long rowId) {
    String table = Data.getViewTable(viewName);

    if (NewsUtils.hasUsageTable(table)) {
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
