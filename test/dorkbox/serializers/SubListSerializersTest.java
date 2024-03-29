/*
 * Copyright 2023 dorkbox, llc
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;

/**
 * Test for {@link SubListSerializers}.
 *
 * @author <a href="mailto:martin.grotzke@javakaffee.de">Martin Grotzke</a>
 */
public class SubListSerializersTest {
    
    private Kryo _kryo;

    @Before
    public void beforeClass() {
        _kryo = new Kryo();

        _kryo.setRegistrationRequired(false);

        SubListSerializers.addDefaultSerializers(_kryo);

        final DefaultInstantiatorStrategy instantiatorStrategy = new DefaultInstantiatorStrategy();
        instantiatorStrategy.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
        _kryo.setInstantiatorStrategy(instantiatorStrategy);
    }

    private void doTest(final List<TestEnum> subList) {
        final byte[] serialized = KryoTest.Companion.serialize( _kryo, subList );
        @SuppressWarnings( "unchecked" )
        final List<TestEnum> deserialized = KryoTest.Companion.deserialize( _kryo, serialized, subList.getClass() );

        assertEquals( deserialized, subList );
        assertEquals( deserialized.remove( 0 ), subList.remove( 0 ) );
    }

    private void doTestCopy(final List<TestEnum> subList) {
        final List<TestEnum> copy = _kryo.copy( subList );

        assertEquals( copy, subList );
        assertEquals( copy.remove( 0 ), subList.remove( 0 ) );
    }

    @Test
    public void testSubList () throws Exception {
        final List<TestEnum> subList = new LinkedList<TestEnum>( Arrays.asList( TestEnum.values() ) ).subList( 1, 2 );
        doTest(subList);
    }

    @Test
    public void testCopySubList () throws Exception {
        final List<TestEnum> subList = new LinkedList<TestEnum>( Arrays.asList( TestEnum.values() ) ).subList( 1, 2 );
        doTestCopy(subList);
    }

    @Test
    public void testSubListSubList () throws Exception {
        final List<TestEnum> subList = new LinkedList<TestEnum>( Arrays.asList( TestEnum.values() ) ).subList( 1, 3 ).subList(1, 2);
        doTest(subList);
    }

    @Test
    public void testCopySubListSubList () throws Exception {
        final List<TestEnum> subList = new LinkedList<TestEnum>( Arrays.asList( TestEnum.values() ) ).subList( 1, 3 ).subList(1, 2);
        doTestCopy(subList);
    }

    @Test
    public void testArrayListSubList () throws Exception {
        final List<TestEnum> subList = new ArrayList<TestEnum>( Arrays.asList( TestEnum.values() ) ).subList( 1, 2 );
        doTest(subList);
    }

    @Test
    public void testCopyArrayListSubList () throws Exception {
        final List<TestEnum> subList = new ArrayList<TestEnum>( Arrays.asList( TestEnum.values() ) ).subList( 1, 2 );
        doTestCopy(subList);
    }

    @Test
    public void testArrayListSubListSubList () throws Exception {
        final List<TestEnum> subList = new ArrayList<TestEnum>( Arrays.asList( TestEnum.values() ) ).subList( 1, 3 ).subList(1, 2);
        doTest(subList);
    }

    @Test
    public void testCopyArrayListSubListSubList () throws Exception {
        final List<TestEnum> subList = new ArrayList<TestEnum>( Arrays.asList( TestEnum.values() ) ).subList( 1, 3 ).subList(1, 2);
        doTestCopy(subList);
    }

    @Test
    public void testArrayListSubListWithSharedItems () throws Exception {
        final List<String> mylist = arrayList("1", "1", "2", "1", "1");
        final List<String> subList = mylist.subList(0, 5);

        final byte[] serialized = KryoTest.Companion.serialize( _kryo, subList );
        @SuppressWarnings( "unchecked" )
        final List<String> deserialized = KryoTest.Companion.deserialize( _kryo, serialized, subList.getClass() );

        assertEquals( deserialized, subList );
        assertEquals( deserialized, mylist );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void testNestedArrayListSubListWithSharedItems_1() throws Exception {
        final List<String> l1 = arrayList("1", "1", "2");
        final List<String> l1s1 = l1.subList(0, 3);
        
        final List<String> l1s2 = l1.subList(1, 3);

        final List<String> l2 = arrayList("1", "2", "3");
        final List<String> l2s1 = l2.subList(0, 3);
        
        final List<List<String>> lists = new ArrayList<List<String>>(Arrays.asList(l1s1, l1s2, l2s1, l1, l2));

        final byte[] serialized = KryoTest.Companion.serialize( _kryo, lists );
        final List<List<String>> deserialized = KryoTest.Companion.deserialize( _kryo, serialized, lists.getClass() );

        assertEquals( deserialized, lists );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void testNestedArrayListSubListWithSharedItems_2() throws Exception {
        final List<String> l1 = arrayList("1", "1", "2");
        final List<String> l1s1 = l1.subList(0, 3);
        
        final List<String> l1s2 = l1.subList(1, 3);

        final List<String> l2 = arrayList("1", "2", "3");
        final List<String> l2s1 = l2.subList(0, 3);
        
        final List<List<String>> lists = new ArrayList<List<String>>(Arrays.asList(l1, l2, l1s1, l1s2, l2s1));

        final byte[] serialized = KryoTest.Companion.serialize( _kryo, lists );
        final List<List<String>> deserialized = KryoTest.Companion.deserialize( _kryo, serialized, lists.getClass() );

        assertEquals( deserialized, lists );
    }
    
    static enum TestEnum {
        ITEM1, ITEM2, ITEM3
    }
    
    private static <T> ArrayList<T> arrayList(final T ... items) {
        return new ArrayList<T>(Arrays.asList(items));
    }

}
