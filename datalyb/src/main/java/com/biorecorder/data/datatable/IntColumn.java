package com.biorecorder.data.datatable;

import com.biorecorder.data.datatable.aggregation.AggFunction;
import com.biorecorder.data.datatable.aggregation.Max;
import com.biorecorder.data.datatable.aggregation.Min;
import com.biorecorder.data.list.IntEditableArrayList;
import com.biorecorder.data.sequence.IntEditableSequence;
import com.biorecorder.data.sequence.IntSequence;
import com.biorecorder.data.sequence.SequenceUtils;

import java.util.Arrays;

public class IntColumn implements Column {
    private BaseType type = BaseType.INT;
    private String name;
    private IntEditableSequence data;

    public IntColumn(String name, IntEditableSequence data) {
        this.name = name;
        this.data = data;
    }

    public IntColumn(String name, IntSequence data) {
        this(name, new BaseIntEditableSequence(data));
    }

    public IntColumn(String name, int[] arrData) {
        this(name, new IntEditableArrayList(arrData));
    }

    public IntColumn(String name) {
        this(name, new IntEditableArrayList());
    }

    public int intValue(int index) {
        return data.get(index);
    }

    public void append(int value) throws UnsupportedOperationException {
        data.add(value);
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
                   IntSequence dataJoined = new IntSequence() {
                       IntSequence data1 = data;
                       IntSequence data2 = ic.data;
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
                   data = new BaseIntEditableSequence(dataJoined);
               }
            }
        }
        String errMsg = "Column of different type can not be append: "+ type + " and " + col.type();
        throw new IllegalArgumentException(errMsg);
    }

    @Override
    public double min() {
        AggFunction agg = new Min();
        if(size() > 0) {
            for (int i = 0; i < size(); i++) {
               agg.addInt(data.get(i));
            }
            return agg.getInt();
        }
        return Double.NaN;
    }

    @Override
    public double max() {
        AggFunction agg = new Max();
        if(size() > 0) {
            for (int i = 0; i < size(); i++) {
                agg.addInt(data.get(i));
            }
            return agg.getInt();
        }
        return Double.NaN;
    }

    @Override
    public Column view(int from, int length) {
        IntSequence subSequence = new IntSequence() {
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
        IntSequence subSequence = new IntSequence() {
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
        return SequenceUtils.sort(data, 0, data.size(), isParallel);
    }

    @Override
    public int bisect(double value) {
        return SequenceUtils.bisect(data, double2int(value), 0, data.size());
    }

    @Override
    public int bisectLeft(double value) {
        return SequenceUtils.bisectLeft(data, double2int(value), 0, data.size());

    }

    @Override
    public int bisectRight(double value) {
        return SequenceUtils.bisectRight(data, double2int(value), 0, data.size());
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

    static class BaseIntEditableSequence implements IntEditableSequence {
        IntSequence sequence;

        public BaseIntEditableSequence(IntSequence sequence) {
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
}
