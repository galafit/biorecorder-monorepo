package com.biorecorder.datalyb.datatable;

import java.util.ArrayList;
import java.util.List;

public class DataTable {
    private String name;
    private List<Column> columns = new ArrayList<>();
    private volatile int size = 0;

    public DataTable(String name) {
        this.name = name;
    }

    public DataTable(String name, Column... columns) {
        this.name = name;
        for (Column col : columns) {
            this.columns.add(col);
        }
        size = calculateSize(this.columns);
    }

    private static int calculateSize(List<Column> columns) {
        if(columns.size() == 0) {
            return 0;
        }
        int minColSize = Integer.MAX_VALUE;
        for (Column col : columns) {
            minColSize = Math.min(minColSize, col.size());
        }
        return minColSize;
    }

    public void updateSize() {
        size = calculateSize(columns);
    }

    public void clear() throws UnsupportedOperationException {
        for (Column column : columns) {
           column.clear();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void removeColumn(int columnNumber) {
        columns.remove(columnNumber);
        size = calculateSize(columns);
    }

    public void addColumns(Column... columns) {
        for (Column col : columns) {
            this.columns.add(col);
        }
        size = calculateSize(this.columns);
    }


    public Column getColumn(int index) {
        return columns.get(index);
    }


    public int rowCount() {
        return size;
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
        return column.bisect(value, 0, size);
    }

    public int bisectLeft(int columnNumber, double value) {
        Column column = columns.get(columnNumber);
        return column.bisectLeft(value, 0, size);
    }

    public int bisectRight(int columnNumber, double value) {
        Column column = columns.get(columnNumber);
        return column.bisectRight(value, 0, size);
    }

    public String getColumnName(int columnNumber) {
        return columns.get(columnNumber).name();
    }

    public double[] minMax(int columnNumber) {
        if(size == 0) {
            return null;
        }
        return columns.get(columnNumber).minMax(0, size);
    }

    /**
     * This method returns a sorted view of the data frame
     * without modifying the order of the underlying data.
     * (like JTable sortedIndices in java)
     */
    public DataTable sort(int columnNumber) {
        return view(sortedIndices(columnNumber));
    }

    /**
     * This method returns an array of row numbers (indices)
     * which represent sorted version (view) of the given column.
     * (Similar to numpy.argsort or google chart DataTable.getSortedRows -
     * https://developers.google.com/chart/interactive/docs/reference#DataTable,)
     *
     * @return array of sorted rows (indices) for the given column.
     */
    public int[] sortedIndices(int sortColumnNumber) {
        boolean isParallel = false;
        return columns.get(sortColumnNumber).sort( 0, size, isParallel);
    }

    public DataTable view(int[] rowOrder) {
        DataTable resultantTable = new DataTable(name);
        for (int i = 0; i < columns.size(); i++) {
            resultantTable.addColumns(columns.get(i).view(rowOrder));
        }
        return resultantTable;
    }

    public DataTable view(int from, int length) {
        DataTable resultantTable = new DataTable(name);
        for (int i = 0; i < columns.size(); i++) {
            resultantTable.addColumns(columns.get(i).view(from, length));
        }
        return resultantTable;
    }

    /**
     * This method do not change the original table neither tableToAppend!
     * But create a new table and put in it joined data from both tables
     */
    public DataTable append(DataTable tableToAppend) throws IllegalArgumentException {
        if(!isCompatible(this, tableToAppend)) {
            String msg = "Table to append is incompatible";
            throw new IllegalArgumentException(msg);
        }
        DataTable resultantTable = new DataTable(name);
        for (int i = 0; i < tableToAppend.columnCount(); i++) {
            resultantTable.addColumns(columns.get(i).append(0, size, tableToAppend.getColumn(i), 0, tableToAppend.rowCount()));
        }
        return resultantTable;
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
