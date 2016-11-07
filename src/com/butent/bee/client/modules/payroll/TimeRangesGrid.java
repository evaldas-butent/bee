package com.butent.bee.client.modules.payroll;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridFormKind;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.payroll.PayrollUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Objects;

class TimeRangesGrid extends AbstractGridInterceptor {

  TimeRangesGrid() {
  }

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {

    if (hasUsage(activeRow)) {
      return DeleteMode.DENY;
    } else {
      return DeleteMode.SINGLE;
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new TimeRangesGrid();
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (event != null && DataUtils.hasId(event.getRowValue()) && hasUsage(event.getRowValue())
        && event.hasAnySource(COL_TR_CODE, COL_TR_FROM, COL_TR_UNTIL, COL_TR_DURATION)
        && !BeeKeeper.getUser().isAdministrator()) {

      event.consume();

    } else {
      super.onEditStart(event);
    }
  }

  @Override
  public void onReadyForInsert(final GridView gridView, final ReadyForInsertEvent event) {
    if (gridView != null && event != null) {
      event.consume();

      final String code = event.getValue(COL_TR_CODE);

      String from = event.getValue(COL_TR_FROM);
      String until = event.getValue(COL_TR_UNTIL);
      String duration = event.getValue(COL_TR_DURATION);

      if (BeeUtils.isEmpty(code)) {
        showError(gridView, Data.getColumnLabel(getViewName(), COL_TR_CODE),
            Localized.dictionary().valueRequired());
        tryFocus(gridView, COL_TR_CODE);
        return;
      }

      if (BeeUtils.isEmpty(from)) {
        showError(gridView, Data.getColumnLabel(getViewName(), COL_TR_FROM),
            Localized.dictionary().valueRequired());
        tryFocus(gridView, COL_TR_FROM);
        return;

      } else if (!PayrollUtils.validTimeOfDay(from)) {
        showError(gridView, Localized.dictionary().invalidTime(), from);
        tryFocus(gridView, COL_TR_FROM);
        return;
      }

      if (BeeUtils.isEmpty(until)) {
        showError(gridView, Data.getColumnLabel(getViewName(), COL_TR_UNTIL),
            Localized.dictionary().valueRequired());
        tryFocus(gridView, COL_TR_UNTIL);
        return;

      } else if (!PayrollUtils.validTimeOfDay(until)) {
        showError(gridView, Localized.dictionary().invalidTime(), until);
        tryFocus(gridView, COL_TR_UNTIL);
        return;
      }

      final Long fromMillis = TimeUtils.parseTime(from);
      final Long untilMillis = TimeUtils.parseTime(until);

      if (BeeUtils.isLeq(untilMillis, fromMillis)) {
        showError(gridView, Localized.dictionary().invalidRange(),
            TimeUtils.renderPeriod(from, until));
        tryFocus(gridView, COL_TR_UNTIL);
        return;
      }

      final Long durationMillis;

      if (BeeUtils.isEmpty(duration)) {
        durationMillis = null;
      } else {
        durationMillis = TimeUtils.parseTime(duration);

        if (!BeeUtils.isPositive(durationMillis)
            || BeeUtils.isMore(durationMillis, TimeUtils.MILLIS_PER_DAY)) {

          showError(gridView, Localized.dictionary().invalidTime(), duration);
          tryFocus(gridView, COL_TR_DURATION);
          return;
        }
      }

      Queries.getRowSet(getViewName(), null, new Queries.RowSetCallback() {
        @Override
        public void onSuccess(BeeRowSet rowSet) {
          boolean fire = true;

          if (!DataUtils.isEmpty(rowSet)) {
            int codeIndex = rowSet.getColumnIndex(COL_TR_CODE);

            for (IsRow row : rowSet) {
              if (BeeUtils.same(row.getString(codeIndex), code)) {
                showError(gridView,
                    BeeUtils.joinWords(Localized.getLabel(rowSet.getColumn(codeIndex)),
                        Localized.dictionary().valueExists(code)));
                tryFocus(gridView, COL_TR_CODE);

                fire = false;
                break;
              }
            }

            if (fire) {
              int fromIndex = rowSet.getColumnIndex(COL_TR_FROM);
              int untilIndex = rowSet.getColumnIndex(COL_TR_UNTIL);
              int durationIndex = rowSet.getColumnIndex(COL_TR_DURATION);

              for (IsRow row : rowSet) {
                if (Objects.equals(fromMillis, TimeUtils.parseTime(row.getString(fromIndex)))
                    && Objects.equals(untilMillis, TimeUtils.parseTime(row.getString(untilIndex)))
                    && Objects.equals(durationMillis,
                    TimeUtils.parseTime(row.getString(durationIndex)))) {

                  showError(gridView,
                      Localized.dictionary().valueExists(Localized.dictionary().timeRange()),
                      BeeUtils.joinWords(Localized.getLabel(rowSet.getColumn(codeIndex)),
                          row.getString(codeIndex)));
                  tryFocus(gridView, COL_TR_FROM);

                  fire = false;
                  break;
                }
              }
            }
          }

          if (fire) {
            gridView.fireEvent(event);
          }
        }
      });
    }
  }

  private boolean hasUsage(IsRow row) {
    if (DataUtils.hasId(row)) {
      int index = getDataIndex(ALS_TR_USAGE);
      if (index >= 0) {
        return BeeUtils.isPositive(row.getInteger(index));
      }
    }

    return false;
  }

  private static void showError(GridView gridView, String... messages) {
    NotificationListener listener;

    if (gridView == null) {
      listener = BeeKeeper.getScreen();
    } else {
      listener = gridView.getForm(GridFormKind.NEW_ROW);
      if (listener == null) {
        listener = gridView;
      }
    }

    listener.notifySevere(messages);
  }

  private static void tryFocus(GridView gridView, String source) {
    if (gridView != null) {
      FormView formView = gridView.getForm(GridFormKind.NEW_ROW);

      if (formView != null) {
        formView.focus(source);
      }
    }
  }
}
