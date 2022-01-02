package com.biorecorder.datalyb.datatable;

import com.biorecorder.datalyb.list.IntArrayList;
import com.biorecorder.datalyb.series.IntSeries;
import com.biorecorder.datalyb.series.SeriesUtils;

import java.util.Arrays;

public class IntColumn implements Column {
    private BaseType type = BaseType.INT;
    private String name;
    private EditableIntSeries data;

    public IntColumn(String name, EditableIntSeries data) {
        this.name = name;
        this.data = data;
    }

    /**
     * Data will not be copied but internalised as it is.
     * So it will be impossible to change underlying column data by using
     * column methods (add, set) but data may be changed from outside
     **/
    public IntColumn(String name, IntSeries data) {
        this(name, new BaseEditableIntSeries(data));
    }

    /**
     * Data will be copied to inner IntArrayList. So underlying column data
     * cannot be changed from outside but may be change  by using
     * column methods (add, set)
     **/
    public IntColumn(String name, int[] data) {
        this(name, new ArrayListWrapperInt(new IntArrayList(data)));
    }

    /**
     * Underlying column data will be saved to inner IntArrayList
     * and may be change by using column methods (add, set).
     **/
    public IntColumn(String name) {
        this(name, new ArrayListWrapperInt(new IntArrayList()));
    }

    public int intValue(int index) {
        return data.get(index);
    }

    @Override
    public void clear() throws UnsupportedOperationException {
        data.clear();
    }

    public void set(int index, int value) throws UnsupportedOperationException {
        data.set(index, value);
    }

    public void append(int value) throws UnsupportedOperationException {
        data.add(value);
    }

    public void append(int[] values) throws UnsupportedOperationException {
        data.add(values);
    }

    @Override
    public Column append(int from, int length, Column colToAppend, int colToAppendFrom, int colToAppendLength) throws IllegalArgumentException {
        checkBounds(from, length, size());
        checkBounds(colToAppendFrom, colToAppendLength, colToAppend.size());
        if(colToAppend.type() == type) {
            IntColumn resultantColumn = new IntColumn(name);
            IntColumn intColToAppend = (IntColumn) colToAppend;
            try {
                resultantColumn.append(data.toArray(from, length));
            } catch (UnsupportedOperationException ex) {
                int till = from + length;
                for (int i = from; i < till; i++) {
                    resultantColumn.append(data.get(i));
                }
            }
            try {
                resultantColumn.append(intColToAppend.data.toArray(colToAppendFrom, colToAppendLength));
            } catch (UnsupportedOperationException ex) {
                int colToAppendTill = colToAppendFrom + colToAppendLength;
                for (int i = colToAppendFrom; i < colToAppendTill; i++) {
                    resultantColumn.append(intColToAppend.data.get(i));
                }
            }
            return resultantColumn;
        } else {
            String errMsg = "Column of different type can not be append: "+ type + " and " + colToAppend.type();
            throw new IllegalArgumentException(errMsg);
        }
    }

    @Override
    public Column emptyCopy() {
        return new IntColumn(name);
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
        return Integer.toString(data.get(index));
    }

    @Override
    public double[] minMax(int from, int length) throws IndexOutOfBoundsException {
        checkBounds(from, length, size());
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int value;
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
        IntSeries subSequence = new IntSeries() {
            @Override
            public int size() {
                return length;
            }

            @Override
            public int get(int index) {
                return data.get(index + from);
            }
        };
        return new IntColumn(name, subSequence);
    }

    @Override
    public Column view(int[] order) {
        IntSeries subSequence = new IntSeries() {
            int[] order1 = Arrays.copyOf(order, order.length);
            @Override
            public int size() {
                return order1.length;
            }

            @Override
            public int get(int index) {
                return data.get(order1[index]);
            }
        };
        return new IntColumn(name, subSequence);
    }

    @Override
    public int[] sort(int from, int length, boolean isParallel) throws IndexOutOfBoundsException  {
        checkBounds(from, length, size());
        return SeriesUtils.sort(data, from, length, isParallel);
    }

    @Override
    public int bisect(double value, int from, int length) throws IndexOutOfBoundsException {
        checkBounds(from, length, size());
        return SeriesUtils.bisect(data, double2int(value), from, length);
    }

    @Override
    public int bisectLeft(double value, int from, int length) throws IndexOutOfBoundsException  {
        checkBounds(from, length, size());
        return SeriesUtils.bisectLeft(data, double2int(value), from, length);

    }

    @Override
    public int bisectRight(double value, int from, int length) throws IndexOutOfBoundsException  {
        checkBounds(from, length, size());
        return SeriesUtils.bisectRight(data, double2int(value), from, length);
    }

    private static int double2int(double d) {
        long l =  (long)(d);
        if(l > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if(l < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) l;
    }

    private static void checkBounds(int from, int length, int size) throws IndexOutOfBoundsException {
        if(from < 0 || length < 0 || from + length > size) {
            String msg = "from: " + from + ", length: " + length + ", size: " + size;
            throw new IndexOutOfBoundsException(msg);
        }
    }

    interface EditableIntSeries extends IntSeries {
        void add(int value) throws UnsupportedOperationException;
        void add(int[] values) throws UnsupportedOperationException;
        void set(int index, int value) throws UnsupportedOperationException;
        int[] toArray(int from, int length) throws UnsupportedOperationException;
        void clear() throws UnsupportedOperationException;
    }

    static class BaseEditableIntSeries implements EditableIntSeries {
        IntSeries sequence;

        public BaseEditableIntSeries(IntSeries sequence) {
            this.sequence = sequence;
        }

        @Override
        public int size() {
            return sequence.size();
        }

        @Override
        public int get(int index) {
            return sequence.get(index);
        }

        @Override
        public void add(int value) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int... values) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(int index, int value) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int[] toArray(int from, int length) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }

    static class ArrayListWrapperInt implements EditableIntSeries {
        private final IntArrayList intArrayList;

        public ArrayListWrapperInt(IntArrayList intArrayList) {
            this.intArrayList = intArrayList;
        }

        @Override
        public void add(int value) throws UnsupportedOperationException {
            intArrayList.add(value);
        }

        @Override
        public void add(int... values) throws UnsupportedOperationException {
            intArrayList.add(values);
        }

        @Override
        public void set(int index, int value) throws UnsupportedOperationException {
            intArrayList.set(index, value);
        }

        @Override
        public int[] toArray(int from, int length) throws UnsupportedOperationException {
            return intArrayList.toArray(from, length);
        }

        @Override
        public int size() {
            return intArrayList.size();
        }

        @Override
        public int get(int index) {
            return intArrayList.get(index);
        }

        @Override
        public void clear() throws UnsupportedOperationException {
            intArrayList.clear();
        }
    }
}
