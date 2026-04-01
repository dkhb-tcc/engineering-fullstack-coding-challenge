package com.example.demo.patient

import com.example.demo.vitalsign.VitalSignService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/patients")
class PatientOverviewController(
    private val patientClient: PatientClient,
    private val vitalSignService: VitalSignService,
) {
    @GetMapping("/{patientId}/overview")
    fun getOverview(@PathVariable patientId: Long): PatientOverview {
        val info = patientClient.getPatient(patientId)
        return PatientOverview(
            patientId = patientId,
            firstName = info.firstName,
            lastName = info.lastName,
            dateOfBirth = info.dateOfBirth,
            latestVitalSign = vitalSignService.getLatest(),
        )
    }
}
