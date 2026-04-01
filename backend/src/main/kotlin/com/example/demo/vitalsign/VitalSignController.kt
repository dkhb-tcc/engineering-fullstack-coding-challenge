package com.example.demo.vitalsign

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/patients")
class VitalSignController(private val service: VitalSignService) {
    @GetMapping("/{patientId}/vitals/latest")
    fun getLatest(@PathVariable patientId: Long): VitalSign = service.getLatest()
}
