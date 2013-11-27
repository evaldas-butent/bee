package lt.lb.webservices.exchangerates;

final class ExchangeRatesConstants {

  public static final String WS_DEFAULT_WSDL =
      "http://webservices.lb.lt/ExchangeRates/ExchangeRates.asmx?WSDL";

  public static final String WS_RESULT_CURRENT_EXCHANGE_RATE_RESULT =
      "getCurrentExchangeRateResult";

  public static final String WS_RESULT_EXCHANGE_RATE_RESULT = "getExchangeRateResult";

  public static final String WS_RESULT_LIST_OF_CURRENCIES_RESULT = "getListOfCurrenciesResult";

  public static final String WS_RESULT_EXCHANGE_RATES_BY_CURRENCY_XML_STRING =
      "getExchangeRatesByCurrency_XmlStringResult";

  public static final String WS_RESULT_EXCHANGE_RATES_BY_CURRENCY =
      "getExchangeRatesByCurrencyResult";

  public static final String TAG_CURRENCY = "currency";
  public static final String TAG_DATE = "date";
  public static final String TAG_QUANTITY = "quantity";
  public static final String TAG_DESCRIPTION = "description";
  public static final String TAG_RATE = "rate";
  public static final String TAG_UNIT = "unit";
  public static final String TAG_MESSAGE = "message";

  public static final String ATTR_LANG = "lang";
  
  public static final String COL_CURRENCY_DESCRIPTION = "Description";
  public static final String COL_CURRENCY_EN_DESCRIPTION = "DescriptionEn";
  public static final String COL_CURRENCY_UNIT = "Unit";
}
