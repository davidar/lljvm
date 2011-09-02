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

package lljvm.runtime;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract implementation of Context.
 * 
 * @author Joshua Arnold
 */
public abstract class AbstractContext implements Context {
    private static final Logger logger = Logger.getLogger(AbstractContext.class.getName());

    private volatile Map<Class<?>, ModuleReference> instances = Collections.emptyMap();

    private boolean closed;

    private final Object instantiationLock = new Object();

    protected AbstractContext() {
    }
    
    @Override
    public <T> T getModule(Class<T> clazz) {
        ModuleReference mod = instances.get(clazz);
        if (mod != null && mod.initialized)
            return clazz.cast(mod.module);
        synchronized (instantiationLock) {
            if (closed)
                throw new IllegalStateException("Context is closed");
            mod = instances.get(clazz);
            if (mod==null) {
                mod = setAndInitializeNewModule(clazz);
            }
        }
        return clazz.cast(mod.module);
    }


    @Override
    public <T> T getOptionalModule(Class<T> clazz) {
        ModuleReference mod = instances.get(clazz);
        return mod != null && mod.initialized ? clazz.cast(mod.module) : null;
    }
    
    /**
     * Explicitly sets the instance for the specified module.
     * @param <T> the type of the module
     * @param clazz the module's class
     * @param instance the module's instance.  Note that if {@code instance} implements {@link Module} then the 
     * {@link Module} life cycle methods will be called on it automatically.
     * @throws IllegalArgumentException if {@code instance} is <code>null</code>.
     * @throws IllegalStateException if the context is closed or it already has an instance for {@code clazz}.
     */
    protected <T> void setModule(Class<T> clazz, T instance) {
        if (instance==null)
            throw new IllegalArgumentException();
        synchronized(instantiationLock) {
            if (closed)
                throw new IllegalStateException("Context is closed");
            if (instances.containsKey(clazz))
                throw new IllegalStateException("There is already an instance for "+clazz);
            setAndInitializeModule(clazz, instance);            
        }        
    }

    public void close() {
        List<Throwable> errs = new ArrayList<Throwable>();
        synchronized (instantiationLock) {
            Object[] modules = instances.values().toArray();
            instances = Collections.emptyMap();
            closed = true;
            for (Object mod : modules) {
                try {
                    if (mod instanceof Module) {
                        ((Module) mod).destroy(this);
                    }
                } catch (Throwable t) {
                    errs.add(t);
                }
            }
        }
        if (errs.isEmpty())
            return;
        if (errs.size() > 1) {
            for (Throwable t : errs) {
                logger.log(Level.SEVERE, "Multiple errors occurred during close", t);
            }
        }
        Throwable first = errs.get(0);
        if (first instanceof java.lang.Error)
            throw (java.lang.Error) first;
        if (first instanceof RuntimeException)
            throw (RuntimeException) first;
        throw new RuntimeException(first);
    }

    /**
     * Instantiates the default implementation of the specified module.
     * <p>
     * This implementation attempts to instantiate the specified class by invoking its
     * no-argument constructor.  If no such constructor is accessible, an appropriate
     * {@link LinkageError} is thrown.  Subclasses may override this method to return
     * alternative implementations.
     * </p>
     * <p>
     * Note that if the returned instance implements {@link Module}, then {@code AbstractContext} will invoke
     * the {@link Module} life cycle methods on it.  
     * </p>
     * @param <T> the module's type
     * @param clazz the module's class.
     * @return an instance of {@code T}, or <code>null</code> to have {@code AbstractContext} attempt to instantiate {@code clazz} directly.
     */
    protected <T> T createModule(Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (IllegalAccessException e) {
            throw (IllegalAccessError) new IllegalAccessError(e.getMessage()).initCause(e);
        } catch (InstantiationException e) {
            throw (InstantiationError) new InstantiationError(e.getMessage()).initCause(e);
        } catch (NoSuchMethodException e) {
            throw (NoSuchMethodError) new NoSuchMethodError(e.getMessage()).initCause(e);
        } catch (InvocationTargetException e) {
            // Propagate the original exception as best we can
            if (e.getCause() instanceof RuntimeException)
                throw (RuntimeException) e.getCause();
            if (e.getCause() instanceof java.lang.Error)
                throw (java.lang.Error) e.getCause();
            throw new RuntimeException(e);
        }
    }
    
    
    private <T> ModuleReference setAndInitializeNewModule(Class<T> clazz) {
        return setAndInitializeModule(clazz, createModule(clazz));
    }
    
    private <T> ModuleReference setAndInitializeModule(Class<T> clazz, T inst) {
        ModuleReference mod = new ModuleReference(inst, false);
        synchronized(instantiationLock) {
            setModuleRef(clazz,mod);
            try {
                if (mod.module instanceof Module) {
                    ((Module) mod.module).initialize(this);
                }
            } finally {
                mod = mod.initialized();
                setModuleRef(clazz,mod);
            }
        }
        return mod;
    }
    
    private void setModuleRef(Class<?> clazz, ModuleReference ref) {
        synchronized(instantiationLock) {
            HashMap<Class<?>, ModuleReference> cpy = new HashMap<Class<?>, AbstractContext.ModuleReference>(instances);
            cpy.put(clazz, ref);
            instances = cpy;
        }
    }    
    

    private static final class ModuleReference {
        final Object module;
        final boolean initialized;

        ModuleReference(Object module, boolean initialized) {
            super();
            this.module = module;
            this.initialized = initialized;
        }

        ModuleReference initialized() {
            return new ModuleReference(module, true);
        }
    }

}
