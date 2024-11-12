package io.confluent.developer.drift.schema.conversion

import assertk.assertThat
import assertk.assertions.*
import com.fasterxml.jackson.databind.JsonMappingException
import io.confluent.developer.drift.schema.ride.RideRequest
import io.confluent.developer.drift.schema.vehicle.VehiclePosition
import io.confluent.developer.drift.schema.vehicle.VehicleType
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

class JsonAvroConverterTest {
    private val schemaRegistry = MockSchemaRegistryClient()
    private val converter = JsonAvroConverter(schemaRegistry)

    @Test
    fun `should convert ride request from json`() {
        val json = """
            {
                "requestId": "req-123",
                "riderId": "rider-456",
                "pickupLocation": {
                    "latitude": 37.7749,
                    "longitude": -122.4194,
                    "timestamp": ${Instant.now().toEpochMilli()}
                },
                "dropoffLocation": {
                    "latitude": 37.7833,
                    "longitude": -122.4167,
                    "timestamp": ${Instant.now().toEpochMilli()}
                }
            }
        """.trimIndent()

        val request = converter.fromJson<RideRequest>(json)

        assertThat(request.requestId).isEqualTo("req-123")
        assertThat(request.riderId).isEqualTo("rider-456")
        assertThat(request.pickupLocation.latitude).isEqualTo(37.7749)
    }

    @Test
    fun `should convert vehicle position from json`() {
        val json = """
            {
                "vehicleId": "vehicle-123",
                "position": {
                    "latitude": 37.7749,
                    "longitude": -122.4194,
                    "timestamp": ${Instant.now().toEpochMilli()}
                },
                "vehicleType": "HUMAN_DRIVER",
                "status": "AVAILABLE",
                "heading": 180.0,
                "speed": 25.5
            }
        """.trimIndent()

        val position = converter.fromJson<VehiclePosition>(json)

        assertThat(position.vehicleId).isEqualTo("vehicle-123")
        assertThat(position.vehicleType).isEqualTo(VehicleType.HUMAN_DRIVER)
        assertThat(position.heading).isEqualTo(180.0)
    }

    @Test
    fun `should handle missing optional fields`() {
        val json = """
            {
                "vehicleId": "vehicle-123",
                "position": {
                    "latitude": 37.7749,
                    "longitude": -122.4194,
                    "timestamp": ${Instant.now().toEpochMilli()}
                },
                "vehicleType": "HUMAN_DRIVER",
                "status": "AVAILABLE"
            }
        """.trimIndent()

        val position = converter.fromJson<VehiclePosition>(json)

        assertThat(position.heading).isNull()
        assertThat(position.speed).isNull()
    }

    @Test
    fun `should convert back to json`() {
        val originalJson = """
            {
                "vehicleId": "vehicle-123",
                "position": {
                    "latitude": 37.7749,
                    "longitude": -122.4194,
                    "timestamp": ${Instant.now().toEpochMilli()}
                },
                "vehicleType": "HUMAN_DRIVER",
                "status": "AVAILABLE"
            }
        """.trimIndent()

        val position = converter.fromJson<VehiclePosition>(originalJson)
        val convertedJson = converter.toJson(position)
        val reconvertedPosition = converter.fromJson<VehiclePosition>(convertedJson)

        assertThat(reconvertedPosition).isEqualTo(position)
    }

    @Test
    fun `should throw exception for invalid enum value`() {
        val json = """
            {
                "vehicleId": "vehicle-123",
                "position": {
                    "latitude": 37.7749,
                    "longitude": -122.4194,
                    "timestamp": ${Instant.now()}
                },
                "vehicleType": "INVALID_TYPE",
                "status": "AVAILABLE"
            }
        """.trimIndent()

        assertThrows<JsonMappingException> {
            converter.fromJson<VehiclePosition>(json)
        }
    }

    @Test
    fun `should work there and back again`() {
        var testVehiclePosition = TestUtils.createTestVehiclePosition()
        var jsonString = converter.toJson(testVehiclePosition)
        var backAgainVehiclePosition = converter.fromJson<VehiclePosition>(jsonString)
        assertThat(backAgainVehiclePosition).isEqualTo(testVehiclePosition)
    }
}
