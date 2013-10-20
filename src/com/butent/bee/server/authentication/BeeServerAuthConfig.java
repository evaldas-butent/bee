package com.butent.bee.server.authentication;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;

/**
 * This class functions as a kind of factory for {@link ServerAuthContext} instances, which are
 * delegates for the actual {@link ServerAuthModule} (SAM) that we're after.
 */
public class BeeServerAuthConfig implements ServerAuthConfig {

  private final String layer;
  private final String appContext;
  private final CallbackHandler handler;
  private final Map<String, String> providerProperties;
  private final ServerAuthModule serverAuthModule;

  public BeeServerAuthConfig(String layer, String appContext, CallbackHandler handler,
      Map<String, String> providerProperties, ServerAuthModule serverAuthModule) {
    this.layer = layer;
    this.appContext = appContext;
    this.handler = handler;
    this.providerProperties = providerProperties;
    this.serverAuthModule = serverAuthModule;
  }

  @Override
  public String getAppContext() {
    return appContext;
  }

  // ### The methods below mostly just return what has been passed into the
  // constructor.
  // ### In practice they don't seem to be called

  @Override
  public ServerAuthContext getAuthContext(String authContextID, Subject serviceSubject,
      @SuppressWarnings("rawtypes") Map properties) throws AuthException {
    return new BeeServerAuthContext(handler, serverAuthModule);
  }

  /**
   * It's not entirely clear what the difference is between the "application context identifier"
   * (appContext) and the "authentication context identifier" (authContext). In early iterations of
   * the specification, authContext was called "operation" and instead of the MessageInfo it was
   * obtained by something called an "authParam".
   */
  @Override
  public String getAuthContextID(MessageInfo messageInfo) {
    return appContext;
  }

  @Override
  public String getMessageLayer() {
    return layer;
  }

  public Map<String, String> getProviderProperties() {
    return providerProperties;
  }

  @Override
  public boolean isProtected() {
    return false;
  }

  @Override
  public void refresh() {
  }
}
