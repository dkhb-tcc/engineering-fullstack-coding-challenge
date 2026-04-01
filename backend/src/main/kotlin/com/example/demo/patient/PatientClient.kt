package com.example.demo.patient

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class PatientClient(
    private val restTemplate: RestTemplate,
    @Value("\${patient.service.url:http://localhost:8000}") private val baseUrl: String,
) {
    fun getPatient(patientId: Long): PatientInfo =
        restTemplate.getForObject("$baseUrl/patients/$patientId", PatientInfo::class.java)!!
}
