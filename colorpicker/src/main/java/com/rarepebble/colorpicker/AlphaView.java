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
import android.util.AttributeSet;

public class AlphaView extends SliderViewBase implements ColorObserver {

	private ObservableColor observableColor = new ObservableColor(0);

	public AlphaView(Context context) {
		this(context, null);
	}

	public AlphaView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void observeColor(ObservableColor observableColor) {
		this.observableColor = observableColor;
		observableColor.addObserver(this);
	}

	@Override
	public void updateColor(ObservableColor observableColor) {
		setPos((float)observableColor.getAlpha()/0xff);
		updateBitmap();
		invalidate();
	}

	@Override
	protected void notifyListener(float currentPos) {
		observableColor.updateAlpha((int)(currentPos * 0xff), this);
	}

	@Override
	protected int getPointerColor(float currentPos) {
		float solidColorLightness = observableColor.getLightness();
		float posLightness = 1 + currentPos * (solidColorLightness - 1);
		return posLightness > 0.5f ? 0xff000000 : 0xffffffff;
	}

	@Override
	protected Bitmap makeBitmap(int w, int h) {
		final boolean isWide = w > h;
		final int n = Math.max(w, h);
		int color = observableColor.getColor();
		int[] colors = new int[n];
		for (int i = 0; i < n; ++i) {
			float alpha = isWide ? (float)i / n : 1 - (float)i / n;
			colors[i] = color & 0xffffff | (int)(alpha * 0xff) << 24;
		}
		final int bmpWidth = isWide ? w : 1;
		final int bmpHeight = isWide ? 1 : h;
		return Bitmap.createBitmap(colors, bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
	}

}
