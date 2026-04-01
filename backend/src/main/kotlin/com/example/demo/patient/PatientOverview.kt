package com.example.demo.patient

import com.example.demo.vitalsign.VitalSign

data class PatientOverview(
    val patientId: Long,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val latestVitalSign: VitalSign,
)
