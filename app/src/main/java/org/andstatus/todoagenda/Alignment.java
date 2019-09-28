package org.andstatus.todoagenda;

import android.view.Gravity;

public enum Alignment {

    LEFT(Gravity.LEFT),
    CENTER(android.view.Gravity.CENTER),
    RIGHT(Gravity.RIGHT);

    public final int gravity;

    Alignment(int gravity) {
        this.gravity = gravity;
    }

}
