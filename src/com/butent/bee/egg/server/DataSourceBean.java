package com.butent.bee.egg.server;

import com.butent.bee.egg.server.utils.BeeDataSource;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@Singleton
@Startup
@DependsOn("ConfigBean")
@Lock(LockType.READ)
public class DataSourceBean {
  private static final String PROPERTY_DSN = "DataSourceName";
  private static Logger logger = Logger.getLogger(DataSourceBean.class.getName());

  @EJB
  ConfigBean config;

  private List<BeeDataSource> bds = new ArrayList<BeeDataSource>();

  @PreDestroy
  public void destroy() {
    if (!bds.isEmpty()) {
      for (BeeDataSource z : bds) {
        if (z.isOpen()) {
          try {
            z.close();
            LogUtils.info(logger, "closed", z.getTp());
          } catch (Exception ex) {
            LogUtils.warning(logger, ex);
          }
        }
      }
    }

    LogUtils.infoNow(logger, getClass().getSimpleName(), "destroy end");
  }

  public BeeDataSource locateDs(String dsn) {
    Assert.notEmpty(dsn);
    BeeDataSource z = null;

    for (BeeDataSource k : bds) {
      if (dsn.equals(k.getTp())) {
        z = k;
        break;
      }
    }

    if (z == null) {
      LogUtils.warning(logger, "dsn", dsn, "not found");
    }

    return z;
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    String dsn = config.getProperty(PROPERTY_DSN);

    if (BeeUtils.isEmpty(dsn)) {
      LogUtils.severe(logger, "property", PROPERTY_DSN, "not found");
      return;
    }

    String[] arr = dsn.split(",");

    String tp, nm;
    DataSource ds;
    boolean ok;

    for (String z : arr) {
      nm = z.trim();

      if (BeeUtils.context("my", nm)) {
        tp = BeeConst.MYSQL;
      } else if (BeeUtils.context("ms", nm)) {
        tp = BeeConst.MSSQL;
      } else if (BeeUtils.context("or", nm)) {
        tp = BeeConst.ORACLE;
      } else if (BeeUtils.context("pg", nm)) {
        tp = BeeConst.PGSQL;
      } else {
        tp = null;
      }

      if (BeeUtils.isEmpty(tp)) {
        LogUtils.warning(logger, "dsn", z, "not recognized");
        continue;
      }

      try {
        ds = (DataSource) InitialContext.doLookup("jdbc/" + nm);
        ok = true;
      } catch (NamingException ex) {
        LogUtils.error(logger, ex);
        ds = null;
        ok = false;
      }

      if (ok) {
        bds.add(new BeeDataSource(tp, ds));
      }
    }

    LogUtils.infoNow(logger, getClass().getSimpleName(), bds.size(),
        "data sources initialized");
  }

}
