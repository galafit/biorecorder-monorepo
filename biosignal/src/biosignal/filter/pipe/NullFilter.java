package biosignal.filter.pipe;

import biosignal.filter.Filter;

public class NullFilter implements Filter {
    @Override
    public int apply(int value) {
        return value;
    }
}
