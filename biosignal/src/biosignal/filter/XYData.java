package biosignal.filter;

import com.biorecorder.bichart.GroupingApproximation;
import com.biorecorder.datalyb.datatable.*;

public class XYData {
    DataTable dataTable;
    private GroupingApproximation yGroupingApproximation;

    public XYData(String name, Column xColumn, Column yColumn) {
        dataTable = new DataTable(name, xColumn, yColumn);
    }

    public XYData(Column xColumn, Column yColumn) {
        this("XYData", xColumn, yColumn);
    }

    public void setName(String name) {
        dataTable.setName(name);
    }

    public String getName() {
        return dataTable.getName();
    }

    public GroupingApproximation getYGroupingApproximation() {
        return yGroupingApproximation;
    }

    public void setYGroupingApproximation(GroupingApproximation yGroupingApproximation) {
        this.yGroupingApproximation = yGroupingApproximation;
    }

    public double getX(int index) {
        return dataTable.value(index, 0);
    }

    public double getY(int index) {
        return dataTable.value(index, 1);
    }

    public int size() {
        return dataTable.rowCount();
    }

    public DataTable getDataTable() {
        return dataTable;
    }
}
