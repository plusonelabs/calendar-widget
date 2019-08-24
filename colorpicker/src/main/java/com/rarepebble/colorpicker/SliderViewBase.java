/*
 * Copyright (C) 2015 Martin Stone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rarepebble.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public abstract class SliderViewBase extends View {

	private final Paint borderPaint;
	private final Paint checkerPaint;
	private final Rect viewRect = new Rect();
	private int w;
	private int h;
	private final Path borderPath;
	private Bitmap bitmap;
	private final Path pointerPath;
	private final Paint pointerPaint;

	private float currentPos;

	public SliderViewBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		checkerPaint = Resources.makeCheckerPaint(context);
		borderPaint = Resources.makeLinePaint(context);
		pointerPaint = Resources.makeLinePaint(context);
		pointerPath = Resources.makePointerPath(context);
		borderPath = new Path();
	}

	protected abstract void notifyListener(float currentPos);

	protected abstract Bitmap makeBitmap(int w, int h);

	protected abstract int getPointerColor(float currentPos);

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		this.w = w;
		this.h = h;
		viewRect.set(0, 0, w, h);
		float inset = borderPaint.getStrokeWidth() / 2;
		borderPath.reset();
		borderPath.addRect(new RectF(inset, inset, w - inset, h - inset), Path.Direction.CW);
		updateBitmap();
	}

	protected void setPos(float pos) {
		currentPos = pos;
		optimisePointerColor();
	}

	protected void updateBitmap() {
		if (w > 0 && h > 0) {
			bitmap = makeBitmap(w, h);
			optimisePointerColor();
		}
		// else not ready yet
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				currentPos = valueForTouchPos(event.getX(), event.getY());
				optimisePointerColor();
				notifyListener(currentPos);
				invalidate();
				getParent().requestDisallowInterceptTouchEvent(true);
				return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawPath(borderPath, checkerPaint);
		canvas.drawBitmap(bitmap, null, viewRect, null);
		canvas.drawPath(borderPath, borderPaint);

		canvas.save();
		if (isWide()) {
			canvas.translate(w * currentPos, h / 2);
		}
		else {
			canvas.translate(w / 2, h * (1 - currentPos));
		}
		canvas.drawPath(pointerPath, pointerPaint);
		canvas.restore();
	}

	private boolean isWide() {
		return w > h;
	}

	private float valueForTouchPos(float x, float y) {
		final float val = isWide() ? x / w : 1 - y / h;
		return Math.max(0, Math.min(1, val));
	}

	private void optimisePointerColor() {
		pointerPaint.setColor(getPointerColor(currentPos));
	}
}
