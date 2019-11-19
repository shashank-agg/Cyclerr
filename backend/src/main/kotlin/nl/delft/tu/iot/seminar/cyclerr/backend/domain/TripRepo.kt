package nl.delft.tu.iot.seminar.cyclerr.backend.domain

import org.springframework.data.mongodb.repository.MongoRepository

interface TripRepo: MongoRepository<Trip, TripId> {
}