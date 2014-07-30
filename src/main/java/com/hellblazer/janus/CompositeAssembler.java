/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Janus Composite Object Framework.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hellblazer.janus;

import static com.hellblazer.janus.CompositeClassGenerator.GENERATED_COMPOSITE_SUFFIX;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class CompositeAssembler<T> {
    static class CompositeClassLoader extends ClassLoader {
        public CompositeClassLoader(ClassLoader parent) {
            super(parent);
        }

        Class<?> define(String compositeName, byte[] definition) {
            return defineClass(compositeName, definition, 0, definition.length);
        }
    }

    private static ClassLoader getLoader(Class<?> composite) {
        return composite.getClassLoader();
    }

    protected final Class<T>               generated;
    protected final CompositeClassLoader   loader;
    protected final Map<Class<?>, Integer> mixInMap;

    protected final Class<?>[]             mixIns;

    public CompositeAssembler(Class<T> composite) {
        this(composite, getLoader(composite));
    }

    @SuppressWarnings("unchecked")
    public CompositeAssembler(Class<T> composite, final ClassLoader parentLoader) {
        if (!composite.isInterface()) {
            throw new IllegalArgumentException(
                                               "Supplied composite class is not an interface: "
                                                       + composite);
        }
        CompositeClassGenerator generator = new CompositeClassGenerator(
                                                                        composite);
        mixInMap = generator.getMixInTypeMapping();
        mixIns = generator.getMixInTypes();
        Class<T> clazz;
        loader = AccessController.doPrivileged(new PrivilegedAction<CompositeClassLoader>() {
            @Override
            public CompositeClassLoader run() {
                return new CompositeClassLoader(parentLoader);
            }
        });

        try {
            clazz = (Class<T>) composite.getClassLoader().loadClass(composite.getCanonicalName()
                                                                            + GENERATED_COMPOSITE_SUFFIX);
        } catch (ClassNotFoundException e) {
            clazz = (Class<T>) loader.define(generator.getGeneratedClassName(),
                                             generator.generateClassBits());
        }
        generated = clazz;
    }

    public T construct(Object... mixInInstances) {
        if (mixInInstances == null) {
            throw new IllegalArgumentException(
                                               "supplied mixin instances must not be null");
        }
        if (mixInInstances.length != mixIns.length) {
            throw new IllegalArgumentException(
                                               "wrong number of arguments supplied");
        }
        Object[] arguments = new Object[mixIns.length];
        for (Object mixIn : mixInInstances) {
            for (Map.Entry<Class<?>, Integer> mapping : mixInMap.entrySet()) {
                if (mapping.getKey().isAssignableFrom(mixIn.getClass())) {
                    arguments[mapping.getValue()] = mixIn;
                }
            }
        }
        T instance = constructInstance(arguments);
        inject(instance, arguments);
        return instance;
    }

    protected T constructInstance(Object[] arguments) {
        Constructor<T> constructor = getConstructor();
        try {
            return constructor.newInstance(arguments);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                                            "Illegal arguments in constructing composite",
                                            e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(
                                            "Unexpected error in constructing composite",
                                            e.getTargetException());
        } catch (InstantiationException e) {
            throw new IllegalStateException("Cannot instantiate composite", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                                            "Cannot access constructor for composite",
                                            e);
        }
    }

    protected Constructor<T> getConstructor() {
        Constructor<T> constructor;
        try {
            constructor = generated.getConstructor(mixIns);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                                            "Cannot find constructor on generated composite class",
                                            e);
        }
        return constructor;
    }

    protected void inject(Object value, Field field, Object instance,
                          Class<?> clazz) {
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Field: " + field
                                            + " is not a part of class: "
                                            + clazz, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to set field: " + field
                                            + " on class: " + clazz, e);
        }
    }

    protected void inject(T instance, Object[] facets) {
        for (int i = 0; i < facets.length; i++) {
            Class<?> mixIn = facets[i].getClass();
            Object mixInInstance = facets[i];
            for (Field field : mixIn.getDeclaredFields()) {
                if (!injectFacet(field, facets, mixInInstance, mixIn)) {
                    injectThis(instance, mixIn, mixInInstance, field);
                }
            }
        }
    }

    protected boolean injectFacet(Field field, Object[] facets,
                                  Object instance, Class<?> clazz) {
        Facet facetAnnotation = field.getAnnotation(Facet.class);
        if (facetAnnotation != null) {
            for (Object facet : facets) {
                if (field.getType().isAssignableFrom(facet.getClass())) {
                    inject(facet, field, instance, clazz);
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean injectThis(T instance, Class<?> mixIn,
                                 Object mixInInstance, Field field) {
        This thisAnnotation = field.getAnnotation(This.class);
        if (thisAnnotation != null) {
            if (field.getType().isAssignableFrom(instance.getClass())) {
                inject(instance, field, mixInInstance, mixIn);
                return true;
            }
        }
        return false;
    }
}
