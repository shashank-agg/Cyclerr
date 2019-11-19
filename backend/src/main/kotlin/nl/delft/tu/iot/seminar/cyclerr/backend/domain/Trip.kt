package nl.delft.tu.iot.seminar.cyclerr.backend.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonUnwrapped
import nl.delft.tu.iot.seminar.cyclerr.backend.domain.TripStatus.*
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Duration
import java.time.Instant
import java.util.*

@Document
data class Trip
@JsonCreator
internal constructor(@JsonUnwrapped @Id val tripId: TripId, val dataChunks: MutableMap<Int, List<DataPoint>>) {

    constructor(tripId: TripId) : this(tripId, mutableMapOf<Int, List<DataPoint>>())

    @Transient
    val data = dataChunks.values.flatten()

    private var lastIndex = -1

    val status: TripStatus
        get() {
            if (lastIndex==-1) return PENDING

            if ((lastIndex+1) == dataChunks.size) return FINISHED

            return INCOMPLETE
        }

    val start: Instant?
        get() = data.map { it.timestamp }.min()

    val end: Instant?
        get() = data.map { it.timestamp }.max()

    val duration: Duration?
        get() = nullableDuration(start, end)

    val minSpeed: Double?
        get() = data.map { it.speed }.filterNotNull().min()

    val maxSpeed: Double?
        get() = data.map { it.speed }.filterNotNull().max()

    val avgSpeed: Double?
        get() = data.map { it.speed }.filterNotNull().average() //We have to make sure, that time intervalls are equal otherwise more complex function requiered

    val minAltitude: Double?
        get() = data.map { it.altitude }.filterNotNull().min()

    val maxAltitude: Double?
        get() = data.map { it.altitude }.filterNotNull().max()

    val absElevation: Double?
        get() = nullableDifference(maxAltitude, minAltitude)

    private fun nullableDifference(max:Double?, min: Double?): Double? {
        return if (max !=null && min !=null) max - min else null
    }

    private fun nullableDuration(start:Instant?, end: Instant?): Duration? {
        return if (start !=null && end !=null) Duration.between(start, start) else null
    }

    fun addDataPointsChunk(index: Int, isLast:Boolean, datapoints: List<DataPoint>) {
        dataChunks.put(index, datapoints)
        if(isLast){
            lastIndex = index;
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Trip

        if (tripId != other.tripId) return false

        return true
    }

    override fun hashCode(): Int {
        return tripId.hashCode()
    }
}

enum class TripStatus { PENDING, INCOMPLETE, FINISHED }

data class TripId(val tripId: String) {
    constructor() : this(UUID.randomUUID().toString())

    override fun toString(): String {
        return tripId
    }
}

data class DataPoint(val timestamp: Instant, val cadence: Double?, val speed: Double?, val altitude: Double?, val gear: Int?)