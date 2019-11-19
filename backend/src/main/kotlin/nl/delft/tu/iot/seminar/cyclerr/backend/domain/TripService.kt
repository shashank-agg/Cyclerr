package nl.delft.tu.iot.seminar.cyclerr.backend.domain

import org.springframework.stereotype.Service
import java.util.*

@Service
class TripService(val tripRepo: TripRepo) {

    fun getAllAvailableTrips(): List<Trip> {
        return Collections.unmodifiableList(tripRepo.findAll().sortedByDescending { it.start })
    }

    fun getTripById(tripId: TripId): Trip {
        return tripRepo.findById(tripId).orElseThrow{ NoSuchElementException("Trip with $tripId does not exist.") }
    }


    fun handle(cmd: AddDataPointsToTripCmd): Trip {

        val trip = tripRepo.findById(cmd.tripId).orElse(Trip(cmd.tripId)) //create new trip if not existing

        trip.addDataPointsChunk(cmd.index, cmd.isLast, cmd.dataPoint)

        tripRepo.save(trip)

        return trip
    }

}

class AddDataPointsToTripCmd(val tripId: TripId, val index: Int, val isLast: Boolean, val dataPoint: List<DataPoint>)
