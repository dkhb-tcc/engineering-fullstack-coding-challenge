package com.example.demo.vitalsign.messaging

data class VitalSignMessage(
    val patientId: Long,
    val measuredAt: Long,
    val heartRate: Int,
)
