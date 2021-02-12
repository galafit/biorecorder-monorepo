package com.biorecorder.bichart;

import com.biorecorder.bichart.button.SwitchButton;
import com.biorecorder.bichart.graphics.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LegendPainter {
    private SwitchButton[] buttons;
    private int legendHeight;

    public LegendPainter(RenderContext renderContext, TraceList traceList, LegendConfig config, BRectangle area) {
        buttons = new SwitchButton[traceList.size()];
        HashMap<BRectangle, List<SwitchButton>> areaToButtons = new HashMap();
        // create buttons
        if (config.isAttachedToStacks()) {
            for (int i = 0; i < traceList.size(); i++) {
                String traceName = traceList.getName(i);
                BColor traceColor = traceList.getColor(i);
                SwitchButton b = new SwitchButton(traceName, config.getTextStyle());
                b.setColor(traceColor);
                b.setBackgroundColor(config.getBackgroundColor());
                BRectangle stackArea = traceList.getTraceStackArea(i);
                List<SwitchButton> stackButtons = areaToButtons.get(stackArea);
                if (stackButtons == null) {
                    stackButtons = new ArrayList<SwitchButton>();
                    areaToButtons.put(stackArea, stackButtons);
                }
                stackButtons.add(b);
                buttons[i] = b;
            }
        } else {
            List<SwitchButton> areaButtons = new ArrayList<SwitchButton>();
            areaToButtons.put(area, areaButtons);
            for (int i = 0; i < traceList.size(); i++) {
                String traceName = traceList.getName(i);
                BColor traceColor = traceList.getColor(i);
                SwitchButton b = new SwitchButton(traceName, config.getTextStyle());
                b.setColor(traceColor);
                b.setBackgroundColor(config.getBackgroundColor());
                areaButtons.add(b);
                buttons[i] = b;
            }
        }

        int selectedTrace = traceList.getSelection();
        if(traceList.getSelection() >= 0)  {
            buttons[selectedTrace].setSelected(true);
        }

        // arrange buttons
        for (BRectangle stackArea : areaToButtons.keySet()) {
            List<SwitchButton> stackButtons = areaToButtons.get(stackArea);
            legendHeight = arrangeButtons(stackButtons, stackArea, renderContext, config);
        }
        if (config.isAttachedToStacks()) {
            legendHeight = 0;
        }
    }

    private int arrangeButtons(List<SwitchButton> areaButtons, BRectangle area, RenderContext renderContext, LegendConfig config) {
        int legendHeight = 0;
        int legendWidth = 0;
        int x = area.x;
        int y = area.y;
        int area_end = area.x + area.width;
        List<SwitchButton> lineButtons = new ArrayList<SwitchButton>();
        BDimension btnDimension = null;
        for (SwitchButton button : areaButtons) {
            btnDimension = button.getPrefferedSize(renderContext);
            if (lineButtons.size() > 0 && x + config.getInterItemSpace() + btnDimension.width >= area_end) {
                legendWidth += (lineButtons.size() - 1) * config.getInterItemSpace();
                if (config.getHorizontalAlign() == HorizontalAlign.LEFT) {
                    moveButtons(lineButtons, 0, 0);
                }
                if (config.getHorizontalAlign() == HorizontalAlign.RIGHT) {
                    moveButtons(lineButtons, area.width - legendWidth, 0);
                }
                if (config.getHorizontalAlign() == HorizontalAlign.CENTER) {
                    moveButtons(lineButtons, (area.width - legendWidth) / 2, 0);
                }

                x = area.x;
                y += btnDimension.height + config.getInterLineSpace();
                button.setBounds(x, y, btnDimension.width, btnDimension.height);

                x += btnDimension.width + config.getInterItemSpace();
                legendHeight += btnDimension.height + config.getInterLineSpace();
                legendWidth = btnDimension.width;
                lineButtons.clear();
            } else {
                button.setBounds(x, y, btnDimension.width, btnDimension.height);
                x += config.getInterItemSpace() + btnDimension.width;
                legendWidth += btnDimension.width;
            }
            lineButtons.add(button);
        }
        legendWidth += (lineButtons.size() - 1) * config.getInterItemSpace();
        if(btnDimension != null) {
            legendHeight += btnDimension.height;
        }

        if (config.getHorizontalAlign() == HorizontalAlign.LEFT) {
            moveButtons(lineButtons, 0, 0);
        }
        if (config.getHorizontalAlign() == HorizontalAlign.RIGHT) {
            moveButtons(lineButtons, area.width - legendWidth, 0);
        }
        if (config.getHorizontalAlign() == HorizontalAlign.CENTER) {
            moveButtons(lineButtons, (area.width - legendWidth) / 2, 0);
        }

        if (config.getVerticalAlign() == VerticalAlign.TOP) {
            moveButtons(areaButtons, 0, 0);
        }
        if (config.getVerticalAlign() == VerticalAlign.BOTTOM) {
            moveButtons(areaButtons, 0, area.height - legendHeight - 0);
        }
        if (config.getVerticalAlign() == VerticalAlign.MIDDLE) {
            moveButtons(areaButtons, 0, (area.height - legendHeight) / 2);
        }
        return legendHeight;
    }


    private void moveButtons(List<SwitchButton> buttons, int dx, int dy) {
        if (dx != 0 || dy != 0) {
            for (SwitchButton button : buttons) {
                BRectangle btnBounds = button.getBounds();
                button.setBounds(btnBounds.x + dx, btnBounds.y + dy, btnBounds.width, btnBounds.height);
            }
        }
    }

    public void draw(BCanvas canvas) {
        if (buttons.length == 0) {
            return;
        }
        for (SwitchButton button : buttons) {
            button.draw(canvas);
        }
    }

    /**
     * return index of the button containing the point (x, y)
     * o -1 if there is no such button
     */
    public int findButton(int x, int y) {
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].getBounds().contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    public void setSelection(int traceIndex) {
        for (int i = 0; i < buttons.length; i++) {
            if(i == traceIndex) {
                buttons[i].setSelected(true);
            } else {
                buttons[i].setSelected(false);
            }

        }

    }

    public int getLegendHeight() {
        return legendHeight;
    }

}
