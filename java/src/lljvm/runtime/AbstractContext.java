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

    public AbstractContext() {

    }

    @Override
    public <T> T getModule(Class<? extends T> clazz) {
        ModuleReference mod = instances.get(clazz);
        if (mod != null && mod.initialized)
            return clazz.cast(mod.module);
        synchronized (instantiationLock) {
            if (closed)
                throw new IllegalStateException("Context is closed");
            mod = instances.get(clazz);
            if (mod != null)
                return clazz.cast(mod.module);
            mod = new ModuleReference(newInstance(clazz), false);

            Map<Class<?>, ModuleReference> update = new HashMap<Class<?>, ModuleReference>(
                    instances);
            update.put(clazz, mod);
            instances = update;
            try {
                if (mod.module instanceof Module) {
                    ((Module) mod.module).initialize(this);
                }
            } finally {
                mod = mod.initialized();
                update = new HashMap<Class<?>, ModuleReference>(instances);
                update.put(clazz, mod);
                instances = update;
            }

        }
        return clazz.cast(mod.module);
    }

    // void

    @Override
    public <T> T getOptionalModule(Class<? extends T> clazz) {
        ModuleReference mod = instances.get(clazz);
        return mod != null && mod.initialized ? clazz.cast(mod.module) : null;
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

    private static Object newInstance(Class<?> clazz) {
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
