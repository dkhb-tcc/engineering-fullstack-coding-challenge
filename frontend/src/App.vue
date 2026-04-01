<template>
  <div class="patient-overview">
    <h1>Patient Overview</h1>
    <div v-if="loading">Loading…</div>
    <div v-else-if="error" style="color: red">{{ error }}</div>
    <div v-else-if="patient">
      <p><strong>Name:</strong> {{ patient.firstName }} {{ patient.lastName }}</p>
      <p><strong>Date of Birth:</strong> {{ patient.dateOfBirth }}</p>
      <p><strong>Heart Rate:</strong> {{ patient.latestVitalSign.heartRate }} bpm</p>
      <p><strong>Measured:</strong> {{ formatDate(patient.latestVitalSign.measuredAt) }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

function formatDate(ts) {
  return new Date(ts).toLocaleDateString('de-DE', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

const patient = ref(null)
const loading = ref(true)
const error = ref(null)

onMounted(async () => {
  try {
    const res = await fetch('/api/patients/1/overview')
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    patient.value = await res.json()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
})
</script>
