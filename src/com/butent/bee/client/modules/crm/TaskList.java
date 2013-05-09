package com.butent.bee.client.modules.crm;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.images.star.Stars;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.HasCellRenderer;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.validation.ValidationHelper;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.edit.EditorAssistant;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.view.search.AbstractFilterSupplier;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.crm.CrmUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.CssUnit;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;

class TaskList {

  public interface SlackTemplate extends SafeHtmlTemplates {
    @Template("<div class=\"bee-crm-SlackLate-bar\" style=\"{0}\"></div><div class=\"bee-crm-SlackLate-label\">{1}</div>")
    SafeHtml late(SafeStyles barStyles, String label);

    @Template("<div class=\"bee-crm-SlackScheduled-bar\" style=\"{0}\"></div><div class=\"bee-crm-SlackScheduled-label\">{1}</div>")
    SafeHtml scheduled(SafeStyles barStyles, String label);
  }

  private static class GridHandler extends AbstractGridInterceptor {

    private static final String NAME_MODE = "Mode";
    private static final String NAME_SLACK = "Slack";

    private final Type type;
    private final Long userId;

    private GridHandler(Type type) {
      this.type = type;
      this.userId = BeeKeeper.getUser().getUserId();
    }

    @Override
    public boolean afterCreateColumn(String columnId, List<? extends IsColumn> dataColumns,
        AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
        EditableColumn editableColumn) {

      if (BeeUtils.same(columnId, NAME_MODE) && column instanceof HasCellRenderer) {
        ((HasCellRenderer) column).setRenderer(new ModeRenderer());

      } else if (BeeUtils.same(columnId, NAME_SLACK) && column instanceof HasCellRenderer) {
        ((HasCellRenderer) column).setRenderer(new SlackRenderer(dataColumns, column.getOptions()));

      } else if (BeeUtils.inListSame(columnId, COL_FINISH_TIME, COL_EXECUTOR)) {
        editableColumn.addCellValidationHandler(ValidationHelper.DO_NOT_VALIDATE);
      }

      return true;
    }

    @Override
    public boolean beforeCreateColumn(String columnId, List<? extends IsColumn> dataColumns,
        ColumnDescription columnDescription) {
      switch (type) {
        case ASSIGNED:
          return !BeeUtils.same(columnId, COL_EXECUTOR);
        case DELEGATED:
          return !BeeUtils.same(columnId, COL_OWNER);
        default:
          return true;
      }
    }

    @Override
    public String getCaption() {
      return type.getCaption();
    }

    @Override
    public String getColumnCaption(String columnName) {
      if (PROP_STAR.equals(columnName)) {
        return Stars.getDefaultHeader();
      } else {
        return super.getColumnCaption(columnName);
      }
    }

    @Override
    public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
        Collection<RowInfo> selectedRows, DeleteMode defMode) {
      Provider provider = presenter.getDataProvider();
      Long owner = activeRow.getLong(provider.getColumnIndex(COL_OWNER));

      if (owner == userId) {
        return GridInterceptor.DeleteMode.SINGLE;
      } else {
        presenter.getGridView().notifyWarning("Užduotį išmesti gali tik vadovas");
        return GridInterceptor.DeleteMode.CANCEL;
      }
    }

    @Override
    public List<String> getDeleteRowMessage(IsRow row) {
      String message = BeeUtils.joinWords("Išmesti užduotį", row.getId(), "?");
      return Lists.newArrayList(message);
    }

    @Override
    public AbstractFilterSupplier getFilterSupplier(String columnName,
        ColumnDescription columnDescription) {
      if (BeeUtils.same(columnName, NAME_SLACK)) {
        return new SlackFilterSupplier(columnDescription.getFilterOptions());

      } else if (BeeUtils.same(columnName, NAME_MODE)) {
        return new ModeFilterSupplier(columnDescription.getFilterOptions());

      } else {
        return super.getFilterSupplier(columnName, columnDescription);
      }
    }

    @Override
    public String getSupplierKey() {
      return BeeUtils.normalize(BeeUtils.join(BeeConst.STRING_UNDER, "grid", GRID_TASKS, type));
    }

    @Override
    public void onEditStart(final EditStartEvent event) {
      if (PROP_STAR.equals(event.getColumnId())) {
        IsRow row = event.getRowValue();
        if (row == null) {
          return;
        }

        if (row.getProperty(PROP_USER) == null) {
          return;
        }

        final CellSource source = CellSource.forProperty(PROP_STAR, ValueType.INTEGER);

        EditorAssistant.editStarCell(event, source, new Consumer<Integer>() {
          @Override
          public void accept(Integer parameter) {
            updateStar(event, source, parameter);
          }
        });
      }
    }

    @Override
    public boolean onLoad(GridDescription gridDescription) {
      gridDescription.setFilter(type.getFilter(new LongValue(userId)));
      return true;
    }

    private void updateStar(final EditStartEvent event, final CellSource source,
        final Integer value) {
      final long rowId = event.getRowValue().getId();

      Filter filter = Filter.and(ComparisonFilter.isEqual(COL_TASK, new LongValue(rowId)),
          ComparisonFilter.isEqual(COL_USER, new LongValue(userId)));

      Queries.update(VIEW_TASK_USERS, filter, COL_STAR, new IntegerValue(value),
          new Queries.IntCallback() {
            @Override
            public void onSuccess(Integer result) {
              BeeKeeper.getBus().fireEvent(new CellUpdateEvent(VIEW_TASKS, rowId,
                  event.getRowValue().getVersion(), source,
                  (value == null) ? null : BeeUtils.toString(value)));
            }
          });
    }
  }

  private static class ModeFilterSupplier extends AbstractFilterSupplier {

    private static final Splitter TASK_SPLITTER =
        Splitter.on(BeeConst.CHAR_COMMA).omitEmptyStrings().trimResults();

    private String caption = null;

    private final List<Long> newTasks = Lists.newArrayList();
    private final List<Long> updTasks = Lists.newArrayList();

    private ModeFilterSupplier(String options) {
      super(VIEW_TASKS, null, options);
    }

    @Override
    public String getDisplayHtml() {
      return getCaption();
    }

    @Override
    public void onRequest(final Element target, NotificationListener notificationListener,
        final Callback<Boolean> callback) {

      ParameterList params = CrmKeeper.createTaskRequestParameters(SVC_GET_CHANGED_TASKS);
      BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          if (response.hasErrors()) {
            if (callback != null) {
              callback.onFailure(response.getErrors());
            }

          } else {
            List<Long> tasks = Lists.newArrayList();
            for (String item : TASK_SPLITTER.split((String) response.getResponse())) {
              tasks.add(BeeUtils.toLong(item));
            }

            if (tasks.size() <= 1) {
              BeeKeeper.getScreen().notifyInfo("Naujų arba pasikeitusių užduočių nerasta");
              return;
            }

            newTasks.clear();
            updTasks.clear();

            int cntNew = BeeUtils.toInt(tasks.get(0));
            if (cntNew > 0) {
              newTasks.addAll(tasks.subList(1, cntNew + 1));
            }

            if (cntNew < tasks.size() - 1) {
              updTasks.addAll(tasks.subList(cntNew + 1, tasks.size()));
            }

            openDialog(target, createWidget(), callback);
          }
        }
      });
    }

    @Override
    public Filter parse(String value) {
      if (BeeUtils.isDigit(value)) {
        switch (BeeUtils.toInt(value)) {
          case 0:
            return getNewFilter();
          case 1:
            return getUpdFilter();
          case 2:
            return Filter.or(getNewFilter(), getUpdFilter());
        }
      }

      return null;
    }

    @Override
    public boolean reset() {
      setCaption(null);
      return super.reset();
    }

    @Override
    protected List<SupplierAction> getActions() {
      return Lists.newArrayList();
    }

    @Override
    protected String getStylePrefix() {
      return "bee-crm-FilterSupplier-Mode-";
    }
    
    private Widget createMode(String styleName) {
      return new CustomDiv(styleName);
    }

    private Widget createWidget() {
      HtmlTable container = new HtmlTable();
      container.addStyleName(getStylePrefix() + "container");

      int row = 0;

      if (!newTasks.isEmpty()) {
        BeeButton bNew = new BeeButton(Localized.constants.taskFilterNew());
        bNew.addStyleName(getStylePrefix() + "new");

        bNew.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            setCaption("n");
            update(getNewFilter());
          }
        });

        container.setWidget(row, 0, bNew);

        Widget modeNew = createMode(TaskList.STYLE_MODE_NEW);
        container.setWidget(row, 1, modeNew);

        container.setText(row, 2, BeeUtils.toString(newTasks.size()));

        row++;
      }

      if (!updTasks.isEmpty()) {
        BeeButton bUpd = new BeeButton(Localized.constants.taskFilterUpdated());
        bUpd.addStyleName(getStylePrefix() + "upd");

        bUpd.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            setCaption("p");
            update(getUpdFilter());
          }
        });

        container.setWidget(row, 0, bUpd);

        Widget modeUpd = createMode(TaskList.STYLE_MODE_UPD);
        container.setWidget(row, 1, modeUpd);

        container.setText(row, 2, BeeUtils.toString(updTasks.size()));

        row++;
      }

      if (!newTasks.isEmpty() && !updTasks.isEmpty()) {
        BeeButton both = new BeeButton(Localized.constants.taskFilterNewOrUpdated());
        both.addStyleName(getStylePrefix() + "both");

        both.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            setCaption("np");
            update(Filter.or(getNewFilter(), getUpdFilter()));
          }
        });

        container.setWidget(row, 0, both);

        Flow modeBoth = new Flow();
        modeBoth.add(createMode(TaskList.STYLE_MODE_NEW));
        modeBoth.add(createMode(TaskList.STYLE_MODE_UPD));

        container.setWidget(row, 1, modeBoth);

        container.setText(row, 2, BeeUtils.toString(newTasks.size() + updTasks.size()));
        
        row++;
      }

      BeeButton all = new BeeButton(Localized.constants.taskFilterAll());
      all.addStyleName(getStylePrefix() + "all");

      all.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          setCaption(null);
          update(null);
        }
      });

      container.setWidget(row, 0, all);

      BeeButton cancel = new BeeButton(Localized.constants.cancel());
      cancel.addStyleName(getStylePrefix() + "cancel");

      cancel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          closeDialog();
        }
      });

      container.setWidget(row, 1, cancel);

      return container;
    }

    private String getCaption() {
      return caption;
    }

    private Filter getNewFilter() {
      return Filter.idIn(newTasks);
    }

    private Filter getUpdFilter() {
      return Filter.idIn(updTasks);
    }

    private void setCaption(String caption) {
      this.caption = caption;
    }
  }

  private static class ModeRenderer extends AbstractCellRenderer {

    private ModeRenderer() {
      super(null);
    }

    @Override
    public String render(IsRow row) {
      if (row == null) {
        return null;
      }

      if (row.getProperty(PROP_USER) == null) {
        return BeeConst.STRING_EMPTY;
      }

      Long access = BeeUtils.toLongOrNull(row.getProperty(PROP_LAST_ACCESS));
      if (access == null) {
        return renderMode(TaskList.STYLE_MODE_NEW);
      }

      Long publish = BeeUtils.toLongOrNull(row.getProperty(PROP_LAST_PUBLISH));
      if (publish != null && access < publish) {
        return renderMode(TaskList.STYLE_MODE_UPD);
      }
      return BeeConst.STRING_EMPTY;
    }
    
    private String renderMode(String styleName) {
      return "<div class=\"" + styleName + "\"></div>";
    }
  }

  private static class SlackFilterSupplier extends AbstractFilterSupplier {

    private String caption = null;

    private SlackFilterSupplier(String options) {
      super(VIEW_TASKS, null, options);
    }

    @Override
    public String getDisplayHtml() {
      return getCaption();
    }

    @Override
    public void onRequest(Element target, NotificationListener notificationListener,
        Callback<Boolean> callback) {
      openDialog(target, createWidget(), callback);
    }

    @Override
    public Filter parse(String value) {
      if (BeeUtils.same(value, "late")) {
        return getLateFilter();
      } else if (BeeUtils.same(value, "scheduled")) {
        return getScheduledFilter();
      } else {
        return null;
      }
    }

    @Override
    public boolean reset() {
      setCaption(null);
      return super.reset();
    }

    @Override
    protected List<SupplierAction> getActions() {
      return Lists.newArrayList();
    }

    @Override
    protected String getStylePrefix() {
      return "bee-crm-FilterSupplier-Slack-";
    }

    private Widget createWidget() {
      Flow container = new Flow();
      container.addStyleName(getStylePrefix() + "container");

      BeeButton late = new BeeButton(Localized.constants.taskFilterLate());
      late.addStyleName(getStylePrefix() + "late");

      late.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          setCaption(Localized.constants.taskLabelLate());
          update(getLateFilter());
        }
      });

      container.add(late);

      BeeButton scheduled = new BeeButton(Localized.constants.taskFilterScheduled());
      scheduled.addStyleName(getStylePrefix() + "scheduled");

      scheduled.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          setCaption(Localized.constants.taskLabelScheduled());
          update(getScheduledFilter());
        }
      });

      container.add(scheduled);

      BeeButton all = new BeeButton(Localized.constants.taskFilterAll());
      all.addStyleName(getStylePrefix() + "all");

      all.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          setCaption(null);
          update(null);
        }
      });

      container.add(all);

      BeeButton cancel = new BeeButton(Localized.constants.cancel());
      cancel.addStyleName(getStylePrefix() + "cancel");

      cancel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          closeDialog();
        }
      });

      container.add(cancel);

      return container;
    }

    private String getCaption() {
      return caption;
    }

    private Filter getLateFilter() {
      return Filter.and(ComparisonFilter.isLess(COL_STATUS, new IntegerValue(4)),
          ComparisonFilter.isLess(COL_FINISH_TIME, new DateTimeValue(TimeUtils.nowMinutes())));
    }

    private Filter getScheduledFilter() {
      return Filter.and(ComparisonFilter.isLess(COL_STATUS, new IntegerValue(4)),
          ComparisonFilter.isMoreEqual(COL_START_TIME,
              new DateTimeValue(TimeUtils.today(1).getDateTime())));
    }

    private void setCaption(String caption) {
      this.caption = caption;
    }
  }

  private static class SlackRenderer extends AbstractCellRenderer {

    private static final SlackTemplate TEMPLATE = GWT.create(SlackTemplate.class);

    private static final Splitter OPTION_SPLITTER =
        Splitter.on(CharMatcher.inRange(BeeConst.CHAR_ZERO, BeeConst.CHAR_NINE).negate())
            .omitEmptyStrings().trimResults();

    private final int statusIndex;
    private final int startIndex;
    private final int finishIndex;

    private int minBarWidth = 10;
    private int maxBarWidth = 100;

    private int maxWidthDays = 30;

    private SlackRenderer(List<? extends IsColumn> columns, String options) {
      super(null);

      this.statusIndex = DataUtils.getColumnIndex(COL_STATUS, columns);
      this.startIndex = DataUtils.getColumnIndex(COL_START_TIME, columns);
      this.finishIndex = DataUtils.getColumnIndex(COL_FINISH_TIME, columns);

      setOptions(options);
    }

    @Override
    public String render(IsRow row) {
      if (row == null) {
        return null;
      }

      TaskStatus status = NameUtils.getEnumByIndex(TaskStatus.class, row.getInteger(statusIndex));

      DateTime start = row.getDateTime(startIndex);
      DateTime finish = row.getDateTime(finishIndex);

      long minutes = getMinutes(status, start, finish);
      if (minutes == 0) {
        return BeeConst.STRING_EMPTY;
      }

      SafeStyles barStyles = getBarStyles(Math.abs(minutes));
      String label = getLabel(Math.abs(minutes));

      if (minutes < 0) {
        return TEMPLATE.late(barStyles, label).asString();
      } else if (minutes > 0) {
        return TEMPLATE.scheduled(barStyles, label).asString();
      } else {
        return BeeConst.STRING_EMPTY;
      }
    }

    private SafeStyles getBarStyles(long minutes) {
      double width = BeeUtils.rescale(Math.abs(minutes),
          0, getMaxWidthDays() * TimeUtils.MINUTES_PER_DAY,
          getMinBarWidth(), getMaxBarWidth());

      return StyleUtils.buildWidth(width, CssUnit.PCT);
    }

    private String getLabel(long minutes) {
      if (Math.abs(minutes) < TimeUtils.MINUTES_PER_DAY) {
        return BeeUtils.toString(minutes / TimeUtils.MINUTES_PER_HOUR)
            + DateTime.TIME_FIELD_SEPARATOR
            + TimeUtils.padTwo((int) (minutes % TimeUtils.MINUTES_PER_HOUR));
      } else {
        return BeeUtils.toString(minutes / TimeUtils.MINUTES_PER_DAY);
      }
    }

    private int getMaxBarWidth() {
      return maxBarWidth;
    }

    private int getMaxWidthDays() {
      return maxWidthDays;
    }

    private int getMinBarWidth() {
      return minBarWidth;
    }

    private long getMinutes(TaskStatus status, DateTime start, DateTime finish) {
      if (status == null || status == TaskStatus.COMPLETED || status == TaskStatus.CANCELED) {
        return 0;
      }

      DateTime now = TimeUtils.nowMinutes();

      if (finish != null && TimeUtils.isLess(finish, now)) {
        return (finish.getTime() - now.getTime()) / TimeUtils.MILLIS_PER_MINUTE;
      } else if (CrmUtils.isScheduled(start)) {
        return TimeUtils.dayDiff(now, start) * TimeUtils.MINUTES_PER_DAY;
      } else {
        return 0;
      }
    }

    private void setMaxBarWidth(int maxBarWidth) {
      this.maxBarWidth = maxBarWidth;
    }

    private void setMaxWidthDays(int maxWidthDays) {
      this.maxWidthDays = maxWidthDays;
    }

    private void setMinBarWidth(int minBarWidth) {
      this.minBarWidth = minBarWidth;
    }

    private void setOptions(String options) {
      if (BeeUtils.isEmpty(options)) {
        return;
      }
      List<String> args = Lists.newArrayList(OPTION_SPLITTER.split(options));
      if (args.isEmpty()) {
        return;
      }

      if (args.size() >= 1) {
        setMinBarWidth(BeeUtils.clamp(BeeUtils.toInt(args.get(0)), 0, 50));
      }
      if (args.size() >= 2) {
        setMaxBarWidth(BeeUtils.clamp(BeeUtils.toInt(args.get(1)), getMinBarWidth() + 10, 100));
      }
      if (args.size() >= 3) {
        setMaxWidthDays(Math.max(BeeUtils.toInt(args.get(2)), 1));
      }
    }
  }

  private enum Type implements HasCaption {
    ASSIGNED("Gautos užduotys") {
      @Override
      Filter getFilter(LongValue userValue) {
        return ComparisonFilter.isEqual(COL_EXECUTOR, userValue);
      }
    },

    DELEGATED("Deleguotos užduotys") {
      @Override
      Filter getFilter(LongValue userValue) {
        return Filter.and(ComparisonFilter.isEqual(COL_OWNER, userValue), ComparisonFilter
            .isNotEqual(COL_EXECUTOR, userValue));
      }
    },

    OBSERVED("Stebimos užduotys") {
      @Override
      Filter getFilter(LongValue userValue) {
        return Filter.and(ComparisonFilter.isNotEqual(COL_OWNER, userValue),
            ComparisonFilter.isNotEqual(COL_EXECUTOR, userValue),
            Filter.in(Data.getIdColumn(VIEW_TASKS), VIEW_TASK_USERS, COL_TASK,
                ComparisonFilter.isEqual(COL_USER, userValue)));
      }
    },

    GENERAL("Užduočių sąrašas") {
      @Override
      Filter getFilter(LongValue userValue) {
        return null;
      }
    };

    private final String caption;

    private Type(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }

    abstract Filter getFilter(LongValue userValue);
  }

  private static final String STYLE_MODE_NEW = "bee-crm-Mode-new";
  private static final String STYLE_MODE_UPD = "bee-crm-Mode-upd";

  public static void open(String args) {
    Type type = null;

    for (Type z : Type.values()) {
      if (BeeUtils.startsSame(args, z.name())) {
        type = z;
        break;
      }
    }

    if (type == null) {
      Global.showError(Lists.newArrayList(GRID_TASKS, "Type not recognized:", args));
    } else {
      GridFactory.openGrid(GRID_TASKS, new GridHandler(type));
    }
  }

  private TaskList() {
    super();
  }
}
