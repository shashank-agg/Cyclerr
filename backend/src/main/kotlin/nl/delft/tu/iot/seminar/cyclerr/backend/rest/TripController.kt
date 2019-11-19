package nl.delft.tu.iot.seminar.cyclerr.backend.rest

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonUnwrapped
import nl.delft.tu.iot.seminar.cyclerr.backend.domain.*
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/trips")
class TripController(val tripService: TripService) {

    @GetMapping
    @CrossOrigin("*")
    fun getAllTrips(): CollectionModel<TripModel> {

        val trips = tripService.getAllAvailableTrips()

        return TripModelAssembler.toCollectionModel(trips)
    }

    @CrossOrigin("*")
    @GetMapping("/{tripId}")
    fun getOneTrip(@PathVariable tripId: TripId): TripModel {

        val trip = tripService.getTripById(tripId)

        return TripModelAssembler.toModel(trip)
    }

    @CrossOrigin("*")
    @GetMapping("/{tripId}/data")
    fun getTripData(@PathVariable tripId: TripId): CollectionModel<DataPoint> {

        val trip = tripService.getTripById(tripId)

        val self = linkTo(methodOn(TripController::class.java).getTripData(trip.tripId)).withSelfRel()
        return CollectionModel(trip.data, self)
    }
    open class TripModel(@JsonUnwrapped @JsonIgnoreProperties("data", "dataChunks") val content: Trip) : RepresentationModel<TripModel>()

    object TripModelAssembler : RepresentationModelAssemblerSupport<Trip, TripModel>(Trip::class.java, TripModel::class.java) {
        override fun toModel(entity: Trip): TripModel {
            val model = TripModel(entity)
            model.add(linkTo(methodOn(TripController::class.java).getOneTrip(entity.tripId)).withSelfRel())
            model.add(linkTo(methodOn(TripController::class.java).getTripData(entity.tripId)).withRel("data"))
            return model
        }

        override fun toCollectionModel(entities: Iterable<Trip>): CollectionModel<TripModel> {
            val model = super.toCollectionModel(entities)
            model.add(linkTo(methodOn(TripController::class.java).getAllTrips()).withSelfRel())
            return model
        }
    }
}