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
package dorkbox.serializers

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.KryoException
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.DefaultSerializers
import dorkbox.serializers.TestClasses.ClassWithoutDefaultConstructor
import dorkbox.serializers.TestClasses.CounterHolder
import dorkbox.serializers.TestClasses.CounterHolderArray
import dorkbox.serializers.TestClasses.Email
import dorkbox.serializers.TestClasses.HashMapWithIntConstructorOnly
import dorkbox.serializers.TestClasses.HolderArray
import dorkbox.serializers.TestClasses.HolderList
import dorkbox.serializers.TestClasses.MyContainer
import dorkbox.serializers.TestClasses.Person
import dorkbox.serializers.TestClasses.Person.Gender
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Modifier
import java.math.BigDecimal
import java.net.URI
import java.sql.Time
import java.sql.Timestamp

import java.util.*
import java.util.concurrent.atomic.*
import java.util.regex.*

/**
 * Test for [Kryo] serialization.
 *
 * @author [Martin Grotzke](mailto:martin.grotzke@javakaffee.de)
 */
class KryoTest {
    @Volatile
    private var _kryo: Kryo = Kryo()

    @Before
    fun beforeTest() {
        _kryo = object : Kryo() { //            @Override
            //            public Serializer<?> getDefaultSerializer( final Class type ) {
            //                if ( EnumSet.class.isAssignableFrom( type ) ) {
            //                    return new EnumMapSerializer();
            //                }
            //                if ( EnumMap.class.isAssignableFrom( type ) ) {
            //                    return new EnumMapSerializer();
            //                }
            //                return super.getDefaultSerializer( type );
            //            }
        }
        _kryo.isRegistrationRequired = false
        _kryo.register(listOf("").javaClass, DefaultSerializers.CollectionsSingletonListSerializer())
        _kryo.register(Pattern::class.java, RegexSerializer())
        _kryo.register(EnumMap::class.java, EnumMapSerializer())
        _kryo.register(UUID::class.java, DefaultSerializers.UUIDSerializer())

        UnmodifiableCollectionsSerializer.registerSerializers(_kryo)
        SynchronizedCollectionsSerializer.registerSerializers(_kryo)
    }

    @Test
    @Throws(Exception::class)
    fun testSingletonList() {
        val obj: List<*> = listOf("foo")
        val deserialized = deserialize(serialize(obj), obj.javaClass)
        assertDeepEquals(deserialized, obj)
    }

    @Test
    @Throws(Exception::class)
    fun testCopySingletonList() {
        val obj: List<*> = listOf("foo")
        val copy = _kryo.copy(obj)
        assertDeepEquals(copy, obj)
    }

    @Test
    @Throws(Exception::class)
    fun testSingletonSet() {
        val obj: Set<*> = setOf("foo")
        val deserialized = deserialize(serialize(obj), obj.javaClass)
        assertDeepEquals(deserialized, obj)
    }

    @Test
    @Throws(Exception::class)
    fun testCopySingletonSet() {
        val obj: Set<*> = setOf("foo")
        val copy = _kryo.copy(obj)
        assertDeepEquals(copy, obj)
    }

    @Test
    @Throws(Exception::class)
    fun testSingletonMap() {
        val obj: Map<*, *> = Collections.singletonMap("foo", "bar")
        val deserialized = deserialize(serialize(obj), obj.javaClass)
        assertDeepEquals(deserialized, obj)
    }

    @Test
    @Throws(Exception::class)
    fun testCopySingletonMap() {
        val obj: Map<*, *> = Collections.singletonMap("foo", "bar")
        val copy = _kryo.copy(obj)
        assertDeepEquals(copy, obj)
    }

    @Test
    @Throws(Exception::class)
    fun testEnumSet() {
        val set: EnumSet<*> = EnumSet.allOf(Gender::class.java)
        val deserialized = deserialize(serialize(set), set.javaClass)
        assertDeepEquals(deserialized, set)
    }

    @Test
    @Throws(Exception::class)
    fun testEmptyEnumSet() {
        val set: EnumSet<*> = EnumSet.allOf(Gender::class.java)
        val deserialized = deserialize(serialize(set), set.javaClass)
        assertDeepEquals(deserialized, set)
    }

    @Test
    @Throws(Exception::class)
    fun testCopyEnumSet() {
        val set: EnumSet<*> = EnumSet.allOf(Gender::class.java)
        val copy = _kryo.copy(set)
        assertDeepEquals(copy, set)
    }

    @Test
    @Throws(Exception::class)
    fun testEnumMap() {
        val map = EnumMap<Gender, String>(Gender::class.java)
        val value = "foo"
        map[Gender.FEMALE] = value
        // Another entry with the same value - to check reference handling
        map[Gender.MALE] = value
        val deserialized = deserialize(serialize(map), map.javaClass)
        assertDeepEquals(deserialized, map)
    }

    @Test(expected = KryoException::class)
    @Throws(Exception::class)
    fun testEmptyEnumMap() {
        val map = EnumMap<Gender, String>(Gender::class.java)
        val deserialized = deserialize(serialize(map), map.javaClass)
        assertDeepEquals(deserialized, map)
    }

    @Test
    @Throws(Exception::class)
    fun testCopyEnumMap() {
        val map = EnumMap<Gender, String>(Gender::class.java)
        val value = "foo"
        map[Gender.FEMALE] = value
        val copy = _kryo.copy(map)
        assertDeepEquals(copy, map)
    }

    /**
     * test that insertion order is retained.
     */
    @Test
    @Throws(Exception::class)
    fun testCopyForIterateMapSerializer() {
        val map: MutableMap<Double, String> = LinkedHashMap()
        // use doubles as e.g. integers hash to the value...
        for (i in 0..9) {
            map[java.lang.Double.valueOf(i.toString() + "." + Math.abs(i))] = "value: $i"
        }
        val deserialized: Map<Double, String> = deserialize(serialize(map), map.javaClass)
        assertDeepEquals(deserialized, map)
    }

    @Test
    @Throws(Exception::class)
    fun testGregorianCalendar() {
        val cal = TestClasses.Holder(Calendar.getInstance(Locale.ENGLISH))
        val deserialized = deserialize(serialize(cal), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, cal)


        val item = deserialized.item as Calendar
        Assert.assertEquals(item.timeInMillis, cal.item.timeInMillis)
        Assert.assertEquals(item.timeZone, cal.item.timeZone)
        Assert.assertEquals(item.minimalDaysInFirstWeek.toLong(), cal.item.minimalDaysInFirstWeek.toLong())
        Assert.assertEquals(item.firstDayOfWeek.toLong(), cal.item.firstDayOfWeek.toLong())
        Assert.assertEquals(item.isLenient, cal.item.isLenient)
    }

    @Test
    @Throws(Exception::class)
    fun testCopyGregorianCalendar() {
        val cal = TestClasses.Holder(Calendar.getInstance(Locale.ENGLISH))
        val copy = _kryo.copy(cal)
        assertDeepEquals(copy, cal)
        Assert.assertEquals(copy.item.timeInMillis, cal.item.timeInMillis)
        Assert.assertEquals(copy.item.timeZone, cal.item.timeZone)
        Assert.assertEquals(copy.item.minimalDaysInFirstWeek.toLong(), cal.item.minimalDaysInFirstWeek.toLong())
        Assert.assertEquals(copy.item.firstDayOfWeek.toLong(), cal.item.firstDayOfWeek.toLong())
        Assert.assertEquals(copy.item.isLenient, cal.item.isLenient)
    }

    @Test
    @Throws(Exception::class)
    fun testJavaUtilDate() {
        val cal = TestClasses.Holder(java.util.Date(System.currentTimeMillis()))
        val deserialized = deserialize(serialize(cal), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, cal)
        val item = deserialized.item as Date
        Assert.assertEquals(item.time, cal.item.time)
    }

    @Test
    @Throws(Exception::class)
    fun testCopyJavaUtilDate() {
        val cal = TestClasses.Holder(java.util.Date(System.currentTimeMillis()))
        val copy = _kryo.copy(cal)
        assertDeepEquals(copy, cal)
        Assert.assertEquals(copy.item.time, cal.item.time)
    }

    @Test
    @Throws(Exception::class)
    fun testJavaSqlTimestamp() {
        val cal = TestClasses.Holder(Timestamp(System.currentTimeMillis()))
        val deserialized = deserialize(serialize(cal), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, cal)
        val item = deserialized.item as Timestamp
        Assert.assertEquals(item.time, cal.item.time)
    }

    @Test
    @Throws(Exception::class)
    fun testCopyJavaSqlTimestamp() {
        val cal = TestClasses.Holder(Timestamp(System.currentTimeMillis()))
        val copy = _kryo.copy(cal)
        assertDeepEquals(copy, cal)
        Assert.assertEquals(copy.item.time, cal.item.time)
    }

    @Test
    @Throws(Exception::class)
    fun testJavaSqlDate() {
        val date = TestClasses.Holder(Date(System.currentTimeMillis()))
        val deserialized = deserialize(serialize(date), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, date)
        val item = deserialized.item as Date
        Assert.assertEquals(item.time, date.item.time)
    }

    @Test
    @Throws(Exception::class)
    fun testCopyJavaSqlDate() {
        val date = TestClasses.Holder(Date(System.currentTimeMillis()))
        val copy = _kryo.copy(date)
        assertDeepEquals(copy, date)
        Assert.assertEquals(copy.item.time, date.item.time)
    }

    @Test
    @Throws(Exception::class)
    fun testJavaSqlTime() {
        val time = TestClasses.Holder(Time(System.currentTimeMillis()))
        val deserialized = deserialize(serialize(time), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, time)
        val item = deserialized.item as Time
        Assert.assertEquals(item.time, time.item.time)
    }

    @Test
    @Throws(Exception::class)
    fun testCopyJavaSqlTime() {
        val time = TestClasses.Holder(Time(System.currentTimeMillis()))
        val copy = _kryo.copy(time)
        assertDeepEquals(copy, time)
        Assert.assertEquals(copy.item.time, time.item.time)
    }

    @Test
    @Throws(Exception::class)
    fun testBitSet() {
        val bitSet = BitSet(10)
        bitSet.flip(2)
        bitSet.flip(4)
        val holder = TestClasses.Holder(bitSet)
        val deserialized = deserialize(serialize(holder), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, holder)
    }

    @Test
    @Throws(Exception::class)
    fun testCopyBitSet() {
        val bitSet = BitSet(10)
        bitSet.flip(2)
        bitSet.flip(4)
        val copy = _kryo.copy(bitSet)
        assertDeepEquals(copy, bitSet)
    }

    @Test
    @Throws(Exception::class)
    fun testURI() {
        val uri = TestClasses.Holder(URI("http://www.google.com"))
        val deserialized = deserialize(serialize(uri), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, uri)
    }

    @Test
    @Throws(Exception::class)
    fun testCopyURI() {
        val uri = TestClasses.Holder(URI("http://www.google.com"))
        val copy = _kryo.copy(uri)
        assertDeepEquals(copy, uri)
    }

    @Test
    @Throws(Exception::class)
    fun testUUID() {
        val uuid = TestClasses.Holder(UUID.randomUUID())
        val deserialized = deserialize(serialize(uuid), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, uuid)
    }

    @Test
    @Throws(Exception::class)
    fun testCopyUUID() {
        val uuid = TestClasses.Holder(UUID.randomUUID())
        val copy = _kryo.copy(uuid)
        assertDeepEquals(copy, uuid)
    }

    @Test
    @Throws(Exception::class)
    fun testRegex() {
        val pattern = TestClasses.Holder(Pattern.compile("regex"))
        val deserialized = deserialize(serialize(pattern), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, pattern)
        val patternWithFlags = TestClasses.Holder(Pattern.compile("\n", Pattern.MULTILINE or Pattern.CASE_INSENSITIVE))
        val deserializedWithFlags = deserialize(serialize(patternWithFlags), TestClasses.Holder::class.java)
        assertDeepEquals(deserializedWithFlags, patternWithFlags)
    }

    @Test
    @Throws(Exception::class)
    fun testCopyRegex() {
        val pattern = TestClasses.Holder(Pattern.compile("regex"))
        val copy = _kryo.copy(pattern)
        assertDeepEquals(copy, pattern)
        val patternWithFlags = TestClasses.Holder(Pattern.compile("\n", Pattern.MULTILINE or Pattern.CASE_INSENSITIVE))
        val copyWithFlags = _kryo.copy(patternWithFlags)
        assertDeepEquals(copyWithFlags, patternWithFlags)
    }

    @Test
    @Throws(Exception::class)
    fun testStringBuffer() {
        val stringBuffer = StringBuffer("<stringbuffer>with some content \n& some lines...</stringbuffer>")
        val deserialized = deserialize(serialize(stringBuffer), StringBuffer::class.java)
        assertDeepEquals(deserialized, stringBuffer)
    }

    @Test
    @Throws(Exception::class)
    fun testStringBuilder() {
        val stringBuilder = StringBuilder("<stringbuilder>with some content \n& some lines...</stringbuilder>")
        val deserialized = deserialize(serialize(stringBuilder), StringBuilder::class.java)
        assertDeepEquals(deserialized, stringBuilder)
    }

    @Test
    @Throws(Exception::class)
    fun testMapWithIntConstructorOnly() {
        val map = HashMapWithIntConstructorOnly(5)
        val deserialized = deserialize(serialize(map), HashMapWithIntConstructorOnly::class.java)
        assertDeepEquals(deserialized, map)
    }

    @Test
    @Throws(Exception::class)
    fun testCurrency() {
        val currency = Currency.getInstance("EUR")
        val deserialized = deserialize(serialize(currency), Currency::class.java)
        assertDeepEquals(deserialized, currency)

        // Check that the transient field defaultFractionDigits is initialized correctly
        Assert.assertEquals(deserialized.currencyCode, currency.currencyCode)
        Assert.assertEquals(deserialized.defaultFractionDigits.toLong(), currency.defaultFractionDigits.toLong())
    }

    fun unmodifiableCollections(): Array<Array<Any>> {
        val m = HashMap<String, String>()
        m["foo"] = "bar"
        return arrayOf(
            arrayOf(Collections.unmodifiableList(ArrayList(mutableListOf("foo", "bar")))), arrayOf(
                Collections.unmodifiableSet(
                    HashSet(
                        mutableListOf("foo", "bar")
                    )
                )
            ), arrayOf(Collections.unmodifiableMap(m))
        )
    }

    @Test
    @Throws(Exception::class)
    fun testUnmodifiableCollections() {
        val collection: Any = unmodifiableCollections()
        val holder = TestClasses.Holder(collection)
        val deserialized = deserialize(serialize(holder), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, holder)
    }

    @Test
    @Throws(Exception::class)
    fun testCopyUnmodifiableCollections() {
        val collection: Any = unmodifiableCollections()
        val unmodifiableCollection = TestClasses.Holder(collection)
        val copy = _kryo.copy(unmodifiableCollection)
        assertDeepEquals(copy, unmodifiableCollection)
    }

    fun synchronizedCollections(): Array<Array<Any>> {
        val m = HashMap<String, String>()
        m["foo"] = "bar"
        return arrayOf(
            arrayOf(Collections.synchronizedList(ArrayList(mutableListOf("foo", "bar")))), arrayOf(
                Collections.synchronizedSet(
                    HashSet(
                        mutableListOf("foo", "bar")
                    )
                )
            ), arrayOf(Collections.synchronizedMap(m))
        )
    }

    @Test
    @Throws(Exception::class)
    fun testSynchronizedCollections() {
        val collection: Any = synchronizedCollections()
        val holder = TestClasses.Holder(collection)
        val deserialized = deserialize(serialize(holder), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, holder)
    }

    @Test
    @Throws(Exception::class)
    fun testCopySynchronizedCollections() {
        val collection: Any = synchronizedCollections()
        val synchronizedCollection = TestClasses.Holder(collection)
        val copy = _kryo.copy(synchronizedCollection)
        assertDeepEquals(copy, synchronizedCollection)
    }

    @Test
    @Throws(Exception::class)
    fun testJavaUtilCollectionsEmptyList() {
        val emptyList = TestClasses.Holder(emptyList<String>())
        val deserialized = deserialize(serialize(emptyList), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, emptyList)
    }

    @Test
    @Throws(Exception::class)
    fun testCopyJavaUtilCollectionsEmptyList() {
        val emptyList = TestClasses.Holder(emptyList<String>())
        val copy = _kryo.copy(emptyList)
        assertDeepEquals(copy, emptyList)
    }

    @Test
    @Throws(Exception::class)
    fun testJavaUtilCollectionsEmptySet() {
        val emptyList = TestClasses.Holder(emptySet<String>())
        val deserialized = deserialize(serialize(emptyList), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, emptyList)
    }

    @Test
    @Throws(Exception::class)
    fun testCopyJavaUtilCollectionsEmptySet() {
        val emptyList = TestClasses.Holder(emptySet<String>())
        val copy = _kryo.copy(emptyList)
        assertDeepEquals(copy, emptyList)
    }

    @Test
    @Throws(Exception::class)
    fun testJavaUtilCollectionsEmptyMap() {
        val emptyMap = TestClasses.Holder(emptyMap<String, String>())
        val deserialized = deserialize(serialize(emptyMap), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, emptyMap)
    }

    @Test
    @Throws(Exception::class)
    fun testCopyJavaUtilCollectionsEmptyMap() {
        val emptyMap = TestClasses.Holder(emptyMap<String, String>())
        val copy = _kryo.copy(emptyMap)
        assertDeepEquals(copy, emptyMap)
    }

    @Test
    @Throws(Exception::class)
    fun testJavaUtilArraysAsListEmpty() {
        val asListHolder = TestClasses.Holder<List<String>>(mutableListOf())
        val deserialized = deserialize(serialize(asListHolder), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, asListHolder)
    }

    @Test
    @Throws(Exception::class)
    fun testJavaUtilArraysAsListPrimitiveArrayElement() {
        val values = intArrayOf(1, 2).toList()
        val asListHolder = TestClasses.Holder<List<Int>>(values)
        val deserialized = deserialize(serialize(asListHolder), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, asListHolder)
    }

    @Test
    @Throws(Exception::class)
    fun testJavaUtilArraysAsListBoxedPrimitives() {
        val values = arrayOf(1, 2)
        val list = Arrays.asList(*values)
        val asListHolder = TestClasses.Holder<List<Int>>(list)
        val deserialized = deserialize(serialize(asListHolder), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, asListHolder)
    }

    @Test
    @Throws(Exception::class)
    fun testJavaUtilArraysAsListString() {
        val asListHolder = TestClasses.Holder<List<String>>(mutableListOf("foo", "bar"))
        val deserialized = deserialize(serialize(asListHolder), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, asListHolder)
    }

    @Test
    @Throws(Exception::class)
    fun testJavaUtilArraysAsListEmail() {
        val asListHolder = TestClasses.Holder(Arrays.asList(Email("foo", "foo@example.org")))
        val deserialized = deserialize(serialize(asListHolder), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, asListHolder)
    }

    @Test
    @Throws(Exception::class)
    fun testCopyJavaUtilArraysAsList() {
        val list: List<String> = mutableListOf("foo", "bar")
        val copy = _kryo.copy(list)
        assertDeepEquals(copy, list)
    }

    @Test
    @Throws(Exception::class)
    fun testClassSerializer() {
        val clazz = TestClasses.Holder<Class<*>>(String::class.java)
        val deserialized = deserialize(serialize(clazz), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, clazz)
    }

    @Test(expected = KryoException::class)
    @Throws(Exception::class)
    fun testInnerClass() {
        // seems to be related to #15
        val container = TestClasses.createContainer()
        val deserialized = deserialize(serialize(container), TestClasses.Container::class.java)
        assertDeepEquals(deserialized, container)
    }

    @Test
    @Throws(Exception::class)
    fun <T> testSharedObjectIdentity_CounterHolder() {
        val sharedObject = AtomicInteger(42)
        val holder1 = CounterHolder(sharedObject)
        val holder2 = CounterHolder(sharedObject)
        val holderHolder = CounterHolderArray(holder1, holder2)
        _kryo.setReferences(true)
        val deserialized = deserialize(serialize(holderHolder), CounterHolderArray::class.java)
        assertDeepEquals(deserialized, holderHolder)
        Assert.assertTrue(deserialized.holders[0].item === deserialized.holders[1].item)
    }

    protected fun createSharedObjectIdentityProviderData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(AtomicInteger::class.java.simpleName, AtomicInteger(42)), arrayOf(
                Email::class.java.simpleName, Email("foo bar", "foo.bar@example.com")
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun <T> testSharedObjectIdentityWithArray() {
        val data = createSharedObjectIdentityProviderData()
        for (datum in data) {
            val name = datum[0] as String
            val sharedObject = datum[1] as T
            val holder1 = TestClasses.Holder(sharedObject)
            val holder2 = TestClasses.Holder(sharedObject)
            val holderHolder = HolderArray(holder1, holder2)
            _kryo.setReferences(true)
            val deserialized = deserialize(serialize(holderHolder), HolderArray::class.java)
            assertDeepEquals(deserialized, holderHolder)
            Assert.assertTrue(deserialized.holders[0].item === deserialized.holders[1].item)
        }
    }

    @Test
    @Throws(Exception::class)
    fun <T> testSharedObjectIdentity() {
        val data = createSharedObjectIdentityProviderData()
        for (datum in data) {
            val name = datum[0] as String
            val sharedObject = datum[1] as T
            val holder1 = TestClasses.Holder(sharedObject)
            val holder2 = TestClasses.Holder(sharedObject)

            val holders = ArrayList(listOf(holder1, holder2))
            val holderHolder = HolderList(holders)
            _kryo.setReferences(true)
            val deserialized = deserialize(serialize(holderHolder), HolderList::class.java)
            assertDeepEquals(deserialized, holderHolder)
            Assert.assertTrue(deserialized.holders[0].item === deserialized.holders[1].item)
        }
    }

    protected fun createTypesAsSessionAttributesData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(Boolean::class.java, java.lang.Boolean.TRUE), arrayOf(
                String::class.java, "42"
            ), arrayOf(StringBuilder::class.java, StringBuilder("42")), arrayOf(
                StringBuffer::class.java, StringBuffer("42")
            ), arrayOf(Class::class.java, String::class.java), arrayOf(
                Long::class.java, 42
            ), arrayOf(Int::class.java, 42), arrayOf(Char::class.java, 'c'), arrayOf(
                Byte::class.java, "b".toByteArray()[0]
            ), arrayOf(Double::class.java, 42.0), arrayOf(
                Float::class.java, 42f
            ), arrayOf(Short::class.java, 42.toShort()), arrayOf(
                BigDecimal::class.java, BigDecimal(42)
            ), arrayOf(AtomicInteger::class.java, AtomicInteger(42)), arrayOf(
                AtomicLong::class.java, AtomicLong(42)
            ), arrayOf(
                Array<Int>::class.java, arrayOf(42)
            ), arrayOf(java.util.Date::class.java, java.util.Date(System.currentTimeMillis() - 10000)), arrayOf(
                Calendar::class.java, Calendar.getInstance()
            ), arrayOf(Currency::class.java, Currency.getInstance("EUR")), arrayOf(
                ArrayList::class.java, ArrayList(mutableListOf("foo"))
            ), arrayOf(
                IntArray::class.java, intArrayOf(1, 2)
            ), arrayOf(LongArray::class.java, longArrayOf(1, 2)), arrayOf(
                ShortArray::class.java, shortArrayOf(1, 2)
            ), arrayOf(FloatArray::class.java, floatArrayOf(1f, 2f)), arrayOf(
                DoubleArray::class.java, doubleArrayOf(1.0, 2.0)
            ), arrayOf(IntArray::class.java, intArrayOf(1, 2)), arrayOf(
                ByteArray::class.java, "42".toByteArray()
            ), arrayOf(CharArray::class.java, "42".toCharArray()), arrayOf(
                Array<String>::class.java, arrayOf("23", "42")
            ), arrayOf(Array<Person>::class.java, arrayOf(TestClasses.createPerson("foo bar", Gender.MALE, 42)))
        )
    }

    @Test
    @Throws(Exception::class)
    fun <T> testTypesAsSessionAttributes() {
        val data = createTypesAsSessionAttributesData()
        for (datum in data) {
            val type = datum[0] as Class<T>
            val instance = datum[1] as T
            val deserialized = deserialize(serialize(instance!!), instance!!::class.java) as T
            assertDeepEquals(deserialized, instance)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testTypesInContainerClass() {
        val myContainer = MyContainer()
        val deserialized = deserialize(serialize(myContainer), MyContainer::class.java)
        assertDeepEquals(deserialized, myContainer)
    }

    @Test(expected = KryoException::class)
    @Throws(Exception::class)
    fun testClassWithoutDefaultConstructor() {
        val obj = TestClasses.createClassWithoutDefaultConstructor("foo")
        val deserialized = deserialize(serialize(obj), ClassWithoutDefaultConstructor::class.java)
        assertDeepEquals(deserialized, obj)
    }

    @Test
    @Throws(Exception::class)
    fun testPrivateClass() {
        val holder: TestClasses.Holder<*> = TestClasses.Holder<Any>(TestClasses.createPrivateClass("foo"))
        val deserialized = deserialize(serialize(holder), TestClasses.Holder::class.java)
        assertDeepEquals(deserialized, holder)
    }

    @Test
    @Throws(Exception::class)
    fun testCollections() {
        val obj = EntityWithCollections()
        val deserialized = deserialize(serialize(obj), EntityWithCollections::class.java)
        assertDeepEquals(deserialized, obj)
    }

    @Test
    @Throws(Exception::class)
    fun testCyclicDependencies() {
        _kryo.setReferences(true)
        val p1 = TestClasses.createPerson("foo bar", Gender.MALE, 42, "foo.bar@example.org", "foo.bar@example.com")
        val p2 = TestClasses.createPerson("bar baz", Gender.FEMALE, 42, "bar.baz@example.org", "bar.baz@example.com")
        p1.addFriend(p2)
        p2.addFriend(p1)
        val deserialized = deserialize(serialize(p1), Person::class.java)
        assertDeepEquals(deserialized, p1)
    }

    class EntityWithCollections {
        private val _bars: Array<String>
        private val _foos: List<String>?
        private val _bazens: MutableMap<String, Int>?

        init {
            _bars = arrayOf("foo", "bar")
            _foos = ArrayList(mutableListOf("foo", "bar"))
            _bazens = HashMap()
            _bazens["foo"] = 1
            _bazens["bar"] = 2
        }

        override fun hashCode(): Int {
            val prime = 31
            var result = 1
            result = prime * result + Arrays.hashCode(_bars)
            result = prime * result + (_bazens?.hashCode() ?: 0)
            result = prime * result + (_foos?.hashCode() ?: 0)
            return result
        }

        override fun equals(obj: Any?): Boolean {
            if (this === obj) {
                return true
            }
            if (obj == null) {
                return false
            }
            if (javaClass != obj.javaClass) {
                return false
            }
            val other = obj as EntityWithCollections
            if (!Arrays.equals(_bars, other._bars)) {
                return false
            }
            if (_bazens == null) {
                if (other._bazens != null) {
                    return false
                }
            } else if (_bazens != other._bazens) {
                return false
            }
            if (_foos == null) {
                if (other._foos != null) {
                    return false
                }
            } else if (_foos != other._foos) {
                return false
            }
            return true
        }
    }

    protected fun serialize(o: Any): ByteArray {
        return serialize(_kryo, o)
    }

    protected fun <T> deserialize(`in`: ByteArray, clazz: Class<out T>): T {
        return deserialize(_kryo, `in`, clazz)
    }

    companion object {
        @Throws(Exception::class)
        fun assertDeepEquals(one: Any?, another: Any?) {
            assertDeepEquals(one, another, IdentityHashMap())
        }

        @Throws(Exception::class)
        private fun assertDeepEquals(one: Any?, another: Any?, alreadyChecked: MutableMap<Any?, Any?>) {
            if (one === another) {
                return
            }
            if (one == null && another != null || one != null && another == null) {
                Assert.fail("One of both is null: $one, $another")
            }
            if (alreadyChecked.containsKey(one)) {
                return
            }

            alreadyChecked[one] = another
            Assert.assertEquals(one!!.javaClass, another!!.javaClass)
            if (one.javaClass.isPrimitive || one is String || one is Char || one is Boolean || one is Class<*>) {
                Assert.assertEquals(one, another)
                return
            }
            if (MutableMap::class.java.isAssignableFrom(one.javaClass)) {
                val m1 = one as Map<*, *>?
                val m2 = another as Map<*, *>?
                Assert.assertEquals(m1!!.size.toLong(), m2!!.size.toLong())
                val iter1 = m1.entries.iterator()
                while (iter1.hasNext()) {
                    val (key, value) = iter1.next()
                    assertDeepEquals(value, m2[key], alreadyChecked)
                }
                return
            }
            if (Number::class.java.isAssignableFrom(one.javaClass)) {
                Assert.assertEquals((one as Number?)!!.toLong(), (another as Number?)!!.toLong())
                return
            }
            if (one is Currency) {
                // Check that the transient field defaultFractionDigits is initialized correctly (that was issue #34)
                val currency1 = one
                val currency2 = another as Currency?
                Assert.assertEquals(currency1.currencyCode, currency2!!.currencyCode)
                Assert.assertEquals(currency1.defaultFractionDigits.toLong(), currency2.defaultFractionDigits.toLong())
            }
            if (MutableCollection::class.java.isAssignableFrom(one.javaClass)) {
                val c1 = one as Collection<*>?
                val c2 = another as Collection<*>?
                Assert.assertEquals(c1!!.size.toLong(), c2!!.size.toLong())
                val iter1 = c1.iterator()
                val iter2 = c2.iterator()
                while (iter1.hasNext()) {
                    assertDeepEquals(iter1.next(), iter2.next(), alreadyChecked)
                }
                Assert.assertFalse(iter2.hasNext())
                return
            }
            var clazz: Class<*>? = one.javaClass
            while (clazz != null) {
                assertEqualDeclaredFields(clazz, one, another, alreadyChecked)
                clazz = clazz.superclass
            }
        }

        @Throws(Exception::class, IllegalAccessException::class)
        private fun assertEqualDeclaredFields(clazz: Class<out Any>, one: Any, another: Any, alreadyChecked: MutableMap<Any?, Any?>) {
            for (field in clazz.declaredFields) {
                field.isAccessible = true
                if (!Modifier.isTransient(field.modifiers)) {
                    assertDeepEquals(field[one], field[another], alreadyChecked)
                }
            }
        }

        fun serialize(kryo: Kryo, o: Any): ByteArray {
            val output = Output(4096)
            kryo.writeObject(output, o)
            output.flush()
            return output.buffer
        }

        fun <T> deserialize(kryo: Kryo, `in`: ByteArray, clazz: Class<out T>): T {
            val input = Input(`in`)
            return kryo.readObject(input, clazz)
        }
    }
}
