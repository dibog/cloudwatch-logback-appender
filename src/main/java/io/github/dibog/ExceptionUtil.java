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

    private  static void appendStackTrace(StringBuilder sb, String prefix, IThrowableProxy aProxy) {

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
