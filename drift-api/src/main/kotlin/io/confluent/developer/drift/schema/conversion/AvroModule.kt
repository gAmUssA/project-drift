package io.confluent.developer.drift.schema.conversion

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.avro.Schema
import org.apache.avro.specific.SpecificRecord
import java.time.Instant

class AvroModule : SimpleModule() {
    init {
        addSerializer(SpecificRecord::class.java, AvroSerializer())
        addDeserializer(SpecificRecord::class.java, AvroDeserializer())
    }
}

class AvroSerializer : JsonSerializer<SpecificRecord>() {
    override fun serialize(
        value: SpecificRecord,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        gen.writeStartObject()
        value.schema.fields.forEach { field ->
            val fieldValue = value.get(field.pos())
            gen.writeFieldName(field.name())
            when (fieldValue) {
                null -> gen.writeNull()
                is SpecificRecord -> serialize(fieldValue, gen, provider)
                else -> gen.writeObject(fieldValue)
            }
        }
        gen.writeEndObject()
    }
}

class AvroDeserializer : JsonDeserializer<SpecificRecord>() {
    override fun deserialize(
        parser: JsonParser,
        context: DeserializationContext
    ): SpecificRecord? {
        try {
            val node = parser.readValueAsTree<JsonNode>()
            if (node.isNull) return null

            val recordClass = context.contextualType.rawClass as Class<out SpecificRecord>
            return deserializeRecord(node as ObjectNode, recordClass)
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> throw e
                else -> throw IllegalArgumentException(e.message ?: "Deserialization failed", e)
            }
        }
    }

    private fun deserializeRecord(
        node: ObjectNode,
        recordClass: Class<out SpecificRecord>
    ): SpecificRecord {
        val record = recordClass.getDeclaredConstructor().newInstance()
        val schema = record.schema

        schema.fields.forEach { field ->
            val fieldNode = node.get(field.name())
            if (fieldNode != null && !fieldNode.isNull) {
                val value = deserializeField(fieldNode, field.schema())
                record.put(field.pos(), value)
            }
        }

        return record
    }

    private fun deserializeField(node: JsonNode, schema: Schema): Any? {
        return when (schema.type) {
            Schema.Type.RECORD -> {
                val recordClass = Class.forName(schema.fullName) as Class<out SpecificRecord>
                deserializeRecord(node as ObjectNode, recordClass)
            }

            Schema.Type.ENUM -> deserializeEnum(node, schema)
            Schema.Type.UNION -> deserializeUnion(node, schema)
            Schema.Type.ARRAY -> deserializeArray(node, schema)
            Schema.Type.STRING -> node.textValue()
            Schema.Type.INT -> node.intValue()
            Schema.Type.LONG -> deserializeLong(node, schema)
            Schema.Type.FLOAT -> node.floatValue()
            Schema.Type.DOUBLE -> node.doubleValue()
            Schema.Type.BOOLEAN -> node.booleanValue()
            else -> throw IllegalArgumentException("Unsupported type: ${schema.type}")
        }
    }

    private fun deserializeEnum(node: JsonNode, schema: Schema): Any {
        if (!node.isTextual) {
            throw IllegalArgumentException("Invalid enum value: must be a string")
        }

        val enumValue = node.textValue()
        if (!schema.enumSymbols.contains(enumValue)) {
            throw IllegalArgumentException("Invalid enum value: $enumValue")
        }

        val enumClass = Class.forName(schema.fullName)
        return java.lang.Enum.valueOf(enumClass as Class<out Enum<*>>, enumValue)
    }

    private fun deserializeUnion(node: JsonNode, schema: Schema): Any? {
        if (node.isNull) {
            return null
        }

        val nonNullSchema = schema.types.find { it.type != Schema.Type.NULL }
            ?: throw IllegalArgumentException("No non-null type in union")
        return deserializeField(node, nonNullSchema)
    }

    private fun deserializeArray(node: JsonNode, schema: Schema): List<Any?> {
        if (!node.isArray) {
            throw IllegalArgumentException("Expected array value")
        }
        return node.map { deserializeField(it, schema.elementType) }
    }

    private fun deserializeLong(node: JsonNode, schema: Schema): Long {
        return if (schema.logicalType?.name == "timestamp-millis") {
            when {
                node.isNumber -> node.longValue()
                node.isTextual -> Instant.parse(node.textValue()).toEpochMilli()
                else -> throw IllegalArgumentException("Invalid timestamp format")
            }
        } else {
            node.longValue()
        }
    }
}