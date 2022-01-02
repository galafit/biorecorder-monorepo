package com.biorecorder.datalyb.datatable;

import java.util.ArrayList;
import java.util.List;


public class Accumulator {
    private DataTable resultantTable;
    List<Setter> setters;

    public DataTable append(Row row) {
        if(resultantTable == null) {
            createDataTable(row);
        }
        for (Setter setter : setters) {
            setter.setData(row);
        }
        resultantTable.updateSize();
        return resultantTable;
    }

    private void createDataTable(Row row) {
        resultantTable = new DataTable("");
        setters = new ArrayList(row.columnCount());
        for (int i = 0; i < row.columnCount(); i++) {
            Column col = row.getColumn(i);
            BaseType type = col.type();
            String name = col.name();
            if(col instanceof RegularColumn) {
                RegularColumn rc = (RegularColumn) col;
                resultantTable.addColumns(new RegularColumn(rc.name(), rc.startValue(), rc.step()));
            } else {

                final int colNumber = i;
                switch (type) {
                    case INT: {
                        final IntColumn ic = new IntColumn(name);
                        resultantTable.addColumns(ic);
                        Setter setter = new Setter() {
                            @Override
                            public void setData(Row row) {
                               ic.append(row.getInt(colNumber));
                            }
                        };
                        setters.add(setter);
                        break;
                    }
                    case DOUBLE: {
                        final DoubleColumn dc = new DoubleColumn(name);
                        resultantTable.addColumns(dc);
                        Setter setter = new Setter() {
                            @Override
                            public void setData(Row row) {
                                dc.append(row.getDouble(colNumber));
                            }
                        };
                        setters.add(setter);
                        break;
                    }
                }
            }
        }
    }

    public void clear() {
       resultantTable.clear();
    }

    interface Setter {
        void setData(Row row);
    }

    public static void main(String[] args) {
        RegularColumn c0 = new RegularColumn("reg", 2, 2, 2);
        int[] intArr = {1, 2, 3};
        double[] doubleArr = {0.5, 1.5, 2.5};
        IntColumn c1 = new IntColumn("int", intArr);
        DoubleColumn c2 = new DoubleColumn("double", doubleArr);
        DataTable expectedTable = new DataTable("expected", c0, c1, c2);

        Accumulator acc = new Accumulator();
        final Column[] cols = {c0, c1, c2};
        DataTable resultantTable = null;
        for (int i = 0; i < 3; i++) {
            final int rowNumber = i;
            Row row = new Row() {
                @Override
                public int columnCount() {
                    return 3;
                }

                @Override
                public Column getColumn(int columnNumber) {
                    return cols[columnNumber];
                }

                @Override
                public int getInt(int columnNumber) {
                    return c1.intValue(rowNumber);
                }

                @Override
                public double getDouble(int columnNumber) {
                    return c2.value(rowNumber);
                }
            };
            resultantTable = acc.append(row);
        }

        for (int i = 0; i < resultantTable.rowCount(); i++) {
            for (int j = 0; j < resultantTable.columnCount(); j++) {
                if(resultantTable.value(i, j) != expectedTable.value(i,j)) {
                    String errMsg = i + " " + j +"=> expected value: " + expectedTable.value(i,j) +
                            ", resultant value: " + resultantTable.value(i, j);
                    throw new RuntimeException(errMsg);
                }
            }
        }
        System.out.println("Test done!");
    }
}
