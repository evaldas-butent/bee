package com.butent.bee.server.datasource.query.parser;

public interface QueryParserConstants {
  int EOF = 0;

  int KW_SELECT = 5;
  int KW_WHERE = 6;
  int KW_GROUP = 7;
  int KW_PIVOT = 8;
  int KW_ORDER = 9;
  int KW_BY = 10;
  int KW_SKIPPING = 11;
  int KW_LIMIT = 12;
  int KW_OFFSET = 13;
  int KW_LABEL = 14;
  int KW_FORMAT = 15;
  int KW_OPTIONS = 16;
  int KW_ASC = 17;
  int KW_DESC = 18;
  int KW_TRUE = 19;
  int KW_FALSE = 20;
  int KW_AND = 21;
  int KW_OR = 22;
  int KW_NOT = 23;
  int KW_DATE = 24;
  int KW_TIMEOFDAY = 25;
  int KW_DATETIME = 26;
  int KW_TIMESTAMP = 27;
  int KW_MIN = 28;
  int KW_MAX = 29;
  int KW_AVG = 30;
  int KW_COUNT = 31;
  int KW_SUM = 32;
  int KW_NO_VALUES = 33;
  int KW_NO_FORMAT = 34;
  int KW_IS = 35;
  int KW_NULL = 36;
  int KW_YEAR = 37;
  int KW_MONTH = 38;
  int KW_DAY = 39;
  int KW_HOUR = 40;
  int KW_MINUTE = 41;
  int KW_SECOND = 42;
  int KW_MILLISECOND = 43;
  int KW_WITH = 44;
  int KW_CONTAINS = 45;
  int KW_STARTS = 46;
  int KW_ENDS = 47;
  int KW_MATCHES = 48;
  int KW_LIKE = 49;
  int KW_NOW = 50;
  int KW_DATEDIFF = 51;
  int KW_QUARTER = 52;
  int KW_LOWER = 53;
  int KW_UPPER = 54;
  int KW_DAYOFWEEK = 55;
  int KW_TODATE = 56;
  int ID = 57;
  int INTEGER_LITERAL = 58;
  int DECIMAL_LITERAL = 59;
  int ALPHANUMERIC = 60;
  int DIGIT = 61;
  int LETTER_OR_UNDERSCORE = 62;
  int STRING_LITERAL = 63;
  int QUOTED_ID = 64;
  int OP_COMMA = 65;
  int OP_LPAREN = 66;
  int OP_RPAREN = 67;
  int OP_EQUALS = 68;
  int OP_NOT_EQUALS = 69;
  int OP_LESS_THAN = 70;
  int OP_LESS_OR_EQUAL = 71;
  int OP_GREATER_THAN = 72;
  int OP_GREATER_OR_EQUAL = 73;
  int OP_ASTERISK = 74;
  int OP_PLUS = 75;
  int OP_MINUS = 76;
  int OP_SLASH = 77;
  int OP_MODULO = 78;
  int UNEXPECTED_CHAR = 79;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"select\"",
    "\"where\"",
    "\"group\"",
    "\"pivot\"",
    "\"order\"",
    "\"by\"",
    "\"skipping\"",
    "\"limit\"",
    "\"offset\"",
    "\"label\"",
    "\"format\"",
    "\"options\"",
    "\"asc\"",
    "\"desc\"",
    "\"true\"",
    "\"false\"",
    "\"and\"",
    "\"or\"",
    "\"not\"",
    "\"date\"",
    "\"timeofday\"",
    "\"datetime\"",
    "\"timestamp\"",
    "\"min\"",
    "\"max\"",
    "\"avg\"",
    "\"count\"",
    "\"sum\"",
    "\"no_values\"",
    "\"no_format\"",
    "\"is\"",
    "\"null\"",
    "\"year\"",
    "\"month\"",
    "\"day\"",
    "\"hour\"",
    "\"minute\"",
    "\"second\"",
    "\"millisecond\"",
    "\"with\"",
    "\"contains\"",
    "\"starts\"",
    "\"ends\"",
    "\"matches\"",
    "\"like\"",
    "\"now\"",
    "\"dateDiff\"",
    "\"quarter\"",
    "\"lower\"",
    "\"upper\"",
    "\"dayOfWeek\"",
    "\"toDate\"",
    "<ID>",
    "<INTEGER_LITERAL>",
    "<DECIMAL_LITERAL>",
    "<ALPHANUMERIC>",
    "<DIGIT>",
    "<LETTER_OR_UNDERSCORE>",
    "<STRING_LITERAL>",
    "<QUOTED_ID>",
    "\",\"",
    "\"(\"",
    "\")\"",
    "\"=\"",
    "<OP_NOT_EQUALS>",
    "\"<\"",
    "\"<=\"",
    "\">\"",
    "\">=\"",
    "\"*\"",
    "\"+\"",
    "\"-\"",
    "\"/\"",
    "\"%\"",
    "<UNEXPECTED_CHAR>",
  };

}
