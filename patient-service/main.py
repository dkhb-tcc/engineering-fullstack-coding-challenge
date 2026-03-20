from fastapi import FastAPI

app = FastAPI()

@app.get("/patients/{patient_id}")
def get_patient(patient_id: str):
    return {
        "patientId": patient_id,
        "firstName": "Anna",
        "lastName": "Schmidt",
        "dateOfBirth": "1983-04-12",
    }
