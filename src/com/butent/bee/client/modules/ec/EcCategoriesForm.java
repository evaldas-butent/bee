package com.butent.bee.client.modules.ec;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.event.logical.CatchEvent;
import com.butent.bee.client.event.logical.CatchEvent.CatchHandler;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.presenter.TreePresenter;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.TreeContainer;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

public class EcCategoriesForm extends AbstractFormInterceptor implements CatchHandler<IsRow> {

  private TreeContainer treeView;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof TreeContainer) {
      treeView = (TreeContainer) widget;
      treeView.addCatchHandler(this);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new EcCategoriesForm();
  }

  @Override
  public void onCatch(final CatchEvent<IsRow> event) {
    if (treeView == null) {
      return;
    }
    final TreePresenter presenter = treeView.getTreePresenter();
    final IsRow source = event.getPacket();
    final IsRow destination = event.getDestination();

    Global.confirm(Localized.getMessages().ecMergeCategory(presenter.evaluate(source),
        presenter.evaluate(destination)), new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        ParameterList args = EcKeeper.createArgs(SVC_MERGE_CATEGORY);
        args.addDataItem(COL_TCD_CATEGORY, source.getId());
        args.addDataItem(COL_TCD_CATEGORY_PARENT, destination.getId());

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            response.notify(getFormView());

            if (response.hasErrors()) {
              return;
            }
            treeView.removeItem(source);
            BeeKeeper.getBus()
                .fireEvent(new RowDeleteEvent(presenter.getViewName(), source.getId()));
          }
        });
      }
    });
  }

  @Override
  public void onShow(final Presenter presenter) {
    EcKeeper.getConfiguration(new Consumer<Map<String, String>>() {
      @Override
      public void accept(Map<String, String> input) {
        String value = input.get(EcConstants.COL_CONFIG_MARGIN_DEFAULT_PERCENT);

        String stylePrefix = EcStyles.name("Margins-defPercent-");

        presenter.getHeader().clearCommandPanel();

        Label label = new Label(Localized.getConstants().ecMarginDefaultPercent());
        label.addStyleName(stylePrefix + "label");
        presenter.getHeader().addCommandItem(label);

        InputNumber dmpInput = new InputNumber();
        dmpInput.addStyleName(stylePrefix + "input");

        if (!BeeUtils.isEmpty(value)) {
          dmpInput.setValue(value);
        }

        dmpInput.addValueChangeHandler(new ValueChangeHandler<String>() {
          @Override
          public void onValueChange(ValueChangeEvent<String> event) {
            EcKeeper.saveConfiguration(EcConstants.COL_CONFIG_MARGIN_DEFAULT_PERCENT,
                event.getValue());
          }
        });

        presenter.getHeader().addCommandItem(dmpInput);
      }
    });
  }
}
