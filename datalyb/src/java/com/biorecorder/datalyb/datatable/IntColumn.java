package com.biorecorder.datalyb.datatable;

import com.biorecorder.datalyb.list.IntArrayList;
import com.biorecorder.datalyb.series.IntEditableSeries;
import com.biorecorder.datalyb.series.IntSeries;
import com.biorecorder.datalyb.series.SeriesUtils;

import java.util.Arrays;

public class IntColumn implements Column {
    private BaseType type = BaseType.INT;
    private String name;
    private IntEditableSeries data;

    public IntColumn(String name, IntEditableSeries data) {
        this.name = name;
        this.data = data;
    }

    public IntColumn(String name, IntSeries data) {
        this(name, new BaseIntEditableSeries(data));
    }

    public IntColumn(String name, int[] arrData) {
        this(name, new IntArrayWrapper(new IntArrayList(arrData)));
    }

    public IntColumn(String name) {
        this(name, new IntArrayWrapper(new IntArrayList()));
    }

    public int intValue(int index) {
        return data.get(index);
    }

    public void append(int value) throws UnsupportedOperationException {
        data.add(value);
    }

    @Override
    public void append(Column col) throws IllegalArgumentException {
        if(col.type() == type) {
            IntColumn ic = (IntColumn) col;
            try {
                data.add(ic.data.toArray());
            } catch (UnsupportedOperationException ex) {
                try{
                    for (int i = 0; i < ic.size(); i++) {
                        data.add(ic.intValue(i));
                    }
                } catch (UnsupportedOperationException ex1) {
                    IntSeries dataJoined = new IntSeries() {
                        IntSeries data1 = data;
                        IntSeries data2 = ic.data;
                        int size1 = data1.size();
                        int size2 = data2.size();
                        int size =  size1 + size2;
                        @Override
                        public int size() {
                            return size;
                        }

                        @Override
                        public int get(int index) {
                            if(index < size1) {
                                return data1.get(index);
                            } else {
                                return data2.get(index - size1);
                            }
                        }
                    };
                    data = new BaseIntEditableSeries(dataJoined);
                }
            }
        } else {
            String errMsg = "Column of different type can not be append: "+ type + " and " + col.type();
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
    public double[] minMax() {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int value;
        if(size() > 0) {
            for (int i = 0; i < size(); i++) {
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
        return null;
    }

    @Override
    public Column view(int from, int length) {
        IntSeries subSequence = new IntSeries() {
            int size = Math.min(data.size() - from, length);
            @Override
            public int size() {
                return size;
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
    public int[] sort(boolean isParallel) {
        return SeriesUtils.sort(data, 0, data.size(), isParallel);
    }

    @Override
    public int bisect(double value) {
        return SeriesUtils.bisect(data, double2int(value), 0, data.size());
    }

    @Override
    public int bisectLeft(double value) {
        return SeriesUtils.bisectLeft(data, double2int(value), 0, data.size());

    }

    @Override
    public int bisectRight(double value) {
        return SeriesUtils.bisectRight(data, double2int(value), 0, data.size());
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

    static class BaseIntEditableSeries implements IntEditableSeries {
        IntSeries sequence;

        public BaseIntEditableSeries(IntSeries sequence) {
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
        public int[] toArray() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }

    static class IntArrayWrapper implements IntEditableSeries {
        private final IntArrayList intArrayList;

        public IntArrayWrapper(IntArrayList intArrayList) {
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
        public int[] toArray() throws UnsupportedOperationException {
            return intArrayList.toArray();
        }

        @Override
        public int size() {
            return intArrayList.size();
        }

        @Override
        public int get(int index) {
            return intArrayList.get(index);
        }
    }
}
