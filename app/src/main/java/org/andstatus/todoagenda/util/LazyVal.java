/*
 * Copyright (C) 2018 yvolk (Yuri Volkov), http://yurivolkov.com
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

package org.andstatus.todoagenda.util;

import java.util.function.Supplier;

/** Lazy holder of a non Null / or Nullable value
 * Blocks on parallel evaluation
 * Inspired by https://www.sitepoint.com/lazy-computations-in-java-with-a-lazy-type/
 * and https://dzone.com/articles/be-lazy-with-java-8 */
public class LazyVal<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    public final boolean isNullable;
    private volatile T value = null;
    private volatile boolean isEvaluated = false;

    public static <T> LazyVal<T> of(Supplier<T> supplier) {
        return new LazyVal<>(supplier, false);
    }

    public static <T> LazyVal<T> ofNullable(Supplier<T> supplier) {
        return new LazyVal<>(supplier, true);
    }

    private LazyVal(Supplier<T> supplier, boolean isNullable) {
        this.supplier = supplier;
        this.isNullable = isNullable;
    }

    @Override
    public T get() {
        T storedValue = value;
        return isEvaluated ? storedValue : evaluate();
    }

    public boolean isEvaluated() {
        return isEvaluated;
    }

    private synchronized T evaluate() {
        if (value != null) return value;

        T evaluatedValue = supplier.get();
        value = evaluatedValue;
        isEvaluated = isNullable || value != null;
        return evaluatedValue;
    }

    public void reset() {
        value = null;
    }
}
