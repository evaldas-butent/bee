package com.butent.bee.server.authentication;

import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import javax.ejb.EJB;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class InstallSAM implements ServletContextListener {

  @EJB
  UserServiceBean usr;

  private static final BeeLogger logger = LogUtils.getLogger(InstallSAM.class);
  private static final String LAYER = "HttpServlet";

  private AuthConfigFactory factory;
  private String identifier;

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    if (identifier != null) {
      factory.removeRegistration(identifier);
      logger.info("Unregistered SAM:", identifier);
    }
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    factory = AuthConfigFactory.getFactory();
    String appContext = BeeUtils.joinWords(sce.getServletContext().getVirtualServerName(),
        sce.getServletContext().getContextPath());

    identifier = factory.registerConfigProvider(
        new BeeAuthConfigProvider(new BeeServerAuthModule(usr)), LAYER, appContext, "BEE SAM");

    logger.info("Registered SAM:", identifier);
  }
}
