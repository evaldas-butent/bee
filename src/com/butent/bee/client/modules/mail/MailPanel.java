package com.butent.bee.client.modules.mail;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dialog.Modality;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.event.DndHelper;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.ActiveRowChangeEvent;
import com.butent.bee.client.event.logical.MutationEvent;
import com.butent.bee.client.event.logical.MutationEvent.Handler;
import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.images.star.Stars;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.screen.BodyPanel;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.GridView.SelectedRows;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.websocket.Endpoint;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.DateTimeLabel;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.client.widget.TextLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.State;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.mail.AccountInfo;
import com.butent.bee.shared.modules.mail.MailFolder;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class MailPanel extends AbstractFormInterceptor {

  private final class EnvelopeRenderer extends AbstractCellRenderer {
    private final int folderIdx;
    private final int senderEmail;
    private final int senderLabel;
    private final int dateIdx;
    private final int subjectIdx;
    private final int flagsIdx;
    private final int attachmentCount;

    private EnvelopeRenderer(CellSource cellSource, List<? extends IsColumn> dataColumns) {
      super(cellSource);

      folderIdx = DataUtils.getColumnIndex(COL_FOLDER, dataColumns);
      senderEmail = DataUtils.getColumnIndex("SenderEmail", dataColumns);
      senderLabel = DataUtils.getColumnIndex("SenderLabel", dataColumns);
      dateIdx = DataUtils.getColumnIndex(COL_DATE, dataColumns);
      subjectIdx = DataUtils.getColumnIndex(COL_SUBJECT, dataColumns);
      flagsIdx = DataUtils.getColumnIndex(COL_FLAGS, dataColumns);
      attachmentCount = DataUtils.getColumnIndex(COL_ATTACHMENT_COUNT, dataColumns);
    }

    @Override
    public String render(final IsRow row) {
      Flow fp = new Flow();
      fp.setStyleName(BeeConst.CSS_CLASS_PREFIX + "mail-Header");

      DomUtils.setDraggable(fp);

      if (!MessageFlag.SEEN.isSet(row.getInteger(flagsIdx))) {
        fp.addStyleName(BeeConst.CSS_CLASS_PREFIX + "mail-HeaderUnread");
      }
      Flow f1 = new Flow();
      fp.add(f1);

      TextLabel sender = new TextLabel(false);
      sender.setStyleName(BeeConst.CSS_CLASS_PREFIX + "mail-HeaderAddress");
      String address;

      if (isSenderFolder(row.getLong(folderIdx))) {
        address = BeeUtils.notEmpty(row.getProperty(COL_EMAIL_LABEL),
            row.getProperty(ClassifierConstants.COL_EMAIL_ADDRESS));

        int cnt = BeeUtils.toInt(row.getProperty(COL_ADDRESS)) - 1;

        if (cnt > 0) {
          address += " (" + cnt + "+)";
        }
      } else {
        address = BeeUtils.notEmpty(row.getString(senderLabel), row.getString(senderEmail));
      }
      sender.setText(address);
      f1.add(sender);

      DateTime date = row.getDateTime(dateIdx);
      DateTimeLabel dt = new DateTimeLabel(TimeUtils.isToday(date) ? "TIME_SHORT" : "DATE_SHORT",
          false);
      dt.setStyleName(BeeConst.CSS_CLASS_PREFIX + "mail-HeaderDate");
      dt.setValue(date);
      f1.add(dt);

      Flow f2 = new Flow();
      fp.add(f2);

      TextLabel subject = new TextLabel(false);
      subject.setStyleName(BeeConst.CSS_CLASS_PREFIX + "mail-HeaderSubject");
      subject.setText(row.getString(subjectIdx));
      f2.add(subject);

      Integer att = row.getInteger(attachmentCount);

      if (BeeUtils.isPositive(att)) {
        TextLabel attachments = new TextLabel(false);
        attachments.setStyleName(BeeConst.CSS_CLASS_PREFIX + "mail-HeaderAttachment");

        if (att > 1) {
          attachments.setText(BeeUtils.toString(att));
        }
        f2.add(attachments);
      }
      return fp.toString();
    }
  }

  private static final class FlagRenderer extends AbstractCellRenderer {

    private final int flags;

    private FlagRenderer(CellSource cellSource, List<? extends IsColumn> dataColumns) {
      super(cellSource);
      flags = DataUtils.getColumnIndex(COL_FLAGS, dataColumns);
    }

    @Override
    public String render(IsRow row) {
      String star = MessageFlag.FLAGGED.isSet(row.getInteger(flags))
          ? Stars.getHtml(0) : Stars.getDefaultHeader();

      if (MessageFlag.ANSWERED.isSet(row.getInteger(flags))) {
        star += new CustomDiv(BeeConst.CSS_CLASS_PREFIX + "mail-FlagAnswered");

      }
      if (MessageFlag.FORWARDED.isSet(row.getInteger(flags))) {
        star += new CustomDiv(BeeConst.CSS_CLASS_PREFIX + "mail-FlagForwarded");
      }
      if (!BeeUtils.isEmpty(row.getProperty(AdministrationConstants.COL_RELATION))) {
        CustomDiv chain = new CustomDiv(BeeConst.CSS_CLASS_PREFIX + "mail-FlagChained");
        chain.setTitle(row.getProperty(AdministrationConstants.COL_RELATION));
        star += chain;
      }
      return star;
    }
  }

  private class MessagesGrid extends AbstractGridInterceptor {
    private final Horizontal dummy = new Horizontal();

    @Override
    public void afterCreate(final GridView gridView) {
      Binder.addDragStartHandler(gridView.getGrid(), new DragStartHandler() {
        @Override
        public void onDragStart(DragStartEvent event) {
          EventTarget target = event.getNativeEvent().getEventTarget();

          if (!Element.is(target)) {
            return;
          }
          Integer rowIdx = BeeUtils.toIntOrNull(DomUtils
              .getDataRow(Element.as(target).getParentElement()));

          if (rowIdx == null || !BeeUtils.isIndex(gridView.getGrid().getRowData(), rowIdx)) {
            return;
          }
          Long placeId = gridView.getGrid().getRowData().get(rowIdx).getId();
          Collection<RowInfo> rows = gridView.getSelectedRows(SelectedRows.ALL);
          Set<Long> ids = new HashSet<>();

          if (!BeeUtils.isEmpty(rows)) {
            for (RowInfo info : rows) {
              ids.add(info.getId());
            }
          }
          String label;

          if (!ids.contains(placeId)) {
            label = Localized.getConstants().mailMessage();
            ids.clear();
            ids.add(placeId);
          } else {
            label = Localized.getConstants().mailMessages(ids.size());
          }
          Label dragLabel = new Label(label);

          dummy.add(dragLabel);
          BodyPanel.get().add(dummy);
          dragLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
          dragLabel.getElement().getStyle().setBackgroundColor("whiteSmoke");

          EventUtils.allowCopyMove(event);

          event.getDataTransfer().setDragImage(dragLabel.getElement(), 0,
              dummy.getElement().getOffsetHeight());

          DndHelper.fillContent(DATA_TYPE_MESSAGE, null, null, DataUtils.buildIdList(ids));
        }
      });

      Binder.addDragEndHandler(gridView.getGrid(), new DragEndHandler() {
        @Override
        public void onDragEnd(DragEndEvent event) {
          dummy.clear();
          BodyPanel.get().remove(dummy);
          DndHelper.reset();
        }
      });
    }

    @Override
    public void afterCreatePresenter(GridPresenter presenter) {
      activateAccount(getCurrentAccount());
    }

    @Override
    public BeeRowSet getInitialRowSet(GridDescription gridDescription) {
      return Data.createRowSet(gridDescription.getViewName());
    }

    @Override
    public GridInterceptor getInstance() {
      return new MessagesGrid();
    }

    @Override
    public AbstractCellRenderer getRenderer(String columnName,
        List<? extends IsColumn> dataColumns, ColumnDescription columnDescription,
        CellSource cellSource) {

      if (BeeUtils.same(columnName, COL_FLAGS)) {
        return new FlagRenderer(cellSource, dataColumns);
      } else if (BeeUtils.same(columnName, COL_MESSAGE)) {
        return new EnvelopeRenderer(cellSource, dataColumns);
      }
      return super.getRenderer(columnName, dataColumns, columnDescription, cellSource);
    }

    @Override
    public void onActiveRowChange(ActiveRowChangeEvent event) {
      IsRow row = event.getRowValue();

      if (row != null) {
        if (!message.samePlace(row.getId())) {
          message.requery(COL_PLACE, row.getId());
          messageWidget.setVisible(true);
          emptySelectionWidget.setVisible(false);

          int flagIdx = Data.getColumnIndex(getGridPresenter().getViewName(), COL_FLAGS);
          int value = BeeUtils.unbox(row.getInteger(flagIdx));

          if (!MessageFlag.SEEN.isSet(value)) {
            row.setValue(flagIdx, MessageFlag.SEEN.set(value));
            getGridView().refreshCell(row.getId(), COL_MESSAGE);
          }
        }
      } else if (getGridView().isEmpty()
          || !Objects.equals(message.getFolder(), getCurrentFolder())) {
        message.reset();
        messageWidget.setVisible(false);
        emptySelectionWidget.setVisible(true);
      }
      super.onActiveRowChange(event);
    }

    @Override
    public void onEditStart(EditStartEvent event) {
      final String col = event.getColumnId();

      if (BeeUtils.same(col, COL_FLAGS)) {
        IsRow row = event.getRowValue();

        if (row == null) {
          return;
        }
        int flagIdx = Data.getColumnIndex(getGridPresenter().getViewName(), col);
        int value = BeeUtils.unbox(row.getInteger(flagIdx));

        flagMessage(row.getId(), MessageFlag.FLAGGED, !MessageFlag.FLAGGED.isSet(value));

        if (MessageFlag.FLAGGED.isSet(value)) {
          value = MessageFlag.FLAGGED.clear(value);
        } else {
          value = MessageFlag.FLAGGED.set(value);
        }
        row.setValue(flagIdx, value);
        getGridView().refreshCell(row.getId(), col);
      }
      event.consume();
    }
  }

  private final class SearchPanel extends Flow {
    protected static final String CSS_SEARCH_PREFIX = BeeConst.CSS_CLASS_PREFIX + "mail-Search";

    private final Map<String, Editor> criteria = new HashMap<>();
    private final Simple folderContainer = new Simple();
    private InputText searchWidget;
    private FaLabel searchOptions;

    private SearchPanel() {
      setStyleName(CSS_SEARCH_PREFIX + "Panel");
      Dictionary loc = Localized.getConstants();

      add(new Label(loc.mailFrom()));
      InputText from = new InputText();
      from.addStyleName(CSS_SEARCH_PREFIX + "From");
      add(from);
      criteria.put(COL_SENDER, from);

      add(new Label(loc.mailTo()));
      InputText to = new InputText();
      to.addStyleName(CSS_SEARCH_PREFIX + "To");
      add(to);
      criteria.put(COL_ADDRESS, to);

      add(new Label(loc.mailSubject()));
      InputText subject = new InputText();
      subject.addStyleName(CSS_SEARCH_PREFIX + "Subject");
      add(subject);
      criteria.put(COL_SUBJECT, subject);

      add(new Label(loc.keywords()));
      InputText content = new InputText();
      content.addStyleName(CSS_SEARCH_PREFIX + "Content");
      content.addValueChangeHandler(new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
          if (searchWidget != null) {
            searchWidget.setValue(event.getValue());
          }
        }
      });
      add(content);
      criteria.put(COL_CONTENT, content);

      InputBoolean starred = new InputBoolean(loc.mailStarred());
      starred.addStyleName(CSS_SEARCH_PREFIX + "Starred");
      add(starred);
      criteria.put(MessageFlag.FLAGGED.name(), starred);

      InputBoolean unread = new InputBoolean(loc.mailUnread());
      unread.addStyleName(CSS_SEARCH_PREFIX + "Unread");
      add(unread);
      criteria.put(MessageFlag.SEEN.name(), unread);

      InputBoolean attachments = new InputBoolean(loc.mailHasAttachments());
      attachments.addStyleName(CSS_SEARCH_PREFIX + "Attachments");
      add(attachments);
      criteria.put(TBL_ATTACHMENTS, attachments);

      folderContainer.addStyleName(CSS_SEARCH_PREFIX + "Folder");
      add(folderContainer);

      final FaLabel search = new FaLabel(FontAwesome.FILTER, CSS_SEARCH_PREFIX + "Button");

      search.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          UiHelper.getParentPopup(search).close();
          doSearch();

        }
      });
      add(search);
    }

    private void clearSearch() {
      for (Editor editor : criteria.values()) {
        editor.clearValue();
      }
      if (searchWidget != null) {
        searchWidget.clearValue();
      }
      toggleOptions();
      folderContainer.clear();
    }

    public void doSearch() {
      refreshMessages(false);
    }

    private Map<String, String> getSearchCriteria() {
      Map<String, String> map = new HashMap<>();

      for (Entry<String, Editor> entry : criteria.entrySet()) {
        String value = entry.getValue().getNormalizedValue();

        if (!BeeUtils.isEmpty(value)) {
          map.put(entry.getKey(), value);
        }
      }
      return map;
    }

    private boolean searchInCurrentFolder() {
      if (folderContainer.getWidget() != null) {
        return ((InputBoolean) folderContainer.getWidget()).isChecked();
      }
      return false;
    }

    private void setSearchOptionsWidget(final FaLabel widget) {
      searchOptions = widget;
      searchOptions.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          searchOptions.getElement().getStyle().setVisibility(Visibility.HIDDEN);

          if (DataUtils.isId(getCurrentFolder())) {
            if (!searchInCurrentFolder()) {
              folderContainer.setWidget(new InputBoolean(Localized.getConstants()
                  .mailOnlyInFolder(getFolderCaption(getCurrentFolder()))));
            }
          } else {
            folderContainer.clear();
          }
          Popup popup = new Popup(OutsideClick.CLOSE, CSS_SEARCH_PREFIX + "Popup");
          popup.setWidget(SearchPanel.this);
          popup.setHideOnEscape(true);

          popup.focusOnOpen(SearchPanel.this);
          popup.showRelativeTo(widget.asWidget().getParent().getElement());
        }
      });
    }

    private void setSearchWidget(InputText widget) {
      searchWidget = widget;
      searchWidget.addValueChangeHandler(new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
          setContent(event.getValue());
        }
      });
      searchWidget.addKeyDownHandler(new KeyDownHandler() {
        @Override
        public void onKeyDown(KeyDownEvent ev) {
          if (ev.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            setContent(searchWidget.getValue());
            doSearch();
          }
        }
      });
    }

    @Override
    protected void onDetach() {
      toggleOptions();
      searchOptions.getElement().getStyle().setVisibility(Visibility.VISIBLE);
      super.onDetach();
    }

    private void setContent(String content) {
      criteria.get(COL_CONTENT).setValue(content);
    }

    private void toggleOptions() {
      if (searchOptions != null) {
        Map<String, String> map = getSearchCriteria();
        map.remove(COL_CONTENT);
        StyleUtils.setStyleName(searchOptions.getElement(), CSS_SEARCH_PREFIX + "Extended",
            !BeeUtils.isEmpty(map));
      }
    }
  }

  private AccountInfo currentAccount;
  private Long currentFolder;

  private final MessagesGrid messages = new MessagesGrid();
  private final MailMessage message = new MailMessage(this);
  private final SearchPanel searchPanel = new SearchPanel();

  private final List<AccountInfo> accounts = new ArrayList<>();

  private Widget messageWidget;
  private Widget emptySelectionWidget;
  private FaLabel purgeWidget = new FaLabel(FontAwesome.RECYCLE);

  MailPanel(List<AccountInfo> availableAccounts, AccountInfo defaultAccount) {
    Assert.notNull(availableAccounts);
    accounts.addAll(availableAccounts);
    this.currentAccount = defaultAccount;
  }

  @Override
  public void afterCreateWidget(String name, final IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (widget instanceof GridPanel && BeeUtils.same(name, TBL_PLACES)) {
      ((GridPanel) widget).setGridInterceptor(messages);

    } else if (BeeUtils.same(name, COL_MESSAGE)) {
      messageWidget = widget.asWidget();

    } else if (BeeUtils.same(name, "EmptySelection")) {
      emptySelectionWidget = widget.asWidget();

    } else if (widget instanceof InputText && BeeUtils.same(name, "Search")) {
      searchPanel.setSearchWidget((InputText) widget);

    } else if (widget instanceof FaLabel && BeeUtils.same(name, "SearchOptions")) {
      searchPanel.setSearchOptionsWidget((FaLabel) widget);
    } else if (widget instanceof Split) {
      Split split = (Split) widget;
      split.addMutationHandler(new Handler() {

        @Override
        public void onMutation(MutationEvent event) {
          int size = split.getDirectionSize(Direction.WEST);

          if (size > 0 && !BeeUtils.isEmpty(getStorageKey())) {
            BeeKeeper.getStorage().set(getStorageKey(), size);
          }
        }
      });
    }
    message.afterCreateWidget(name, widget, callback);
  }

  @Override
  public boolean beforeCreateWidget(String name, com.google.gwt.xml.client.Element description) {

    if (BeeUtils.same(name, "Split")) {
      Integer size = BeeKeeper.getStorage().getInteger(getStorageKey());

      if (BeeUtils.isPositive(size)) {

        com.google.gwt.xml.client.Element west =
            XmlUtils.getFirstChildElement(description, Direction.WEST.name()
                .toLowerCase());

        if (west != null) {
          west.setAttribute(UiConstants.ATTR_SIZE, size.toString());
        }

      }
    }
    return super.beforeCreateWidget(name, description);
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    switch (action) {
      case ADD:
        NewMailMessage.create(accounts, getCurrentAccount(),
            null, null, null, null, null, null, null, false);
        break;

      case DELETE:
        Set<Long> ids = new HashSet<>();
        GridView grid = messages.getGridView();

        for (RowInfo row : grid.getSelectedRows(SelectedRows.ALL)) {
          ids.add(row.getId());
        }

        if (BeeUtils.isEmpty(ids) && grid.getActiveRow() != null) {
          ids.add(grid.getActiveRow().getId());
        }
        removeMessages(ids);
        break;

      case PRINT:
        message.beforeAction(action, presenter);
        break;

      default:
        return true;
    }
    return false;
  }

  @Override
  public Domain getDomain() {
    return Domain.MAIL;
  }

  @Override
  public FormInterceptor getInstance() {
    return null;
  }

  @Override
  public void onShow(Presenter presenter) {
    HeaderView header = presenter.getHeader();
    header.clearCommandPanel();

    ListBox accountsWidget = new ListBox();
    initAccounts(accountsWidget);
    header.addCommandItem(accountsWidget);

    if (BeeKeeper.getUser().isDataVisible(TBL_ACCOUNTS)
        && BeeKeeper.getUser().canCreateData(TBL_RULES)) {
      FaLabel accountSettings = new FaLabel(FontAwesome.MAGIC);

      accountSettings.setTitle(Localized.getConstants().mailRule());
      accountSettings.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent ev) {
          if (!Objects.equals(getCurrentAccount().getUserId(), BeeKeeper.getUser().getUserId())) {
            getFormView().notifyWarning(Localized.getConstants().actionNotAllowed());
            return;
          }
          DataInfo dataInfo = Data.getDataInfo(TBL_RULES);
          BeeRow newRow = RowFactory.createEmptyRow(dataInfo, true);
          Data.setValue(TBL_RULES, newRow, COL_ACCOUNT, getCurrentAccount().getAccountId());

          GridPresenter grid = messages.getGridPresenter();
          IsRow row = grid.getActiveRow();

          if (row != null) {
            if (isSenderFolder(DataUtils.getLong(grid.getDataColumns(), row, COL_FOLDER))) {
              Data.setValue(TBL_RULES, newRow, COL_RULE_CONDITION,
                  RuleCondition.RECIPIENTS.ordinal());
              Data.setValue(TBL_RULES, newRow, COL_RULE_CONDITION_OPTIONS,
                  row.getProperty(ClassifierConstants.COL_EMAIL_ADDRESS));
            } else {
              Data.setValue(TBL_RULES, newRow, COL_RULE_CONDITION, RuleCondition.SENDER.ordinal());
              Data.setValue(TBL_RULES, newRow, COL_RULE_CONDITION_OPTIONS,
                  DataUtils.getString(grid.getDataColumns(), row, "SenderEmail"));
            }
          }
          RowFactory.createRow(dataInfo, newRow, Modality.ENABLED);
        }
      });
      header.addCommandItem(accountSettings);
    }
    FaLabel refreshWidget = new FaLabel(FontAwesome.REFRESH);

    refreshWidget.setTitle(Localized.getConstants().actionRefresh());
    refreshWidget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        checkFolderRecursively(getCurrentAccount().getRootFolder(), clickEvent.isShiftKeyDown());
      }
    });
    header.addCommandItem(refreshWidget);

    FaLabel unseenWidget = new FaLabel(FontAwesome.EYE_SLASH);

    unseenWidget.setTitle(Localized.getConstants().mailMarkAsUnread());
    unseenWidget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent ev) {
        GridPresenter grid = messages.getGridPresenter();
        IsRow row = grid.getActiveRow();

        if (row != null) {
          flagMessage(row.getId(), MessageFlag.SEEN, false);

          int flagIdx = Data.getColumnIndex(grid.getViewName(), COL_FLAGS);
          row.setValue(flagIdx, MessageFlag.SEEN.clear(BeeUtils.unbox(row.getInteger(flagIdx))));
          grid.getGridView().refreshCell(row.getId(), COL_MESSAGE);
        }
      }
    });
    header.addCommandItem(unseenWidget);

    purgeWidget.setTitle(Localized.getConstants().mailEmptyTrashFolder());
    purgeWidget.setVisible(false);
    purgeWidget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent ev) {
        if (getCurrentAccount().isTrashFolder(getCurrentFolder())) {
          purgeWidget.setEnabled(false);

          Queries.getRowSet(TBL_PLACES, Collections.singletonList(COL_FOLDER),
              Filter.equals(COL_FOLDER, getCurrentFolder()),
              new Queries.RowSetCallback() {
                @Override
                public void onSuccess(BeeRowSet result) {
                  removeMessages(result.getRowIds());
                  purgeWidget.setEnabled(true);
                }
              });
        }
      }
    });
    header.addCommandItem(purgeWidget);

    message.setFormView(getFormView());
  }

  @Override
  public void onStateChange(State state) {
    if (State.ACTIVATED.equals(state)) {
      MailKeeper.activateController(this);

    } else if (State.REMOVED.equals(state)) {
      MailKeeper.removeMailPanel(this);
    }
  }

  void checkFolder(final Long folderId, boolean syncAll) {
    final ParameterList params = MailKeeper.createArgs(SVC_CHECK_MAIL);
    params.addDataItem(COL_ACCOUNT, getCurrentAccount().getAccountId());
    params.addDataItem(COL_FOLDER, folderId);

    if (syncAll) {
      params.addDataItem(Service.VAR_CHECK, BeeUtils.toString(syncAll));
    }
    Endpoint.initProgress(getFolderCaption(folderId), new Consumer<String>() {
      @Override
      public void accept(String progress) {
        if (!BeeUtils.isEmpty(progress)) {
          params.addDataItem(Service.VAR_PROGRESS, progress);
        }
        BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            response.notify(getFormView());
          }
        });
      }
    });
  }

  List<AccountInfo> getAccounts() {
    return accounts;
  }

  AccountInfo getCurrentAccount() {
    return currentAccount;
  }

  Long getCurrentFolder() {
    return currentFolder;
  }

  void refreshFolder(Long folderId) {
    if (DataUtils.isId(folderId)) {
      searchPanel.clearSearch();
    }
    setCurrentFolder(folderId);
    MailKeeper.refreshController();
    refreshMessages(false);
  }

  void refreshMessages(boolean preserveActiveRow) {
    GridPresenter grid = messages.getGridPresenter();

    if (grid != null) {
      Filter clause = null;
      Map<String, String> criteria = searchPanel.getSearchCriteria();

      if (!BeeUtils.isEmpty(criteria)) {
        if (preserveActiveRow) {
          return;
        }
        if (searchPanel.searchInCurrentFolder()) {
          criteria.put(COL_FOLDER, BeeUtils.toString(getCurrentFolder()));
        } else {
          setCurrentFolder(null);
          MailKeeper.refreshController();
        }
        criteria.put(COL_ACCOUNT, BeeUtils.toString(getCurrentAccount().getAccountId()));
        clause = Filter.custom(TBL_PLACES, Codec.beeSerialize(criteria));
      }
      if (DataUtils.isId(getCurrentFolder())) {
        clause = Filter.and(Filter.equals(COL_FOLDER, getCurrentFolder()), clause);
      } else {
        clause = Filter.and(Filter.equals(COL_ACCOUNT, getCurrentAccount().getAccountId()),
            Filter.notEquals(COL_FOLDER, getCurrentAccount().getTrashId()), clause);
      }
      grid.getDataProvider().setUserFilter(clause);

      if (!preserveActiveRow) {
        grid.getGridView().getGrid().reset();
        message.reset();
        messageWidget.setVisible(false);
        emptySelectionWidget.setVisible(true);
      }

      grid.refresh(preserveActiveRow, !preserveActiveRow);
    }
  }

  void removeRows(Collection<Long> ids) {
    if (ids != null) {
      CellGrid grid = messages.getGridView().getGrid();

      for (Long id : ids) {
        grid.removeRowById(id);
      }
      grid.refresh();
    }
  }

  void requeryFolders() {
    requeryFolders(null);
  }

  void requeryFolders(final ScheduledCommand afterRequery) {
    final AccountInfo account = getCurrentAccount();

    ParameterList params = MailKeeper.createArgs(SVC_GET_FOLDERS);
    params.addDataItem(COL_ACCOUNT, account.getAccountId());

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.state(response.hasResponse(MailFolder.class));
        response.notify(getFormView());

        account.setRootFolder(MailFolder.restore(response.getResponseAsString()));

        if (account == getCurrentAccount()) {
          MailKeeper.rebuildController();

          if (afterRequery != null) {
            afterRequery.execute();
          }
        }
      }
    });
  }

  private void activateAccount(AccountInfo selectedAccount) {
    currentAccount = selectedAccount;

    requeryFolders(new ScheduledCommand() {
      @Override
      public void execute() {
        searchPanel.clearSearch();
        setCurrentFolder(null);
        refreshFolder(getCurrentAccount().getInboxId());
      }
    });
  }

  private void checkFolderRecursively(MailFolder folder, boolean syncAll) {
    if (DataUtils.isId(folder.getId())) {
      checkFolder(folder.getId(), syncAll);
    }
    for (MailFolder subFolder : folder.getSubFolders()) {
      checkFolderRecursively(subFolder, syncAll);
    }
  }

  private void flagMessage(Long placeId, MessageFlag flag, boolean on) {
    ParameterList params = MailKeeper.createArgs(SVC_FLAG_MESSAGE);
    params.addDataItem(COL_PLACE, placeId);
    params.addDataItem(COL_FLAGS, flag.name());
    params.addDataItem("on", Codec.pack(on));

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(getFormView());
      }
    });
  }

  private String getFolderCaption(Long folderId) {
    String cap;
    AccountInfo account = getCurrentAccount();

    if (account.isDraftsFolder(folderId)) {
      cap = Localized.getConstants().mailFolderDrafts();
    } else if (account.isInboxFolder(folderId)) {
      cap = Localized.getConstants().mailFolderInbox();
    } else if (account.isSentFolder(folderId)) {
      cap = Localized.getConstants().mailFolderSent();
    } else if (account.isTrashFolder(folderId)) {
      cap = Localized.getConstants().mailFolderTrash();
    } else {
      cap = account.findFolder(folderId).getName();
    }
    return cap;
  }

  private static String getStorageKey() {
    String key =
        BeeUtils.join(BeeConst.STRING_MINUS, "MailTree", BeeKeeper.getUser().getUserId(),
            UiConstants.ATTR_SIZE);

    return key;
  }

  private void initAccounts(final ListBox accountsWidget) {
    accountsWidget.clear();

    for (int i = 0; i < accounts.size(); i++) {
      AccountInfo accountInfo = accounts.get(i);
      accountsWidget.addItem(accountInfo.getDescription() + " <" + accountInfo.getAddress() + ">");

      if (Objects.equals(accountInfo, currentAccount)) {
        accountsWidget.setSelectedIndex(i);
      }
    }
    accountsWidget.setEnabled(accountsWidget.getItemCount() > 1);
    accountsWidget.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        AccountInfo selectedAccount = accounts.get(accountsWidget.getSelectedIndex());

        if (!Objects.equals(selectedAccount, currentAccount)) {
          activateAccount(selectedAccount);
        }
      }
    });
  }

  private boolean isSenderFolder(Long folderId) {
    return getCurrentAccount().isSentFolder(folderId)
        || getCurrentAccount().isDraftsFolder(folderId);
  }

  private void removeMessages(final Collection<Long> ids) {
    if (BeeUtils.isEmpty(ids)) {
      return;
    }
    final boolean purge = getCurrentAccount().isTrashFolder(getCurrentFolder())
        || getCurrentAccount().isDraftsFolder(getCurrentFolder());

    Global.confirm(purge ? Localized.getConstants().delete()
            : Localized.getConstants().mailActionMoveToTrash(), purge ? Icon.ALARM : Icon.WARNING,
        Collections.singletonList(Localized.getConstants().mailMessages(ids.size())),
        new ConfirmationCallback() {
          @Override
          public void onConfirm() {
            ParameterList params = MailKeeper.createArgs(SVC_REMOVE_MESSAGES);
            params.addDataItem(COL_ACCOUNT, getCurrentAccount().getAccountId());
            params.addDataItem(COL_PLACE, DataUtils.buildIdList(ids));

            BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
              @Override
              public void onResponse(ResponseObject response) {
                response.notify(getFormView());

                if (!response.hasErrors()) {
                  String msg = response.getResponseAsString();
                  Dictionary loc = Localized.getConstants();

                  getFormView().notifyInfo(purge ? loc.mailDeletedMessages(msg)
                      : loc.mailMovedMessagesToTrash(msg));
                }
              }
            });
            removeRows(ids);
          }
        });
  }

  private void setCurrentFolder(Long currentFolder) {
    this.currentFolder = currentFolder;
    purgeWidget.setVisible(getCurrentAccount().isTrashFolder(this.currentFolder));
  }
}
