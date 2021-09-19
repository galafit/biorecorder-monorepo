package biosignal.filter;

public class DerivateFilter implements Filter {
    private CircularFifoBuffer buffer;

    public DerivateFilter(double dataSampleRate, int stepMs) {
        int bufferSize = (int) dataSampleRate * stepMs / 1000;
        if(bufferSize < 1) {
            bufferSize = 1;
        }
        buffer = new CircularFifoBuffer(bufferSize);
    }

    @Override
    public int apply(int value) {
        int result = 0;
        if (buffer.size() < buffer.maxSize()){
            buffer.add(value);
        }
        else {
           result = value - buffer.get();
           buffer.add(value);
        }
        return result;
    }
}
