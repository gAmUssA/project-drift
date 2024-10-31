package io.confluent.developer.drift.schema.ride

import assertk.assertThat
import assertk.assertions.isEqualTo
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test

class RideMatchTest {

    @Test
    fun `should create ride match with proposed status`() {
        val timestamp = Instant.now()
        val match = RideMatch.newBuilder()
            .setMatchId("match-123")
            .setRequestId("request-123")
            .setVehicleId("vehicle-123")
            .setEstimatedPickupTime(timestamp.plus(5, ChronoUnit.MINUTES)) // 5 minutes from now
            .setDistanceToPickup(1500.0)
            .setStatus(MatchStatus.PROPOSED)
            .setCreatedAt(timestamp)
            .build()

        assertThat(match.matchId).isEqualTo("match-123")
        assertThat(match.status).isEqualTo(MatchStatus.PROPOSED)
        assertThat(match.distanceToPickup).isEqualTo(1500.0)
    }

    @Test
    fun `should update match status`() {
        val original = createTestMatch()
        val updated = RideMatch.newBuilder(original)
            .setStatus(MatchStatus.ACCEPTED)
            .build()

        assertThat(updated.matchId).isEqualTo(original.matchId)
        assertThat(updated.status).isEqualTo(MatchStatus.ACCEPTED)
    }

    private fun createTestMatch() = RideMatch.newBuilder()
        .setMatchId("match-123")
        .setRequestId("request-123")
        .setVehicleId("vehicle-123")
        .setEstimatedPickupTime(Instant.now().plus(5, ChronoUnit.MINUTES))
        .setDistanceToPickup(1500.0)
        .setStatus(MatchStatus.PROPOSED)
        .setCreatedAt(Instant.now())
        .build()
}