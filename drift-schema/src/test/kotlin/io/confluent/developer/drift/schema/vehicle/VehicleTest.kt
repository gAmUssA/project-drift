package io.confluent.developer.drift.schema.vehicle

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.confluent.developer.drift.schema.TestUtils
import kotlin.test.Test

class VehicleTest {
    @Test
    fun `should create human driver vehicle`() {
        val vehicle = TestUtils.createTestVehicle(
            type = VehicleType.HUMAN_DRIVER,
            status = VehicleStatus.AVAILABLE
        )

        assertThat(vehicle.type).isEqualTo(VehicleType.HUMAN_DRIVER)
        assertThat(vehicle.status).isEqualTo(VehicleStatus.AVAILABLE)
    }

    @Test
    fun `should create autonomous vehicle`() {
        val vehicle = TestUtils.createTestVehicle(
            type = VehicleType.AUTONOMOUS,
            status = VehicleStatus.AVAILABLE
        )

        assertThat(vehicle.type).isEqualTo(VehicleType.AUTONOMOUS)
    }

    @Test
    fun `should update vehicle status`() {
        val vehicle = TestUtils.createTestVehicle()
        val updatedVehicle = Vehicle.newBuilder(vehicle)
            .setStatus(VehicleStatus.BUSY)
            .build()

        assertThat(updatedVehicle.vehicleId).isEqualTo(vehicle.vehicleId)
        assertThat(updatedVehicle.status).isEqualTo(VehicleStatus.BUSY)
    }

    @Test
    fun `should maintain consistency through serialization`() {
        val original = TestUtils.createTestVehicle()
        val deserialized = TestUtils.serializeAndDeserialize(original) as Vehicle

        assertThat(deserialized).isEqualTo(original)
    }
}