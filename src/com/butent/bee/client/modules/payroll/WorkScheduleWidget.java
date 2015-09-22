package com.butent.bee.client.modules.payroll;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.data.Queries;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class WorkScheduleWidget extends HtmlTable {

  private static final String STYLE_PREFIX = PayrollKeeper.STYLE_PREFIX + "ws-";

  private static final String STYLE_TABLE = STYLE_PREFIX + "table";

  private final long objectId;

  private BeeRowSet wsData;
  private BeeRowSet eoData;
  private BeeRowSet tcData;

  WorkScheduleWidget(long objectId) {
    super(STYLE_TABLE);

    this.objectId = objectId;
  }

  void refresh() {
    Queries.getRowSet(VIEW_WORK_SCHEDULE, null, Filter.equals(COL_PAYROLL_OBJECT, objectId),
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet wsRowSet) {
            setWsData(wsRowSet);

            Queries.getRowSet(VIEW_EMPLOYEE_OBJECTS, null,
                Filter.equals(COL_PAYROLL_OBJECT, objectId), new Queries.RowSetCallback() {
                  @Override
                  public void onSuccess(BeeRowSet eoRowSet) {
                    setEoData(eoRowSet);

                    Collection<Long> employees = getEmployees();
                    if (employees.isEmpty()) {
                      setTcData(null);
                      render();

                    } else {
                      Queries.getRowSet(VIEW_TIME_CARD_CHANGES, null,
                          Filter.any(COL_EMPLOYEE, employees), new Queries.RowSetCallback() {
                            @Override
                            public void onSuccess(BeeRowSet tcRowSet) {
                              setTcData(tcRowSet);
                              render();
                            }
                          });
                    }
                  }
                });
          }
        });
  }

  private void render() {
    if (!isEmpty()) {
      clear();
    }

    int r = 0;

    setText(r, 0, VIEW_WORK_SCHEDULE);
    if (!DataUtils.isEmpty(wsData)) {
      setText(r, 1, BeeUtils.toString(wsData.getNumberOfRows()));
    }

    r++;
    setText(r, 0, VIEW_EMPLOYEE_OBJECTS);
    if (!DataUtils.isEmpty(eoData)) {
      setText(r, 1, BeeUtils.toString(eoData.getNumberOfRows()));
    }

    r++;
    setText(r, 0, VIEW_TIME_CARD_CHANGES);
    if (!DataUtils.isEmpty(tcData)) {
      setText(r, 1, BeeUtils.toString(tcData.getNumberOfRows()));
    }
  }

  private Collection<Long> getEmployees() {
    Set<Long> employees = new HashSet<>();

    if (!DataUtils.isEmpty(wsData)) {
      employees.addAll(wsData.getDistinctLongs(wsData.getColumnIndex(COL_EMPLOYEE)));
    }
    if (!DataUtils.isEmpty(eoData)) {
      employees.addAll(eoData.getDistinctLongs(eoData.getColumnIndex(COL_EMPLOYEE)));
    }

    return employees;
  }

  private void setWsData(BeeRowSet wsData) {
    this.wsData = wsData;
  }

  private void setEoData(BeeRowSet eoData) {
    this.eoData = eoData;
  }

  private void setTcData(BeeRowSet tcData) {
    this.tcData = tcData;
  }
}
