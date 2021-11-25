package com.biorecorder.bichart;

import com.biorecorder.bichart.button.SwitchButton;
import com.biorecorder.bichart.graphics.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LegendPainter {
    private List<SwitchButton> buttons;
    private Insets margin;
    private BDimension prefferedSize;
    private boolean isAttachedToStacks;

    public LegendPainter(RenderContext renderContext, TraceList traceList, LegendConfig config, boolean isAttachedToStacks, int x, int y, int width) {
        buttons = new ArrayList(traceList.traceCount());
        this.isAttachedToStacks = isAttachedToStacks;
        margin = config.getLegendMargin();
        HashMap<BRectangle, List<SwitchButton>> areaToButtons = new HashMap();
        // create buttons
        if (isAttachedToStacks) {
            for (int i = 0; i < traceList.traceCount(); i++) {
                String traceName = traceList.getName(i);
                SwitchButton b = new SwitchButton(traceName, config.getTextStyle());
                b.setMargin(config.getButtonsMargin());
                b.setBackgroundColor(config.getBackgroundColor());
                BRectangle stackArea = traceList.getTraceStackArea(i);
                List<SwitchButton> stackButtons = areaToButtons.get(stackArea);
                if (stackButtons == null) {
                    stackButtons = new ArrayList<SwitchButton>();
                    areaToButtons.put(stackArea, stackButtons);
                }
                stackButtons.add(b);
                buttons.add(b);
            }
        } else {
            BRectangle area = new BRectangle(x, y, width, 0);
            List<SwitchButton> areaButtons = new ArrayList<SwitchButton>();
            areaToButtons.put(area, areaButtons);
            for (int i = 0; i < traceList.traceCount(); i++) {
                String traceName = traceList.getName(i);
                SwitchButton b = new SwitchButton(traceName, config.getTextStyle());
                b.setBackgroundColor(config.getBackgroundColor());
                b.setMargin(config.getButtonsMargin());
                areaButtons.add(b);
                buttons.add(b);
            }
        }

        // arrange buttons
        for (BRectangle stackArea : areaToButtons.keySet()) {
            List<SwitchButton> stackButtons = areaToButtons.get(stackArea);
            prefferedSize = arrangeButtons(stackButtons, stackArea, renderContext, config);
        }
        if (isAttachedToStacks) {
            prefferedSize = new BDimension(0, 0);
         }
    }

    private BDimension arrangeButtons(List<SwitchButton> areaButtons, BRectangle area, RenderContext renderContext, LegendConfig config) {
        int legendHeight = 0;

        int x_start = area.x + margin.left();
        int y_start = area.y + margin.top();
        int width = area.width - margin.right() - margin.left();
        int height = area.height - margin.top() - margin.bottom();
        int area_end = area.x + area.width - margin.right();
        int x = x_start;
        int y = y_start;
        List<SwitchButton> lineButtons = new ArrayList<SwitchButton>();
        BDimension btnDimension = null;
        int lineButtonsWidth = 0;
        for (SwitchButton button : areaButtons) {
            btnDimension = button.getPrefferedSize(renderContext);
            if (lineButtons.size() > 0 && x + config.getInterItemSpace() + btnDimension.width > area_end) {
                lineButtonsWidth += (lineButtons.size() - 1) * config.getInterItemSpace();

                if (config.getHorizontalAlign() == HorizontalAlign.RIGHT) {
                    moveButtons(lineButtons, width - lineButtonsWidth, 0);
                }
                if (config.getHorizontalAlign() == HorizontalAlign.CENTER) {
                    moveButtons(lineButtons, (width - lineButtonsWidth) / 2, 0);
                }

                x = x_start;
                y += btnDimension.height + config.getInterLineSpace();
                button.setBounds(x, y, btnDimension.width, btnDimension.height);

                x += btnDimension.width + config.getInterItemSpace();
                legendHeight += btnDimension.height + config.getInterLineSpace();
                lineButtonsWidth = btnDimension.width;
                lineButtons.clear();
            } else {
                button.setBounds(x, y, btnDimension.width, btnDimension.height);
                x += config.getInterItemSpace() + btnDimension.width;
                lineButtonsWidth += btnDimension.width;
            }
            lineButtons.add(button);
        }

        lineButtonsWidth += (lineButtons.size() - 1) * config.getInterItemSpace();
        legendHeight += btnDimension.height + margin.bottom() + margin.top();

        if (config.getHorizontalAlign() == HorizontalAlign.RIGHT) {
            moveButtons(lineButtons, width - lineButtonsWidth, 0);
        }
        if (config.getHorizontalAlign() == HorizontalAlign.CENTER) {
            moveButtons(lineButtons, (width - lineButtonsWidth) / 2, 0);
        }

        if(isAttachedToStacks) {
            if (config.getVerticalAlign() == VerticalAlign.BOTTOM) {
                moveButtons(areaButtons, 0, height - legendHeight);
            }
            if (config.getVerticalAlign() == VerticalAlign.MIDDLE) {
                moveButtons(areaButtons, 0, (height - legendHeight) / 2);
            }

        }
        return new BDimension(area.width, legendHeight);
    }

    public void moveButtons(int dx, int dy) {
        if(!isAttachedToStacks) {
            moveButtons(buttons, dx, dy);
        }
    }


    private void moveButtons(List<SwitchButton> buttons, int dx, int dy) {
        if (dx != 0 || dy != 0) {
            for (SwitchButton button : buttons) {
                BRectangle btnBounds = button.getBounds();
                button.setBounds(btnBounds.x + dx, btnBounds.y + dy, btnBounds.width, btnBounds.height);
            }
        }
    }

    public void draw(BCanvas canvas, ColorsAndSelections colorsAndSelections) {
        if (buttons.size() == 0) {
            return;
        }
        for (int i = 0; i < buttons.size(); i++) {
            SwitchButton b = buttons.get(i);
            b.setColor(colorsAndSelections.getColor(i));
            b.setSelected(colorsAndSelections.isSelected(i));
            b.draw(canvas);
        }
    }

    /**
     * return index of the button containing the point (x, y)
     * o -1 if there is no such button
     */
    public int findButton(int x, int y) {
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).getBounds().contain(x, y)) {
                return i;
            }
        }
        return -1;
    }

    public BDimension getPrefferedSize() {
        return prefferedSize;
    }
}
