package com.biorecorder.datalyb.datatable;

import com.biorecorder.datalyb.list.DoubleArrayList;
import com.biorecorder.datalyb.series.DoubleSeries;
import com.biorecorder.datalyb.series.IntSeries;
import com.biorecorder.datalyb.series.SeriesUtils;

import java.util.Arrays;

public class DoubleColumn implements Column {
    private BaseType type = BaseType.DOUBLE;
    private String name;
    EditableDoubleSeries data;

    public DoubleColumn(String name, EditableDoubleSeries data) {
        this.name = name;
        this.data = data;
    }

    /**
     * Data will not be copied but internalised as it is.
     * So it will be impossible to change underlying column data by using
     * column methods (add, set) but data may be changed from outside
     **/
    public DoubleColumn(String name, DoubleSeries data) {
        this(name, new BaseEditableDoubleSeries(data));
    }

    /**
     * Data will be copied to inner DoubleArrayList. So underlying column data
     * cannot be changed from outside but may be change  by using
     * column methods (add, set)
     **/
    public DoubleColumn(String name, double[] data) {
        this(name, new ArrayListWrapperDouble(new DoubleArrayList(data)));
    }

    /**
     * Underlying column data will be saved to inner DoubleArrayList
     * and may be change by using column methods (add, set).
     **/
    public DoubleColumn(String name) {
        this(name, new ArrayListWrapperDouble(new DoubleArrayList()));
    }

    @Override
    public void clear() throws UnsupportedOperationException {
        data.clear();
    }

    public void set(int index, double value) throws UnsupportedOperationException {
        data.set(index, value);
    }

    public void append(double value) throws UnsupportedOperationException {
        data.add(value);
    }

    public void append(double[] values) throws UnsupportedOperationException {
        data.add(values);
    }

    @Override
    public Column append(int from, int length, Column colToAppend, int colToAppendFrom, int colToAppendLength) throws IllegalArgumentException {
        checkBounds(from, length, size());
        checkBounds(colToAppendFrom, colToAppendLength, colToAppend.size());
        DoubleColumn resultantColumn = new DoubleColumn(name);
        try {
            resultantColumn.append(data.toArray(from, length));
        } catch (UnsupportedOperationException ex) {
            int till = from + length;
            for (int i = from; i < till; i++) {
                resultantColumn.append(data.get(i));
            }
        }
        if(colToAppend.type() == type) {
            DoubleColumn doubleColToAppend = (DoubleColumn) colToAppend;
            try {
                resultantColumn.append(doubleColToAppend.data.toArray(colToAppendFrom, colToAppendLength));
                return resultantColumn;
            } catch (UnsupportedOperationException ex) {
              // do nothing;
            }
        }
        int colToAppendTill = colToAppendFrom + colToAppendLength;
        for (int i = colToAppendFrom; i < colToAppendTill; i++) {
            resultantColumn.append(colToAppend.value(i));
        }
        return resultantColumn;
    }


    @Override
    public Column emptyCopy() {
        return new DoubleColumn(name);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public BaseType type() {
        return type;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public double value(int index) {
        return data.get(index);
    }

    @Override
    public String label(int index) {
        return Double.toString(data.get(index));
    }

    @Override
    public double[] minMax(int from, int length) throws IndexOutOfBoundsException {
        checkBounds(from, length, size());
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double value;
        int till = from + length;
        for (int i = from; i < till; i++) {
            value = data.get(i);
            if(min > value) {
                min = value;
            }
            if(max < value) {
                max = value;
            }
        }
        double[] minMax  = {min, max};
        return minMax;
    }

    @Override
    public Column view(int from, int length) throws IndexOutOfBoundsException {
        checkBounds(from, length, size());
        DoubleSeries subSequence = new DoubleSeries() {
            @Override
            public int size() {
                return length;
            }

            @Override
            public double get(int index) {
                return data.get(index + from);
            }
        };
        return new DoubleColumn(name, subSequence);
    }

    @Override
    public Column view(int[] order) {
        DoubleSeries subSequence = new DoubleSeries() {
            int[] order1 = Arrays.copyOf(order, order.length);
            @Override
            public int size() {
                return order1.length;
            }

            @Override
            public double get(int index) {
                return data.get(order1[index]);
            }
        };
        return new DoubleColumn(name, subSequence);
    }

    @Override
    public int[] sort(int from, int length, boolean isParallel) throws IndexOutOfBoundsException  {
        checkBounds(from, length, size());
        return SeriesUtils.sort(data, from, length, isParallel);
    }

    @Override
    public int bisect(double value, int from, int length) throws IndexOutOfBoundsException {
        checkBounds(from, length, size());
        return SeriesUtils.bisect(data, value, from, length);
    }

    @Override
    public int bisectLeft(double value, int from, int length) throws IndexOutOfBoundsException  {
        checkBounds(from, length, size());
        return SeriesUtils.bisectLeft(data, value, from, length);

    }

    @Override
    public int bisectRight(double value, int from, int length) throws IndexOutOfBoundsException  {
        checkBounds(from, length, size());
        return SeriesUtils.bisectRight(data, value, from, length);
    }

    private static void checkBounds(int from, int length, int size) throws IndexOutOfBoundsException {
        if(from < 0 || length < 0 || from + length > size) {
            String msg = "from: " + from + ", length: " + length + ", size: " + size;
            throw new IndexOutOfBoundsException(msg);
        }
    }

    public interface EditableDoubleSeries extends DoubleSeries {
        void add(double value) throws UnsupportedOperationException;
        void add(double[] values) throws UnsupportedOperationException;
        void set(int index, double value) throws UnsupportedOperationException;
        double[] toArray(int from, int length) throws UnsupportedOperationException;
        void clear() throws UnsupportedOperationException;
    }

    static class BaseEditableDoubleSeries implements EditableDoubleSeries {
        DoubleSeries sequence;

        public BaseEditableDoubleSeries(DoubleSeries sequence) {
            this.sequence = sequence;
        }

        @Override
        public int size() {
            return sequence.size();
        }

        @Override
        public double get(int index) {
            return sequence.get(index);
        }

        @Override
        public void add(double value) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(double[] values) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(int index, double value) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public double[] toArray(int from, int length) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }

    static class ArrayListWrapperDouble implements EditableDoubleSeries {
        private final DoubleArrayList doubleArrayList;

        public ArrayListWrapperDouble(DoubleArrayList doubleArrayList) {
            this.doubleArrayList = doubleArrayList;
        }

        @Override
        public void add(double value) throws UnsupportedOperationException {
            doubleArrayList.add(value);
        }

        @Override
        public void add(double... values) throws UnsupportedOperationException {
            doubleArrayList.add(values);
        }

        @Override
        public void set(int index, double value) throws UnsupportedOperationException {
            doubleArrayList.set(index, value);
        }

        @Override
        public double[] toArray(int from, int length) throws UnsupportedOperationException {
            return doubleArrayList.toArray(from, length);
        }

        @Override
        public int size() {
            return doubleArrayList.size();
        }

        @Override
        public double get(int index) {
            return doubleArrayList.get(index);
        }

        @Override
        public void clear() throws UnsupportedOperationException {
            doubleArrayList.clear();
        }
    }
}

