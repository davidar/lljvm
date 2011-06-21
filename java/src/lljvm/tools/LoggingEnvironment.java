/*
* Copyright (c) 2011 Joshua Arnold
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/
package lljvm.tools;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.ConsoleHandler;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Configures {@code java.util.logging} for the command line tools.
 * @author Joshua Arnold
 */
public class LoggingEnvironment implements Closeable {
    /**
     * Saves a hard reference to our loggers because some j.u.l implementations use weak references.
     */
    private static final IdentityHashMap<Object, Object> rememberedRefs = new IdentityHashMap<Object, Object>();
    
    private final List<String> nonLoggingArgs;
    private final List<String> args;
    
    private final Logger base;
    private final boolean saveUseParents;
    private final Level saveLevel;
    private final Filter saveFilter;
    private final Handler[] saveHandlers;
    
    private final AtomicBoolean closed = new AtomicBoolean();

    private LoggingEnvironment(List<String> args, List<String> nonLoggingArgs, Level logLevel) {
        this.nonLoggingArgs = nonLoggingArgs;
        this.args = args;
        
        base = Logger.getLogger("lljvm");
        saveUseParents = base.getUseParentHandlers();
        saveLevel = base.getLevel();
        saveFilter = base.getFilter();
        saveHandlers = base.getHandlers();
        
        base.setUseParentHandlers(false);
        base.setFilter(null);
        for(Handler h : saveHandlers)
            base.removeHandler(h);
        
        base.setLevel(logLevel);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new SimpleFormatter());
        ch.setLevel(logLevel);
        base.addHandler(ch);
        
        synchronized (rememberedRefs) {
            rememberedRefs.put(this, null);            
        }
    }
    
    /**
     * Creates a {@link LoggingEnvironment} and configures the appropriate j.u.l. loggers.
     * @param args the command line arguments used to run the tool. Standard logging-related settings,
     * such as {@code --verbose} are extracted from these.
     * @return the {@link LoggingEnvironment}
     */
    public static LoggingEnvironment setupLogging(String[] args) {
        List<String> argList = new ArrayList<String>(Arrays.asList(args));
        List<String> nonLoggingArgList = new ArrayList<String>();
        int verboseCount = 0;
        for(String a : args) {
            if ("--verbose".equals(a))
                verboseCount++;
            else 
                nonLoggingArgList.add(a);
        }
        Level level;
        if (verboseCount==0)
            level = Level.INFO;
        else if (verboseCount==1)
            level = Level.FINE;
        else if (verboseCount==2)
            level = Level.FINER;
        else
            level = Level.FINEST;
        return new LoggingEnvironment(Collections.unmodifiableList(argList), Collections.unmodifiableList(nonLoggingArgList),level);
    }
    
    /**
     * Returns a copy of the arguments used to create this logging environment.
     * @return the cli arguments
     */
    public String[] getArgs() {
        return args.toArray(new String[0]);
    }
    
    
    /**
     * Returns a copy of the arguments used to create this environment, but with the logging
     * related arguments removed.
     * @return the cli arguments, with logging-related arguments removed.
     */
    public String[] getNonLoggingArgs() {
        return nonLoggingArgs.toArray(new String[0]);
    }
    
    /**
     * Uninstalls this logging environment, and restores the corresponding j.u.l loggers to the
     * state they were in when the environment was {@linkplain #setupLogging(String[]) created}.
     * <p>
     * If the environment is intended to persist for the duration of the process, then there is no
     * need to call this method.
     * </p>
     */
    public void close() {
        if (!closed.compareAndSet(false, true))
            return;
        
        base.setUseParentHandlers(saveUseParents);
        base.setFilter(saveFilter);
        base.setLevel(saveLevel);
        for(Handler h : base.getHandlers())
            base.removeHandler(h);
        for(Handler h : saveHandlers)
            base.addHandler(h);
        synchronized (rememberedRefs) {
            rememberedRefs.remove(this);
        }
        
    }
    

}
