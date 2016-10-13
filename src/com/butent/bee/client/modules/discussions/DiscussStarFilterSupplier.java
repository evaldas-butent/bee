package com.butent.bee.client.modules.discussions;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.view.search.AbstractFilterSupplier;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.FilterValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.utils.BeeUtils;

final class DiscussStarFilterSupplier extends AbstractFilterSupplier {

  private boolean starred;

  DiscussStarFilterSupplier(String options) {
    super(VIEW_DISCUSSIONS, null, null, options);
  }

  @Override
  public FilterValue getFilterValue() {
    return isStarred() ? FilterValue.of(null, false) : null;
  }

  @Override
  public String getLabel() {
    return isStarred() ? Localized.dictionary().discussFilterStarred() : null;
  }

  @Override
  public void onRequest(Element target, ScheduledCommand onChange) {
    openDialog(target, createWidget(), null, onChange);
  }

  @Override
  public Filter parse(FilterValue input) {
    if (input != null && BeeUtils.isFalse(input.getEmptyValues())) {
      return Filter.in(Data.getIdColumn(VIEW_DISCUSSIONS), VIEW_DISCUSSIONS_USERS, COL_DISCUSSION,
          Filter.and(BeeKeeper.getUser().getFilter(AdministrationConstants.COL_USER), Filter
              .notNull(COL_STAR)));
    } else {
      return null;
    }
  }

  @Override
  public void setFilterValue(FilterValue filterValue) {
    setStarred(filterValue != null && BeeUtils.isFalse(filterValue.getEmptyValues()));
  }

  @Override
  protected String getStylePrefix() {
    return BeeConst.CSS_CLASS_PREFIX + "discuss-FilterSupplier-Star-";
  }

  private Widget createWidget() {
    Flow container = new Flow();
    container.addStyleName(getStylePrefix() + "container");

    Button star = new Button(Localized.dictionary().crmTaskFilterStarred());
    star.addStyleName(getStylePrefix() + "starred");

    star.addClickHandler(event -> {
      boolean changed = !isStarred();
      setStarred(true);
      update(changed);
    });

    container.add(star);

    Button all = new Button(Localized.dictionary().crmTaskFilterAll());
    all.addStyleName(getStylePrefix() + "all");

    all.addClickHandler(event -> {
      boolean changed = isStarred();
      setStarred(false);
      update(changed);
    });

    container.add(all);

    Button cancel = new Button(Localized.dictionary().cancel());
    cancel.addStyleName(getStylePrefix() + "cancel");

    cancel.addClickHandler(event -> closeDialog());

    container.add(cancel);

    return container;
  }

  private boolean isStarred() {
    return starred;
  }

  private void setStarred(boolean starred) {
    this.starred = starred;
  }
}
