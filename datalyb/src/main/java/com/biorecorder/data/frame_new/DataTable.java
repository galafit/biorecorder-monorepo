package com.biorecorder.data.frame_new;

import java.util.ArrayList;
import java.util.List;

public class DataTable {
    private String name;
    private List<Column> columns = new ArrayList<>();

    public DataTable() {
        this("DataTable");
    }

    public DataTable(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public Column getColumn(int index) {
        return columns.get(index);
    }

    public void removeColumn(int columnNumber) {
        columns.remove(columnNumber);
    }

    public void addColumn(Column column) {
        columns.add(column);
    }

    public void addColumn(int position, Column col) {
        columns.add(position, col);
    }

    public int rowCount() {
        if(columns.size() > 0) {
          return columns.get(0).size();
        }
        return 0;
    }

    public int columnCount() {
        return columns.size();
    }

    public double value(int rowNumber, int columnNumber) {
        return columns.get(columnNumber).value(rowNumber);
    }

    public String label(int rowNumber, int columnNumber) {
        return columns.get(columnNumber).label(rowNumber);
    }

    /**
     * Binary search algorithm. The column data must be sorted!
     * Find the index of the <b>value</b> in the given column. If the column containsInt
     * multiple elements equal to the searched <b>value</b>, there is no guarantee which
     * one will be found. If there is no element equal to the searched value function returns
     * the insertion point for <b>value</b> in the column to maintain sorted order
     * (i.e. index of the first element in the column which is bigger than the searched value).
     *
     * @param sorter - Default null.
     *               Optional array of integer indices that sortedIndices column data
     *               into ascending order (if data column itself is not sorted).
     *               They are typically the result of {@link #sortedIndices(int)}
     */
    public int bisect(int columnNumber, double value, int[] sorter) {
        Column column = columns.get(columnNumber);
        if (sorter != null) {
            column = column.view(sorter);
        }
        int length1 = rowCount();
        return column.bisect(value);
    }


    /**
     * This method returns a sorted view of the data frame
     * without modifying the order of the underlying data.
     * (like JTable sortedIndices in java)
     */
    public DataTable sort(int sortColumn) {
        return view(sortedIndices(sortColumn));
    }

    /**
     * This method returns an array of row numbers (indices)
     * which represent sorted version (view) of the given column.
     * (Similar to numpy.argsort or google chart DataTable.getSortedRows -
     * https://developers.google.com/chart/interactive/docs/reference#DataTable,)
     *
     * @return array of sorted rows (indices) for the given column.
     */
    public int[] sortedIndices(int sortColumn) {
        boolean isParallel = false;
        return columns.get(sortColumn).sort(isParallel);
    }

    public DataTable view(int[] rowOrder) {
        DataTable resultantTable = new DataTable(name);
        for (int i = 0; i < columns.size(); i++) {
            resultantTable.columns.add(columns.get(i).view(rowOrder));
        }
        return resultantTable;
    }

    public DataTable append(DataTable tableToAppend) throws IllegalArgumentException {
        if(!isCompatible(this, tableToAppend)) {
            String msg = "Table to append is incompatible";
            throw new IllegalArgumentException(msg);
        }
        for (int i = 0; i < tableToAppend.columnCount(); i++) {
            columns.get(i).append(tableToAppend.columns.get(i));
        }
        return this;
    }

    public static boolean isCompatible(DataTable table1, DataTable table2) {
        if(table1.columnCount() != table2.columnCount()) {
            return false;
        }
        for (int i = 0; i < table1.columnCount(); i++) {
            if(table1.columns.get(i).type() != table2.columns.get(i).type()) {
                return false;
            }
        }
        return true;
    }

}
