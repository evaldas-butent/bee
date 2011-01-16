package com.butent.bee.egg.server.datasource.query.parser;

import com.butent.bee.egg.server.datasource.query.Query;
import com.butent.bee.egg.server.datasource.query.QueryFormat;
import com.butent.bee.egg.server.datasource.query.QueryGroup;
import com.butent.bee.egg.server.datasource.query.QueryLabels;
import com.butent.bee.egg.server.datasource.query.QueryOptions;
import com.butent.bee.egg.server.datasource.query.QueryPivot;
import com.butent.bee.egg.server.datasource.query.QuerySelection;
import com.butent.bee.egg.server.datasource.query.QuerySort;
import com.butent.bee.egg.shared.data.Aggregation;
import com.butent.bee.egg.shared.data.InvalidQueryException;
import com.butent.bee.egg.shared.data.SortOrder;
import com.butent.bee.egg.shared.data.column.AbstractColumn;
import com.butent.bee.egg.shared.data.column.AggregationColumn;
import com.butent.bee.egg.shared.data.column.ScalarFunctionColumn;
import com.butent.bee.egg.shared.data.column.SimpleColumn;
import com.butent.bee.egg.shared.data.filter.ColumnColumnFilter;
import com.butent.bee.egg.shared.data.filter.ColumnIsNullFilter;
import com.butent.bee.egg.shared.data.filter.ColumnValueFilter;
import com.butent.bee.egg.shared.data.filter.ComparisonFilter;
import com.butent.bee.egg.shared.data.filter.CompoundFilter;
import com.butent.bee.egg.shared.data.filter.NegationFilter;
import com.butent.bee.egg.shared.data.filter.RowFilter;
import com.butent.bee.egg.shared.data.function.Constant;
import com.butent.bee.egg.shared.data.function.CurrentDateTime;
import com.butent.bee.egg.shared.data.function.DateDiff;
import com.butent.bee.egg.shared.data.function.Difference;
import com.butent.bee.egg.shared.data.function.Lower;
import com.butent.bee.egg.shared.data.function.Modulo;
import com.butent.bee.egg.shared.data.function.Product;
import com.butent.bee.egg.shared.data.function.Quotient;
import com.butent.bee.egg.shared.data.function.ScalarFunction;
import com.butent.bee.egg.shared.data.function.Sum;
import com.butent.bee.egg.shared.data.function.TimeComponentExtractor;
import com.butent.bee.egg.shared.data.function.ToDate;
import com.butent.bee.egg.shared.data.function.Upper;
import com.butent.bee.egg.shared.data.value.BooleanValue;
import com.butent.bee.egg.shared.data.value.NumberValue;
import com.butent.bee.egg.shared.data.value.TextValue;
import com.butent.bee.egg.shared.data.value.Value;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QueryParser implements QueryParserConstants {
  private static int[] jjLa10;

  private static int[] jjLa11;

  private static int[] jjLa12;

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

  @SuppressWarnings("serial")
  private static final class LookaheadSuccess extends Error {
  }

  static {
    jjLa1Init0();
    jjLa1Init1();
    jjLa1Init2();
  }

  public static Query parseString(String queryString) throws ParseException, InvalidQueryException {
    Reader r = new BufferedReader(new StringReader(queryString));
    QueryParser parser = new QueryParser(r);
    Query query = parser.queryStatement();
    return query;
  }

  private static void jjLa1Init0() {
    jjLa10 = new int[]{
        0x20, 0x40, 0x80, 0x100, 0x200, 0x800, 0x1000, 0x2000, 0x4000, 0x8000, 0x10000, 0x0,
        0xff180000, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x400000, 0x200000, 0xff980000, 0x0, 0x800000,
        0xff180000, 0x0, 0xff180000, 0x0, 0xc000000, 0xf180000, 0x0, 0xff180000, 0xff180000, 0x0,
        0xff180000, 0x0, 0x0, 0x0, 0x0, 0xf0000000, 0x0, 0xf0000000, 0xf0000000, 0x60000, 0x0, 0x0,
        0x0, 0x180000,};
  }

  private static void jjLa1Init1() {
    jjLa11 = new int[]{
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x8fffffff, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x6, 0x0, 0x0, 0x8fffffff, 0x0, 0x10, 0x8fffffff, 0x3e008, 0x8fffffff, 0x3e000, 0x0,
        0x8c000000, 0x6, 0x8fffffff, 0x8fffffff, 0x0, 0x8fffffff, 0x0, 0x0, 0x0, 0x0, 0x1,
        0x1fc0fe0, 0x3ffffff, 0x1ffffff, 0x0, 0x4000000, 0xc000000, 0xc000000, 0x0,};
  }

  private static void jjLa1Init2() {
    jjLa12 = new int[]{
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x2, 0x1405, 0x2, 0x2, 0x2, 0x2,
        0x2, 0x0, 0x0, 0x0, 0x1005, 0x4, 0x0, 0x1005, 0x3f0, 0x1005, 0x3f0, 0x0, 0x1000, 0x0,
        0x1005, 0x1005, 0x2, 0x1005, 0x1800, 0x1800, 0x6400, 0x6400, 0x0, 0x0, 0x1, 0x0, 0x0,
        0x1000, 0x0, 0x1000, 0x0,};
  }

  public QueryParserTokenManager tokenSource;

  public Token token;

  public Token jjNt;

  JavaCharStream jjInputStream;

  private int jjNtk;

  private Token jjScanpos, jjLastpos;

  private int jjLa;

  private int jjGen;

  private final int[] jjLa1 = new int[48];

  private final JJCalls[] jj2rtns = new JJCalls[6];

  private boolean jjRescan = false;

  private int jjGc = 0;

  private final LookaheadSuccess jjLs = new LookaheadSuccess();

  private List<int[]> jjExpentries = new ArrayList<int[]>();

  private int[] jjExpentry;

  private int jjKind = -1;

  private int[] jjLasttokens = new int[100];

  private int jjEndpos;

  public QueryParser(java.io.InputStream stream) {
    this(stream, null);
  }

  public QueryParser(java.io.InputStream stream, String encoding) {
    try {
      jjInputStream = new JavaCharStream(stream, encoding, 1, 1);
    } catch (java.io.UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    tokenSource = new QueryParserTokenManager(jjInputStream);
    token = new Token();
    jjNtk = -1;
    jjGen = 0;
    for (int i = 0; i < 48; i++) {
      jjLa1[i] = -1;
    }
    for (int i = 0; i < jj2rtns.length; i++) {
      jj2rtns[i] = new JJCalls();
    }
  }

  public QueryParser(java.io.Reader stream) {
    jjInputStream = new JavaCharStream(stream, 1, 1);
    tokenSource = new QueryParserTokenManager(jjInputStream);
    token = new Token();
    jjNtk = -1;
    jjGen = 0;
    for (int i = 0; i < 48; i++) {
      jjLa1[i] = -1;
    }
    for (int i = 0; i < jj2rtns.length; i++) {
      jj2rtns[i] = new JJCalls();
    }
  }

  public QueryParser(QueryParserTokenManager tm) {
    tokenSource = tm;
    token = new Token();
    jjNtk = -1;
    jjGen = 0;
    for (int i = 0; i < 48; i++) {
      jjLa1[i] = -1;
    }
    for (int i = 0; i < jj2rtns.length; i++) {
      jj2rtns[i] = new JJCalls();
    }
  }

  public final AbstractColumn abstractColumnDescriptor() throws ParseException,
      InvalidQueryException {
    AbstractColumn column;
    AbstractColumn result = null;
    if (jj24(2)) {
      column = arithmeticExpression();
      result = column;
    } else {
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case KW_TRUE:
        case KW_FALSE:
        case KW_DATE:
        case KW_TIMEOFDAY:
        case KW_DATETIME:
        case KW_TIMESTAMP:
        case KW_MIN:
        case KW_MAX:
        case KW_AVG:
        case KW_COUNT:
        case KW_SUM:
        case KW_NO_VALUES:
        case KW_NO_FORMAT:
        case KW_IS:
        case KW_NULL:
        case KW_YEAR:
        case KW_MONTH:
        case KW_DAY:
        case KW_HOUR:
        case KW_MINUTE:
        case KW_SECOND:
        case KW_MILLISECOND:
        case KW_WITH:
        case KW_CONTAINS:
        case KW_STARTS:
        case KW_ENDS:
        case KW_MATCHES:
        case KW_LIKE:
        case KW_NOW:
        case KW_DATEDIFF:
        case KW_QUARTER:
        case KW_LOWER:
        case KW_UPPER:
        case KW_DAYOFWEEK:
        case KW_TODATE:
        case ID:
        case INTEGER_LITERAL:
        case DECIMAL_LITERAL:
        case STRING_LITERAL:
        case QUOTED_ID:
        case OP_LPAREN:
        case OP_MINUS:
          column = atomicAbstractColumnDescriptor();
          result = column;
          break;
        default:
          jjLa1[31] = jjGen;
          jjConsumeToken(-1);
          throw new ParseException();
      }
    }
    return result;
  }

  public final Aggregation aggregationFunction() throws ParseException {
    Aggregation result = null;
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_MIN:
        jjConsumeToken(KW_MIN);
        result = Aggregation.MIN;
        break;
      case KW_MAX:
        jjConsumeToken(KW_MAX);
        result = Aggregation.MAX;
        break;
      case KW_COUNT:
        jjConsumeToken(KW_COUNT);
        result = Aggregation.COUNT;
        break;
      case KW_AVG:
        jjConsumeToken(KW_AVG);
        result = Aggregation.AVG;
        break;
      case KW_SUM:
        jjConsumeToken(KW_SUM);
        result = Aggregation.SUM;
        break;
      default:
        jjLa1[39] = jjGen;
        jjConsumeToken(-1);
        throw new ParseException();
    }
    return result;
  }

  public final AbstractColumn arithmeticExpression() throws ParseException, InvalidQueryException {
    AbstractColumn column;
    column = possibleSecondOrderArithmeticExpression();
    return column;
  }

  public final AbstractColumn atomicAbstractColumnDescriptor() throws ParseException,
      InvalidQueryException {
    Aggregation aggregationType;
    ScalarFunction scalarFunction;
    String columnId;
    AbstractColumn column;
    ArrayList<AbstractColumn> columns = new ArrayList<AbstractColumn>();
    AbstractColumn result = null;
    Value value;
    if (jj25(2)) {
      aggregationType = aggregationFunction();
      jjConsumeToken(OP_LPAREN);
      columnId = columnId();
      jjConsumeToken(OP_RPAREN);
      result = new AggregationColumn(new SimpleColumn(columnId),
           aggregationType);
    } else if (jj26(2)) {
      scalarFunction = scalarFunction();
      jjConsumeToken(OP_LPAREN);
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case KW_TRUE:
        case KW_FALSE:
        case KW_DATE:
        case KW_TIMEOFDAY:
        case KW_DATETIME:
        case KW_TIMESTAMP:
        case KW_MIN:
        case KW_MAX:
        case KW_AVG:
        case KW_COUNT:
        case KW_SUM:
        case KW_NO_VALUES:
        case KW_NO_FORMAT:
        case KW_IS:
        case KW_NULL:
        case KW_YEAR:
        case KW_MONTH:
        case KW_DAY:
        case KW_HOUR:
        case KW_MINUTE:
        case KW_SECOND:
        case KW_MILLISECOND:
        case KW_WITH:
        case KW_CONTAINS:
        case KW_STARTS:
        case KW_ENDS:
        case KW_MATCHES:
        case KW_LIKE:
        case KW_NOW:
        case KW_DATEDIFF:
        case KW_QUARTER:
        case KW_LOWER:
        case KW_UPPER:
        case KW_DAYOFWEEK:
        case KW_TODATE:
        case ID:
        case INTEGER_LITERAL:
        case DECIMAL_LITERAL:
        case STRING_LITERAL:
        case QUOTED_ID:
        case OP_LPAREN:
        case OP_MINUS:
          column = abstractColumnDescriptor();
          columns.add(column);
          break;
        default:
          jjLa1[32] = jjGen;
      }
      label_10 : while (true) {
        switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
          case OP_COMMA:
            break;
          default:
            jjLa1[33] = jjGen;
            break label_10;
        }
        jjConsumeToken(OP_COMMA);
        column = abstractColumnDescriptor();
        columns.add(column);
      }
      jjConsumeToken(OP_RPAREN);
      result = new ScalarFunctionColumn(
              GenericsHelper.makeTypedList(columns), scalarFunction);
    } else {
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case OP_LPAREN:
          jjConsumeToken(OP_LPAREN);
          column = abstractColumnDescriptor();
          jjConsumeToken(OP_RPAREN);
          result = column;
          break;
        case KW_TRUE:
        case KW_FALSE:
        case KW_DATE:
        case KW_TIMEOFDAY:
        case KW_DATETIME:
        case KW_TIMESTAMP:
        case INTEGER_LITERAL:
        case DECIMAL_LITERAL:
        case STRING_LITERAL:
        case OP_MINUS:
          value = literal();
          result = new ScalarFunctionColumn(new ArrayList<AbstractColumn>(),
              new Constant(value));
          break;
        case KW_MIN:
        case KW_MAX:
        case KW_AVG:
        case KW_COUNT:
        case KW_SUM:
        case KW_NO_VALUES:
        case KW_NO_FORMAT:
        case KW_IS:
        case KW_NULL:
        case KW_YEAR:
        case KW_MONTH:
        case KW_DAY:
        case KW_HOUR:
        case KW_MINUTE:
        case KW_SECOND:
        case KW_MILLISECOND:
        case KW_WITH:
        case KW_CONTAINS:
        case KW_STARTS:
        case KW_ENDS:
        case KW_MATCHES:
        case KW_LIKE:
        case KW_NOW:
        case KW_DATEDIFF:
        case KW_QUARTER:
        case KW_LOWER:
        case KW_UPPER:
        case KW_DAYOFWEEK:
        case KW_TODATE:
        case ID:
        case QUOTED_ID:
          columnId = columnId();
          result = new SimpleColumn(columnId);
          break;
        default:
          jjLa1[34] = jjGen;
          jjConsumeToken(-1);
          throw new ParseException();
      }
    }
    return result;
  }

  public final boolean booleanLiteral() throws ParseException {
    boolean b;
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_TRUE:
        jjConsumeToken(KW_TRUE);
        b = true;
        break;
      case KW_FALSE:
        jjConsumeToken(KW_FALSE);
        b = false;
        break;
      default:
        jjLa1[47] = jjGen;
        jjConsumeToken(-1);
        throw new ParseException();
    }
    return b;
  }

  public final String columnId() throws ParseException {
    Token t;
    String result = null;
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case ID:
        t = jjConsumeToken(ID);
        result = t.image;
        break;
      case QUOTED_ID:
        t = jjConsumeToken(QUOTED_ID);
        result = ParserUtils.stripQuotes(t.image);
        break;
      case KW_MIN:
      case KW_MAX:
      case KW_AVG:
      case KW_COUNT:
      case KW_SUM:
      case KW_NO_VALUES:
      case KW_NO_FORMAT:
      case KW_IS:
      case KW_NULL:
      case KW_YEAR:
      case KW_MONTH:
      case KW_DAY:
      case KW_HOUR:
      case KW_MINUTE:
      case KW_SECOND:
      case KW_MILLISECOND:
      case KW_WITH:
      case KW_CONTAINS:
      case KW_STARTS:
      case KW_ENDS:
      case KW_MATCHES:
      case KW_LIKE:
      case KW_NOW:
      case KW_DATEDIFF:
      case KW_QUARTER:
      case KW_LOWER:
      case KW_UPPER:
      case KW_DAYOFWEEK:
      case KW_TODATE:
        result = nonReservedKeyword();
        break;
      default:
        jjLa1[41] = jjGen;
        jjConsumeToken(-1);
        throw new ParseException();
    }
    return result;
  }

  public final ComparisonFilter.Operator comparisonOperator() throws ParseException {
    ComparisonFilter.Operator op;
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case OP_EQUALS:
        jjConsumeToken(OP_EQUALS);
        op = ComparisonFilter.Operator.EQ;
        break;
      case OP_NOT_EQUALS:
        jjConsumeToken(OP_NOT_EQUALS);
        op = ComparisonFilter.Operator.NE;
        break;
      case OP_LESS_THAN:
        jjConsumeToken(OP_LESS_THAN);
        op = ComparisonFilter.Operator.LT;
        break;
      case OP_LESS_OR_EQUAL:
        jjConsumeToken(OP_LESS_OR_EQUAL);
        op = ComparisonFilter.Operator.LE;
        break;
      case OP_GREATER_THAN:
        jjConsumeToken(OP_GREATER_THAN);
        op = ComparisonFilter.Operator.GT;
        break;
      case OP_GREATER_OR_EQUAL:
        jjConsumeToken(OP_GREATER_OR_EQUAL);
        op = ComparisonFilter.Operator.GE;
        break;
      case KW_CONTAINS:
        jjConsumeToken(KW_CONTAINS);
        op = ComparisonFilter.Operator.CONTAINS;
        break;
      case KW_STARTS:
        jjConsumeToken(KW_STARTS);
        jjConsumeToken(KW_WITH);
        op = ComparisonFilter.Operator.STARTS_WITH;
        break;
      case KW_ENDS:
        jjConsumeToken(KW_ENDS);
        jjConsumeToken(KW_WITH);
        op = ComparisonFilter.Operator.ENDS_WITH;
        break;
      case KW_MATCHES:
        jjConsumeToken(KW_MATCHES);
        op = ComparisonFilter.Operator.MATCHES;
        break;
      case KW_LIKE:
        jjConsumeToken(KW_LIKE);
        op = ComparisonFilter.Operator.LIKE;
        break;
      default:
        jjLa1[27] = jjGen;
        jjConsumeToken(-1);
        throw new ParseException();
    }
    return op;
  }

  public final double decimalLiteral() throws ParseException {
    Token t1;
    Token t2;
    String s;
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case DECIMAL_LITERAL:
        t1 = jjConsumeToken(DECIMAL_LITERAL);
        s = t1.image;
        break;
      case INTEGER_LITERAL:
        t1 = jjConsumeToken(INTEGER_LITERAL);
        s = t1.image;
        break;
      case OP_MINUS:
        t1 = jjConsumeToken(OP_MINUS);
        switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
          case DECIMAL_LITERAL:
            t2 = jjConsumeToken(DECIMAL_LITERAL);
            break;
          case INTEGER_LITERAL:
            t2 = jjConsumeToken(INTEGER_LITERAL);
            break;
          default:
            jjLa1[45] = jjGen;
            jjConsumeToken(-1);
            throw new ParseException();
        }
        s = t1.image + t2.image;
        break;
      default:
        jjLa1[46] = jjGen;
        jjConsumeToken(-1);
        throw new ParseException();
    }
    return Double.parseDouble(s);
  }

  public final void formatClause(Query query) throws ParseException, InvalidQueryException {
    QueryFormat formats = new QueryFormat();
    AbstractColumn column;
    String pattern;
    jjConsumeToken(KW_FORMAT);
    column = abstractColumnDescriptor();
    pattern = stringLiteral();
    formats.addPattern(column, pattern);
    label_6 : while (true) {
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case OP_COMMA:
          break;
        default:
          jjLa1[17] = jjGen;
          break label_6;
      }
      jjConsumeToken(OP_COMMA);
      column = abstractColumnDescriptor();
      pattern = stringLiteral();
      formats.addPattern(column, pattern);
    }
    query.setUserFormatOptions(formats);
  }

  public ParseException generateParseException() {
    jjExpentries.clear();
    boolean[] la1tokens = new boolean[80];
    if (jjKind >= 0) {
      la1tokens[jjKind] = true;
      jjKind = -1;
    }
    for (int i = 0; i < 48; i++) {
      if (jjLa1[i] == jjGen) {
        for (int j = 0; j < 32; j++) {
          if ((jjLa10[i] & (1 << j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jjLa11[i] & (1 << j)) != 0) {
            la1tokens[32 + j] = true;
          }
          if ((jjLa12[i] & (1 << j)) != 0) {
            la1tokens[64 + j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 80; i++) {
      if (la1tokens[i]) {
        jjExpentry = new int[1];
        jjExpentry[0] = i;
        jjExpentries.add(jjExpentry);
      }
    }
    jjEndpos = 0;
    jjRescanToken();
    jjAddErrorToken(0, 0);
    int[][] exptokseq = new int[jjExpentries.size()][];
    for (int i = 0; i < jjExpentries.size(); i++) {
      exptokseq[i] = jjExpentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  public final Token getNextToken() {
    if (token.next != null) {
      token = token.next;
    } else {
      token = token.next = tokenSource.getNextToken();
    }
    jjNtk = -1;
    jjGen++;
    return token;
  }

  public final Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) {
        t = t.next;
      } else {
        t = t.next = tokenSource.getNextToken();
      }
    }
    return t;
  }

  public final void groupByClause(Query query) throws ParseException, InvalidQueryException {
    QueryGroup group = new QueryGroup();
    AbstractColumn column;
    jjConsumeToken(KW_GROUP);
    jjConsumeToken(KW_BY);
    column = abstractColumnDescriptor();
    group.addColumn(column);
    label_2 : while (true) {
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case OP_COMMA:
          break;
        default:
          jjLa1[13] = jjGen;
          break label_2;
      }
      jjConsumeToken(OP_COMMA);
      column = abstractColumnDescriptor();
      group.addColumn(column);
    }
    query.setGroup(group);
  }

  public final int integerLiteral() throws ParseException {
    Token t1;
    Token t2;
    String s;
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case INTEGER_LITERAL:
        t1 = jjConsumeToken(INTEGER_LITERAL);
        s = t1.image;
        break;
      case OP_MINUS:
        t1 = jjConsumeToken(OP_MINUS);
        t2 = jjConsumeToken(INTEGER_LITERAL);
        s = t1.image + t2.image;
        break;
      default:
        jjLa1[44] = jjGen;
        jjConsumeToken(-1);
        throw new ParseException();
    }
    return Integer.parseInt(s);
  }

  public final void labelClause(Query query) throws ParseException, InvalidQueryException {
    QueryLabels labels = new QueryLabels();
    AbstractColumn column;
    String label;
    jjConsumeToken(KW_LABEL);
    column = abstractColumnDescriptor();
    label = stringLiteral();
    labels.addLabel(column, label);
    label_5 : while (true) {
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case OP_COMMA:
          break;
        default:
          jjLa1[16] = jjGen;
          break label_5;
      }
      jjConsumeToken(OP_COMMA);
      column = abstractColumnDescriptor();
      label = stringLiteral();
      labels.addLabel(column, label);
    }
    query.setLabels(labels);
  }

  public final void limitClause(Query query) throws ParseException, InvalidQueryException {
    int limit;
    jjConsumeToken(KW_LIMIT);
    limit = integerLiteral();
    query.setRowLimit(limit);
  }

  public final Value literal() throws ParseException, InvalidQueryException {
    Value val;
    String str;
    double num;
    boolean bool;
    String dateStr;
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case STRING_LITERAL:
        str = stringLiteral();
        val = new TextValue(str);
        break;
      case INTEGER_LITERAL:
      case DECIMAL_LITERAL:
      case OP_MINUS:
        num = decimalLiteral();
        val = new NumberValue(num);
        break;
      case KW_TRUE:
      case KW_FALSE:
        bool = booleanLiteral();
        val = BooleanValue.getInstance(bool);
        break;
      case KW_DATE:
        jjConsumeToken(KW_DATE);
        dateStr = stringLiteral();
        val = ParserUtils.stringToDate(dateStr);
        break;
      case KW_TIMEOFDAY:
        jjConsumeToken(KW_TIMEOFDAY);
        dateStr = stringLiteral();
        val = ParserUtils.stringToTimeOfDay(dateStr);
        break;
      case KW_DATETIME:
      case KW_TIMESTAMP:
        switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
          case KW_DATETIME:
            jjConsumeToken(KW_DATETIME);
            break;
          case KW_TIMESTAMP:
            jjConsumeToken(KW_TIMESTAMP);
            break;
          default:
            jjLa1[28] = jjGen;
            jjConsumeToken(-1);
            throw new ParseException();
        }
        dateStr = stringLiteral();
        val = ParserUtils.stringToDatetime(dateStr);
        break;
      default:
        jjLa1[29] = jjGen;
        jjConsumeToken(-1);
        throw new ParseException();
    }
    return val;
  }

  public final RowFilter logicalExpression() throws ParseException, InvalidQueryException {
    RowFilter filter;
    filter = possibleOrExpression();
    return filter;
  }

  public final String nonReservedKeyword() throws ParseException {
    Token t;
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_MIN:
        t = jjConsumeToken(KW_MIN);
        break;
      case KW_MAX:
        t = jjConsumeToken(KW_MAX);
        break;
      case KW_AVG:
        t = jjConsumeToken(KW_AVG);
        break;
      case KW_COUNT:
        t = jjConsumeToken(KW_COUNT);
        break;
      case KW_SUM:
        t = jjConsumeToken(KW_SUM);
        break;
      case KW_NO_VALUES:
        t = jjConsumeToken(KW_NO_VALUES);
        break;
      case KW_NO_FORMAT:
        t = jjConsumeToken(KW_NO_FORMAT);
        break;
      case KW_IS:
        t = jjConsumeToken(KW_IS);
        break;
      case KW_NULL:
        t = jjConsumeToken(KW_NULL);
        break;
      case KW_YEAR:
        t = jjConsumeToken(KW_YEAR);
        break;
      case KW_MONTH:
        t = jjConsumeToken(KW_MONTH);
        break;
      case KW_DAY:
        t = jjConsumeToken(KW_DAY);
        break;
      case KW_HOUR:
        t = jjConsumeToken(KW_HOUR);
        break;
      case KW_MINUTE:
        t = jjConsumeToken(KW_MINUTE);
        break;
      case KW_SECOND:
        t = jjConsumeToken(KW_SECOND);
        break;
      case KW_MILLISECOND:
        t = jjConsumeToken(KW_MILLISECOND);
        break;
      case KW_WITH:
        t = jjConsumeToken(KW_WITH);
        break;
      case KW_CONTAINS:
        t = jjConsumeToken(KW_CONTAINS);
        break;
      case KW_STARTS:
        t = jjConsumeToken(KW_STARTS);
        break;
      case KW_ENDS:
        t = jjConsumeToken(KW_ENDS);
        break;
      case KW_MATCHES:
        t = jjConsumeToken(KW_MATCHES);
        break;
      case KW_LIKE:
        t = jjConsumeToken(KW_LIKE);
        break;
      case KW_NOW:
        t = jjConsumeToken(KW_NOW);
        break;
      case KW_DATEDIFF:
        t = jjConsumeToken(KW_DATEDIFF);
        break;
      case KW_QUARTER:
        t = jjConsumeToken(KW_QUARTER);
        break;
      case KW_LOWER:
        t = jjConsumeToken(KW_LOWER);
        break;
      case KW_UPPER:
        t = jjConsumeToken(KW_UPPER);
        break;
      case KW_DAYOFWEEK:
        t = jjConsumeToken(KW_DAYOFWEEK);
        break;
      case KW_TODATE:
        t = jjConsumeToken(KW_TODATE);
        break;
      default:
        jjLa1[42] = jjGen;
        jjConsumeToken(-1);
        throw new ParseException();
    }
    return t.image;
  }

  public final void offsetClause(Query query) throws ParseException, InvalidQueryException {
    int offset;
    jjConsumeToken(KW_OFFSET);
    offset = integerLiteral();
    query.setRowOffset(offset);
  }

  public final void optionsClause(Query query) throws ParseException {
    QueryOptions queryOptions = new QueryOptions();
    QueryOptionEnum optionEnum;
    jjConsumeToken(KW_OPTIONS);
    label_7 : while (true) {
      optionEnum = queryOption();
      optionEnum.setInQueryOptions(queryOptions);
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case KW_NO_VALUES:
        case KW_NO_FORMAT:
          break;
        default:
          jjLa1[18] = jjGen;
          break label_7;
      }
    }
    query.setOptions(queryOptions);
  }

  public final void orderByClause(Query query) throws ParseException, InvalidQueryException {
    QuerySort sort = new QuerySort();
    AbstractColumn column;
    SortOrder order;
    jjConsumeToken(KW_ORDER);
    jjConsumeToken(KW_BY);
    column = abstractColumnDescriptor();
    order = sortOrder();
    sort.addSort(column, order);
    label_4 : while (true) {
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case OP_COMMA:
          break;
        default:
          jjLa1[15] = jjGen;
          break label_4;
      }
      jjConsumeToken(OP_COMMA);
      column = abstractColumnDescriptor();
      order = sortOrder();
      sort.addSort(column, order);
    }
    query.setSort(sort);
  }

  public final void pivotClause(Query query) throws ParseException, InvalidQueryException {
    QueryPivot pivot = new QueryPivot();
    AbstractColumn column;
    jjConsumeToken(KW_PIVOT);
    column = abstractColumnDescriptor();
    pivot.addColumn(column);
    label_3 : while (true) {
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case OP_COMMA:
          break;
        default:
          jjLa1[14] = jjGen;
          break label_3;
      }
      jjConsumeToken(OP_COMMA);
      column = abstractColumnDescriptor();
      pivot.addColumn(column);
    }
    query.setPivot(pivot);
  }

  public final RowFilter possibleAndExpression() throws ParseException, InvalidQueryException {
    ArrayList<RowFilter> subFilters = new ArrayList<RowFilter>();
    RowFilter filter;
    filter = possibleNotExpression();
    subFilters.add(filter);
    label_9 : while (true) {
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case KW_AND:
          break;
        default:
          jjLa1[20] = jjGen;
          break label_9;
      }
      jjConsumeToken(KW_AND);
      filter = possibleNotExpression();
      subFilters.add(filter);
    }
    if (subFilters.size() == 1) {
      return subFilters.get(0);
    } else {
      return new CompoundFilter(CompoundFilter.LogicalOperator.AND,
          GenericsHelper.makeTypedList(subFilters));
    }
  }

  public final AbstractColumn possibleFirstOrderArithmeticExpression() throws ParseException,
      InvalidQueryException {
    AbstractColumn column;
    AbstractColumn column1;
    column = atomicAbstractColumnDescriptor();
    label_12 : while (true) {
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case OP_ASTERISK:
        case OP_SLASH:
        case OP_MODULO:
          break;
        default:
          jjLa1[37] = jjGen;
          break label_12;
      }
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case OP_ASTERISK:
          jjConsumeToken(OP_ASTERISK);
          column1 = atomicAbstractColumnDescriptor();
          column =
              new ScalarFunctionColumn(GenericsHelper.makeAbstractColumnList(
                new AbstractColumn[]{column, column1}), Product.getInstance());
          break;
        case OP_SLASH:
          jjConsumeToken(OP_SLASH);
          column1 = atomicAbstractColumnDescriptor();
          column =
              new ScalarFunctionColumn(GenericsHelper.makeAbstractColumnList(
                new AbstractColumn[]{column, column1}),
                  Quotient.getInstance());
          break;
        case OP_MODULO:
          jjConsumeToken(OP_MODULO);
          column1 = atomicAbstractColumnDescriptor();
          column =
              new ScalarFunctionColumn(GenericsHelper.makeAbstractColumnList(
                new AbstractColumn[]{column, column1}),
                  Modulo.getInstance());
          break;
        default:
          jjLa1[38] = jjGen;
          jjConsumeToken(-1);
          throw new ParseException();
      }
    }
    return column;
  }

  public final RowFilter possibleNotExpression() throws ParseException, InvalidQueryException {
    RowFilter subFilter;
    RowFilter filter;
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_NOT:
        jjConsumeToken(KW_NOT);
        subFilter = primaryExpression();
        filter = new NegationFilter(subFilter);
        break;
      case KW_TRUE:
      case KW_FALSE:
      case KW_DATE:
      case KW_TIMEOFDAY:
      case KW_DATETIME:
      case KW_TIMESTAMP:
      case KW_MIN:
      case KW_MAX:
      case KW_AVG:
      case KW_COUNT:
      case KW_SUM:
      case KW_NO_VALUES:
      case KW_NO_FORMAT:
      case KW_IS:
      case KW_NULL:
      case KW_YEAR:
      case KW_MONTH:
      case KW_DAY:
      case KW_HOUR:
      case KW_MINUTE:
      case KW_SECOND:
      case KW_MILLISECOND:
      case KW_WITH:
      case KW_CONTAINS:
      case KW_STARTS:
      case KW_ENDS:
      case KW_MATCHES:
      case KW_LIKE:
      case KW_NOW:
      case KW_DATEDIFF:
      case KW_QUARTER:
      case KW_LOWER:
      case KW_UPPER:
      case KW_DAYOFWEEK:
      case KW_TODATE:
      case ID:
      case INTEGER_LITERAL:
      case DECIMAL_LITERAL:
      case STRING_LITERAL:
      case QUOTED_ID:
      case OP_LPAREN:
      case OP_MINUS:
        subFilter = primaryExpression();
        filter = subFilter;
        break;
      default:
        jjLa1[21] = jjGen;
        jjConsumeToken(-1);
        throw new ParseException();
    }
    return filter;
  }

  public final RowFilter possibleOrExpression() throws ParseException, InvalidQueryException {
    ArrayList<RowFilter> subFilters = new ArrayList<RowFilter>();
    RowFilter filter;
    filter = possibleAndExpression();
    subFilters.add(filter);
    label_8 : while (true) {
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case KW_OR:
          break;
        default:
          jjLa1[19] = jjGen;
          break label_8;
      }
      jjConsumeToken(KW_OR);
      filter = possibleAndExpression();
      subFilters.add(filter);
    }
    if (subFilters.size() == 1) {
      return subFilters.get(0);
    } else {
      return new CompoundFilter(CompoundFilter.LogicalOperator.OR,
            GenericsHelper.makeTypedList(subFilters));
    }
  }

  public final AbstractColumn possibleSecondOrderArithmeticExpression() throws ParseException,
      InvalidQueryException {
    AbstractColumn column;
    AbstractColumn column1;
    column = possibleFirstOrderArithmeticExpression();
    label_11 : while (true) {
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case OP_PLUS:
        case OP_MINUS:
          break;
        default:
          jjLa1[35] = jjGen;
          break label_11;
      }
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case OP_PLUS:
          jjConsumeToken(OP_PLUS);
          column1 = possibleFirstOrderArithmeticExpression();
          column =
              new ScalarFunctionColumn(GenericsHelper.makeAbstractColumnList(
                new AbstractColumn[]{column, column1}), Sum.getInstance());
          break;
        case OP_MINUS:
          jjConsumeToken(OP_MINUS);
          column1 = possibleFirstOrderArithmeticExpression();
          column =
              new ScalarFunctionColumn(GenericsHelper.makeAbstractColumnList(
                new AbstractColumn[]{column, column1}),
                  Difference.getInstance());
          break;
        default:
          jjLa1[36] = jjGen;
          jjConsumeToken(-1);
          throw new ParseException();
      }
    }
    return column;
  }

  public final RowFilter primaryExpression() throws ParseException, InvalidQueryException {
    RowFilter filter;
    if (jj21(2147483647)) {
      filter = primitiveFilter();
    } else {
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case OP_LPAREN:
          jjConsumeToken(OP_LPAREN);
          filter = logicalExpression();
          jjConsumeToken(OP_RPAREN);
          break;
        default:
          jjLa1[22] = jjGen;
          jjConsumeToken(-1);
          throw new ParseException();
      }
    }
    return filter;
  }

  public final RowFilter primitiveFilter() throws ParseException, InvalidQueryException {
    RowFilter filter;
    AbstractColumn col1;
    AbstractColumn col2;
    ComparisonFilter.Operator op;
    Value val;
    if (jj23(2147483647)) {
      val = literal();
      op = comparisonOperator();
      col1 = abstractColumnDescriptor();
      filter = new ColumnValueFilter(col1, val, op, true);
    } else {
      switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
        case KW_TRUE:
        case KW_FALSE:
        case KW_DATE:
        case KW_TIMEOFDAY:
        case KW_DATETIME:
        case KW_TIMESTAMP:
        case KW_MIN:
        case KW_MAX:
        case KW_AVG:
        case KW_COUNT:
        case KW_SUM:
        case KW_NO_VALUES:
        case KW_NO_FORMAT:
        case KW_IS:
        case KW_NULL:
        case KW_YEAR:
        case KW_MONTH:
        case KW_DAY:
        case KW_HOUR:
        case KW_MINUTE:
        case KW_SECOND:
        case KW_MILLISECOND:
        case KW_WITH:
        case KW_CONTAINS:
        case KW_STARTS:
        case KW_ENDS:
        case KW_MATCHES:
        case KW_LIKE:
        case KW_NOW:
        case KW_DATEDIFF:
        case KW_QUARTER:
        case KW_LOWER:
        case KW_UPPER:
        case KW_DAYOFWEEK:
        case KW_TODATE:
        case ID:
        case INTEGER_LITERAL:
        case DECIMAL_LITERAL:
        case STRING_LITERAL:
        case QUOTED_ID:
        case OP_LPAREN:
        case OP_MINUS:
          col1 = abstractColumnDescriptor();
          switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
            case KW_IS:
              jjConsumeToken(KW_IS);
              switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
                case KW_NULL:
                  jjConsumeToken(KW_NULL);
                  filter = new ColumnIsNullFilter(col1);
                  break;
                case KW_NOT:
                  jjConsumeToken(KW_NOT);
                  jjConsumeToken(KW_NULL);
                  filter = new NegationFilter(new ColumnIsNullFilter(col1));
                  break;
                default:
                  jjLa1[23] = jjGen;
                  jjConsumeToken(-1);
                  throw new ParseException();
              }
              break;
            case KW_CONTAINS:
            case KW_STARTS:
            case KW_ENDS:
            case KW_MATCHES:
            case KW_LIKE:
            case OP_EQUALS:
            case OP_NOT_EQUALS:
            case OP_LESS_THAN:
            case OP_LESS_OR_EQUAL:
            case OP_GREATER_THAN:
            case OP_GREATER_OR_EQUAL:
              op = comparisonOperator();
              if (jj22(2147483647)) {
                val = literal();
                filter = new ColumnValueFilter(col1, val, op, false);
              } else {
                switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
                  case KW_TRUE:
                  case KW_FALSE:
                  case KW_DATE:
                  case KW_TIMEOFDAY:
                  case KW_DATETIME:
                  case KW_TIMESTAMP:
                  case KW_MIN:
                  case KW_MAX:
                  case KW_AVG:
                  case KW_COUNT:
                  case KW_SUM:
                  case KW_NO_VALUES:
                  case KW_NO_FORMAT:
                  case KW_IS:
                  case KW_NULL:
                  case KW_YEAR:
                  case KW_MONTH:
                  case KW_DAY:
                  case KW_HOUR:
                  case KW_MINUTE:
                  case KW_SECOND:
                  case KW_MILLISECOND:
                  case KW_WITH:
                  case KW_CONTAINS:
                  case KW_STARTS:
                  case KW_ENDS:
                  case KW_MATCHES:
                  case KW_LIKE:
                  case KW_NOW:
                  case KW_DATEDIFF:
                  case KW_QUARTER:
                  case KW_LOWER:
                  case KW_UPPER:
                  case KW_DAYOFWEEK:
                  case KW_TODATE:
                  case ID:
                  case INTEGER_LITERAL:
                  case DECIMAL_LITERAL:
                  case STRING_LITERAL:
                  case QUOTED_ID:
                  case OP_LPAREN:
                  case OP_MINUS:
                    col2 = abstractColumnDescriptor();
                    filter = new ColumnColumnFilter(col1, col2, op);
                    break;
                  default:
                    jjLa1[24] = jjGen;
                    jjConsumeToken(-1);
                    throw new ParseException();
                }
              }
              break;
            default:
              jjLa1[25] = jjGen;
              jjConsumeToken(-1);
              throw new ParseException();
          }
          break;
        default:
          jjLa1[26] = jjGen;
          jjConsumeToken(-1);
          throw new ParseException();
      }
    }
    return filter;
  }

  public final QueryOptionEnum queryOption() throws ParseException {
    QueryOptionEnum result = null;
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_NO_VALUES:
        jjConsumeToken(KW_NO_VALUES);
        result = QueryOptionEnum.NO_VALUES;
        break;
      case KW_NO_FORMAT:
        jjConsumeToken(KW_NO_FORMAT);
        result = QueryOptionEnum.NO_FORMAT;
        break;
      default:
        jjLa1[30] = jjGen;
        jjConsumeToken(-1);
        throw new ParseException();
    }
    return result;
  }

  public final Query queryStatement() throws ParseException, InvalidQueryException {
    Query query = new Query();
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_SELECT:
        selectClause(query);
        break;
      default:
        jjLa1[0] = jjGen;
    }
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_WHERE:
        whereClause(query);
        break;
      default:
        jjLa1[1] = jjGen;
    }
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_GROUP:
        groupByClause(query);
        break;
      default:
        jjLa1[2] = jjGen;
    }
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_PIVOT:
        pivotClause(query);
        break;
      default:
        jjLa1[3] = jjGen;
    }
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_ORDER:
        orderByClause(query);
        break;
      default:
        jjLa1[4] = jjGen;
    }
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_SKIPPING:
        skippingClause(query);
        break;
      default:
        jjLa1[5] = jjGen;
    }
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_LIMIT:
        limitClause(query);
        break;
      default:
        jjLa1[6] = jjGen;
    }
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_OFFSET:
        offsetClause(query);
        break;
      default:
        jjLa1[7] = jjGen;
    }
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_LABEL:
        labelClause(query);
        break;
      default:
        jjLa1[8] = jjGen;
    }
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_FORMAT:
        formatClause(query);
        break;
      default:
        jjLa1[9] = jjGen;
    }
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_OPTIONS:
        optionsClause(query);
        break;
      default:
        jjLa1[10] = jjGen;
    }
    jjConsumeToken(0);
    return query;
  }

  public void reInit(java.io.InputStream stream) {
    reInit(stream, null);
  }

  public void reInit(InputStream stream, String encoding) {
    try {
      jjInputStream.reInit(stream, encoding, 1, 1);
    } catch (java.io.UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    tokenSource.reInit(jjInputStream);
    token = new Token();
    jjNtk = -1;
    jjGen = 0;
    for (int i = 0; i < 48; i++) {
      jjLa1[i] = -1;
    }
    for (int i = 0; i < jj2rtns.length; i++) {
      jj2rtns[i] = new JJCalls();
    }
  }

  public void reInit(Reader stream) {
    jjInputStream.reInit(stream, 1, 1);
    tokenSource.reInit(jjInputStream);
    token = new Token();
    jjNtk = -1;
    jjGen = 0;
    for (int i = 0; i < 48; i++) {
      jjLa1[i] = -1;
    }
    for (int i = 0; i < jj2rtns.length; i++) {
      jj2rtns[i] = new JJCalls();
    }
  }

  public void reInit(QueryParserTokenManager tm) {
    tokenSource = tm;
    token = new Token();
    jjNtk = -1;
    jjGen = 0;
    for (int i = 0; i < 48; i++) {
      jjLa1[i] = -1;
    }
    for (int i = 0; i < jj2rtns.length; i++) {
      jj2rtns[i] = new JJCalls();
    }
  }

  public final ScalarFunction scalarFunction() throws ParseException {
    ScalarFunction result = null;
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_YEAR:
        jjConsumeToken(KW_YEAR);
        result = TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.YEAR);
        break;
      case KW_MONTH:
        jjConsumeToken(KW_MONTH);
        result = TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.MONTH);
        break;
      case KW_DAY:
        jjConsumeToken(KW_DAY);
        result = TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.DAY);
        break;
      case KW_HOUR:
        jjConsumeToken(KW_HOUR);
        result = TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.HOUR);
        break;
      case KW_MINUTE:
        jjConsumeToken(KW_MINUTE);
        result = TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.MINUTE);
        break;
      case KW_SECOND:
        jjConsumeToken(KW_SECOND);
        result = TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.SECOND);
        break;
      case KW_MILLISECOND:
        jjConsumeToken(KW_MILLISECOND);
        result = TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.MILLISECOND);
        break;
      case KW_NOW:
        jjConsumeToken(KW_NOW);
        result = CurrentDateTime.getInstance();
        break;
      case KW_DATEDIFF:
        jjConsumeToken(KW_DATEDIFF);
        result = DateDiff.getInstance();
        break;
      case KW_LOWER:
        jjConsumeToken(KW_LOWER);
        result = Lower.getInstance();
        break;
      case KW_UPPER:
        jjConsumeToken(KW_UPPER);
        result = Upper.getInstance();
        break;
      case KW_QUARTER:
        jjConsumeToken(KW_QUARTER);
        result = TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.QUARTER);
        break;
      case KW_DAYOFWEEK:
        jjConsumeToken(KW_DAYOFWEEK);
        result = TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.DAY_OF_WEEK);
        break;
      case KW_TODATE:
        jjConsumeToken(KW_TODATE);
        result = ToDate.getInstance();
        break;
      default:
        jjLa1[40] = jjGen;
        jjConsumeToken(-1);
        throw new ParseException();
    }
    return result;
  }

  public final void selectClause(Query query) throws ParseException, InvalidQueryException {
    QuerySelection selection = new QuerySelection();
    AbstractColumn column;
    jjConsumeToken(KW_SELECT);
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_TRUE:
      case KW_FALSE:
      case KW_DATE:
      case KW_TIMEOFDAY:
      case KW_DATETIME:
      case KW_TIMESTAMP:
      case KW_MIN:
      case KW_MAX:
      case KW_AVG:
      case KW_COUNT:
      case KW_SUM:
      case KW_NO_VALUES:
      case KW_NO_FORMAT:
      case KW_IS:
      case KW_NULL:
      case KW_YEAR:
      case KW_MONTH:
      case KW_DAY:
      case KW_HOUR:
      case KW_MINUTE:
      case KW_SECOND:
      case KW_MILLISECOND:
      case KW_WITH:
      case KW_CONTAINS:
      case KW_STARTS:
      case KW_ENDS:
      case KW_MATCHES:
      case KW_LIKE:
      case KW_NOW:
      case KW_DATEDIFF:
      case KW_QUARTER:
      case KW_LOWER:
      case KW_UPPER:
      case KW_DAYOFWEEK:
      case KW_TODATE:
      case ID:
      case INTEGER_LITERAL:
      case DECIMAL_LITERAL:
      case STRING_LITERAL:
      case QUOTED_ID:
      case OP_LPAREN:
      case OP_MINUS:
        column = abstractColumnDescriptor();
        selection.addColumn(column);
        label_1 : while (true) {
          switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
            case OP_COMMA:
              break;
            default:
              jjLa1[11] = jjGen;
              break label_1;
          }
          jjConsumeToken(OP_COMMA);
          column = abstractColumnDescriptor();
          selection.addColumn(column);
        }
        query.setSelection(selection);
        break;
      case OP_ASTERISK:
        jjConsumeToken(OP_ASTERISK);
        break;
      default:
        jjLa1[12] = jjGen;
        jjConsumeToken(-1);
        throw new ParseException();
    }
  }

  public final void skippingClause(Query query) throws ParseException, InvalidQueryException {
    int skipping;
    jjConsumeToken(KW_SKIPPING);
    skipping = integerLiteral();
    query.setRowSkipping(skipping);
  }

  public final SortOrder sortOrder() throws ParseException {
    switch ((jjNtk == -1) ? jjNtk() : jjNtk) {
      case KW_ASC:
        jjConsumeToken(KW_ASC);
        return SortOrder.ASCENDING;
      case KW_DESC:
        jjConsumeToken(KW_DESC);
        return SortOrder.DESCENDING;
      default:
        jjLa1[43] = jjGen;
        return SortOrder.ASCENDING;
    }
  }

  public final String stringLiteral() throws ParseException {
    Token t;
    t = jjConsumeToken(STRING_LITERAL);
    return ParserUtils.stripQuotes(t.image);
  }

  public final void whereClause(Query query) throws ParseException, InvalidQueryException {
    RowFilter filter;
    jjConsumeToken(KW_WHERE);
    filter = logicalExpression();
    query.setFilter(filter);
  }

  private boolean jj21(int xla) {
    jjLa = xla;
    jjLastpos = jjScanpos = token;
    try {
      return !jj31();
    } catch (LookaheadSuccess ls) {
      return true;
    } finally {
      jjSave(0, xla);
    }
  }

  private boolean jj22(int xla) {
    jjLa = xla;
    jjLastpos = jjScanpos = token;
    try {
      return !jj32();
    } catch (LookaheadSuccess ls) {
      return true;
    } finally {
      jjSave(1, xla);
    }
  }

  private boolean jj23(int xla) {
    jjLa = xla;
    jjLastpos = jjScanpos = token;
    try {
      return !jj33();
    } catch (LookaheadSuccess ls) {
      return true;
    } finally {
      jjSave(2, xla);
    }
  }

  private boolean jj24(int xla) {
    jjLa = xla;
    jjLastpos = jjScanpos = token;
    try {
      return !jj34();
    } catch (LookaheadSuccess ls) {
      return true;
    } finally {
      jjSave(3, xla);
    }
  }

  private boolean jj25(int xla) {
    jjLa = xla;
    jjLastpos = jjScanpos = token;
    try {
      return !jj35();
    } catch (LookaheadSuccess ls) {
      return true;
    } finally {
      jjSave(4, xla);
    }
  }

  private boolean jj26(int xla) {
    jjLa = xla;
    jjLastpos = jjScanpos = token;
    try {
      return !jj36();
    } catch (LookaheadSuccess ls) {
      return true;
    } finally {
      jjSave(5, xla);
    }
  }

  private boolean jj31() {
    if (jj3R13()) {
      return true;
    }
    return false;
  }

  private boolean jj32() {
    if (jj3R14()) {
      return true;
    }
    return false;
  }

  private boolean jj33() {
    if (jj3R14()) {
      return true;
    }
    return false;
  }

  private boolean jj34() {
    if (jj3R15()) {
      return true;
    }
    return false;
  }

  private boolean jj35() {
    if (jj3R16()) {
      return true;
    }
    if (jjScanToken(OP_LPAREN)) {
      return true;
    }
    if (jj3R86()) {
      return true;
    }
    if (jjScanToken(OP_RPAREN)) {
      return true;
    }
    return false;
  }

  private boolean jj36() {
    if (jj3R17()) {
      return true;
    }
    if (jjScanToken(OP_LPAREN)) {
      return true;
    }
    Token xsp;
    xsp = jjScanpos;
    if (jj3R87()) {
      jjScanpos = xsp;
    }
    while (true) {
      xsp = jjScanpos;
      if (jj3R88()) {
        jjScanpos = xsp;
        break;
      }
    }
    if (jjScanToken(OP_RPAREN)) {
      return true;
    }
    return false;
  }

  private boolean jj3R13() {
    Token xsp;
    xsp = jjScanpos;
    if (jj3R18()) {
      jjScanpos = xsp;
      if (jj3R19()) {
        return true;
      }
    }
    return false;
  }

  private boolean jj3R14() {
    Token xsp;
    xsp = jjScanpos;
    if (jj3R20()) {
      jjScanpos = xsp;
      if (jj3R21()) {
        jjScanpos = xsp;
        if (jj3R22()) {
          jjScanpos = xsp;
          if (jj3R23()) {
            jjScanpos = xsp;
            if (jj3R24()) {
              jjScanpos = xsp;
              if (jj3R25()) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }

  private boolean jj3R15() {
    if (jj3R26()) {
      return true;
    }
    return false;
  }

  private boolean jj3R16() {
    Token xsp;
    xsp = jjScanpos;
    if (jj3R27()) {
      jjScanpos = xsp;
      if (jj3R28()) {
        jjScanpos = xsp;
        if (jj3R29()) {
          jjScanpos = xsp;
          if (jj3R30()) {
            jjScanpos = xsp;
            if (jj3R31()) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private boolean jj3R17() {
    Token xsp;
    xsp = jjScanpos;
    if (jj3R32()) {
      jjScanpos = xsp;
      if (jj3R33()) {
        jjScanpos = xsp;
        if (jj3R34()) {
          jjScanpos = xsp;
          if (jj3R35()) {
            jjScanpos = xsp;
            if (jj3R36()) {
              jjScanpos = xsp;
              if (jj3R37()) {
                jjScanpos = xsp;
                if (jj3R38()) {
                  jjScanpos = xsp;
                  if (jj3R39()) {
                    jjScanpos = xsp;
                    if (jj3R40()) {
                      jjScanpos = xsp;
                      if (jj3R41()) {
                        jjScanpos = xsp;
                        if (jj3R42()) {
                          jjScanpos = xsp;
                          if (jj3R43()) {
                            jjScanpos = xsp;
                            if (jj3R44()) {
                              jjScanpos = xsp;
                              if (jj3R45()) {
                                return true;
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return false;
  }

  private boolean jj3R18() {
    if (jj3R14()) {
      return true;
    }
    if (jj3R46()) {
      return true;
    }
    if (jj3R47()) {
      return true;
    }
    return false;
  }

  private boolean jj3R19() {
    if (jj3R47()) {
      return true;
    }
    Token xsp;
    xsp = jjScanpos;
    if (jj3R48()) {
      jjScanpos = xsp;
      if (jj3R49()) {
        return true;
      }
    }
    return false;
  }

  private boolean jj3R20() {
    if (jj3R50()) {
      return true;
    }
    return false;
  }

  private boolean jj3R21() {
    if (jj3R51()) {
      return true;
    }
    return false;
  }

  private boolean jj3R22() {
    if (jj3R52()) {
      return true;
    }
    return false;
  }

  private boolean jj3R23() {
    if (jjScanToken(KW_DATE)) {
      return true;
    }
    if (jj3R50()) {
      return true;
    }
    return false;
  }

  private boolean jj3R24() {
    if (jjScanToken(KW_TIMEOFDAY)) {
      return true;
    }
    if (jj3R50()) {
      return true;
    }
    return false;
  }

  private boolean jj3R25() {
    Token xsp;
    xsp = jjScanpos;
    if (jjScanToken(26)) {
      jjScanpos = xsp;
      if (jjScanToken(27)) {
        return true;
      }
    }
    if (jj3R50()) {
      return true;
    }
    return false;
  }

  private boolean jj3R26() {
    if (jj3R53()) {
      return true;
    }
    Token xsp;
    while (true) {
      xsp = jjScanpos;
      if (jj3R54()) {
        jjScanpos = xsp;
        break;
      }
    }
    return false;
  }

  private boolean jj3R27() {
    if (jjScanToken(KW_MIN)) {
      return true;
    }
    return false;
  }

  private boolean jj3R28() {
    if (jjScanToken(KW_MAX)) {
      return true;
    }
    return false;
  }

  private boolean jj3R29() {
    if (jjScanToken(KW_COUNT)) {
      return true;
    }
    return false;
  }

  private boolean jj3R30() {
    if (jjScanToken(KW_AVG)) {
      return true;
    } 
    return false;
  }

  private boolean jj3R31() {
    if (jjScanToken(KW_SUM)) {
      return true;
    }
    return false;
  }

  private boolean jj3R32() {
    if (jjScanToken(KW_YEAR)) {
      return true;
    }
    return false;
  }

  private boolean jj3R33() {
    if (jjScanToken(KW_MONTH)) {
      return true;
    }
    return false;
  }

  private boolean jj3R34() {
    if (jjScanToken(KW_DAY)) {
      return true;
    }
    return false;
  }

  private boolean jj3R35() {
    if (jjScanToken(KW_HOUR)) {
      return true;
    }
    return false;
  }

  private boolean jj3R36() {
    if (jjScanToken(KW_MINUTE)) {
      return true;
    }
    return false;
  }

  private boolean jj3R37() {
    if (jjScanToken(KW_SECOND)) {
      return true;
    }
    return false;
  }

  private boolean jj3R38() {
    if (jjScanToken(KW_MILLISECOND)) {
      return true;
    }
    return false;
  }

  private boolean jj3R39() {
    if (jjScanToken(KW_NOW)) {
      return true;
    }
    return false;
  }

  private boolean jj3R40() {
    if (jjScanToken(KW_DATEDIFF)) {
      return true;
    }
    return false;
  }

  private boolean jj3R41() {
    if (jjScanToken(KW_LOWER)) {
      return true;
    }
    return false;
  }

  private boolean jj3R42() {
    if (jjScanToken(KW_UPPER)) {
      return true;
    }
    return false;
  }

  private boolean jj3R43() {
    if (jjScanToken(KW_QUARTER)) {
      return true;
    }
    return false;
  }

  private boolean jj3R44() {
    if (jjScanToken(KW_DAYOFWEEK)) {
      return true;
    }
    return false;
  }

  private boolean jj3R45() {
    if (jjScanToken(KW_TODATE)) {
      return true;
    }
    return false;
  }

  private boolean jj3R46() {
    Token xsp;
    xsp = jjScanpos;
    if (jj3R55()) {
      jjScanpos = xsp;
      if (jj3R56()) {
        jjScanpos = xsp;
        if (jj3R57()) {
          jjScanpos = xsp;
          if (jj3R58()) {
            jjScanpos = xsp;
            if (jj3R59()) {
              jjScanpos = xsp;
              if (jj3R60()) {
                jjScanpos = xsp;
                if (jj3R61()) {
                  jjScanpos = xsp;
                  if (jj3R62()) {
                    jjScanpos = xsp;
                    if (jj3R63()) {
                      jjScanpos = xsp;
                      if (jj3R64()) {
                        jjScanpos = xsp;
                        if (jj3R65()) {
                          return true;
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return false;
  }

  private boolean jj3R47() {
    Token xsp;
    xsp = jjScanpos;
    if (jj34()) {
      jjScanpos = xsp;
      if (jj3R66()) {
        return true;
      }
    }
    return false;
  }

  private boolean jj3R48() {
    if (jjScanToken(KW_IS)) {
      return true;
    }
    Token xsp;
    xsp = jjScanpos;
    if (jj3R67()) {
      jjScanpos = xsp;
      if (jj3R68()) {
        return true;
      }
    }
    return false;
  }

  private boolean jj3R49() {
    if (jj3R46()) {
      return true;
    }
    Token xsp;
    xsp = jjScanpos;
    if (jj3R69()) {
      jjScanpos = xsp;
      if (jj3R70()) {
        return true;
      }
    }
    return false;
  }

  private boolean jj3R50() {
    if (jjScanToken(STRING_LITERAL)) {
      return true;
    }
    return false;
  }

  private boolean jj3R51() {
    Token xsp;
    xsp = jjScanpos;
    if (jj3R71()) {
      jjScanpos = xsp;
      if (jj3R72()) {
        jjScanpos = xsp;
        if (jj3R73()) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean jj3R52() {
    Token xsp;
    xsp = jjScanpos;
    if (jj3R74()) {
      jjScanpos = xsp;
      if (jj3R75()) {
        return true;
      }
    }
    return false;
  }

  private boolean jj3R53() {
    if (jj3R76()) {
      return true;
    }
    Token xsp;
    while (true) {
      xsp = jjScanpos;
      if (jj3R77()) {
        jjScanpos = xsp;
        break;
      }
    }
    return false;
  }
  private boolean jj3R54() {
    Token xsp;
    xsp = jjScanpos;
    if (jj3R78()) {
      jjScanpos = xsp;
      if (jj3R79()) {
        return true;
      }
    }
    return false;
  }
  private boolean jj3R55() {
    if (jjScanToken(OP_EQUALS)) {
      return true;
    }
    return false;
  }
  private boolean jj3R56() {
    if (jjScanToken(OP_NOT_EQUALS)) {
      return true;
    }
    return false;
  }
  private boolean jj3R57() {
    if (jjScanToken(OP_LESS_THAN)) {
      return true;
    }
    return false;
  }
  
  private boolean jj3R58() {
    if (jjScanToken(OP_LESS_OR_EQUAL)) {
      return true;
    }
    return false;
  }
  private boolean jj3R59() {
    if (jjScanToken(OP_GREATER_THAN)) {
      return true;
    }
    return false;
  }
  private boolean jj3R60() {
    if (jjScanToken(OP_GREATER_OR_EQUAL)) {
      return true;
    }
    return false;
  }
  private boolean jj3R61() {
    if (jjScanToken(KW_CONTAINS)) {
      return true;
    }
    return false;
  }
  private boolean jj3R62() {
    if (jjScanToken(KW_STARTS)) {
      return true;
    }
    if (jjScanToken(KW_WITH)) {
      return true;
    }
    return false;
  }
  private boolean jj3R63() {
    if (jjScanToken(KW_ENDS)) {
      return true;
    }
    if (jjScanToken(KW_WITH)) {
      return true;
    }
    return false;
  }
  private boolean jj3R64() {
    if (jjScanToken(KW_MATCHES)) {
      return true;
    }
    return false;
  }
  private boolean jj3R65() {
    if (jjScanToken(KW_LIKE)) {
      return true;
    }
    return false;
  }

  private boolean jj3R66() {
    if (jj3R76()) {
      return true;
    }
    return false;
  }

  private boolean jj3R67() {
    if (jjScanToken(KW_NULL)) {
      return true;
    }
    return false;
  }

  private boolean jj3R68() {
    if (jjScanToken(KW_NOT)) {
      return true;
    }
    if (jjScanToken(KW_NULL)) {
      return true;
    }
    return false;
  }

  private boolean jj3R69() {
    if (jj3R14()) {
      return true;
    }
    return false;
  }
  private boolean jj3R70() {
    if (jj3R47()) {
      return true;
    }
    return false;
  }
  private boolean jj3R71() {
    if (jjScanToken(DECIMAL_LITERAL)) {
      return true;
    }
    return false;
  }

  private boolean jj3R72() {
    if (jjScanToken(INTEGER_LITERAL)) {
      return true;
    }
    return false;
  }

  private boolean jj3R73() {
    if (jjScanToken(OP_MINUS)) {
      return true;
    }
    Token xsp;
    xsp = jjScanpos;
    if (jjScanToken(59)) {
      jjScanpos = xsp;
      if (jjScanToken(58)) {
        return true;
      }
    }
    return false;
  }

  private boolean jj3R74() {
    if (jjScanToken(KW_TRUE)) {
      return true;
    }
    return false;
  }

  private boolean jj3R75() {
    if (jjScanToken(KW_FALSE)) {
      return true;
    }
    return false;
  }

  private boolean jj3R76() {
    Token xsp;
    xsp = jjScanpos;
    if (jj35()) {
      jjScanpos = xsp;
      if (jj36()) {
        jjScanpos = xsp;
        if (jj3R80()) {
          jjScanpos = xsp;
          if (jj3R81()) {
            jjScanpos = xsp;
            if (jj3R82()) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private boolean jj3R77() {
    Token xsp;
    xsp = jjScanpos;
    if (jj3R83()) {
      jjScanpos = xsp;
      if (jj3R84()) {
        jjScanpos = xsp;
        if (jj3R85()) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean jj3R78() {
    if (jjScanToken(OP_PLUS)) {
      return true;
    }
    if (jj3R53()) {
      return true;
    }
    return false;
  }

  private boolean jj3R79() {
    if (jjScanToken(OP_MINUS)) {
      return true;
    }
    if (jj3R53()) {
      return true;
    }
    return false;
  }

  private boolean jj3R80() {
    if (jjScanToken(OP_LPAREN)) {
      return true;
    }
    if (jj3R47()) {
      return true;
    }
    if (jjScanToken(OP_RPAREN)) {
      return true;
    }
    return false;
  }

  private boolean jj3R81() {
    if (jj3R14()) {
      return true;
    }
    return false;
  }

  private boolean jj3R82() {
    if (jj3R86()) {
      return true;
    }
    return false;
  }

  private boolean jj3R83() {
    if (jjScanToken(OP_ASTERISK)) {
      return true;
    }
    if (jj3R76()) {
      return true;
    }
    return false;
  }

  private boolean jj3R84() {
    if (jjScanToken(OP_SLASH)) {
      return true;
    }
    if (jj3R76()) {
      return true;
    }
    return false;
  }

  private boolean jj3R85() {
    if (jjScanToken(OP_MODULO)) {
      return true;
    }
    if (jj3R76()) {
      return true;
    }
    return false;
  }

  private boolean jj3R86() {
    Token xsp;
    xsp = jjScanpos;
    if (jj3R89()) {
      jjScanpos = xsp;
      if (jj3R90()) {
        jjScanpos = xsp;
        if (jj3R91()) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean jj3R87() {
    if (jj3R47()) {
      return true;
    }
    return false;
  }
  private boolean jj3R88() {
    if (jjScanToken(OP_COMMA)) {
      return true;
    }
    if (jj3R47()) {
      return true;
    }
    return false;
  }
  private boolean jj3R89() {
    if (jjScanToken(ID)) {
      return true;
    }
    return false;
  }
  private boolean jj3R90() {
    if (jjScanToken(QUOTED_ID)) {
      return true;
    }
    return false;
  }
  private boolean jj3R91() {
    if (jj3R92()) {
      return true;
    }
    return false;
  }

  private boolean jj3R92() {
    Token xsp;
    xsp = jjScanpos;
    if (jjScanToken(28)) {
      jjScanpos = xsp;
      if (jjScanToken(29)) {
        jjScanpos = xsp;
        if (jjScanToken(30)) {
          jjScanpos = xsp;
          if (jjScanToken(31)) {
            jjScanpos = xsp;
            if (jjScanToken(32)) {
              jjScanpos = xsp;
              if (jjScanToken(33)) {
                jjScanpos = xsp;
                if (jjScanToken(34)) {
                  jjScanpos = xsp;
                  if (jjScanToken(35)) {
                    jjScanpos = xsp;
                    if (jjScanToken(36)) {
                      jjScanpos = xsp;
                      if (jjScanToken(37)) {
                        jjScanpos = xsp;
                        if (jjScanToken(38)) {
                          jjScanpos = xsp;
                          if (jjScanToken(39)) {
                            jjScanpos = xsp;
                            if (jjScanToken(40)) {
                              jjScanpos = xsp;
                              if (jjScanToken(41)) {
                                jjScanpos = xsp;
                                if (jjScanToken(42)) {
                                  jjScanpos = xsp;
                                  if (jjScanToken(43)) {
                                    jjScanpos = xsp;
                                    if (jjScanToken(44)) {
                                      jjScanpos = xsp;
                                      if (jjScanToken(45)) {
                                        jjScanpos = xsp;
                                        if (jjScanToken(46)) {
                                          jjScanpos = xsp;
                                          if (jjScanToken(47)) {
                                            jjScanpos = xsp;
                                            if (jjScanToken(48)) {
                                              jjScanpos = xsp;
                                              if (jjScanToken(49)) {
                                                jjScanpos = xsp;
                                                if (jjScanToken(50)) {
                                                  jjScanpos = xsp;
                                                  if (jjScanToken(51)) {
                                                    jjScanpos = xsp;
                                                    if (jjScanToken(52)) {
                                                      jjScanpos = xsp;
                                                      if (jjScanToken(53)) {
                                                        jjScanpos = xsp;
                                                        if (jjScanToken(54)) {
                                                          jjScanpos = xsp;
                                                          if (jjScanToken(55)) {
                                                            jjScanpos = xsp;
                                                            if (jjScanToken(56)) {
                                                              return true;
                                                            }
                                                          }
                                                        }
                                                      }
                                                    }
                                                  }
                                                }
                                              }
                                            }
                                          }
                                        }
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return false;
  }

  private void jjAddErrorToken(int kind, int pos) {
    if (pos >= 100) {
      return;
    }
    if (pos == jjEndpos + 1) {
      jjLasttokens[jjEndpos++] = kind;
    } else if (jjEndpos != 0) {
      jjExpentry = new int[jjEndpos];
      for (int i = 0; i < jjEndpos; i++) {
        jjExpentry[i] = jjLasttokens[i];
      }
      jj_entries_loop : for (Iterator<int[]> it = jjExpentries.iterator(); it.hasNext();) {
        int[] oldentry = it.next();
        if (oldentry.length == jjExpentry.length) {
          for (int i = 0; i < jjExpentry.length; i++) {
            if (oldentry[i] != jjExpentry[i]) {
              continue jj_entries_loop;
            }
          }
          jjExpentries.add(jjExpentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) {
        jjLasttokens[(jjEndpos = pos) - 1] = kind;
      }
    }
  }

  private Token jjConsumeToken(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) {
      token = token.next;
    } else {
      token = token.next = tokenSource.getNextToken();
    }
    jjNtk = -1;
    if (token.kind == kind) {
      jjGen++;
      if (++jjGc > 100) {
        jjGc = 0;
        for (int i = 0; i < jj2rtns.length; i++) {
          JJCalls c = jj2rtns[i];
          while (c != null) {
            if (c.gen < jjGen) {
              c.first = null;
            }
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jjKind = kind;
    throw generateParseException();
  }

  private int jjNtk() {
    if ((jjNt = token.next) == null) {
      return (jjNtk = (token.next = tokenSource.getNextToken()).kind);
    } else {
      return (jjNtk = jjNt.kind);
    }
  }

  private void jjRescanToken() {
    jjRescan = true;
    for (int i = 0; i < 6; i++) {
      try {
        JJCalls p = jj2rtns[i];
        do {
          if (p.gen > jjGen) {
            jjLa = p.arg;
            jjLastpos = jjScanpos = p.first;
            switch (i) {
              case 0:
                jj31();
                break;
              case 1:
                jj32();
                break;
              case 2:
                jj33();
                break;
              case 3:
                jj34();
                break;
              case 4:
                jj35();
                break;
              case 5:
                jj36();
                break;
            }
          }
          p = p.next;
        } while (p != null);
      } catch (LookaheadSuccess ls) {
      }
    }
    jjRescan = false;
  }

  private void jjSave(int index, int xla) {
    JJCalls p = jj2rtns[index];
    while (p.gen > jjGen) {
      if (p.next == null) {
        p = p.next = new JJCalls();
        break;
      }
      p = p.next;
    }
    p.gen = jjGen + xla - jjLa;
    p.first = token;
    p.arg = xla;
  }

  private boolean jjScanToken(int kind) {
    if (jjScanpos == jjLastpos) {
      jjLa--;
      if (jjScanpos.next == null) {
        jjLastpos = jjScanpos = jjScanpos.next = tokenSource.getNextToken();
      } else {
        jjLastpos = jjScanpos = jjScanpos.next;
      }
    } else {
      jjScanpos = jjScanpos.next;
    }
    if (jjRescan) {
      int i = 0;
      Token tok = token;
      while (tok != null && tok != jjScanpos) {
        i++;
        tok = tok.next;
      }
      if (tok != null) {
        jjAddErrorToken(kind, i);
      }
    }
    if (jjScanpos.kind != kind) {
      return true;
    }
    if (jjLa == 0 && jjScanpos == jjLastpos) {
      throw jjLs;
    }
    return false;
  }
}
