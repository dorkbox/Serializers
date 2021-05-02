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
package dorkbox.serializers.bouncycastle;

import org.bouncycastle.crypto.params.IESParameters;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Only public keys are ever sent across the wire.
 */
public
class IesParametersSerializer extends Serializer<IESParameters> {

    @Override
    public
    void write(Kryo kryo, Output output, IESParameters key) {
        byte[] bytes;
        int length;

        ///////////
        bytes = key.getDerivationV();
        length = bytes.length;

        output.writeInt(length, true);
        output.writeBytes(bytes, 0, length);

        ///////////
        bytes = key.getEncodingV();
        length = bytes.length;

        output.writeInt(length, true);
        output.writeBytes(bytes, 0, length);

        ///////////
        output.writeInt(key.getMacKeySize(), true);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public
    IESParameters read(Kryo kryo, Input input, Class type) {
        int length;

        /////////////
        length = input.readInt(true);
        byte[] derivation = new byte[length];
        input.readBytes(derivation, 0, length);

        /////////////
        length = input.readInt(true);
        byte[] encoding = new byte[length];
        input.readBytes(encoding, 0, length);

        /////////////
        int macKeySize = input.readInt(true);

        return new IESParameters(derivation, encoding, macKeySize);
    }
}
