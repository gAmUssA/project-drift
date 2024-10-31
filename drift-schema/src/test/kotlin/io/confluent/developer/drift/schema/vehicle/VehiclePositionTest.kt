package io.confluent.developer.drift.schema.vehicle

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.confluent.developer.drift.schema.TestUtils
import kotlin.test.Test

class VehiclePositionTest {

    @Test
    fun `should create vehicle position update with required fields`() {
        val vehiclePosition = VehiclePosition.newBuilder()
            .setVehicleId("vehicle-123")
            .setPosition(TestUtils.createTestPosition())
            .setVehicleType(VehicleType.HUMAN_DRIVER)
            .setStatus(VehicleStatus.AVAILABLE)
            .build()

        assertThat(vehiclePosition.vehicleId).isEqualTo("vehicle-123")
        assertThat(vehiclePosition.vehicleType).isEqualTo(VehicleType.HUMAN_DRIVER)
        assertThat(vehiclePosition.status).isEqualTo(VehicleStatus.AVAILABLE)
        assertThat(vehiclePosition.heading).isNull()
        assertThat(vehiclePosition.speed).isNull()
    }

    @Test
    fun `should create vehicle position with all fields`() {
        val vehiclePosition = VehiclePosition.newBuilder()
            .setVehicleId("av-123")
            .setPosition(TestUtils.createTestPosition())
            .setVehicleType(VehicleType.AUTONOMOUS)
            .setStatus(VehicleStatus.BUSY)
            .setHeading(180.0)
            .setSpeed(13.5)
            .build()

        assertThat(vehiclePosition.heading).isEqualTo(180.0)
        assertThat(vehiclePosition.speed).isEqualTo(13.5)
    }

    @Test
    fun `should maintain consistency through serialization`() {
        val original = VehiclePosition.newBuilder()
            .setVehicleId("vehicle-123")
            .setPosition(TestUtils.createTestPosition())
            .setVehicleType(VehicleType.HUMAN_DRIVER)
            .setStatus(VehicleStatus.AVAILABLE)
            .setHeading(90.0)
            .setSpeed(15.0)
            .build()

        val deserialized = TestUtils.serializeAndDeserialize(original) as VehiclePosition
        assertThat(deserialized).isEqualTo(original)
    }
}