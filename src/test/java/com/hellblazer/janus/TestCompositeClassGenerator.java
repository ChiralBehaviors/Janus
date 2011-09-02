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

import java.io.PrintWriter;
import java.util.Map;

import junit.framework.TestCase;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import com.hellblazer.janus.CompositeAssembler.CompositeClassLoader;
import com.hellblazer.janus.testClasses.Composite1;
import com.hellblazer.janus.testClasses.MixIn1;
import com.hellblazer.janus.testClasses.MixIn2;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class TestCompositeClassGenerator extends TestCase {
    public void testGeneratedBits() {
        CompositeClassGenerator generator = new CompositeClassGenerator(
                                                                        Composite1.class);
        byte[] generatedBits = generator.generateClassBits();
        assertNotNull(generatedBits);
        TraceClassVisitor tcv = new TraceClassVisitor(
                                                      new PrintWriter(
                                                                      System.out));
        CheckClassAdapter cv = new CheckClassAdapter(tcv);
        ClassReader reader = new ClassReader(generatedBits);
        reader.accept(cv, 0);
    }

    public void testGeneratedClass() {
        CompositeClassGenerator generator = new CompositeClassGenerator(
                                                                        Composite1.class);
        CompositeClassLoader loader = new CompositeClassLoader(
                                                               Composite1.class.getClassLoader());
        Class<?> generated = loader.define(generator.getGeneratedClassName(),
                                           generator.generateClassBits());
        assertNotNull(generated);
    }

    public void testInitialization() {
        CompositeClassGenerator generator = new CompositeClassGenerator(
                                                                        Composite1.class);
        Map<Class<?>, Integer> mixInMap = generator.getMixInTypeMapping();
        assertEquals(2, mixInMap.size());
        assertEquals(new Integer(0), mixInMap.get(MixIn1.class));
        assertEquals(new Integer(1), mixInMap.get(MixIn2.class));
    }
}
