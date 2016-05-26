package com.butent.bee.client.view.form.interceptor;

import com.google.common.collect.Lists;

import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.output.ReportUtils;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BiConsumer;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PrintFormInterceptor extends AbstractFormInterceptor {

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action == Action.PRINT) {
      if (getFormView().isAdding()) {
        return saveOnPrintNewRow() || DataUtils.hasId(getActiveRow());
      }
      if (DataUtils.isNewRow(getActiveRow())) {
        return false;
      }

      if (printJasperReport()) {
        return false;
      }

      String[] reports = getReportsOld();

      if (!ArrayUtils.isEmpty(reports)) {
        final List<FormDescription> forms = Lists.newArrayListWithCapacity(reports.length);
        for (int i = 0; i < reports.length; i++) {
          forms.add(null);
        }
        final ChoiceCallback choice = value -> {
          FormDescription form = forms.get(value);
          String viewName = form.getViewName();
          IsRow row = getFormView().getActiveRow();

          if (BeeUtils.isEmpty(viewName)
              || BeeUtils.same(viewName, getFormView().getViewName())) {

            RowEditor.openForm(form.getName(), Data.getDataInfo(getFormView().getViewName()),
                row, Opener.MODAL, null, getPrintFormInterceptor());
          } else {
            RowEditor.openForm(form.getName(), Data.getDataInfo(viewName), row.getId(),
                Opener.MODAL, null, getPrintFormInterceptor());
          }
        };
        final Holder<Integer> counter = Holder.of(0);

        for (int i = 0; i < reports.length; i++) {
          final int idx = i;
          FormFactory.getFormDescription(reports[i], new Callback<FormDescription>() {
            @Override
            public void onFailure(String... reason) {
              Callback.super.onFailure(reason);
              process();
            }

            @Override
            public void onSuccess(FormDescription formDescription) {
              forms.set(idx, formDescription);
              process();
            }

            private void process() {
              counter.set(counter.get() + 1);

              if (counter.get() == forms.size()) {
                List<String> captions = new ArrayList<>();
                List<FormDescription> descriptions = new ArrayList<>();

                for (FormDescription dscr : forms) {
                  if (dscr != null) {
                    captions.add(BeeUtils.notEmpty(Localized.maybeTranslate(dscr.getCaption()),
                        dscr.getName()));
                    descriptions.add(dscr);
                  }
                }
                BeeUtils.overwrite(forms, descriptions);

                if (captions.size() > 1) {
                  Global.choice(null,
                      Localized.dictionary().choosePrintingForm(), captions, choice);

                } else if (captions.size() == 1) {
                  choice.onSuccess(0);
                }
              }
            }
          });
        }
        return false;
      }
    }
    return super.beforeAction(action, presenter);
  }

  public FormInterceptor getPrintFormInterceptor() {
    return null;
  }

  protected Consumer<FileInfo> getReportCallback() {
    return null;
  }

  protected void getReportData(Consumer<BeeRowSet[]> dataConsumer) {
    dataConsumer.accept(null);
  }

  protected void getReportParameters(Consumer<Map<String, String>> parametersConsumer) {
    Map<String, String> params = new HashMap<>();

    for (BeeColumn column : getFormView().getDataColumns()) {
      String value = getStringValue(column.getId());

      if (!BeeUtils.isEmpty(value)) {
        params.put(column.getId(), value);
      }
    }
    parametersConsumer.accept(params);
  }

  protected String[] getReports() {
    return BeeUtils.split(getFormView().getProperty("jasperReports"), BeeConst.CHAR_COMMA);
  }

  protected String[] getReportsOld() {
    return BeeUtils.split(getFormView().getProperty("reports"), BeeConst.CHAR_COMMA);
  }

  protected void print(BiConsumer<Map<String, String>, BeeRowSet[]> consumer) {
    getReportParameters(parameters -> getReportData(data -> consumer.accept(parameters, data)));
  }

  private boolean printJasperReport() {
    String[] reports = getReports();

    if (ArrayUtils.isEmpty(reports)) {
      return false;
    }
    List<String> reps = new ArrayList<>();
    List<String> caps = new ArrayList<>();

    for (String report : reports) {
      String[] arr = BeeUtils.split(report, BeeConst.CHAR_COLON);
      String rep = arr[0];
      String cap = ArrayUtils.getQuietly(arr, 1);

      if (BeeUtils.isEmpty(cap)) {
        cap = BeeUtils.notEmpty(Localized.translate("report" + rep), rep);
      } else {
        cap = Localized.maybeTranslate(cap);
      }
      reps.add(rep);
      caps.add(cap);
    }
    Consumer<String> consumer = report -> print((parameters, data) ->
        ReportUtils.showReport(report, getReportCallback(), parameters, data));

    if (reps.size() > 1) {
      Global.choice(null, Localized.dictionary().choosePrintingForm(), caps,
          idx -> consumer.accept(reps.get(idx)));
    } else {
      consumer.accept(reps.get(0));
    }
    return true;
  }
}
