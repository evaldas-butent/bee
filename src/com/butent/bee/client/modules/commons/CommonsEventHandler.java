package com.butent.bee.client.modules.commons;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.MenuManager;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.modules.commons.ParametersHandler.ParameterFormHandler;
import com.butent.bee.client.presenter.GridFormPresenter;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.modules.ParameterType;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsObjectType;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;
import java.util.Set;

public class CommonsEventHandler {

  private static class ItemFormHandler extends AbstractFormCallback {

    @Override
    public FormCallback getInstance() {
      return new ItemFormHandler();
    }

    @Override
    public boolean onReadyForInsert(final ReadyForInsertEvent event) {
      Assert.notNull(event);

      String price = null;
      String currency = null;

      for (int i = 0; i < event.getColumns().size(); i++) {
        String colName = event.getColumns().get(i).getId();
        String value = event.getValues().get(i);

        if (BeeUtils.same(colName, "Price")) {
          price = value;
        } else if (BeeUtils.same(colName, "Currency")) {
          currency = value;
        }
      }

      if (!BeeUtils.isEmpty(price) && BeeUtils.isEmpty(currency)) {
        event.getCallback().onFailure("Currency required");
        return false;
      }

      BeeRowSet rs = new BeeRowSet("Items", event.getColumns());
      rs.addRow(0, event.getValues().toArray(new String[0]));

      ParameterList args = createArgs(CommonsConstants.SVC_ITEM_CREATE);
      args.addDataItem(CommonsConstants.VAR_ITEM_DATA, Codec.beeSerialize(rs));
      
      String categories = 
          getFormView().getActiveRow().getProperty(CommonsConstants.PROP_CATEGORIES);

      if (!BeeUtils.isEmpty(categories)) {
        args.addDataItem(CommonsConstants.VAR_ITEM_CATEGORIES, categories);
      }
      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          Assert.notNull(response);

          if (response.hasErrors()) {
            event.getCallback().onFailure(response.getErrors());

          } else if (response.hasResponse(BeeRow.class)) {
            event.getCallback().onSuccess(BeeRow.restore((String) response.getResponse()));

          } else {
            event.getCallback().onFailure("Unknown response");
          }
        }
      });
      return false;
    }

    @Override
    public boolean onStartEdit(final FormView form, final IsRow row,
        final Scheduler.ScheduledCommand focusCommand) {
 
      Filter flt = ComparisonFilter.isEqual(CommonsConstants.COL_ITEM, new LongValue(row.getId()));

      Queries.getRowSet(CommonsConstants.TBL_ITEM_CATEGORIES, null, flt, null,
          new RowSetCallback() {
            @Override
            public void onSuccess(BeeRowSet result) {
              if (!result.isEmpty()) {
                List<Long> categ = Lists.newArrayList();

                int index = result.getColumnIndex(CommonsConstants.COL_CATEGORY);
                for (IsRow r : result.getRows()) {
                  categ.add(r.getLong(index));
                }
                row.setProperty(CommonsConstants.PROP_CATEGORIES, DataUtils.buildIdList(categ));
              }
              
              form.updateRow(row, true);
              focusCommand.execute();
            }
          });

      return false;
    }

    @Override
    public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
      if (form.getViewPresenter() instanceof GridFormPresenter) {
        GridCallback gcb = ((GridFormPresenter) form.getViewPresenter()).getGridCallback();

        if (gcb != null && gcb instanceof ItemGridHandler) {
          ItemGridHandler grd = (ItemGridHandler) gcb;

          if (grd.showServices()) {
            newRow.setValue(form.getDataIndex(CommonsConstants.COL_SERVICE), 1);
          }

          IsRow category = grd.getSelectedCategory();
          if (category != null) {
            newRow.setProperty(CommonsConstants.PROP_CATEGORIES,
                BeeUtils.toString(category.getId()));
          }
        }
      }
    }
  }

  private static class ItemGridHandler extends AbstractGridCallback
      implements SelectionHandler<IsRow> {
    
    private static final String FILTER_KEY = "f1";
    private final boolean services;
    private IsRow selectedCategory = null;

    private ItemGridHandler(boolean showServices) {
      this.services = showServices;
    }

    @Override
    public String getCaption() {
      if (showServices()) {
        return "Paslaugos";
      } else {
        return "Prekės";
      }
    }

    @Override
    public String getRowCaption(IsRow row, boolean edit) {
      return (edit ? "" : "Nauja ") + (showServices() ? "Paslauga" : "Prekė");
    }

    @Override
    public boolean onLoad(GridDescription gridDescription) {
      gridDescription.setCaption(null);

      Filter filter = Filter.isEmpty(CommonsConstants.COL_SERVICE);

      if (showServices()) {
        filter = Filter.isNot(filter);
      }
      gridDescription.setFilter(filter);
      return true;
    }
    
    @Override
    public boolean onSaveChanges(GridView gridView, SaveChangesEvent event) {
      String oldCateg = event.getOldRow().getProperty(CommonsConstants.PROP_CATEGORIES);
      String newCateg = event.getNewRow().getProperty(CommonsConstants.PROP_CATEGORIES);
      
      if (!BeeUtils.same(oldCateg, newCateg)) {
        Set<Long> oldValues = DataUtils.parseIdSet(oldCateg);
        Set<Long> newValues = DataUtils.parseIdSet(newCateg);
        
        Set<Long> insert = Sets.newHashSet(newValues);
        insert.removeAll(oldValues);

        Set<Long> delete = Sets.newHashSet(oldValues);
        delete.removeAll(newValues);
        
        long itemId = event.getRowId();
        
        if (!delete.isEmpty()) {
          ParameterList args = createArgs(CommonsConstants.SVC_REMOVE_CATEGORIES);
          args.addDataItem(CommonsConstants.VAR_ITEM_ID, itemId);
          args.addDataItem(CommonsConstants.VAR_ITEM_CATEGORIES, DataUtils.buildIdList(delete));

          BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
            @Override
            public void onResponse(ResponseObject response) {
              logger.debug(CommonsConstants.SVC_REMOVE_CATEGORIES, response.getResponse());
            }
          });
        }

        if (!insert.isEmpty()) {
          ParameterList args = createArgs(CommonsConstants.SVC_ADD_CATEGORIES);
          args.addDataItem(CommonsConstants.VAR_ITEM_ID, itemId);
          args.addDataItem(CommonsConstants.VAR_ITEM_CATEGORIES, DataUtils.buildIdList(insert));

          BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
            @Override
            public void onResponse(ResponseObject response) {
              logger.debug(CommonsConstants.SVC_ADD_CATEGORIES, response.getResponse());
            }
          });
        }
      }
      
      return super.onSaveChanges(gridView, event);
    }

    @Override
    public void onSelection(SelectionEvent<IsRow> event) {
      if (event == null) {
        return;
      }
      if (getGridPresenter() != null) {
        Long category = null;
        setSelectedCategory(event.getSelectedItem());

        if (getSelectedCategory() != null) {
          category = getSelectedCategory().getId();
        }
        getGridPresenter().getDataProvider().setParentFilter(FILTER_KEY, getFilter(category));
        getGridPresenter().refresh(true);
      }
    }

    @Override
    public void onShow(GridPresenter presenter) {
      setGridPresenter(presenter);
    }

    public boolean showServices() {
      return services;
    }

    private Filter getFilter(Long category) {
      if (category == null) {
        return null;
      } else {
        return ComparisonFilter.isEqual(CommonsConstants.COL_CATEGORY, new LongValue(category));
      }
    }

    private IsRow getSelectedCategory() {
      return selectedCategory;
    }

    private void setSelectedCategory(IsRow selectedCategory) {
      this.selectedCategory = selectedCategory;
    }
  }

  public static void register() {
    FormFactory.registerFormCallback("Item", new ItemFormHandler());

    BeeKeeper.getMenu().registerMenuCallback("items", new MenuManager.MenuCallback() {
      @Override
      public void onSelection(String parameters) {
        GridFactory.openGrid("Items", new ItemGridHandler(BeeUtils.startsSame(parameters, "s")));
      }
    });

    FormFactory.registerFormCallback("Parameter", new ParameterFormHandler());

    BeeKeeper.getMenu().registerMenuCallback("system_parameters", new MenuManager.MenuCallback() {
      @Override
      public void onSelection(String parameters) {
        GridFactory.openGrid("Parameters", new ParametersHandler(parameters));
      }
    });

    SelectorEvent.register(new CommonsSelectorHandler());

    Global.registerCaptions(RightsObjectType.class);
    Global.registerCaptions(RightsState.class);
    Global.registerCaptions(ParameterType.class);
  }

  static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(CommonsConstants.COMMONS_MODULE);
    args.addQueryItem(CommonsConstants.COMMONS_METHOD, name);
    return args;
  }

  private CommonsEventHandler() {
  }
}
