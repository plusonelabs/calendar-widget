/*
 * Copyright (C) 2015 yvolk (Yuri Volkov), http://yurivolkov.com
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

package com.plusonelabs.calendar;

import org.joda.time.DateTime;

public enum EndedSometimeAgo {
    NONE("NONE", 0),
    TODAY("TODAY", 0) {
        @Override
        public long endedAt(long millisNow) {
            return new DateTime(millisNow).withTimeAtStartOfDay().getMillis();
        }
    },
    ONE_HOUR("ONE_HOUR", 1),
    TWO_HOURS("TWO_HOURS", 2),
    FOUR_HOURS("FOUR_HOURS", 4),
    ONE_DAY("ONE_DAY", 24);

    private final String value;
    private final int hoursAgo;

    private EndedSometimeAgo(String valueIn, int hoursAgo) {
        this.value = valueIn;
        this.hoursAgo = hoursAgo;
    }

    public long endedAt(long millisNow) {
        return new DateTime(millisNow).minusHours(hoursAgo).getMillis();
    }

    public static EndedSometimeAgo fromValue(String valueIn) {
        EndedSometimeAgo ended = NONE;
        for (EndedSometimeAgo item : EndedSometimeAgo.values()) {
            if (item.value.equals(valueIn)) {
                ended = item;
                break;
            }
        }
        return ended;
    }
}
