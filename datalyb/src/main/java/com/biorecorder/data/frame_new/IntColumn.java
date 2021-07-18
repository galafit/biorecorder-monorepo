package com.biorecorder.data.frame_new;

import com.biorecorder.data.frame_new.aggregation.Aggregation;
import com.biorecorder.data.frame_new.aggregation.Max;
import com.biorecorder.data.frame_new.aggregation.Min;
import com.biorecorder.data.list.IntArrayList;
import com.biorecorder.data.sequence.IntSequence;
import com.biorecorder.data.sequence.SequenceUtils;
import com.biorecorder.data.utils.PrimitiveUtils;

public class IntColumn implements Column {
    ColumnType type = IntColumnType.instance();
    String name;
    EditableIntSequence data;

    public IntColumn(String name, int[] arrData) {
        this.name = name;
        this.data = new IntArrayList(arrData);
    }

    public IntColumn(String name, EditableIntSequence data) {
        this.name = name;
        this.data = data;
    }

    public IntColumn(String name) {
        this.name = name;
        data = new IntArrayList();
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


    public int intValue(int index) {
        return data.get(index);
    }


    @Override
    public String label(int index) {
        return Integer.toString(data.get(index));
    }

    public Column append(int value) {
        data.add(value);
        return this;
    }

    @Override
    public Column append(Column col) throws UnsupportedOperationException, IllegalArgumentException {
        if(col.type() != type) {
            IntColumn ic = (IntColumn) col;
            for (int i = 0; i < col.size(); i++) {
                data.add(ic.intValue(i));
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
        Aggregation agg = new Min();
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
        Aggregation agg = new Max();
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
        EditableIntSequence subSequence = new BaseIntSequence() {
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
        EditableIntSequence subSequence = new BaseIntSequence() {
            @Override
            public int size() {
                return order.length;
            }

            @Override
            public int get(int index) {
                return data.get(order[index]);
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
