// src/main/kotlin/io/confluent/developer/drift/schema/conversion/JsonAvroConverter.kt
package io.confluent.developer.drift.schema.conversion

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import org.apache.avro.specific.SpecificRecord
import org.apache.avro.specific.SpecificRecordBase

/**
 * Converts between JSON and Avro objects using Jackson for JSON processing
 */
class JsonAvroConverter(
    private val schemaRegistry: SchemaRegistryClient
) {
    val objectMapper = ObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        registerModule(AvroModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    inline fun <reified T : SpecificRecord> fromJson(json: String): T {
        return objectMapper.readValue(json, T::class.java)
    }

    fun toJson(record: SpecificRecord): String {
        return objectMapper.writeValueAsString(record)
    }

    private fun extractAvroValue(value: Any?): Any? = when (value) {
        is SpecificRecordBase -> {
            val data = mutableMapOf<String, Any?>()
            value.schema.fields.forEach { field ->
                data[field.name()] = extractAvroValue(value.get(field.pos()))
            }
            data
        }

        is List<*> -> value.map { extractAvroValue(it) }
        is Map<*, *> -> value.mapValues { extractAvroValue(it.value) }
        else -> value
    }
}