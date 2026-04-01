package com.example.demo.vitalsign.messaging

import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["rabbitmq.enabled"], havingValue = "true", matchIfMissing = false)
class VitalSignListener(private val dsl: DSLContext) {

    companion object {
        private val TABLE         = DSL.table("vital_sign")
        private val F_ID          = DSL.field("id",          Long::class.java)
        private val F_PATIENT_ID  = DSL.field("patient_id",  Long::class.java)
        private val F_MEASURED_AT = DSL.field("measured_at", Long::class.java)
        private val F_HEART_RATE  = DSL.field("heart_rate",  Int::class.java)
    }

    @RabbitListener(queues = [VITAL_SIGN_QUEUE])
    fun onVitalSign(message: VitalSignMessage) {
        dsl.insertInto(TABLE)
            .set(F_ID,          System.nanoTime())
            .set(F_PATIENT_ID,  message.patientId)
            .set(F_MEASURED_AT, message.measuredAt)
            .set(F_HEART_RATE,  message.heartRate)
            .execute()
    }
}
