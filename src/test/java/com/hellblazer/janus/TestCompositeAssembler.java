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

import junit.framework.TestCase;

import com.hellblazer.janus.testClasses.Composite1;
import com.hellblazer.janus.testClasses.MixIn1Impl;
import com.hellblazer.janus.testClasses.MixIn2Impl;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class TestCompositeAssembler extends TestCase {

    public void testConstruct() {
        CompositeAssembler<Composite1> assembler = new CompositeAssembler<Composite1>(
                                                                                      Composite1.class);
        MixIn1Impl mixIn1 = new MixIn1Impl();
        MixIn2Impl mixIn2 = new MixIn2Impl();

        Composite1 instance = assembler.construct(mixIn2, mixIn1);
        assertNotNull(instance);
        assertEquals("MixIn1-Method1", instance.m11());
        assertEquals("MixIn1-Method2", instance.m12());
        assertEquals("Hello",
                     instance.m13("Goodbye", "Hello", "Not here at the moment"));
        assertEquals("MixIn2-Method1", instance.m21());
        assertEquals("MixIn2-Method2", instance.m22());
        instance.m23("Hello");
        assertEquals("Hello", MixIn2Impl.RESULT);
        assertEquals(0, instance.m24());
    }

    public void testFacets() {
        CompositeAssembler<Composite1> assembler = new CompositeAssembler<Composite1>(
                                                                                      Composite1.class);
        MixIn1Impl mixIn1 = new MixIn1Impl();
        MixIn2Impl mixIn2 = new MixIn2Impl();

        Composite1 instance = assembler.construct(mixIn2, mixIn1);
        assertNotNull(instance);
        assertSame(mixIn2, instance.getFriend1());
        assertSame(mixIn1, instance.getFriend2());
    }

    public void testThis() {
        CompositeAssembler<Composite1> assembler = new CompositeAssembler<Composite1>(
                                                                                      Composite1.class);
        MixIn1Impl mixIn1 = new MixIn1Impl();
        MixIn2Impl mixIn2 = new MixIn2Impl();

        Composite1 instance = assembler.construct(mixIn2, mixIn1);
        assertNotNull(instance);
        assertSame(instance, instance.getComposite());
    }
}
