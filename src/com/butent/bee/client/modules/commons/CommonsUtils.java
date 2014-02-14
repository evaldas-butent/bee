package com.butent.bee.client.modules.commons;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseMessage;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Map;

public final class CommonsUtils {

  private static final String STYLE_COMPANY = "bee-co-companyInfo";
  private static final String STYLE_COMPANY_ITEM = STYLE_COMPANY + "-item";
  private static final String STYLE_COMPANY_LABEL = STYLE_COMPANY + "-label";

  private static final String STYLE_UPDATE_RATES_PREFIX = "bee-co-updateRates-";

  public static void blockHost(String caption, final String host,
      final NotificationListener notificationListener, final Callback<String> callback) {

    if (BeeUtils.isEmpty(host)) {
      if (callback != null) {
        callback.onFailure("host not specified");
      }
      return;
    }

    Global.confirm(caption, Icon.WARNING, Lists.newArrayList(host),
        Localized.getConstants().actionBlock(), Localized.getConstants().actionCancel(),
        new ConfirmationCallback() {
          @Override
          public void onConfirm() {
            ParameterList args = CommonsKeeper.createArgs(SVC_BLOCK_HOST);
            args.addDataItem(COL_IP_FILTER_HOST, host);

            BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
              @Override
              public void onResponse(ResponseObject response) {
                if (response.hasResponse()) {
                  DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_IP_FILTERS);
                }

                if (notificationListener != null) {
                  response.notify(notificationListener);
                }

                if (response.is(host)) {
                  if (notificationListener != null) {
                    notificationListener.notifyInfo(Localized.getConstants().ipBlocked(), host);
                  }
                  if (callback != null) {
                    callback.onSuccess(host);
                  }
                }
              }
            });
          }
        });
  }

  public static void createCompany(final Map<String, String> parameters,
      final NotificationListener notificationListener, final IdCallback callback) {

    Assert.notEmpty(parameters);

    ParameterList args = CommonsKeeper.createArgs(SVC_CREATE_COMPANY);
    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      if (!BeeUtils.anyEmpty(entry.getKey(), entry.getValue())) {
        args.addDataItem(entry.getKey(), entry.getValue());
      }
    }

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (notificationListener != null) {
          response.notify(notificationListener);
        }

        if (response.hasResponse(Long.class)) {
          DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_COMPANIES);
          if (callback != null) {
            callback.onSuccess(response.getResponseAsLong());
          }
        }
      }
    });
  }

  public static void createUser(String caption, final String login, final String password,
      final UserInterface userInterface, final Map<String, String> parameters,
      final NotificationListener notificationListener, final IdCallback callback) {

    if (BeeUtils.isEmpty(login)) {
      if (callback != null) {
        callback.onFailure("login not specified");
      }
      return;
    }

    final String pswd = BeeUtils.notEmpty(password, login.trim().substring(0, 1));

    String separator = BeeConst.STRING_COLON + BeeConst.STRING_SPACE;
    final String msgLogin = Localized.getConstants().userLogin() + separator + login.trim();
    final String msgPswd = Localized.getConstants().password() + separator + pswd.trim();

    Global.confirm(caption, Icon.QUESTION, Lists.newArrayList(msgLogin, msgPswd),
        Localized.getConstants().actionCreate(), Localized.getConstants().actionCancel(),
        new ConfirmationCallback() {
          @Override
          public void onConfirm() {
            ParameterList args = CommonsKeeper.createArgs(SVC_CREATE_USER);
            args.addDataItem(COL_LOGIN, login);
            args.addDataItem(COL_PASSWORD, Codec.encodePassword(pswd));

            if (userInterface != null) {
              args.addDataItem(COL_USER_INTERFACE, userInterface.ordinal());
            }
            if (!BeeUtils.isEmpty(parameters)) {
              for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (!BeeUtils.anyEmpty(entry.getKey(), entry.getValue())) {
                  args.addDataItem(entry.getKey(), entry.getValue());
                }
              }
            }

            BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
              @Override
              public void onResponse(ResponseObject response) {
                if (notificationListener != null) {
                  response.notify(notificationListener);
                }

                if (response.hasResponse(Long.class)) {
                  DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_USERS);

                  if (notificationListener != null) {
                    notificationListener.notifyInfo(Localized.getConstants().newUser(), msgLogin,
                        msgPswd);
                  }
                  if (callback != null) {
                    callback.onSuccess(response.getResponseAsLong());
                  }
                }
              }
            });
          }
        });
  }

  public static void getCompanyInfo(Long companyId, final Widget target) {
    Assert.notNull(target);

    if (!DataUtils.isId(companyId)) {
      return;
    }
    ParameterList args = CommonsKeeper.createArgs(SVC_COMPANY_INFO);
    args.addDataItem(COL_COMPANY, companyId);

    String locale = DomUtils.getDataProperty(target.getElement(), "locale");

    if (!BeeUtils.isEmpty(locale)) {
      args.addDataItem("locale", locale);
    }
    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(BeeKeeper.getScreen());
        if (response.hasErrors()) {
          return;
        }

        Map<String, Pair<String, String>> info = Maps.newHashMap();
        for (Map.Entry<String, String> entry
        : Codec.beeDeserializeMap(response.getResponseAsString()).entrySet()) {
          info.put(entry.getKey(), Pair.restore(entry.getValue()));
        }
        Flow flow = new Flow();
        flow.setStyleName(STYLE_COMPANY);

        for (String col : new String[] {COL_COMPANY_NAME, COL_COMPANY_CODE, COL_COMPANY_VAT_CODE,
            COL_ADDRESS, COL_PHONE, COL_EMAIL_ADDRESS}) {

          Flow record = new Flow();
          record.setStyleName(STYLE_COMPANY + "-" + col.toLowerCase());

          if (BeeUtils.same(col, COL_ADDRESS)) {
            String value = BeeUtils.joinItems(info.get(COL_ADDRESS).getB(),
                info.get(COL_CITY).getB(), info.get(COL_COUNTRY).getB(),
                info.get(COL_POST_INDEX).getB());

            if (!BeeUtils.isEmpty(value)) {
              Widget widget = new Label(info.get(col).getA());
              widget.setStyleName(STYLE_COMPANY_LABEL);
              record.add(widget);
              widget = new Label(value);
              widget.setStyleName(STYLE_COMPANY_ITEM);
              record.add(widget);
            }
          } else if (BeeUtils.same(col, COL_PHONE)) {
            Flow phone = new Flow();

            for (String fld : new String[] {COL_PHONE, COL_MOBILE, COL_FAX}) {
              Pair<String, String> pair = info.get(fld);

              if (!BeeUtils.isEmpty(pair.getB())) {
                Widget label = new Label(pair.getA());
                label.setStyleName(STYLE_COMPANY_LABEL);
                phone.add(label);

                Widget item = new Label(pair.getB());
                item.setStyleName(STYLE_COMPANY_ITEM);
                phone.add(item);
              }
            }
            if (!phone.isEmpty()) {
              record.add(phone);
            }
          } else {
            Pair<String, String> pair = info.get(col);

            if (!BeeUtils.isEmpty(pair.getB())) {
              Widget widget = new Label(pair.getA());
              widget.setStyleName(STYLE_COMPANY_LABEL);
              record.add(widget);
              widget = new Label(pair.getB());
              widget.setStyleName(STYLE_COMPANY_ITEM);
              record.add(widget);
            }
          }
          if (!record.isEmpty()) {
            flow.add(record);
          }
        }
        target.getElement().setInnerHTML(flow.getElement().getString());
      }
    });
  }

  public static void updateExchangeRates() {
    Flow panel = new Flow(STYLE_UPDATE_RATES_PREFIX + "panel");

    Label lowLabel = new Label(Localized.getConstants().updateExchangeRatesDateLow());
    lowLabel.addStyleName(STYLE_UPDATE_RATES_PREFIX + "lowLabel");
    panel.add(lowLabel);

    final InputDate lowInput = new InputDate();
    lowInput.addStyleName(STYLE_UPDATE_RATES_PREFIX + "lowInput");
    lowInput.setDate(TimeUtils.today());
    lowInput.setNullable(false);
    panel.add(lowInput);

    CustomDiv rangeSeparator = new CustomDiv(STYLE_UPDATE_RATES_PREFIX + "rangeSeparator");
    panel.add(rangeSeparator);

    Label highLabel = new Label(Localized.getConstants().updateExchangeRatesDateHigh());
    highLabel.addStyleName(STYLE_UPDATE_RATES_PREFIX + "highLabel");
    panel.add(highLabel);

    final InputDate highInput = new InputDate();
    highInput.addStyleName(STYLE_UPDATE_RATES_PREFIX + "highInput");
    highInput.setDate(TimeUtils.today());
    highInput.setNullable(false);
    panel.add(highInput);

    final Flow output = new Flow(STYLE_UPDATE_RATES_PREFIX + "output");
    panel.add(output);

    CustomDiv actionSeparator = new CustomDiv(STYLE_UPDATE_RATES_PREFIX + "actionSeparator");
    panel.add(actionSeparator);

    final Button submit = new Button(Localized.getConstants().actionUpdate());
    submit.addStyleName(STYLE_UPDATE_RATES_PREFIX + "submit");
    panel.add(submit);

    Button cancel = new Button(Localized.getConstants().actionCancel());
    cancel.addStyleName(STYLE_UPDATE_RATES_PREFIX + "cancel");
    panel.add(cancel);

    String caption = Localized.getConstants().updateExchangeRatesDialogCaption();
    final DialogBox dialog = DialogBox.create(caption, STYLE_UPDATE_RATES_PREFIX + "dialog");
    dialog.setWidget(panel);

    dialog.setAnimationEnabled(true);
    dialog.setHideOnEscape(true);

    dialog.center();

    submit.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        JustDate lowDate = lowInput.getDate();
        if (lowDate == null) {
          BeeKeeper.getScreen().notifyWarning(Localized.getConstants().valueRequired());
          lowInput.setFocus(true);
          return;
        }

        JustDate hightDate = highInput.getDate();
        if (hightDate == null) {
          BeeKeeper.getScreen().notifyWarning(Localized.getConstants().valueRequired());
          highInput.setFocus(true);
          return;
        }

        if (TimeUtils.isMore(lowDate, hightDate)) {
          BeeKeeper.getScreen().notifyWarning(Localized.getConstants().invalidRange(),
              BeeUtils.joinWords(lowDate, hightDate));
          return;
        }

        submit.setEnabled(false);

        output.clear();
        output.add(new Image(Global.getImages().loading()));

        ParameterList params = CommonsKeeper.createArgs(SVC_UPDATE_EXCHANGE_RATES);
        params.addQueryItem(VAR_DATE_LOW, lowDate.getDays());
        params.addQueryItem(VAR_DATE_HIGH, hightDate.getDays());

        BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            output.clear();

            if (response.hasMessages()) {
              for (ResponseMessage rm : response.getMessages()) {
                Label label = new Label(rm.getMessage());
                String styleSuffix = (rm.getLevel() == null)
                    ? "message" : rm.getLevel().name().toLowerCase();
                label.addStyleName(STYLE_UPDATE_RATES_PREFIX + styleSuffix);

                output.add(label);
              }
            }

            DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_CURRENCY_RATES);

            submit.setEnabled(true);
          }
        });
      }
    });

    cancel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.close();
      }
    });
  }

  private CommonsUtils() {
  }
}
