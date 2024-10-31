package io.confluent.developer.drift.schema

import io.confluent.developer.drift.schema.common.Position
import io.confluent.developer.drift.schema.vehicle.Vehicle
import io.confluent.developer.drift.schema.vehicle.VehiclePosition
import io.confluent.developer.drift.schema.vehicle.VehicleStatus
import io.confluent.developer.drift.schema.vehicle.VehicleType
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.apache.avro.specific.SpecificDatumWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.*

object TestUtils {
    fun createTestPosition(
        latitude: Double = 37.7749,
        longitude: Double = -122.4194,
        timestamp: Instant = Instant.now(),
        accuracy: Float? = null
    ): Position = Position.newBuilder()
        .setLatitude(latitude)
        .setLongitude(longitude)
        .setTimestamp(timestamp)
        .apply { accuracy?.let { setAccuracy(it) } }
        .build()

    fun createTestVehicle(
        vehicleId: String = UUID.randomUUID().toString(),
        type: VehicleType = VehicleType.HUMAN_DRIVER,
        status: VehicleStatus = VehicleStatus.AVAILABLE,
        position: Position = createTestPosition()
    ): Vehicle = Vehicle.newBuilder()
        .setVehicleId(vehicleId)
        .setType(type)
        .setStatus(status)
        .setPosition(position)
        .build()

    fun serializeAndDeserialize(obj: Any): Any {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val encoder = EncoderFactory.get().binaryEncoder(byteArrayOutputStream, null)

        when (obj) {
            is Position -> {
                val writer = SpecificDatumWriter(Position::class.java)
                writer.write(obj, encoder)
                encoder.flush()

                val decoder = DecoderFactory.get().binaryDecoder(
                    ByteArrayInputStream(byteArrayOutputStream.toByteArray()), null
                )
                return SpecificDatumReader(Position::class.java).read(null, decoder)
            }

            is Vehicle -> {
                val writer = SpecificDatumWriter(Vehicle::class.java)
                writer.write(obj, encoder)
                encoder.flush()

                val decoder = DecoderFactory.get().binaryDecoder(
                    ByteArrayInputStream(byteArrayOutputStream.toByteArray()), null
                )
                return SpecificDatumReader(Vehicle::class.java).read(null, decoder)
            }

            is VehiclePosition -> {
                SpecificDatumWriter(VehiclePosition::class.java).write(obj, encoder)
                encoder.flush()
                val decoder = DecoderFactory.get().binaryDecoder(
                    ByteArrayInputStream(byteArrayOutputStream.toByteArray()), null
                )
                return SpecificDatumReader(VehiclePosition::class.java).read(null, decoder)
            }
            // Add other types as needed
            else -> throw IllegalArgumentException("Unsupported type: ${obj.javaClass}")
        }
    }
}