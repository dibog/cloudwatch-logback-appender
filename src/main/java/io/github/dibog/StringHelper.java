/*
 * Copyright 2018  Dieter Bogdoll
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.dibog;

class StringHelper {
    private StringBuilder sb = new StringBuilder();
    private boolean first = true;
    private boolean done = false;

    public StringHelper(Object aObject) {
        this(aObject.getClass().getSimpleName());
    }

    public StringHelper(String aName) {
        sb.append(aName).append("[ ");
    }

    public void add(String aProperty, String aValue, boolean aQuote) {
        if(first) {
            first=false;
        }
        else {
            sb.append(", ");
        }
        if(aQuote) {
            sb.append(aProperty + " = '" + aValue + "'");
        }
        else {
            sb.append(aProperty + " = " + aValue);
        }
    }

    public void add(String aProperty, boolean aValue) {
        add(aProperty, Boolean.toString(aValue), true);
    }

    public void add(String aProperty, long aValue) {
        add(aProperty, Long.toString(aValue), true);
    }

    public void add(String aProperty, Object aValue) {
        add(aProperty, aValue.toString(), false);
    }

    public String toString() {
        if(!done) {
            sb.append("]");
            done = true;
        }
        return sb.toString();
    }
}
