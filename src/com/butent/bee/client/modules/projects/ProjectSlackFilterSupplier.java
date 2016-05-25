package com.butent.bee.client.modules.projects;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.view.search.AbstractFilterSupplier;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.FilterValue;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.projects.ProjectStatus;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

class ProjectSlackFilterSupplier extends AbstractFilterSupplier {

  private enum Slack {
    LATE {
      @Override
      Filter getFilter() {
        return Filter.and(Filter.isLessEqual(COL_PROJECT_STATUS, IntegerValue
            .of(ProjectStatus.ACTIVE)),
            Filter.isLess(COL_DATES_END_DATE, new DateValue(new JustDate())));
      }

      @Override
      String getLabel() {
        return Localized.dictionary().crmTaskLabelLate();
      }

      @Override
      String getValue() {
        return VALUE_LATE;
      }
    },

    SCHEDULED {
      @Override
      Filter getFilter() {
        return Filter.and(Filter.isLessEqual(COL_PROJECT_STATUS, IntegerValue
            .of(ProjectStatus.ACTIVE)),
            Filter.isMoreEqual(COL_DATES_END_DATE, new DateValue(new JustDate())));
      }

      @Override
      String getLabel() {
        return Localized.dictionary().prjLabelNotLate();
      }

      @Override
      String getValue() {
        return VALUE_SCHEDULED;
      }
    };

    private static Slack parseValue(String value) {
      for (Slack slack : Slack.values()) {
        if (BeeUtils.same(value, slack.getValue())) {
          return slack;
        }
      }
      return null;
    }

    abstract Filter getFilter();

    abstract String getLabel();

    abstract String getValue();
  }

  private Slack slack;

  ProjectSlackFilterSupplier(String options) {
    super(VIEW_PROJECTS, null, null, options);
  }

  @Override
  public String getComponentLabel(String ownerLabel) {
    return getLabel();
  }

  @Override
  public FilterValue getFilterValue() {
    return (getSlack() == null) ? null : FilterValue.of(getSlack().getValue());
  }

  @Override
  public String getLabel() {
    return (getSlack() == null) ? null : getSlack().getLabel();
  }

  @Override
  public void onRequest(Element target, ScheduledCommand onChange) {
    openDialog(target, createWidget(), null, onChange);
  }

  @Override
  public Filter parse(FilterValue input) {
    Slack s = (input == null) ? null : Slack.parseValue(input.getValue());
    return (s == null) ? null : s.getFilter();
  }

  @Override
  public void setFilterValue(FilterValue filterValue) {
    setSlack((filterValue == null) ? null : Slack.parseValue(filterValue.getValue()));
  }

  @Override
  protected String getStylePrefix() {
    return BeeConst.CSS_CLASS_PREFIX + FILTERSUPPLIER_SLACK_PREFIX;
  }

  private Widget createWidget() {
    Flow container = new Flow();
    container.addStyleName(getStylePrefix() + STYLE_CONTAINER);

    Button late = new Button(Localized.dictionary().crmTaskFilterLate());
    late.addStyleName(getStylePrefix() + STYLE_LATE);

    late.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        boolean changed = !Slack.LATE.equals(getSlack());
        setSlack(Slack.LATE);
        update(changed);
      }
    });

    container.add(late);

    Button scheduled = new Button(Localized.dictionary().prjFilterNotLate());
    scheduled.addStyleName(getStylePrefix() + STYLE_NOT_LATE);

    scheduled.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        boolean changed = !Slack.SCHEDULED.equals(getSlack());
        setSlack(Slack.SCHEDULED);
        update(changed);
      }
    });

    container.add(scheduled);

    Button all = new Button(Localized.dictionary().crmTaskFilterAll());
    all.addStyleName(getStylePrefix() + STYLE_ALL);

    all.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        boolean changed = getSlack() != null;
        setSlack(null);
        update(changed);
      }
    });

    container.add(all);

    Button cancel = new Button(Localized.dictionary().cancel());
    cancel.addStyleName(getStylePrefix() + STYLE_CANCEL);

    cancel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        closeDialog();
      }
    });

    container.add(cancel);

    return container;
  }

  private Slack getSlack() {
    return slack;
  }

  private void setSlack(Slack slack) {
    this.slack = slack;
  }

}
