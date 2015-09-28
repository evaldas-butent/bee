package com.butent.bee.server.rest.autodata;

import com.butent.bee.shared.Assert;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

public final class AutodataClient {

  private static AutodataClient instance;

  private final String baseUrl = "http://api.autodata-group.com/";
  private final String tokenUrl = "https://account.autodata-group.com/oauth/access_token";
  private final String clientId;
  private final String clientSecret;

  private String countryCode = "gb";
  private String languageCode = "en-gb";

  private String accessToken;
  private long tokenExpires;

  private AutodataClient(String clientId, String clientSecret) {
    this.clientId = Assert.notEmpty(clientId);
    this.clientSecret = Assert.notEmpty(clientSecret);
  }

  public static AutodataClient getInstance(String clientId, String clientSecret) {
    if (instance == null || !instance.sameCredentials(clientId, clientSecret)) {
      instance = new AutodataClient(clientId, clientSecret);
    }
    return instance;
  }

  public Map<String, String> getManufacturers() {
    Map<String, String> manufacturers = new LinkedHashMap<>();

    call("manufacturers", new Consumer<JsonObject>() {
      @Override
      public void accept(JsonObject json) {
        for (JsonValue item : json.getJsonArray("data")) {
          manufacturers.put(((JsonObject) item).getString("manufacturer_id"),
              ((JsonObject) item).getString("manufacturer"));
        }
      }
    });
    return manufacturers;
  }

  private void call(String method, Consumer<JsonObject> consumer) {
    Client client = ClientBuilder.newClient();

    try {
      JsonObject json = client.target(this.baseUrl).path("v1").path(method)
          .queryParam("country-code", this.countryCode)
          .queryParam("access_token", getToken())
          .request()
          .acceptLanguage(this.languageCode)
          .accept(MediaType.APPLICATION_JSON_TYPE)
          .get(JsonObject.class);

      consumer.accept(json);
    } finally {
      client.close();
    }
  }

  private String getToken() {
    if ((int) (new Date().getTime() / 1000) > this.tokenExpires) {
      Client client = ClientBuilder.newClient();

      try {
        WebTarget target = client.target(this.tokenUrl);
        Form requestForm = new Form()
            .param("client_id", this.clientId)
            .param("client_secret", this.clientSecret)
            .param("grant_type", "client_credentials")
            .param("scope", "scope1");

        JsonObject response = target.request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.form(requestForm), JsonObject.class);

        this.accessToken = response.getString("access_token");
        this.tokenExpires = response.getInt("expires");
      } finally {
        client.close();
      }
    }
    return this.accessToken;
  }

  private boolean sameCredentials(String id, String secret) {
    return Objects.equals(id, this.clientId) && Objects.equals(secret, this.clientSecret);
  }
}