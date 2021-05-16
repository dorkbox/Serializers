package dorkbox.serializers

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.util.regex.Pattern

/**
 * Kryo [Serializer] for regex [Pattern]s.
 *
 * @author [Martin Grotzke](mailto:martin.grotzke@javakaffee.de)
 * @author serverperformance
 */
class RegexSerializer : Serializer<Pattern>() {
    init {
        isImmutable = true
    }

    override fun write(kryo: Kryo, output: Output, pattern: Pattern) {
        output.writeString(pattern.pattern())
        output.writeInt(pattern.flags(), true)
    }

    override fun read(kryo: Kryo, input: Input, patternClass: Class<out Pattern>): Pattern {
        val regex = input.readString()
        val flags = input.readInt(true)
        return Pattern.compile(regex, flags)
    }
}
