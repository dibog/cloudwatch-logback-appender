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

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;

class ExceptionUtil {

    public static String toString(IThrowableProxy aException) {
        StringBuilder sb = new StringBuilder();
        appendProxy(sb, "", "", aException);
        return sb.toString();
    }

    private static void appendProxy(StringBuilder sb, String aPrefix, String aHeader, IThrowableProxy aException) {

        sb.append(aPrefix + aHeader + aException.getClassName() + ": " +aException.getMessage() + "\n");

        String prefix = aPrefix+"\t";
        appendStackTrace(sb, prefix, aException);

        IThrowableProxy[] suppressed = aException.getSuppressed();
        if(suppressed!=null) {
            for(IThrowableProxy supp : suppressed) {
                if(supp!=null) {
                    appendProxy(sb, prefix, "Suppressed: ", supp);
                }
            }
        }

        IThrowableProxy cause = aException.getCause();
        if(cause!=null) {
            appendProxy(sb, prefix, "Causd by: ", cause);
        }
    }

    private static void appendStackTrace(StringBuilder sb, String prefix, IThrowableProxy aProxy) {

        StackTraceElementProxy[] frames = aProxy.getStackTraceElementProxyArray();
        int commonFrames = aProxy.getCommonFrames();
        for(int i=0, size=frames.length-commonFrames; i<size; ++i) {
            sb.append(prefix+frames[i]+"\n");
        }
        if (commonFrames>0) {
            sb.append(prefix+"... "+commonFrames+" more"+"\n");
        }
    }
}
