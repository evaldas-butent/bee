package com.butent.bee.egg.server.ui;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.ui.UiComponent;
import com.butent.bee.egg.shared.ui.UiLoader;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

@Singleton
public class UiHolderBean {

  @EJB
  UiLoaderBean loaderBean;

  UiLoader loader;
  Map<String, UiComponent> formCache = new HashMap<String, UiComponent>();

  @Lock(LockType.READ)
  public UiComponent getForm(String root, Object... params) {
    Assert.notEmpty(root);

    if (!formCache.containsKey(root)) {
      loadForm(root, params);
    }
    return formCache.get(root);
  }

  @Lock(LockType.READ)
  public UiComponent getMenu(String root, Object... params) {
    Assert.notEmpty(root);

    return loader.getMenu(root, params);
  }

  public void setLoader(UiLoader loader) {
    this.loader = loader;
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    setLoader(loaderBean);
  }

  private void loadForm(String root, Object... params) {
    UiComponent form = loader.getForm(root, params);
    formCache.put(root, form);
  }
}
