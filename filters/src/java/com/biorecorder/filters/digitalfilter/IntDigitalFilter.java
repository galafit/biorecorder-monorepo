package com.biorecorder.filters.digitalfilter;

/**
 * Any LINEAR transformation.
 * Filter length describes how many data elements affect the result:
 * <ul>
 *     <li>Filter length = 0.  Constant filter. If resultant filtered value do not depend on input value</li>
 *     <li>Filter length = 1.  Filter sin state. If resultant filtered value depends only on input value </li>
 *     <li>Filter length = buffer size + 1. Filter with state </li>
 * </ul>
 */
public interface IntDigitalFilter {
    int filteredValue(int inputValue);

    int getFilterLength();
}
