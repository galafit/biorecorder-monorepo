package com.biorecorder.data.frame_new;

import com.biorecorder.data.frame_new.aggregation.AggFunction;
import com.biorecorder.data.frame_new.aggregation.Max;
import com.biorecorder.data.frame_new.aggregation.Min;
import com.biorecorder.data.list.DoubleArrayList;
import com.biorecorder.data.sequence.SequenceUtils;
import com.biorecorder.data.utils.PrimitiveUtils;

public class DoubleColumn implements Column {
    ColumnType type = DoubleColumnType.instance();
    String name = "";
    EditableDoubleSequence data;

    public DoubleColumn(String name, double[] arrData) {
        this.name = name;
        this.data = new DoubleArrayList(arrData);
    }

    public DoubleColumn(String name, EditableDoubleSequence data) {
        this.name = name;
        this.data = data;
    }

    public DoubleColumn(EditableDoubleSequence data) {
        this.data = data;
    }

    public DoubleColumn(String name) {
        this.name = name;
        data = new DoubleArrayList();
    }

    @Override
    public boolean isNumberColumn() {
        return type.isNumberType();
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
    public ColumnType type() {
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

    public Column append(double value) {
        data.add(value);
        return this;
    }

    @Override
    public Column append(Column col) throws UnsupportedOperationException, IllegalArgumentException {
        if(col.type() != type) {
            for (int i = 0; i < col.size(); i++) {
                data.add(col.value(i));
            }
            return this;
            /*try {
                for (int i = 0; i < c.size(); i++) {
                    data.add(ic.intValue(i));
                }
            } catch (UnsupportedOperationException ex) {
               EditableIntSequence dataJoined = new BaseIntSequence() {
                   EditableIntSequence data1 = data;
                   EditableIntSequence data2 = ic.data;
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
               data = dataJoined;
               return this;
            }*/
        }
        String errMsg = "Column of different type can not be append: "+ type + " and " + col.type();
        throw new IllegalArgumentException(errMsg);
    }

    @Override
    public double min() {
        AggFunction agg = new Min();
        if(size() > 0) {
            for (int i = 0; i < size(); i++) {
                agg.addDouble(data.get(i));
            }
            return agg.getDouble();
        }
        return Double.NaN;
    }

    @Override
    public double max() {
        AggFunction agg = new Max();
        if(size() > 0) {
            for (int i = 0; i < size(); i++) {
                agg.addDouble(data.get(i));
            }
            return agg.getDouble();
        }
        return Double.NaN;
    }

    @Override
    public Column view(int from, int length) {
        EditableDoubleSequence subSequence = new BaseDoubleSequence() {
            int size = Math.min(data.size() - from, length);
            @Override
            public int size() {
                return size;
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
        EditableDoubleSequence subSequence = new BaseDoubleSequence() {
            @Override
            public int size() {
                return order.length;
            }

            @Override
            public double get(int index) {
                return data.get(order[index]);
            }
        };

        return new DoubleColumn(name, subSequence);
    }

    @Override
    public int[] sort(boolean isParallel) {
        return SequenceUtils.sort(data, 0, data.size(), isParallel);
    }


    @Override
    public int bisect(double value) {
        return SequenceUtils.bisect(data, PrimitiveUtils.roundDouble2int(value), 0, data.size());
    }


    @Override
    public int bisectLeft(double value) {
        return SequenceUtils.bisectLeft(data, PrimitiveUtils.roundDouble2int(value), 0, data.size());

    }

    @Override
    public int bisectRight(double value) {
        return SequenceUtils.bisectRight(data, PrimitiveUtils.roundDouble2int(value), 0, data.size());
    }
}

