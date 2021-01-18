package com.biorecorder.bichart;


/**
 * Created by galafit on 14/2/19.
 */
class DataPainterTrace {
    protected final DataPainter dataPainter;
    protected final int trace;

    public DataPainterTrace(DataPainter dataPainter, int trace) {
        this.dataPainter = dataPainter;
        this.trace = trace;
    }

    public DataPainter getDataPainter() {
        return dataPainter;
    }

    public int getTrace() {
        return trace;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof DataPainterTrace)) {
            return false;
        }

        DataPainterTrace tc = (DataPainterTrace) o;
        return ((trace == tc.trace) &&
                (dataPainter == tc.dataPainter));
    }


    /**
     * https://www.mkyong.com/java/java-how-to-overrides-equals-and-hashcode/
     * https://medium.com/codelog/overriding-hashcode-method-effective-java-notes-723c1fedf51c
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + trace;
        result = 31 * result + dataPainter.hashCode();
        return result;
    }

}
