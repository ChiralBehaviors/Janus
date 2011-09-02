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
package com.hellblazer.janus.testClasses;

import com.hellblazer.janus.Facet;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class MixIn2Impl implements MixIn2 {
    @Facet
    MixIn1 friend;

    public static String RESULT = null;

    @Override
    public MixIn1 getFriend2() {
        return friend;
    }

    @Override
    public String m21() {
        return "MixIn2-Method1";
    }

    @Override
    public String m22() {
        return "MixIn2-Method2";
    }

    @Override
    public void m23(String arg) {
        RESULT = arg;
    }

    @Override
    public int m24() {
        return 0;
    }
}
