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
@Lock(LockType.READ)
public class UiHolderBean {

  @EJB
  UiLoaderImpl loaderBean;

  UiLoader loader;
  Map<String, UiComponent> formCache = new HashMap<String, UiComponent>();

  public UiComponent getForm(String root, Object... params) {
    Assert.notEmpty(root);

    if (!formCache.containsKey(root)) {
      loadForm(root, params);
    }
    return formCache.get(root);
  }

  public UiComponent getMenu(String root, Object... params) {
    Assert.notEmpty(root);

    return loader.getMenu(root, params);
  }

  @Lock(LockType.WRITE)
  public void setLoader(UiLoader loader) {
    this.loader = loader;
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    setLoader(loaderBean);
  }

  @Lock(LockType.WRITE)
  private void loadForm(String root, Object... params) {
    UiComponent form = loader.getForm(root, params);
    formCache.put(root, form);
  }
}
