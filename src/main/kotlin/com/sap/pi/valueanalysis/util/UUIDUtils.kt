package com.sap.pi.valueanalysis.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.sap.pi.valueanalysis.exception.ServerException
import java.io.IOException
import java.util.*

class DashTolerantUUIDDeserializer : JsonDeserializer<UUID>() {
    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): UUID {
        return getUuidFromStringOrException(p.text)
    }
}

class UUIDWithoutDashesSerializer : JsonSerializer<UUID>() {
    @Throws(IOException::class)
    override fun serialize(value: UUID, gen: JsonGenerator, provider: SerializerProvider?) {
        gen.writeString(removeDashesFromUuid(value))
    }
}

private val VALID_UUID_REGEX =
    Regex("(\\p{XDigit}{8})-?(\\p{XDigit}{4})-?(\\p{XDigit}{4})-?(\\p{XDigit}{4})-?(\\p{XDigit}{12})")

fun getUuidFromString(id: String): UUID? {
    return if (isValidUuid(id)) {
        val id = id.replaceFirst(VALID_UUID_REGEX, "$1-$2-$3-$4-$5")
        UUID.fromString(id)
    } else {
        null
    }
}

fun getUuidFromStringOrException(id: String): UUID {
    return if (isValidUuid(id)) {
        // adjusted code from http://stackoverflow.com/a/19399768
        // Use regex to format the hex string by inserting hyphens in the canonical format: 8-4-4-4-12
        val id = id.replaceFirst(VALID_UUID_REGEX, "$1-$2-$3-$4-$5")
        UUID.fromString(id)
    } else {
        val msg = "id is not a valid UUID: $id"
        throw ServerException(msg)
    }
}

fun isValidUuid(id: String?): Boolean {
    return id != null && id.matches(VALID_UUID_REGEX)
}

fun removeDashesFromUuid(uuid: UUID): String {
    return removeDashesFromStringUuid(uuid.toString())
}

fun removeDashesFromStringUuid(uuid: String): String {
    return uuid.replace("-", "")
}
