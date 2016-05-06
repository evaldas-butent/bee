package com.butent.bee.client.modules.payroll;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

enum ScheduleParent {
  LOCATION {
    @Override
    String getEmployeeObjectRelationColumn() {
      return COL_PAYROLL_OBJECT;
    }

    @Override
    String getPartitionViewName() {
      return VIEW_EMPLOYEES;
    }

    @Override
    String getStyleSuffix() {
      return "object";
    }

    @Override
    String getWorkSchedulePartitionColumn() {
      return COL_EMPLOYEE;
    }

    @Override
    String getWorkScheduleRelationColumn() {
      return COL_PAYROLL_OBJECT;
    }
  },

  EMPLOYEE {
    @Override
    String getEmployeeObjectRelationColumn() {
      return COL_EMPLOYEE;
    }

    @Override
    String getPartitionViewName() {
      return VIEW_LOCATIONS;
    }

    @Override
    String getStyleSuffix() {
      return "employee";
    }

    @Override
    String getWorkSchedulePartitionColumn() {
      return COL_PAYROLL_OBJECT;
    }

    @Override
    String getWorkScheduleRelationColumn() {
      return COL_EMPLOYEE;
    }
  };

  abstract String getEmployeeObjectRelationColumn();

  abstract String getPartitionViewName();

  abstract String getStyleSuffix();

  abstract String getWorkSchedulePartitionColumn();

  abstract String getWorkScheduleRelationColumn();
}
