package io.confluent.developer.drift.schema.conversion


import io.confluent.developer.drift.schema.common.Position
import io.confluent.developer.drift.schema.vehicle.VehiclePosition
import io.confluent.developer.drift.schema.vehicle.VehicleStatus
import io.confluent.developer.drift.schema.vehicle.VehicleType
import java.time.Instant

object TestUtils {
    fun createTestPosition(
        latitude: Double = 37.7749,
        longitude: Double = -122.4194,
        timestamp: Instant = Instant.now(),
        accuracy: Float? = null
    ): Position {
        return Position.newBuilder()
            .setLatitude(latitude)
            .setLongitude(longitude)
            .setTimestamp(timestamp)
            .apply { accuracy?.let { setAccuracy(it) } }
            .build()
    }

    fun createTestVehiclePosition(
        vehicleId: String = "test-vehicle",
        position: Position = createTestPosition(),
        type: VehicleType = VehicleType.HUMAN_DRIVER,
        status: VehicleStatus = VehicleStatus.AVAILABLE
    ): VehiclePosition {
        return VehiclePosition.newBuilder()
            .setVehicleId(vehicleId)
            .setPosition(position)
            .setVehicleType(type)
            .setStatus(status)
            .build()
    }
}