package biosignal.application.filter;

public class NullFilter implements Filter{
    @Override
    public int apply(int value) {
        return value;
    }
}
