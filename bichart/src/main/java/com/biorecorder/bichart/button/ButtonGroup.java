package com.biorecorder.bichart.button;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by galafit on 18/12/17.
 */
public class ButtonGroup {
    private Vector<SwitchButton> buttons = new Vector<SwitchButton>();
    private SwitchButton selection;


    public void add(SwitchButton item) {
       buttons.add(item);
       item.setGroup(this);
    }

    public void remove(SwitchButton item) {
        if(selection == item) {
            selection = null;
        }
        buttons.remove(item);
    }

    public void setSelected(SwitchButton item, boolean isSelected) {
        if (isSelected && item != null && item != selection) {
            SwitchButton oldSelection = selection;
            selection = item;
            if (oldSelection != null) {
                oldSelection.setSelected(false);
            }

        }
        if (!isSelected && item != null && item == selection) {
            selection = null;
        }
    }

    public Enumeration<SwitchButton> getElements() {
        return buttons.elements();
    }

    public SwitchButton getSelection() {
        return selection;
    }

    public boolean isSelected(SwitchButton m) {
        return (m == selection);
    }
}
