package com.butent.bee.client.modules.mail;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.data.Queries;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.output.Report;
import com.butent.bee.client.output.ReportParameters;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.ReportInterceptor;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MailReport extends ReportInterceptor {

  private static final String NAME_MAIL_FROM = "MailFrom";
  private static final String NAME_MAIL_TO = "MailTo";
  private static final String NAME_MAIL_SUBJECT = "MailSubject";
  private static final String NAME_MAIL_KEYWORDS = "MailKeywords";
  private static final String NAME_START_DATE = "StartDate";
  private static final String NAME_END_DATE = "EndDate";
  private static final String NAME_MAIL_HAS_ATTACHMENTS = "MailHasAttachments";

  public MailReport() {
  }

  @Override
  public Set<Action> getEnabledActions(Set<Action> defaultActions) {
    Set<Action> actions = super.getEnabledActions(defaultActions);
    actions.remove(Action.EXPORT);
    actions.remove(Action.BOOKMARK);
    return actions;
  }

  @Override
  public FormInterceptor getInstance() {
    return new MailReport();
  }

  @Override
  protected void clearFilter() {
    clearEditor(NAME_MAIL_FROM);
    clearEditor(NAME_MAIL_TO);
    clearEditor(NAME_MAIL_SUBJECT);
    clearEditor(NAME_MAIL_KEYWORDS);
    clearEditor(NAME_START_DATE);
    clearEditor(NAME_END_DATE);
    clearEditor(NAME_MAIL_HAS_ATTACHMENTS);
  }

  @Override
  protected void doReport() {

    List<String> args = new ArrayList<>();

    String mailFrom = getEditorValue(NAME_MAIL_FROM);
    String mailTo = getEditorValue(NAME_MAIL_TO);
    String mailSubject = getEditorValue(NAME_MAIL_SUBJECT);
    String mailKeywords = getEditorValue(NAME_MAIL_KEYWORDS);
    String mailHasAttachments = getEditorValue(NAME_MAIL_HAS_ATTACHMENTS);
    DateTime start = getDateTime(NAME_START_DATE);
    DateTime end = getDateTime(NAME_END_DATE);

    if (!BeeUtils.isEmpty(mailFrom)) {
      args.add(MailConstants.COL_SENDER_EMAIL);
      args.add(mailFrom);
    }

    if (!BeeUtils.isEmpty(mailTo)) {
      args.add(MailConstants.COL_RECIPIENT_EMAIL);
      args.add(mailTo);
    }

    if (!BeeUtils.isEmpty(mailSubject)) {
      args.add(MailConstants.COL_SUBJECT);
      args.add(mailSubject);
    }

    if (!BeeUtils.isEmpty(mailKeywords)) {
      args.add(MailConstants.COL_CONTENT);
      args.add(mailKeywords);
    }

    if (start != null) {
      args.add(Service.VAR_FROM);
      args.add(start.serialize());
    }

    if (end != null) {
      TimeUtils.addHour(end, 24);
      args.add(Service.VAR_TO);
      args.add(end.serialize());
    }

    if (!BeeUtils.isEmpty(mailHasAttachments) && BeeUtils.same(BooleanValue.S_TRUE,
        mailHasAttachments)) {
      args.add(MailConstants.COL_ATTACHMENT_COUNT);
      args.add(BooleanValue.S_TRUE);
    }

    Filter flt = Filter.custom(MailConstants.FILTER_MAIL_REPORT, args);
    if (!BeeUtils.isEmpty(mailTo)) {
      flt = Filter.and(Filter.contains(MailConstants.COL_RECIPIENT_EMAIL, mailTo), flt);
    }

    if (BeeUtils.isEmpty(args)) {
      getFormView().notifySevere(Localized.dictionary().enterFilterCriteria());
      return;
    }

    final Filter filter = flt;

    Queries.getRowCount(MailConstants.VIEW_MAIL_REPORTS, filter, new Queries.IntCallback() {
      @Override
      public void onSuccess(Integer result) {
        if (BeeUtils.isPositive(result)) {
          openGrid(filter);
        } else {
          getFormView().notifyWarning(Localized.dictionary().nothingFound());
        }
      }
    });
  }

  @Override
  protected String getBookmarkLabel() {
    return null;
  }

  @Override
  protected Report getReport() {
    return Report.MAIL_REPORT;
  }

  @Override
  protected ReportParameters getReportParameters() {
    return null;
  }

  @Override
  protected boolean validateParameters(ReportParameters parameters) {
    return false;
  }

  private void openGrid(Filter filter) {
    HasIndexedWidgets container = getDataContainer();
    if (!container.isEmpty()) {
      container.clear();
    }

    GridFactory.openGrid(MailConstants.GRID_MAIL_REPORT, null, GridOptions.forFilter(filter),
        new PresenterCallback() {
          @Override
          public void onCreate(Presenter presenter) {
            Widget widget = presenter.getMainView().asWidget();
            StyleUtils.occupy(widget);

            getDataContainer().add(widget);

          }
        });
  }

}
