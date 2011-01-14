package com.butent.bee.egg.server.datasource.util;

import com.google.common.collect.Lists;

import com.butent.bee.egg.server.datasource.base.DataSourceException;
import com.butent.bee.egg.server.datasource.base.ReasonType;
import com.butent.bee.egg.server.datasource.base.TypeMismatchException;
import com.butent.bee.egg.server.datasource.datatable.ColumnDescription;
import com.butent.bee.egg.server.datasource.datatable.DataTable;
import com.butent.bee.egg.server.datasource.datatable.TableRow;
import com.butent.bee.egg.server.datasource.datatable.ValueFormatter;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.value.Value;
import com.butent.bee.egg.shared.data.value.ValueType;

import com.ibm.icu.util.ULocale;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

public class CsvDataSourceHelper {

  public static Reader getCsvFileReader(String file) throws DataSourceException {
    Reader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      throw new DataSourceException(ReasonType.INVALID_REQUEST,
          "Couldn't read csv file from: " + file);
    }
    return reader;
  }

  public static Reader getCsvUrlReader(String url) throws DataSourceException {
    Reader reader;
    try {
      reader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"));
    } catch (MalformedURLException e) {
      throw new DataSourceException(ReasonType.INVALID_REQUEST, "url is malformed: " + url);
    } catch (IOException e) {
      throw new DataSourceException(ReasonType.INVALID_REQUEST,
          "Couldn't read csv file from url: " + url);
    }
    return reader;
  }

  public static DataTable read(Reader reader, List<ColumnDescription> columnDescriptions,
      Boolean headerRow)
      throws IOException, CsvDataSourceException {
    return read(reader, columnDescriptions, headerRow, null);
  }

  public static DataTable read(Reader reader, List<ColumnDescription> columnDescriptions,
      Boolean headerRow, ULocale locale)
      throws IOException, CsvDataSourceException {
    DataTable dataTable = new DataTable();

    if (reader == null) {
      return dataTable;
    }

    CSVReader csvReader = new CSVReader(reader);
    Map<ValueType, ValueFormatter> defaultFormatters =
        ValueFormatter.createDefaultFormatters(locale);

    String[] line;
    boolean firstLine = true;
    while ((line = csvReader.readNext()) != null) {
      if ((line.length == 1) && (line[0].equals(""))) {
        continue;
      }

      if ((columnDescriptions != null) && (line.length != columnDescriptions.size())) {
        throw new CsvDataSourceException(ReasonType.INTERNAL_ERROR,
            "Wrong number of columns in the data.");
      }
      if (firstLine) {
        if (columnDescriptions == null) {
          columnDescriptions = Lists.newArrayList();
        }

        List<ColumnDescription> tempColumnDescriptions = new ArrayList<ColumnDescription>();

        for (int i = 0; i < line.length; i++) {
          ColumnDescription tempColumnDescription =
              (columnDescriptions.isEmpty() || columnDescriptions.get(i) == null)
              ? null : columnDescriptions.get(i);

          String id =
              ((tempColumnDescription == null) || (tempColumnDescription.getId() == null))
              ? "Col" + (i) : tempColumnDescription.getId();
          ValueType type =
              ((tempColumnDescription == null) || (tempColumnDescription.getType() == null))
              ? ValueType.TEXT : tempColumnDescription.getType();
          String label =
              ((tempColumnDescription == null) || (tempColumnDescription.getLabel() == null))
              ? "Column" + i : tempColumnDescription.getLabel();
          String pattern =
              ((tempColumnDescription == null) || (tempColumnDescription.getPattern() == null))
              ? "" : tempColumnDescription.getPattern();

          tempColumnDescription = new ColumnDescription(id, type, label);
          tempColumnDescription.setPattern(pattern);
          tempColumnDescriptions.add(tempColumnDescription);
        }

        if (headerRow) {
          for (int i = 0; i < line.length; i++) {
            String string = line[i];
            if (string == null) {
              tempColumnDescriptions.get(i).setLabel("");
            } else {
              tempColumnDescriptions.get(i).setLabel(line[i].trim());
            }
          }
        }

        columnDescriptions = tempColumnDescriptions;
        dataTable = new DataTable();
        dataTable.addColumns(columnDescriptions);
      }
      if (!(firstLine && headerRow)) {
        TableRow tableRow = new TableRow();
        for (int i = 0; i < line.length; i++) {
          ColumnDescription columnDescription = columnDescriptions.get(i);
          ValueType valueType = columnDescription.getType();
          String string = line[i];
          if (string != null) {
            string = string.trim();
          }
          String pattern = columnDescription.getPattern();
          ValueFormatter valueFormatter;
          if (pattern == null || pattern.equals("")) {
            valueFormatter = defaultFormatters.get(valueType);
          } else {
            valueFormatter = ValueFormatter.createFromPattern(valueType, pattern, locale);
          }
          Value value = valueFormatter.parse(string);
          
          tableRow.addCell(value);
        }
        try {
          dataTable.addRow(tableRow);
        } catch (TypeMismatchException e) {
          Assert.untouchable();
        }
      }

      firstLine = false;
    }

    return dataTable;
  }

  private CsvDataSourceHelper() {
  }
}
