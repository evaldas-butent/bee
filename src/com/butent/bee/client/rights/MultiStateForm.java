package com.butent.bee.client.rights;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

abstract class MultiStateForm extends RightsForm {

  private static final String STYLE_STATE_LABEL = STYLE_PREFIX + "state-label";
  private static final String STYLE_STATE_LABEL_CELL = STYLE_STATE_LABEL + STYLE_SUFFIX_CELL;
  private static final String STYLE_STATE_TOGGLE = STYLE_PREFIX + "state-toggle";
  private static final String STYLE_STATE_TOGGLE_CELL = STYLE_STATE_TOGGLE + STYLE_SUFFIX_CELL;

  private static final String STYLE_ROLE_COMMAND = STYLE_PREFIX + "role-command";
  private static final String STYLE_ROLE_SELECTOR = STYLE_PREFIX + "role-selector";
  private static final String STYLE_ROLE_EMPTY = STYLE_PREFIX + "role-empty";
  private static final String STYLE_ROLE_NOT_EMPTY = STYLE_PREFIX + "role-not-empty";

  private static final String DATA_KEY_STATE = "rights-state";

  private static final String DATA_TYPE_STATE_LABEL = "sl";
  private static final String DATA_TYPE_STATE_TOGGLE = "st";

  private static RightsState getRightsState(Widget widget) {
    Integer value = DomUtils.getDataPropertyInt(widget.getElement(), DATA_KEY_STATE);
    RightsState rs = EnumUtils.getEnumByIndex(RightsState.class, value);
    if (rs == null) {
      severe("Widget", DomUtils.getId(widget), "has no rights state");
    }
    return rs;
  }

  private final Multimap<String, RightsState> initialValues = HashMultimap.create();
  private final Multimap<String, RightsState> changes = HashMultimap.create();

  private Long roleId;
  private String roleName;

  @Override
  public void onShow(Presenter presenter) {
    HeaderView header = presenter.getHeader();

    if (header != null && !header.hasCommands()) {
      List<String> columns = Lists.newArrayList(COL_ROLE_NAME);
      Relation relation = Relation.create(VIEW_ROLES, columns);

      relation.disableEdit();
      relation.disableNewRow();

      final DataSelector roleSelector = new DataSelector(relation, true);
      roleSelector.setEditing(true);
      roleSelector.addStyleName(STYLE_ROLE_SELECTOR);

      final Button roleCommand = new Button();
      roleCommand.addStyleName(STYLE_ROLE_COMMAND);

      if (DataUtils.isId(getRoleId())) {
        roleCommand.setHtml(getRoleName());
        roleCommand.addStyleName(STYLE_ROLE_NOT_EMPTY);
      } else {
        roleCommand.setHtml(Localized.getConstants().selectRole());
        roleCommand.addStyleName(STYLE_ROLE_EMPTY);
      }

      roleSelector.addSelectorHandler(new SelectorEvent.Handler() {
        @Override
        public void onDataSelector(SelectorEvent event) {
          if (event.isChanged() && event.getRelatedRow() != null) {
            long id = event.getValue();
            String name = Data.getString(VIEW_ROLES, event.getRelatedRow(), COL_ROLE_NAME);

            Long oldId = getRoleId();

            if (DataUtils.isId(id) && !BeeUtils.isEmpty(name) && !Objects.equals(id, oldId)) {
              setRoleId(id);
              setRoleName(name);

              roleCommand.setHtml(name);

              if (DataUtils.isId(oldId)) {
                getRoleData(new Consumer<Boolean>() {
                  @Override
                  public void accept(Boolean input) {
                    if (BeeUtils.isTrue(input)) {
                      createUi();
                    }
                  }
                });

              } else {
                roleCommand.removeStyleName(STYLE_ROLE_EMPTY);
                roleCommand.addStyleName(STYLE_ROLE_NOT_EMPTY);

                init();
              }
            }
          }
        }
      });

      final Runnable activateSelector = new Runnable() {
        @Override
        public void run() {
          roleSelector.clearValue();
          roleSelector.setFocus(true);

          roleSelector.startEdit(null, DataSelector.SHOW_SELECTOR, null, null);
        }
      };

      roleCommand.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

          if (DataUtils.isId(getRoleId()) && !changes.isEmpty()) {
            onClose(activateSelector);
          } else {
            activateSelector.run();
          }
        }
      });

      header.addCommandItem(roleCommand);
      header.addCommandItem(roleSelector);
    }

    if (DataUtils.isId(getRoleId())) {
      init();
    }
  }

  @Override
  protected String getChangeMessage(RightsObject object) {
    Collection<RightsState> changedStates = changes.get(object.getName());

    List<String> stateCaptions = Lists.newArrayList();
    for (RightsState state : getRightsStates()) {
      if (changedStates.contains(state)) {
        stateCaptions.add(state.getCaption());
      }
    }

    return BeeUtils.joinWords(object.getCaption(), stateCaptions);
  }

  @Override
  protected Multimap<String, ?> getChanges() {
    return changes;
  }

  @Override
  protected String getDialogCaption() {
    return BeeUtils.joinWords(getFormView().getCaption(), getRoleName());
  }

  @Override
  protected String getPanelStyleName() {
    return STYLE_PREFIX + "multi-state";
  }

  protected abstract List<RightsState> getRightsStates();

  @Override
  protected void initData(final Consumer<Boolean> callback) {
    getRoleData(callback);
  }

  @Override
  protected boolean isObjectChecked(String objectName) {
    for (RightsState state : getRightsStates()) {
      if (!isChecked(objectName, state)) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected void onObjectToggle(boolean checked) {
    List<Toggle> stateToggles = getStateToggles();

    for (Toggle toggle : stateToggles) {
      if (checked) {
        toggle.setChecked(isStateChecked(getRightsState(toggle)));
      } else if (toggle.isChecked()) {
        toggle.setChecked(false);
      }
    }
  }

  @Override
  protected void populateTable() {
    int col = getValueStartCol();

    for (RightsState state : getRightsStates()) {
      addColumnLabel(col, createStateLabel(state), STYLE_STATE_LABEL_CELL);
      addColumnToggle(col, createStateToggle(state), STYLE_STATE_TOGGLE_CELL);
      col++;
    }

    int row = getValueStartRow();

    for (RightsObject object : getObjects()) {
      addObjectLabel(row, object);
      addObjectToggle(row, object);

      col = getValueStartCol();

      String objectName = object.getName();
      String objectCaption = object.getCaption();

      for (RightsState state : getRightsStates()) {
        String title = BeeUtils.joinWords(objectCaption, state.getCaption());
        addValueToggle(row, col++, createValueToggle(objectName, state, title));
      }

      afterCreateValueRow(row, object);
      row++;
    }

    for (RightsObject object : getObjects()) {
      if (object.hasChildren()) {
        for (RightsState state : getRightsStates()) {
          if (!isChecked(object.getName(), state)) {
            enableChildren(object, state, false);
          }
        }
      }
    }
  }

  @Override
  protected void save(final Consumer<Boolean> callback) {
    if (changes.isEmpty() || !DataUtils.isId(getRoleId())) {
      BeeKeeper.getScreen().notifyInfo("no changes");
      if (callback != null) {
        callback.accept(false);
      }

    } else {

      final Holder<Integer> latch = Holder.of(getRightsStates().size());

      Consumer<Boolean> stateCallback = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean input) {
          latch.set(latch.get() - 1);

          if (BeeUtils.isTrue(input)) {
            if (latch.get() <= 0) {
              changes.clear();
              onClearChanges();

              String message = BeeUtils.joinWords(getObjectType(), getRoleName(), "changes saved");
              debug(message);
              BeeKeeper.getScreen().notifyInfo(message);

              if (callback != null) {
                callback.accept(true);
              }
            }

          } else if (callback != null) {
            callback.accept(false);
          }
        }
      };

      String roleValue = getRoleId().toString();

      for (RightsState state : getRightsStates()) {
        if (changes.containsValue(state)) {
          Map<String, String> diff = Maps.newHashMap();
          for (Map.Entry<String, RightsState> entry : changes.entries()) {
            if (entry.getValue() == state) {
              diff.put(entry.getKey(), roleValue);
            }
          }

          saveDiff(state, diff, stateCallback);

        } else {
          stateCallback.accept(true);
        }
      }
    }
  }

  @Override
  protected void updateObjectValueToggles(String objectName, boolean checked) {
    for (Widget widget : getTable()) {
      if (widget instanceof Toggle && ((Toggle) widget).isChecked() != checked
          && isValueWidget(widget) && hasObject(widget, objectName)) {

        ((Toggle) widget).setChecked(checked);

        boolean isChanged = toggleValue(objectName, getRightsState(widget));
        updateValueCell(widget, isChanged);
      }
    }

    RightsObject object = findObject(objectName);
    if (object != null && object.hasChildren()) {
      for (RightsState state : getRightsStates()) {
        if (checked && object.hasParent()) {
          boolean value = checked;

          String parentName = object.getParent();
          while (!BeeUtils.isEmpty(parentName)) {
            if (!isChecked(parentName, state)) {
              value = false;
              break;
            }

            RightsObject parent = findObject(parentName);
            if (parent != null && parent.hasParent()) {
              parentName = parent.getParent();
            } else {
              break;
            }
          }

          enableChildren(object, state, value);

        } else {
          enableChildren(object, state, checked);
        }
      }
    }
  }

  private Widget createStateLabel(RightsState state) {
    Label widget = new Label(state.getCaption());
    widget.addStyleName(STYLE_STATE_LABEL);

    DomUtils.setDataProperty(widget.getElement(), DATA_KEY_STATE, state.ordinal());
    setDataType(widget, DATA_TYPE_STATE_LABEL);

    return widget;
  }

  private Widget createStateToggle(RightsState state) {
    Toggle toggle = createToggle(FontAwesome.SQUARE_O, FontAwesome.CHECK_SQUARE_O,
        STYLE_STATE_TOGGLE);

    boolean checked = true;
    for (RightsObject object : getObjects()) {
      if (!initialValues.containsEntry(object.getName(), state)) {
        checked = false;
        break;
      }
    }

    if (checked) {
      toggle.setChecked(true);
    }

    DomUtils.setDataProperty(toggle.getElement(), DATA_KEY_STATE, state.ordinal());
    setDataType(toggle, DATA_TYPE_STATE_TOGGLE);

    toggle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (event.getSource() instanceof Toggle) {
          Toggle rt = (Toggle) event.getSource();
          updateStateValueToggles(getRightsState(rt), rt.isChecked());

          List<Toggle> objectToggles = getObjectToggles();
          for (Toggle ot : objectToggles) {
            if (rt.isChecked()) {
              ot.setChecked(isObjectChecked(getObjectName(ot)));
            } else if (ot.isChecked()) {
              ot.setChecked(false);
            }
          }
        }
      }
    });

    return toggle;
  }

  private Widget createValueToggle(String objectName, RightsState state, String title) {
    Toggle toggle = createValueToggle(objectName);
    toggle.setTitle(title);

    if (initialValues.containsEntry(objectName, state)) {
      toggle.setChecked(true);
    }

    DomUtils.setDataProperty(toggle.getElement(), DATA_KEY_STATE, state.ordinal());

    toggle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (event.getSource() instanceof Toggle) {
          Toggle t = (Toggle) event.getSource();

          String name = getObjectName(t);
          RightsState rs = getRightsState(t);

          boolean isChanged = toggleValue(name, rs);
          updateValueCell(t, isChanged);

          Toggle objectToggle = getObjectToggle(name);
          Toggle stateToggle = getStateToggle(rs);

          if (t.isChecked()) {
            objectToggle.setChecked(isObjectChecked(name));
            stateToggle.setChecked(isStateChecked(rs));

          } else {
            objectToggle.setChecked(false);
            stateToggle.setChecked(false);
          }

          RightsObject object = findObject(name);
          if (object != null && object.hasChildren()) {
            enableChildren(object, rs, t.isChecked());
          }
        }
      }
    });
    return toggle;
  }

  private void enableChildren(RightsObject object, RightsState state, boolean enabled) {
    List<RightsObject> children = Lists.newArrayList();

    for (RightsObject ro : getObjects()) {
      if (object.getName().equals(ro.getParent())) {
        children.add(ro);
      }
    }

    if (children.isEmpty()) {
      warning("object", object.getName(), "children not found");

    } else {
      for (Widget widget : getTable()) {
        if (isValueWidget(widget) && getRightsState(widget) == state) {
          String childName = getObjectName(widget);

          for (RightsObject child : children) {
            if (child.getName().equals(childName)) {
              enableValueWidet(widget, enabled);

              if (child.hasChildren()) {
                boolean childEnabled = enabled
                    && widget instanceof Toggle && ((Toggle) widget).isChecked();
                enableChildren(child, state, childEnabled);
              }
            }
          }
        }
      }
    }
  }

  private void getRoleData(final Consumer<Boolean> callback) {
    ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_ROLE_RIGHTS);
    params.addQueryItem(COL_OBJECT_TYPE, getObjectType().ordinal());
    params.addQueryItem(COL_ROLE, getRoleId());

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasErrors()) {
          response.notify(BeeKeeper.getScreen());
          callback.accept(false);

        } else {
          if (!initialValues.isEmpty()) {
            initialValues.clear();
          }
          if (!changes.isEmpty()) {
            changes.clear();
          }

          if (response.hasResponse()) {
            Map<String, String> rights = Codec.deserializeMap(response.getResponseAsString());

            Multimap<String, RightsState> values = HashMultimap.create();
            for (Map.Entry<String, String> entry : rights.entrySet()) {
              Set<RightsState> states = EnumUtils.parseIndexSet(RightsState.class,
                  entry.getValue());
              states.retainAll(getRightsStates());

              if (!states.isEmpty()) {
                values.putAll(entry.getKey(), states);
              }
            }

            for (RightsState state : getRightsStates()) {
              if (state.isChecked()) {
                for (RightsObject object : getObjects()) {
                  if (!values.containsEntry(object.getName(), state)) {
                    initialValues.put(object.getName(), state);
                  }
                }

              } else if (values.containsValue(state)) {
                for (Map.Entry<String, RightsState> entry : values.entries()) {
                  if (entry.getValue() == state) {
                    initialValues.put(entry.getKey(), entry.getValue());
                  }
                }
              }
            }

          } else {
            for (RightsState state : getRightsStates()) {
              if (state.isChecked()) {
                for (RightsObject object : getObjects()) {
                  initialValues.put(object.getName(), state);
                }
              }
            }
          }

          callback.accept(true);
        }
      }
    });
  }

  private Long getRoleId() {
    return roleId;
  }

  private String getRoleName() {
    return roleName;
  }

  private Toggle getStateToggle(RightsState state) {
    for (Widget widget : getTable()) {
      if (widget instanceof Toggle && isDataType(widget, DATA_TYPE_STATE_TOGGLE)
          && getRightsState(widget) == state) {
        return (Toggle) widget;
      }
    }

    severe("rights state", state, "toggle not found");
    return null;
  }

  private List<Toggle> getStateToggles() {
    List<Toggle> toggles = Lists.newArrayList();

    for (Widget widget : getTable()) {
      if (widget instanceof Toggle && isDataType(widget, DATA_TYPE_STATE_TOGGLE)) {
        toggles.add((Toggle) widget);
      }
    }
    return toggles;
  }

  private boolean isChecked(String objectName, RightsState state) {
    return initialValues.containsEntry(objectName, state)
        != changes.containsEntry(objectName, state);
  }

  private boolean isStateChecked(RightsState state) {
    for (RightsObject object : getObjects()) {
      if (!isChecked(object.getName(), state)) {
        return false;
      }
    }
    return true;
  }

  private void saveDiff(final RightsState state, final Map<String, String> diff,
      final Consumer<Boolean> callback) {

    ParameterList params = BeeKeeper.getRpc().createParameters(Service.SET_STATE_RIGHTS);
    params.addQueryItem(COL_OBJECT_TYPE, getObjectType().name());
    params.addQueryItem(COL_STATE, state.name());

    params.addDataItem(COL_OBJECT, Codec.beeSerialize(diff));

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasErrors()) {
          response.notify(BeeKeeper.getScreen());
          if (callback != null) {
            callback.accept(false);
          }

        } else {
          for (String objectName : diff.keySet()) {
            if (initialValues.containsEntry(objectName, state)) {
              initialValues.remove(objectName, state);
            } else {
              initialValues.put(objectName, state);
            }

            changes.remove(objectName, state);
          }

          debug(getObjectType(), state, "saved", diff.size(), "changes");
          if (callback != null) {
            callback.accept(true);
          }
        }
      }
    });
  }

  private void setRoleId(Long roleId) {
    this.roleId = roleId;
  }

  private void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  private boolean toggleValue(String objectName, RightsState state) {
    if (BeeUtils.isEmpty(objectName)) {
      severe("toggle value: object name not specified");
      return false;
    } else if (state == null) {
      severe("toggle value: rights state not specified");
      return false;

    } else if (changes.containsEntry(objectName, state)) {
      changes.remove(objectName, state);
      return false;
    } else {
      changes.put(objectName, state);
      return true;
    }
  }

  private void updateStateValueToggles(RightsState state, boolean checked) {
    for (Widget widget : getTable()) {
      if (widget instanceof Toggle && ((Toggle) widget).isChecked() != checked
          && isValueWidget(widget) && getRightsState(widget) == state) {

        ((Toggle) widget).setChecked(checked);

        boolean isChanged = toggleValue(getObjectName(widget), state);
        updateValueCell(widget, isChanged);

        if (checked) {
          enableValueWidet(widget, checked);
        }
      }
    }

    if (!checked) {
      for (RightsObject object : getObjects()) {
        if (object.hasChildren() && !object.hasParent()) {
          enableChildren(object, state, checked);
        }
      }
    }
  }
}
