package dorkbox.serializers

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.net.URI

class URISerializer : Serializer<URI>() {
    init {
        isImmutable = true
    }

    override fun write(kryo: Kryo, output: Output, uri: URI) {
        output.writeString(uri.toString())
    }

    override fun read(kryo: Kryo, input: Input, uriClass: Class<out URI>): URI {
        return URI.create(input.readString())
    }
}
