package com.butent.bee.client.calendar.resourceview;

import com.google.common.collect.Lists;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.calendar.Appointment;
import com.butent.bee.client.calendar.HasSettings;
import com.butent.bee.client.calendar.util.AppointmentAdapter;
import com.butent.bee.client.calendar.util.AppointmentUtil;
import com.butent.bee.client.calendar.util.TimeBlock;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.HashMap;
import java.util.List;

public class ResourceViewLayoutStrategy {

  private static final int MINUTES_PER_HOUR = 60;
  private static final int HOURS_PER_DAY = 24;

  private final HasSettings settings;

  public ResourceViewLayoutStrategy(HasSettings settings) {
    this.settings = settings;
  }

  public List<AppointmentAdapter> doLayout(List<Appointment> appointments, int index, int cc) {

    int intervalsPerHour = settings.getSettings().getIntervalsPerHour();
    int intervalSize = settings.getSettings().getPixelsPerInterval();

    int minutesPerInterval = MINUTES_PER_HOUR / intervalsPerHour;
    int numberOfTimeBlocks = MINUTES_PER_HOUR / minutesPerInterval * HOURS_PER_DAY;

    TimeBlock[] timeBlocks = new TimeBlock[numberOfTimeBlocks];

    for (int i = 0; i < numberOfTimeBlocks; i++) {
      TimeBlock t = new TimeBlock();
      t.setOrder(i);
      
      t.setStart(i * minutesPerInterval);
      t.setEnd(t.getStart() + minutesPerInterval);
      
      t.setTop(i * intervalSize);
      t.setBottom(t.getTop() + intervalSize);
      
      timeBlocks[i] = t;
    }

    List<AppointmentAdapter> appointmentCells = Lists.newArrayList();

    int groupMaxColumn = 0;
    int groupStartIndex = -1;
    int groupEndIndex = -2;

    for (Appointment appointment : appointments) {
      TimeBlock startBlock = null;
      TimeBlock endBlock = null;

      AppointmentAdapter apptCell = new AppointmentAdapter(appointment);
      appointmentCells.add(apptCell);

      for (TimeBlock block : timeBlocks) {
        if (block.intersectsWith(apptCell)) {
          startBlock = block;

          if (groupEndIndex < startBlock.getOrder()) {
            for (int i = groupStartIndex; i <= groupEndIndex; i++) {
              TimeBlock tb = timeBlocks[i];
              tb.setTotalColumns(groupMaxColumn + 1);
            }
            groupStartIndex = startBlock.getOrder();
            groupMaxColumn = 0;
          }
          break;
        }
      }

      startBlock.getAppointments().add(apptCell);
      apptCell.getIntersectingBlocks().add(startBlock);

      int column = startBlock.getFirstAvailableColumn();
      apptCell.setColumnStart(column);
      apptCell.setColumnSpan(1);

      startBlock.getOccupiedColumns().put(column, column);

      apptCell.setCellStart(startBlock.getOrder());

      for (int i = startBlock.getOrder() + 1; i < timeBlocks.length; i++) {
        TimeBlock nextBlock = timeBlocks[i];

        if (nextBlock.intersectsWith(apptCell)) {
          nextBlock.getAppointments().add(apptCell);
          nextBlock.getOccupiedColumns().put(column, column);
          endBlock = nextBlock;

          apptCell.getIntersectingBlocks().add(nextBlock);
        }
      }

      endBlock = (endBlock == null) ? startBlock : endBlock;
      if (column > groupMaxColumn) {
        groupMaxColumn = column;
      }

      if (groupEndIndex < endBlock.getOrder()) {
        groupEndIndex = endBlock.getOrder();
      }

      apptCell.setCellSpan(endBlock.getOrder() - startBlock.getOrder() + 1);
    }

    for (int i = groupStartIndex; i <= groupEndIndex; i++) {
      TimeBlock tb = timeBlocks[i];
      tb.setTotalColumns(groupMaxColumn + 1);
    }

    double widthAdj = 1d / cc;

    double paddingLeft = 0.5d;
    double paddingRight = 0.5d;
    double paddingBottom = 2;

    for (AppointmentAdapter apptCell : appointmentCells) {
      int totalColumns = apptCell.getIntersectingBlocks().get(0).getTotalColumns();
      double width = 1d / totalColumns * 100;
      double left = (double) apptCell.getColumnStart() / totalColumns * 100;

      apptCell.setLeft(widthAdj * 100 * index + left * widthAdj + paddingLeft);
      apptCell.setWidth(width * widthAdj - paddingLeft - paddingRight);

      apptCell.setTop(apptCell.getCellStart() * intervalSize);
      apptCell.setHeight(apptCell.getIntersectingBlocks().size() * intervalSize - paddingBottom);

      double apptStart = apptCell.getAppointmentStart();
      double apptEnd = apptCell.getAppointmentEnd();
      double apptDuration = apptEnd - apptStart;

      double blockStart = timeBlocks[apptCell.getCellStart()].getStart();
      double blockEnd = timeBlocks[apptCell.getCellStart() + apptCell.getCellSpan() - 1].getEnd();
      double blockDuration = blockEnd - blockStart;

      double timeFillHeight = apptDuration / blockDuration * 100d;
      double timeFillStart = (apptStart - blockStart) / blockDuration * 100d;

      apptCell.setCellPercentFill(timeFillHeight);
      apptCell.setCellPercentStart(timeFillStart);
    }
    return appointmentCells;
  }

  public int doMultiDayLayout(List<Appointment> appointments, List<AppointmentAdapter> adapters,
      JustDate start, int days) {

    HashMap<Integer, HashMap<Integer, Integer>> daySlotMap =
        new HashMap<Integer, HashMap<Integer, Integer>>();

    int minHeight = 30;
    int maxRow = 0;

    for (Appointment appointment : appointments) {
      adapters.add(new AppointmentAdapter(appointment));
    }

    List<JustDate> dateList = Lists.newArrayList();
    JustDate tmp = JustDate.copyOf(start);

    for (int i = 0; i < days; i++) {
      JustDate d = JustDate.copyOf(tmp);

      daySlotMap.put(i, new HashMap<Integer, Integer>());
      dateList.add(d);
      
      TimeUtils.moveOneDayForward(tmp);
    }

    for (AppointmentAdapter adapter : adapters) {
      int columnSpan = 0;
      boolean isStart = true;

      for (int i = 0; i < dateList.size(); i++) {
        JustDate date = dateList.get(i);
        boolean isWithinRange =  AppointmentUtil.rangeContains(adapter.getAppointment(),
            TimeUtils.startOfDay(date), TimeUtils.startOfDay(date, 1));

        if (isWithinRange) {
          if (isStart) {
            adapter.setColumnStart(i);
            isStart = false;
          }

          adapter.setColumnSpan(columnSpan);
          columnSpan++;
        }
      }

      for (int x = 0; x < adapters.size(); x++) {
        boolean isRowOccupied = false;
        for (int y = adapter.getColumnStart(); y <= adapter.getColumnStart()
            + adapter.getColumnSpan(); y++) {
          try {
            HashMap<Integer, Integer> rowMap = daySlotMap.get(y);
            if (rowMap.containsKey(x)) {
              isRowOccupied = true;
            } else {
              break;
            }
          } catch (Exception ex) {
            BeeKeeper.getLog().severe("Exception: y=", y, "x=", x,
                "adapters.size=", adapters.size(), "start=", adapter.getAppointment().getStart(),
                "end=", adapter.getAppointment().getEnd());
          }
        }

        if (!isRowOccupied) {
          for (int y = adapter.getColumnStart(); y <= adapter.getColumnStart()
              + adapter.getColumnSpan(); y++) {
            HashMap<Integer, Integer> rowMap = daySlotMap.get(y);
            rowMap.put(x, x);
            if (x > maxRow) {
              maxRow = x;
            }
          }

          adapter.setCellStart(x);

          double top = adapter.getCellStart() * 25d + 5d;
          double width = (adapter.getColumnSpan() + 1d) / days * 100d - 1d;
          double left = ((double) adapter.getColumnStart()) / days * 100d + 0.5d;

          adapter.setWidth(width);
          adapter.setLeft(left);
          adapter.setTop(top);
          adapter.setHeight(20);

          break;
        }
      }
    }

    int height = (maxRow + 1) * 25 + 5;
    return Math.max(height, minHeight);
  }
}
