/*
 * Copyright (C) 2014 yvolk (Yuri Volkov), http://yurivolkov.com
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

import java.util.concurrent.atomic.AtomicLong;

public class InstanceId {
    /**
     * IDs used for testing purposes to identify instances of reference types.
     */
    private static final AtomicLong PREV_INSTANCE_ID = new AtomicLong(0);

    private InstanceId() {
    }

    /**
     * @return Unique for this process long value, numbers are given in the order starting from 1
     */
    public static long next() {
        return PREV_INSTANCE_ID.incrementAndGet();
    }
}
