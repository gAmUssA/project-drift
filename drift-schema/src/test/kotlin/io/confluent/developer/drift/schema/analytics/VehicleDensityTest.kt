package io.confluent.developer.drift.schema.analytics

import assertk.assertThat
import assertk.assertions.isEqualTo
import java.time.Instant
import kotlin.test.Test

class VehicleDensityTest {

    @Test
    fun `should create vehicle density metrics`() {
        val timestamp = Instant.now()
        val density = VehicleDensity.newBuilder()
            .setGeohash("u4pruydq")
            .setTotalVehicles(10)
            .setAvailableVehicles(7)
            .setAutonomousCount(3)
            .setHumanDriverCount(7)
            .setTimestamp(timestamp)
            .setWindowDuration(60000L)
            .build()

        assertThat(density.geohash).isEqualTo("u4pruydq")
        assertThat(density.totalVehicles).isEqualTo(10)
        assertThat(density.availableVehicles).isEqualTo(7)
        assertThat(density.autonomousCount + density.humanDriverCount).isEqualTo(density.totalVehicles)
    }
}