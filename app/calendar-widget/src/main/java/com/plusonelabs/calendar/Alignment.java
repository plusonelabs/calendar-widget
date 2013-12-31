package com.plusonelabs.calendar;

public enum Alignment {

    LEFT(R.layout.day_header_left),
    CENTER(R.layout.day_header_center),
    RIGHT(R.layout.day_header_right);

    private int layoutId;

    Alignment(int layoutId) {
        this.layoutId = layoutId;
    }

    public int getLayoutId() {
        return layoutId;
    }
}
