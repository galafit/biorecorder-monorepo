package biosignal.application.filter;

public class HiPassFilter implements Filter {
    private CircularFifoBuffer buffer;
    private long sum;

    public HiPassFilter(int bufferSize) {
        buffer = new CircularFifoBuffer(bufferSize);
    }

    @Override
    public int apply(int value) {
        if (buffer.size() < buffer.maxSize()){
            sum = sum + value;
            buffer.add(value);
        }
        else {
            sum = sum + value - buffer.get();
            buffer.add(value);
            return (value - (int) (sum / buffer.maxSize()));
        }
        return 0;
    }

    // Test
    public static void main(String[] args) {
        int bufferSize = 3;
        int avg = 12;
        int[] dataIn =  {12, 10, 14, 12, 10, 14, 12, 10, 14, 12 };
        int[] dataOut = new int[dataIn.length];

        for (int i = 0; i < dataOut.length; i++) {
            if(i < bufferSize) {
                dataOut[i] = 0;
            } else {
                dataOut[i] = dataIn[i] - avg;
            }
        }

        HiPassFilter hiPassFilter = new HiPassFilter(bufferSize);

        System.out.println();
        for (int i = 0; i < dataOut.length; i++) {
            int filtered = hiPassFilter.apply(dataIn[i]);
            System.out.println(i + " dataOut = " + dataOut[i] + " filtered = "+ filtered);
            if(filtered != dataOut[i]) {
                throw new RuntimeException("filtered value not equal expected value");
            }
        }
        System.out.println("Test is ok!");
    }
}
