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

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.V1_5;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class CompositeClassGenerator {
    class Visitor extends ClassAdapter {
        class MVisitor extends MethodAdapter {
            int access;
            String[] exceptions;
            String name, desc, signature;

            public MVisitor(int access, String name, String desc,
                            String signature, String[] exceptions,
                            MethodVisitor mv) {
                super(mv);
                this.access = access;
                this.name = name;
                this.desc = desc;
                this.signature = signature;
                this.exceptions = exceptions;
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                CompositeClassGenerator.this.visitMethod(mixIn, fieldName,
                                                         access, name, desc,
                                                         signature, exceptions);
            }
        }

        protected String fieldName;

        protected Type mixIn;

        public Visitor(Type mixIn, String fieldName) {
            super(new EmptyVisitor());
            this.mixIn = mixIn;
            this.fieldName = fieldName;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature,
                                                 exceptions);
            return new MVisitor(access, name, desc, signature, exceptions, mv);
        }
    }

    public static final String GENERATED_COMPOSITE_SUFFIX = "$composite";
    protected static final String MIX_IN_VAR_PREFIX = "mixIn_";

    public static ClassReader getClassReader(Class<?> clazz) {
        Type type = Type.getType(clazz);
        String classResourceName = '/' + type.getInternalName() + ".class";
        InputStream is = clazz.getResourceAsStream(classResourceName);
        if (is == null) {
            throw new VerifyError("cannot read class resource for: "
                                  + classResourceName);
        }
        ClassReader reader;
        try {
            reader = new ClassReader(is);
        } catch (IOException e) {
            VerifyError v = new VerifyError("cannot read class resource for: "
                                            + classResourceName);
            v.initCause(e);
            throw v;
        }
        return reader;
    }

    protected Class<?> composite;
    protected Type compositeType;
    protected Type generatedType;
    protected Map<Class<?>, Integer> mixInTypeMapping = new HashMap<Class<?>, Integer>();
    protected Class<?>[] mixInTypes;
    protected ClassWriter writer;

    public CompositeClassGenerator(Class<?> composite) {
        this.composite = composite;
        initialize();
    }

    public byte[] generateClassBits() {
        writer = new ClassWriter(COMPUTE_FRAMES);
        writer.visit(V1_5, ACC_PUBLIC, generatedType.getInternalName(), null,
                     Type.getType(Object.class).getInternalName(),
                     new String[] { compositeType.getInternalName() });
        generateConstructor();
        for (Map.Entry<Class<?>, Integer> entry : mixInTypeMapping.entrySet()) {
            String fieldName = MIX_IN_VAR_PREFIX + entry.getValue();
            writer.visitField(ACC_PRIVATE, fieldName,
                              Type.getType(entry.getKey()).getDescriptor(),
                              null, null);
            Visitor visitor = new Visitor(Type.getType(entry.getKey()),
                                          fieldName);
            getClassReader(entry.getKey()).accept(visitor, 0);
        }

        writer.visitEnd();
        return writer.toByteArray();
    }

    public String getGeneratedClassName() {
        return generatedType.getClassName();
    }

    protected void addMixInTypesTo(Class<?> iFace, Set<Class<?>> collected) {
        for (Class<?> extended : iFace.getInterfaces()) {
            if (!extended.equals(Object.class)) {
                collected.add(extended);
                addMixInTypesTo(extended, collected);
            }
        }
    }

    protected void generateConstructor() {
        Type[] orderedMixIns = new Type[mixInTypes.length];
        for (Map.Entry<Class<?>, Integer> entry : mixInTypeMapping.entrySet()) {
            orderedMixIns[entry.getValue()] = Type.getType(entry.getKey());
        }
        Method constructor = new Method(
                                        "<init>",
                                        Type.getMethodDescriptor(Type.VOID_TYPE,
                                                                 orderedMixIns));
        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, constructor,
                                                    null, new Type[] {}, writer);
        gen.visitCode();
        gen.loadThis();
        gen.invokeConstructor(Type.getType(Object.class),
                              new Method(
                                         "<init>",
                                         Type.getMethodDescriptor(Type.VOID_TYPE,
                                                                  new Type[] {})));
        for (int i = 0; i < orderedMixIns.length; i++) {
            gen.loadThis();
            gen.loadArg(i);
            gen.putField(generatedType, MIX_IN_VAR_PREFIX + i, orderedMixIns[i]);
        }
        gen.returnValue();
        gen.endMethod();
    }

    protected Map<Class<?>, Integer> getMixInTypeMapping() {
        return mixInTypeMapping;
    }

    protected Class<?>[] getMixInTypes() {
        return mixInTypes;
    }

    protected void initialize() {
        compositeType = Type.getType(composite);
        generatedType = Type.getObjectType(compositeType.getInternalName()
                                           + GENERATED_COMPOSITE_SUFFIX);
        mixInTypes = mixInTypesFor();
        for (int i = 0; i < mixInTypes.length; i++) {
            mixInTypeMapping.put(mixInTypes[i], i);
        }
    }

    protected Class<?>[] mixInTypesFor() {
        Comparator<Class<?>> comparator = new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getCanonicalName().compareTo(o2.getCanonicalName());
            }
        };
        Set<Class<?>> mixInTypes = new TreeSet<Class<?>>(comparator);
        addMixInTypesTo(composite, mixInTypes);
        return mixInTypes.toArray(new Class<?>[mixInTypes.size()]);
    }

    protected void visitMethod(Type mixIn, String fieldName, int access,
                               String name, String desc, String signature,
                               String[] exceptions) {
        Type[] exceptionTypes;
        if (exceptions != null) {
            exceptionTypes = new Type[exceptions.length];
            int i = 0;
            for (String exception : exceptions) {
                exceptionTypes[i++] = Type.getObjectType(exception);
            }
        } else {
            exceptionTypes = new Type[0];
        }
        access = access ^ ACC_ABSTRACT;
        Method method = new Method(name, desc);
        GeneratorAdapter gen = new GeneratorAdapter(access, method, null,
                                                    exceptionTypes, writer);
        gen.visitCode();
        gen.loadThis();
        gen.getField(generatedType, fieldName, mixIn);
        gen.loadArgs();
        gen.invokeInterface(mixIn, method);
        gen.returnValue();
        gen.endMethod();
    }

}
