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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

import org.junit.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * A test case for the {@link FieldAnnotationAwareSerializer}.
 *
 * @author <a href="mailto:rafael.wth@web.de">Rafael Winterhalter</a>
 * @author <a href="mailto:martin.grotzke@javakaffee.de">Martin Grotzke</a>
 */
public class FieldAnnotationAwareSerializerTest {

    // Use Non-ASCII characters in order to be able to check the byte buffer for
    // the existence of the string values.
    protected static final String FIRST_VALUE = "åæø first value";
    protected static final String SECOND_VALUE = "äöü second value";

    private static final int BUFFER_SIZE = 1024;

    private CustomBean makeBean() {
        final CustomBean customBean = new CustomBean();
        customBean.setFirstValue(FIRST_VALUE);
        customBean.setSecondValue(SECOND_VALUE);
        return customBean;
    }

    private byte[] makeBuffer() {
        return new byte[BUFFER_SIZE];
    }

    @Test
    public void testExcludeFields() throws Exception {

        final Kryo kryo = new Kryo();
        @SuppressWarnings("unchecked")
		final SerializerFactory disregardingSerializerFactory = new FieldAnnotationAwareSerializer.Factory(
                Arrays.<Class<? extends Annotation>>asList(CustomMark.class), true);
        kryo.addDefaultSerializer(CustomBean.class, disregardingSerializerFactory);
        kryo.register(CustomBean.class);

        final byte[] buffer = makeBuffer();

        final CustomBean outputBean = makeBean();
        final Output output = new Output(buffer);
        kryo.writeObject(output, outputBean);

        final Input input = new Input(buffer);
        final CustomBean inputBean = kryo.readObject(input, CustomBean.class);

        assertEquals(inputBean.getSecondValue(), outputBean.getSecondValue());
        assertFalse(new String(buffer).contains(outputBean.getFirstValue()));
        assertTrue(new String(buffer).contains(outputBean.getSecondValue()));
        assertNull(inputBean.getFirstValue());
    }

    @Test
    public void testIncludeFields() throws Exception {

        final Kryo kryo = new Kryo();
        @SuppressWarnings("unchecked")
		final SerializerFactory regardingSerializerFactory = new FieldAnnotationAwareSerializer.Factory(
                Arrays.<Class<? extends Annotation>>asList(CustomMark.class), false);
        kryo.addDefaultSerializer(CustomBean.class, regardingSerializerFactory);
        kryo.register(CustomBean.class);

        final byte[] buffer = makeBuffer();

        final CustomBean outputBean = makeBean();
        final Output output = new Output(buffer);
        kryo.writeObject(output, outputBean);

        final Input input = new Input(buffer);
        final CustomBean inputBean = kryo.readObject(input, CustomBean.class);

        assertEquals(inputBean.getFirstValue(), outputBean.getFirstValue());
        assertTrue(new String(buffer).contains(outputBean.getFirstValue()));
        assertFalse(new String(buffer).contains(outputBean.getSecondValue()));
        assertNull(inputBean.getSecondValue());
    }

    private static class CustomBean {

        @CustomMark
        private String firstValue;

        private String secondValue;

        public String getSecondValue() {
            return secondValue;
        }

        public void setSecondValue(final String secondValue) {
            this.secondValue = secondValue;
        }

        public String getFirstValue() {
            return firstValue;
        }

        public void setFirstValue(final String firstValue) {
            this.firstValue = firstValue;
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface CustomMark {
    }
}
