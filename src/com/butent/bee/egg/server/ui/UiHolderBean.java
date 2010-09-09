package com.butent.bee.egg.server.ui;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.ui.UiComponent;
import com.butent.bee.egg.shared.ui.UiLoader;

@Singleton
@Lock(LockType.READ)
public class UiHolderBean {

  @EJB
  UiLoaderImpl loaderBean;

  UiLoader loader;
  Map<String, UiComponent> formCache = new HashMap<String, UiComponent>();

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    setLoader(loaderBean);
  }

  @Lock(LockType.WRITE)
  public void setLoader(UiLoader loader) {
    this.loader = loader;
  }

  public UiComponent getForm(String root, Object... params) {
    Assert.notEmpty(root);

    if (!formCache.containsKey(root)) {
      loadForm(root, params);
    }
    return formCache.get(root);
  }

  @Lock(LockType.WRITE)
  private void loadForm(String root, Object... params) {
    UiComponent form = loader.getFormContent(root, params);
    formCache.put(root, form);
  }
}
