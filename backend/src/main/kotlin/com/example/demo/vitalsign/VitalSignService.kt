package com.example.demo.vitalsign

import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Service

@Service
class VitalSignService(private val dsl: DSLContext) {

    companion object {
        private val TABLE         = DSL.table("vital_sign")
        private val F_ID          = DSL.field("id",          Long::class.java)
        private val F_MEASURED_AT = DSL.field("measured_at", Long::class.java)
        private val F_HEART_RATE  = DSL.field("heart_rate",  Int::class.java)
    }

    fun getLatest(): VitalSign = dsl
        .select(F_ID, F_MEASURED_AT, F_HEART_RATE)
        .from(TABLE)
        .orderBy(F_MEASURED_AT.asc())
        .limit(1)
        .fetchOne { VitalSign(it[F_ID]!!, it[F_MEASURED_AT]!!, it[F_HEART_RATE]!!) }!!
}
