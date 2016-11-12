package com.butent.bee.client.modules.cars;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.cars.CarsConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.ParentRowCreator;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.modules.mail.NewMailMessage;
import com.butent.bee.client.output.ReportUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.client.view.form.interceptor.StageFormInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfoList;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.modules.cars.Option;
import com.butent.bee.shared.modules.cars.Specification;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class CarOrderForm extends PrintFormInterceptor implements StageFormInterceptor,
    Consumer<Specification> {

  private Flow stageContainer = new Flow();
  private List<Stage> orderStages;

  private Flow objectContainer;
  private Specification objectSpecification;

  @Override
  public void accept(Specification specification) {
    Map<Long, Integer> opts = new HashMap<>();

    for (Option option : specification.getOptions()) {
      opts.put(option.getId(), specification.getOptionPrice(option));
    }
    Queries.getRowSet(TBL_CONF_OPTIONS, Collections.singletonList(COL_ITEM),
        Filter.and(opts.isEmpty() ? Filter.isFalse() : Filter.idIn(opts.keySet()),
            Filter.notNull(COL_ITEM)), new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet res) {
            Holder<Integer> price = Holder.of(specification.getPrice());
            List<BeeColumn> columns = Data.getColumns(TBL_CAR_ORDER_ITEMS,
                Arrays.asList(COL_ORDER, COL_ITEM, COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE,
                    COL_OBJECT));
            BeeRowSet items = new BeeRowSet(TBL_CAR_ORDER_ITEMS, columns);

            for (BeeRow item : res) {
              BeeRow row = items.addEmptyRow();
              row.setValue(DataUtils.getColumnIndex(COL_ORDER, columns), getActiveRowId());
              row.setValue(DataUtils.getColumnIndex(COL_ITEM, columns), item.getLong(0));
              row.setValue(DataUtils.getColumnIndex(COL_TRADE_ITEM_QUANTITY,
                  columns), 1);
              row.setValue(DataUtils.getColumnIndex(COL_TRADE_ITEM_PRICE, columns),
                  opts.get(item.getId()));
              row.setValue(DataUtils.getColumnIndex(COL_OBJECT, columns),
                  specification.getId());
              price.set(price.get() - opts.get(item.getId()));
            }
            Consumer<Integer> consumer = itemCount -> {
              BeeRowSet rs = DataUtils.getUpdated(getViewName(), getFormView().getDataColumns(),
                  getFormView().getOldRow(), getActiveRow(), getFormView().getChildrenForUpdate());

              if (DataUtils.isEmpty(rs)) {
                rs = new BeeRowSet(getViewName(), new ArrayList<>());
                rs.addRow(getActiveRowId(), getActiveRow().getVersion(), new ArrayList<>());
              } else {
                for (String col : new String[] {
                    COL_DESCRIPTION, COL_TRADE_AMOUNT}) {
                  int idx = DataUtils.getColumnIndex(col, rs.getColumns());

                  if (!BeeConst.isUndef(idx)) {
                    rs.removeColumn(idx);
                  }
                }
              }
              Long oldObject = getLongValue(COL_OBJECT);
              int c = rs.getNumberOfColumns();
              rs.addColumns(Data.getColumns(getViewName(),
                  Arrays.asList(COL_OBJECT, COL_TRADE_AMOUNT)));

              rs.getRow(0).setValue(c, oldObject);
              rs.getRow(0).preliminaryUpdate(c, BeeUtils.toString(specification.getId()));
              rs.getRow(0).setValue(c + 1, getIntegerValue(COL_TRADE_AMOUNT));
              rs.getRow(0).preliminaryUpdate(c + 1, BeeUtils.toString(price.get()));

              Queries.updateRow(rs, new RowUpdateCallback(getViewName()) {
                @Override
                public void onSuccess(BeeRow result) {
                  if (DataUtils.isId(oldObject)) {
                    Queries.deleteRow(TBL_CONF_OBJECTS, oldObject,
                        new Queries.IntCallback() {
                          @Override
                          public void onSuccess(Integer cnt) {
                            Data.onViewChange(TBL_CAR_ORDER_ITEMS, DataChangeEvent.RESET_REFRESH);
                          }
                        });
                  } else if (BeeUtils.isPositive(itemCount)) {
                    Data.onViewChange(TBL_CAR_ORDER_ITEMS, DataChangeEvent.RESET_REFRESH);
                  }
                  super.onSuccess(result);
                }
              });
            };
            if (!DataUtils.isEmpty(items)) {
              Queries.insertRows(items, new RpcCallback<RowInfoList>() {
                @Override
                public void onSuccess(RowInfoList result) {
                  consumer.accept(result.size());
                }
              });
            } else {
              consumer.accept(0);
            }
          }
        });
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, NameUtils.getClassName(SpecificationBuilder.class))
        && widget instanceof HasClickHandlers) {
      ((HasClickHandlers) widget).addClickHandler(clickEvent -> createObject());

    } else if (BeeUtils.same(name, COL_OBJECT) && widget instanceof Flow) {
      objectContainer = (Flow) widget;
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    StageFormInterceptor.super.beforeRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new CarOrderForm();
  }

  @Override
  public Flow getStageContainer() {
    return stageContainer;
  }

  @Override
  public List<Stage> getStages() {
    return orderStages;
  }

  @Override
  public void onLoad(FormView form) {
    getHeaderView().addCommandItem(stageContainer);
    super.onLoad(form);
  }

  @Override
  public void onSetActiveRow(IsRow row) {
    Long objectId = row.getLong(getDataIndex(COL_OBJECT));

    if (DataUtils.isId(objectId)) {
      ParameterList args = CarsKeeper.createSvcArgs(SVC_GET_OBJECT);
      args.addDataItem(COL_OBJECT, objectId);

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          response.notify(getFormView());

          if (!response.hasErrors()) {
            refreshObject(Specification.restore(response.getResponseAsString()));
          }
        }
      });
    } else {
      refreshObject(null);
    }
    super.onSetActiveRow(row);
  }

  @Override
  public void refreshStages() {
    StageFormInterceptor.super.refreshStages();
    Stage stage = StageUtils.findStage(getStages(), getLongValue(COL_STAGE));
    getFormView().setEnabled(Objects.isNull(stage) || !stage.hasAction(STAGE_ACTION_READONLY));

    if (DataUtils.isNewRow(getActiveRow()) && Objects.isNull(stage)) {
      triggerStage(STAGE_TRIGGER_NEW);
    }
  }

  @Override
  public void setStages(List<Stage> stages) {
    orderStages = stages;
  }

  @Override
  public void updateStage(Stage stage) {
    Runnable doDefault = () -> StageFormInterceptor.super.updateStage(stage);

    if (stage.hasAction(STAGE_ACTION_LOST)) {
      InputArea comment = new InputArea();
      comment.setWidth("100%");
      comment.setVisibleLines(4);

      Relation relation = Relation.create(TBL_LOSS_REASONS,
          Collections.singletonList(COL_LOSS_REASON_NAME));

      relation.disableNewRow();
      relation.disableEdit();
      UnboundSelector reason = UnboundSelector.create(relation);

      reason.addSelectorHandler(event -> {
        if (event.isChanged()) {
          comment.setValue(event.getRelatedRow() != null
              ? Data.getString(event.getRelatedViewName(), event.getRelatedRow(),
              COL_LOSS_REASON_TEMPLATE) : null);
        }
      });
      HtmlTable layout = new HtmlTable();
      layout.setText(0, 0, Localized.dictionary().reason());
      layout.setWidget(0, 1, reason);
      layout.getCellFormatter().setColSpan(1, 0, 2);
      layout.setText(1, 0, Localized.dictionary().comment());
      layout.getCellFormatter().setColSpan(2, 0, 2);
      layout.setWidget(2, 0, comment);

      Global.inputWidget(stage.getName(), layout, new InputCallback() {
        @Override
        public String getErrorMessage() {
          if (!DataUtils.isId(reason.getRelatedId())) {
            reason.setFocus(true);
            return Localized.dictionary().valueRequired();
          }
          return null;
        }

        @Override
        public void onSuccess() {
          IsRow row = getActiveRow();
          row.setValue(getFormView().getDataIndex(COL_LOSS_REASON), reason.getRelatedId());
          row.setValue(getFormView().getDataIndex(COL_LOSS_REASON_NAME), reason.getDisplayValue());
          row.setValue(getFormView().getDataIndex(COL_LOSS_NOTES), comment.getValue());
          doDefault.run();
        }
      });
    } else {
      doDefault.run();
    }
  }

  @Override
  protected ReportUtils.ReportCallback getReportCallback() {
    return new ReportUtils.ReportCallback() {
      @Override
      public void accept(FileInfo fileInfo) {
        NewMailMessage.create(BeeUtils.notEmpty(getStringValue("ContactEmail"),
            getStringValue("CustomerEmail")), BeeUtils.joinWords(Localized.dictionary().offer(),
            getStringValue("OrderNo")), null, Collections.singleton(fileInfo),
            (messageId, isSaved) -> triggerStage(STAGE_TRIGGER_SENT));
      }

      @Override
      public Widget getActionWidget() {
        FaLabel action = new FaLabel(FontAwesome.ENVELOPE_O);
        action.setTitle(Localized.dictionary().trWriteEmail());
        return action;
      }
    };
  }

  @Override
  protected void getReportData(Consumer<BeeRowSet[]> dataConsumer) {
    Queries.getRowSet(TBL_CAR_ORDER_ITEMS, null, Filter.equals(COL_ORDER, getActiveRowId()),
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            dataConsumer.accept(new BeeRowSet[] {result});
          }
        });
  }

  @Override
  protected void getReportParameters(Consumer<Map<String, String>> parametersConsumer) {
    Map<String, Long> companies = new HashMap<>();
    companies.put(COL_TRADE_CUSTOMER, getLongValue(COL_TRADE_CUSTOMER));
    companies.put(COL_TRADE_SUPPLIER, BeeKeeper.getUser().getCompany());

    super.getReportParameters(defaultParameters ->
        ClassifierUtils.getCompaniesInfo(companies, companiesInfo -> {
          defaultParameters.putAll(companiesInfo);

          if (objectSpecification != null) {
            defaultParameters.put(TBL_CONF_OBJECT_OPTIONS,
                objectSpecification.renderSummary(false).toString());

            for (int i = 0; i < objectSpecification.getPhotos().size(); i++) {
              Long photo = objectSpecification.getPhotos().get(i);

              if (DataUtils.isId(photo)) {
                defaultParameters.put(COL_PHOTO + i, BeeUtils.toString(photo));
              }
            }
          }
          parametersConsumer.accept(defaultParameters);
        }));
  }

  private void createObject() {
    if (!getFormView().isEnabled()) {
      return;
    }
    if (DataUtils.isId(getActiveRowId())) {
      new SpecificationBuilder(objectSpecification, this);
    } else {
      FormView form = getFormView();

      if (form.getViewPresenter() instanceof ParentRowCreator) {
        ((ParentRowCreator) form.getViewPresenter()).createParentRow(form, result ->
            new SpecificationBuilder(null, CarOrderForm.this));
      }
    }
  }

  private void refreshObject(Specification specification) {
    objectSpecification = specification;

    if (objectContainer != null) {
      objectContainer.clear();

      if (objectSpecification != null) {
        objectContainer.add(objectSpecification.renderSummary(false));

        objectSpecification.getPhotos().stream().filter(DataUtils::isId).forEach(photo -> {
          Flow thumbnail = new Flow(SpecificationBuilder.STYLE_THUMBNAIL);
          thumbnail.addStyleName(StyleUtils.NAME_FLEXIBLE);
          thumbnail.add(new Image(FileUtils.getUrl(photo)));
          objectContainer.insert(thumbnail, objectContainer.getWidgetCount() - 1);
        });
      }
    }
  }
}
