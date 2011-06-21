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
package lljvm.tools.ld;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import lljvm.util.ClassInfo;
import lljvm.util.ClassName;
import lljvm.util.ReflectionUtils;

public class AsmLinker {
    private static final Logger logger = Logger.getLogger(AsmLinker.class.getName());
    
    private final List<AsmSource> sources;
    private final ClassName unresolvedTarget;
    private final ClassLoader loader;      
    private final List<String> libraries;
    
    
    private final UncaughtExceptionHandler uncaughtExceptions = new UncaughtExceptionHandler();
    
    
    
    public AsmLinker(LinkerParameters params) {
        this(params,AsmLinker.class.getClassLoader());
    }
    
    public AsmLinker(LinkerParameters params, ClassLoader loader) {
        
        unresolvedTarget = params.getUnresolvedTarget() != null ? ClassName.from(params.getUnresolvedTarget()) : null;
        this.loader = loader;
        
        this.sources = params.getSources();
        this.libraries = params.getLibraryClasses();
        
    }
    
    
    
    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThrdFac(uncaughtExceptions));
        try {
            try {
                run(executor);
            } finally {                
                executor.shutdown();
                boolean didShutdown;
                try {
                    didShutdown = executor.awaitTermination(5,TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    didShutdown = false;
                }
                if (!didShutdown) {
                    logger.warning("Timeout waiting for executor service to shut down");
                }
            }
        } finally {
            executor.shutdownNow();
            for(Throwable t : uncaughtExceptions.drainUncaughtExceptions()) {
                logger.log(Level.SEVERE,"There was an uncaught exception in a worker thread", t);
            }
        }
    }

    private void run(ExecutorService executor) {
        //Run the inspection pass in the background
        List<Future<ClassInfo>> inspectionFutures = new ArrayList<Future<ClassInfo>>();
        for(AsmSource source : sources) {
            inspectionFutures.add(executor.submit(new InspectionPass(source)));
        }
        
        //Resolve libraries on this thread while the inspection pass takes place.
        List<ClassInfo> libInfos = resolveLibs();
        
        //Get results for inspection pass.
        List<ClassInfo> inspectionResults = waitFor(inspectionFutures);        
        checkForDuplicates(inspectionResults);
        
        //Prep the resolver
        List<ClassInfo> preResolved = concat(inspectionResults,libInfos);
        List<ClassName> implicitNames = toNames(preResolved);        
        Resolver resolver = new DefaultResolver(preResolved, implicitNames, unresolvedTarget, loader);
        
        //Run the resolution passes
        List<Future<Object>> linkFutures = new ArrayList<Future<Object>>();
        for(int i=0;i<sources.size();i++) {
            ResolvePass pass = new ResolvePass(sources.get(i), inspectionResults.get(i), resolver);
            linkFutures.add(executor.submit(pass));
        }        
        waitFor(linkFutures);
        
        
    }
    
    
    private List<ClassInfo> resolveLibs() {
        List<ClassInfo> libInfos = new ArrayList<ClassInfo>(libraries.size());
        Set<ClassName> libsSeen = new HashSet<ClassName>();
        for(String lib : libraries) {
            ClassName libName = ClassName.from(lib);
            if (!libsSeen.add(libName))
                continue;
            Class<?> libClass;
            try {
                libClass = Class.forName(libName.getJavaName(), false, loader);
            } catch (ClassNotFoundException e) {
                throw new AsmLinkerException("Library class "+libName+" not found",e);
            } catch (LinkageError e) {
                throw new AsmLinkerException("Library class "+libName+" could not be loaded",e);
            }
            libInfos.add(ReflectionUtils.infoFor(libClass));
        }
        return libInfos;
    }
    
    
    private static <T> List<T> waitFor(List<? extends Future<? extends T>> futures) {
        List<T> res = new ArrayList<T>(futures.size());
        List<Throwable> exc = new ArrayList<Throwable>();
        for(Future<? extends T> future : futures) {
            T oneRes = null;
            Throwable oneExc = null;
            try {
                oneRes = future.get();
            } catch (ExecutionException e) {
                oneExc = e.getCause()!=null ? e.getCause() : e;
            } catch (CancellationException e) {
                oneExc = e;
            } catch (InterruptedException e) {
                oneExc = e;
                Thread.currentThread().interrupt();
            }
            res.add(oneRes);
            if (oneExc!=null)
                exc.add(oneExc);
        }
        throwUnchecked(exc);
        return Collections.unmodifiableList(res);
    }
    
    private static void throwUnchecked(Collection<? extends Throwable> exceptions) {
        if (exceptions==null || exceptions.isEmpty())
            return;
        Iterator<? extends Throwable> iter = exceptions.iterator();
        Throwable t = iter.next();
        while(iter.hasNext()) {
            logger.log(Level.SEVERE,"One of multiple exceptions",iter.next());
        }
        throwUnchecked(t);
        throw new AssertionError(); //Can't get here
    }
    
    
    private static void throwUnchecked(Throwable t) {
        if (t instanceof RuntimeException)
            throw (RuntimeException)t;
        if (t instanceof Error)
            throw (Error)t;
        throw new RuntimeException(t);
    }
    
    private static <T> List<T> concat(Collection<? extends T> c1, Collection<? extends T> c2) {
        ArrayList<T> res = new ArrayList<T>(c1.size()+c2.size());
        res.addAll(c1);
        res.addAll(c2);
        return res;
    }
    
    private static List<ClassName> toNames(Collection<? extends ClassInfo> infos) {
        ArrayList<ClassName> res = new ArrayList<ClassName>(infos.size());
        for(ClassInfo info : infos)
            res.add(info.getName());
        return res;
    }
    
    private static void checkForDuplicates(Collection<? extends ClassInfo> infos) {
        HashSet<ClassName> namesSeen = new HashSet<ClassName>();
        for(ClassInfo info : infos) {
            if (!namesSeen.add(info.getName()))
                throw new AsmLinkerException("Duplicate class : "+info.getName().getBinaryName());
        }
    }
    
    
    private static class ResolvePass implements Callable<Object> {
        private final AsmSource source;
        private final ClassInfo classInfo;
        private final Resolver resolver;
        ResolvePass(AsmSource source, ClassInfo classInfo, Resolver resolver) {
            super();
            this.source = source;
            this.classInfo = classInfo;
            this.resolver = resolver;
        }
        @Override
        public Object call() {
            ResolutionPass p2 = new ResolutionPass(source,classInfo,resolver);
            ResolvedTargets targets = p2.call();
            WriterPass p3 = new WriterPass(source,classInfo,targets);
            p3.call();
            try {
                source.close();
            } catch (IOException e) {
                throw new AsmLinkerException(e).withFileName(source.getName());
            }
            return null;
        }
        
    }
    
    private static class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        private List<Throwable> uncaughts = new ArrayList<Throwable>(4);
        UncaughtExceptionHandler() {}
        @Override
        public synchronized void uncaughtException(Thread t, Throwable e) {
            uncaughts.add(e);
        }
        synchronized List<Throwable> drainUncaughtExceptions() {
            List<Throwable> res = Collections.unmodifiableList(uncaughts);
            uncaughts = new ArrayList<Throwable>(4);
            return res;
        }
    }
    
    private static class ThrdFac implements ThreadFactory {
        private static final AtomicInteger threadCounter = new AtomicInteger();
        private final Thread.UncaughtExceptionHandler uncaughtHandler;
        
        ThrdFac(java.lang.Thread.UncaughtExceptionHandler uncaughtHandler) {
            super();
            this.uncaughtHandler = uncaughtHandler;
        }

        @Override
        public Thread newThread(final Runnable r) {
            Thread t = new Thread(r,"Linker-"+threadCounter.getAndIncrement());
            t.setUncaughtExceptionHandler(uncaughtHandler);
            t.setDaemon(true);
            return t;
        }
        
    }
}
