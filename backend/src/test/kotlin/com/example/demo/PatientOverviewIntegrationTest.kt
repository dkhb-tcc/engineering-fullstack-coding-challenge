package com.example.demo

import com.example.demo.patient.PatientClient
import com.example.demo.patient.PatientInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PatientOverviewIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @MockBean
    private lateinit var patientClient: PatientClient

    @Test
    fun shouldReturnLatestVitalSign() {
        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/patients/1/vitals/latest",
            Map::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        // The most recent measurement (2024-06-10) has heartRate 68
        assertEquals(68, (response.body?.get("heartRate") as Number).toInt())
    }

    @Test
    fun shouldReturnPatientOverviewWithCorrectNames() {
        given(patientClient.getPatient(1L))
            .willReturn(PatientInfo("1", "Anna", "Schmidt", "1983-04-12"))

        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/patients/1/overview",
            Map::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Anna", response.body?.get("firstName"))
        assertEquals("Schmidt", response.body?.get("lastName"))
    }
}
