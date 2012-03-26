package com.butent.bee.client.calendar.dayview;

import com.butent.bee.client.calendar.Appointment;
import com.butent.bee.client.calendar.DateUtils;
import com.butent.bee.client.calendar.HasSettings;
import com.butent.bee.client.calendar.util.AppointmentUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DayViewLayoutStrategy {

  private static final int MINUTES_PER_HOUR = 60;
  private static final int HOURS_PER_DAY = 24;
  private HasSettings settings = null;

  public DayViewLayoutStrategy(HasSettings settings) {
    this.settings = settings;
  }

  public ArrayList<AppointmentAdapter> doLayout(List<Appointment> appointments, int dayIndex,
      int dayCount) {

    int intervalsPerHour = settings.getSettings().getIntervalsPerHour();
    double intervalSize = settings.getSettings().getPixelsPerInterval();

    int minutesPerInterval = MINUTES_PER_HOUR / intervalsPerHour;

    int numberOfTimeBlocks = MINUTES_PER_HOUR / minutesPerInterval * HOURS_PER_DAY;
    TimeBlock[] timeBlocks = new TimeBlock[numberOfTimeBlocks];

    for (int i = 0; i < numberOfTimeBlocks; i++) {
      TimeBlock t = new TimeBlock();
      t.setStart(i * minutesPerInterval);
      t.setEnd(t.getStart() + minutesPerInterval);
      t.setOrder(i);
      t.setTop(i * intervalSize);
      t.setBottom(t.getTop() + intervalSize);
      timeBlocks[i] = t;
    }

    ArrayList<AppointmentAdapter> appointmentCells = new ArrayList<AppointmentAdapter>();

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

    double widthAdj = 1f / dayCount;

    double paddingLeft = .5f;
    double paddingRight = .5f;
    double paddingBottom = 2;

    for (AppointmentAdapter apptCell : appointmentCells) {
      double width = 1f / apptCell.getIntersectingBlocks().get(0).getTotalColumns() * 100;
      double left =
          (double) apptCell.getColumnStart()
              / (double) apptCell.getIntersectingBlocks().get(0).getTotalColumns() * 100;

      apptCell.setTop(apptCell.getCellStart() * intervalSize);
      apptCell.setLeft((widthAdj * 100 * dayIndex) + (left * widthAdj) + paddingLeft);
      apptCell.setWidth(width * widthAdj - paddingLeft - paddingRight);
      apptCell.setHeight(apptCell.getIntersectingBlocks().size() * intervalSize - paddingBottom);

      double apptStart = apptCell.getAppointmentStart();
      double apptEnd = apptCell.getAppointmentEnd();
      double blockStart = timeBlocks[apptCell.getCellStart()].getStart();
      double blockEnd = timeBlocks[apptCell.getCellStart() + apptCell.getCellSpan() - 1].getEnd();
      double blockDuration = blockEnd - blockStart;
      double apptDuration = apptEnd - apptStart;
      double timeFillHeight = apptDuration / blockDuration * 100f;
      double timeFillStart = (apptStart - blockStart) / blockDuration * 100f;

      apptCell.setCellPercentFill(timeFillHeight);
      apptCell.setCellPercentStart(timeFillStart);
    }
    return appointmentCells;
  }

  public int doMultiDayLayout(List<Appointment> appointments, List<AppointmentAdapter> adapters,
      Date start, int days) {

    HashMap<Integer, HashMap<Integer, Integer>> daySlotMap =
        new HashMap<Integer, HashMap<Integer, Integer>>();

    int minHeight = 30;
    int maxRow = 0;

    for (Appointment appointment : appointments) {
      adapters.add(new AppointmentAdapter(appointment));
    }

    ArrayList<Date> dateList = new ArrayList<Date>();
    Date tempStartDate = (Date) start.clone();

    for (int i = 0; i < days; i++) {
      Date d = (Date) tempStartDate.clone();
      DateUtils.resetTime(d);

      daySlotMap.put(i, new HashMap<Integer, Integer>());
      dateList.add(d);
      DateUtils.moveOneDayForward(tempStartDate);
    }

    for (AppointmentAdapter adapter : adapters) {
      int columnSpan = 0;
      boolean isStart = true;

      for (int i = 0; i < dateList.size(); i++) {
        Date date = dateList.get(i);
        boolean isWithinRange =  AppointmentUtil.rangeContains(adapter.getAppointment(), date);

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
            System.out.println("Exception: y=" + y + " x=" + x + " adapters.size="
                + adapters.size() + " start=" + adapter.getAppointment().getStart() + " end="
                + adapter.getAppointment().getEnd().toString());
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

          double top = adapter.getCellStart() * 25f + 5f;
          double width = (adapter.getColumnSpan() + 1f) / days * 100f - 1f;
          double left = ((double) adapter.getColumnStart()) / days * 100f + .5f;

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
