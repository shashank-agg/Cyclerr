package nl.delft.tu.iot.seminar.cyclerr.backend.rest

import nl.delft.tu.iot.seminar.cyclerr.backend.domain.AddDataPointsToTripCmd
import nl.delft.tu.iot.seminar.cyclerr.backend.domain.DataPoint
import nl.delft.tu.iot.seminar.cyclerr.backend.domain.TripId
import nl.delft.tu.iot.seminar.cyclerr.backend.domain.TripService
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity



@RestController
@RequestMapping("/data")
class DataController(val tripService: TripService) {

    @PostMapping
    fun dataReceived(@RequestBody dataReceivedDto: DataReceivedDto): ResponseEntity<String> {
        println("RECEIVED: $dataReceivedDto")
        tripService.handle(dataReceivedDto.toCmd())


        return ResponseEntity
                .created(linkTo(methodOn(TripController::class.java).getOneTrip(dataReceivedDto.triId)).toUri())
                .build<String>();
    }
}

data class DataReceivedDto( val tripId: String, val index: Int,val isLast: Boolean, val data: List<DataPoint>){

    val triId:TripId
        get() = TripId(tripId)

    fun toCmd(): AddDataPointsToTripCmd {
        return AddDataPointsToTripCmd(triId, index, isLast, data)
    }
}