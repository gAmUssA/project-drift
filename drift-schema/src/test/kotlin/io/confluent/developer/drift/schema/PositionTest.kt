package io.confluent.developer.drift.schema

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.confluent.developer.drift.schema.common.Position
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test

class PositionTest {
    @Test
    fun `should create position with required fields`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        val position = TestUtils.createTestPosition(
            latitude = 37.7749,
            longitude = -122.4194,
            timestamp = now
        )

        assertThat(position.latitude).isEqualTo(37.7749)
        assertThat(position.longitude).isEqualTo(-122.4194)
        assertThat(position.timestamp).isEqualTo(now)
        assertThat(position.accuracy).isNull()
    }

    @Test
    fun `should create position with all fields`() {
        val position = TestUtils.createTestPosition(accuracy = 5.0f)
        assertThat(position.accuracy).isNotNull()
        assertThat(position.accuracy).isEqualTo(5.0f)
    }

    @Test
    fun `should maintain consistency through serialization`() {
        val original = TestUtils.createTestPosition(accuracy = 5.0f)
        val deserialized = TestUtils.serializeAndDeserialize(original) as Position

        assertThat(deserialized).isEqualTo(original)
    }
}