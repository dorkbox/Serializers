/*
 * Copyright 2021 dorkbox, llc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dorkbox.serializers;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.esotericsoftware.kryo.Kryo;

/**
 * A test case for the {@link EnumMapSerializer}.
 */
public
class EnumMapSerializerTest {
    private static
    enum Vipers {
        SNAKE_CHARMER, BLACK_MAMBA, COTTONMOUTH, COPPERHEAD, CALIFORNIA_MOUNTAIN_SNAKE, SIDEWINDER
    }


    private static
    enum Colors {
        BLUE, ORANGE, PINK, WHITE, BROWN, BLONDE
    }


    private Kryo _kryo;
    private EnumMap<Vipers, Set<String>> _original;

    @Before
    public
    void beforeTest() {
        _kryo = new Kryo();
        _kryo.register(EnumMap.class, new EnumMapSerializer());
        _original = new EnumMap<Vipers, Set<String>>(Vipers.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test(expected = ClassCastException.class)
    public
    void testCopyEmpty() throws Exception {
        EnumMap copy = _kryo.copy(_original);
        // The next statement asserts that the key type of the copy is initialized correctly - 
        // it should throw the expected ClassCastException.
        copy.put(Colors.BROWN, new HashSet<String>());
    }

    @Test
    public
    void testDeepCopy() throws Exception {
        _kryo.register(java.util.HashSet.class);

        final Set<String> mambaAka = new HashSet<String>();
        mambaAka.add("Beatrix Kiddo");
        mambaAka.add("The Bride");
        _original.put(Vipers.BLACK_MAMBA, mambaAka);

        EnumMap<Vipers, Set<String>> copy = _kryo.copy(_original);
        assertNotSame(_original, copy);
        assertTrue(copy.containsKey(Vipers.BLACK_MAMBA));
        assertNotSame(_original.get(Vipers.BLACK_MAMBA), copy.get(Vipers.BLACK_MAMBA));
        assertEquals(_original, copy);
    }
}
