package biosignal.filter;

public class FilterChain implements Filter {
    private Filter[] filters;

    public FilterChain(Filter... filters) {
        this.filters = filters;
    }

    @Override
    public int apply(int value) {
        int result = value;
        for (Filter filter : filters) {
          result = filter.apply(result);
        }
        return result;
    }
}
