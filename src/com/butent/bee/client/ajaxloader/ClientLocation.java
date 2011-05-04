package com.butent.bee.client.ajaxloader;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Enables to get specific information about clients location(country, city, region etc).
 */

public class ClientLocation extends JavaScriptObject {

  protected ClientLocation() {
  }

  public final native String getCity() /*-{
		return this.address.city;
  }-*/;

  public final native String getCountry() /*-{
		return this.address.country;
  }-*/;

  public final native String getCountryCode() /*-{
		return this.address.country_code;
  }-*/;

  public final native double getLatitude() /*-{
		return this.latitude;
  }-*/;

  public final native double getLongitude() /*-{
		return this.longitude;
  }-*/;

  public final native String getRegion() /*-{
		return this.address.region;
  }-*/;
}
