package com.butent.bee.client.modules.service;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.timeboard.TimeBoard;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.ViewCallback;
import com.butent.bee.client.view.ViewFactory;
import com.butent.bee.client.view.ViewSupplier;
import com.butent.bee.client.view.grid.interceptor.FileGridInterceptor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.menu.MenuHandler;
import com.butent.bee.shared.menu.MenuService;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.service.ServiceConstants.ObjectStatus;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.utils.EnumUtils;

public final class ServiceKeeper {

  public static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "svc-";

  public static ParameterList createArgs(String method) {
    return BeeKeeper.getRpc().createParameters(Module.SERVICE, method);
  }

  public static void register() {
    GridFactory.registerGridInterceptor(VIEW_SERVICE_FILES,
        new FileGridInterceptor(COL_SERVICE_OBJECT, AdministrationConstants.COL_FILE,
            AdministrationConstants.COL_FILE_CAPTION, AdministrationConstants.ALS_FILE_NAME));

    GridFactory.registerGridInterceptor(VIEW_MAINTENANCE, new MaintenanceGrid());

    GridFactory.registerGridInterceptor(GRID_OBJECT_INVOICES, new ObjectInvoicesGrid());
    GridFactory.registerGridInterceptor(GRID_OBJECT_DEFECTS, new ObjectDefectsGrid());
    GridFactory.registerGridInterceptor(GRID_SERVICE_DEFECTS, new ServiceDefectsGrid());
    GridFactory.registerGridInterceptor(GRID_SERVICE_INVOICES, new ServiceInvoicesGrid());
    GridFactory.registerGridInterceptor(GRID_SVC_PROJECT_INVOICES, new SvcProjectInvoicesGrid());

    FormFactory.registerFormInterceptor("ServiceObject", new ServiceObjectForm());
    FormFactory.registerFormInterceptor("ServiceInvoice", new ServiceInvoiceForm());
    FormFactory.registerFormInterceptor("ServiceDefect", new ServiceDefectForm());

    SelectorEvent.register(new SelectorHandler());

    TimeBoard.ensureStyleSheet();

    registerServiceOjectsGridSupplier();

    MenuService.SERVICE_CALENDAR.setHandler(new MenuHandler() {
      @Override
      public void onSelection(String parameters) {
        ServiceCalendar.open(new ViewCallback() {
          @Override
          public void onSuccess(View result) {
            BeeKeeper.getScreen().show(result);
          }
        }, ObjectStatus.SERVICE_OBJECT);
      }
    });

    MenuService.SERVICE_PROJECTS_CALENDAR.setHandler(new MenuHandler() {
      @Override
      public void onSelection(String parameters) {
        ServiceCalendar.open(new ViewCallback() {
          @Override
          public void onSuccess(View result) {
            BeeKeeper.getScreen().show(result);
          }
        }, ObjectStatus.PROJECT_OBJECT);
      }
    });

    MenuService.SERVICE_OBJECTS.setHandler(new MenuHandler() {

      @Override
      public void onSelection(String parameters) {
        Assert.notEmpty(parameters);

        ObjectStatus status = EnumUtils.getEnumByName(ObjectStatus.class, parameters);

        if (status == null) {
          Global.showError(Lists.newArrayList(GRID_SERVICE_OBJECTS, "Type not recognized:",
              parameters));
        } else {
          ViewFactory.createAndShow(status.getSuplierKey());
        }
      }

    });

    ViewFactory.registerSupplier(ServiceCalendar.SUPPLIER_KEY, new ViewSupplier() {
      @Override
      public void create(ViewCallback callback) {
        ServiceCalendar.open(callback, ObjectStatus.SERVICE_OBJECT);
      }
    });

    ViewFactory.registerSupplier(ServiceCalendar.SUPPLIER_KEY_PROJECTS, new ViewSupplier() {
      @Override
      public void create(ViewCallback callback) {
        ServiceCalendar.open(callback, ObjectStatus.PROJECT_OBJECT);
      }
    });
  }

  private static void registerServiceOjectsGridSupplier() {
    for (ObjectStatus objStatus : ObjectStatus.values()) {
      GridFactory.registerGridSupplier(objStatus.getSuplierKey(), GRID_SERVICE_OBJECTS,
          new ServiceObjectsGrid(objStatus));
    }
  }

  private ServiceKeeper() {
  }
}
