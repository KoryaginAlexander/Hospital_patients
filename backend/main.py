from fastapi import FastAPI, Depends, HTTPException, status, Query
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from typing import List, Optional
from . import models, schemas, crud, auth, database
from sqlalchemy.exc import IntegrityError
from fastapi.middleware.cors import CORSMiddleware

models.Base.metadata.create_all(bind=database.engine)

app = FastAPI()

origins = ["*"]
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/login")

def get_db():
    db = database.SessionLocal()
    try:
        yield db
    finally:
        db.close()

def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)):
    username = auth.decode_access_token(token)
    if username is None:
        raise HTTPException(status_code=401, detail="Invalid authentication credentials")
    user = crud.get_user_by_username(db, username)
    if user is None:
        raise HTTPException(status_code=401, detail="User not found")
    return user

@app.post("/register", response_model=schemas.UserOut)
def register(user: schemas.UserCreate, db: Session = Depends(get_db)):
    db_user = crud.get_user_by_username(db, user.username)
    if db_user:
        raise HTTPException(status_code=400, detail="Username already registered")
    try:
        return crud.create_user(db, user)
    except IntegrityError:
        db.rollback()
        raise HTTPException(status_code=400, detail="Username already registered")

@app.post("/login", response_model=schemas.Token)
def login(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    user = crud.authenticate_user(db, form_data.username, form_data.password)
    if not user:
        raise HTTPException(status_code=400, detail="Incorrect username or password")
    access_token = auth.create_access_token(data={"sub": user.username})
    return {"access_token": access_token, "token_type": "bearer"}

# --- Hospital endpoints ---
@app.get("/hospitals", response_model=List[schemas.HospitalOut])
def get_hospitals(db: Session = Depends(get_db)):
    return crud.get_hospitals(db)

@app.post("/hospitals", response_model=schemas.HospitalOut)
def add_hospital(hospital: schemas.HospitalCreate, db: Session = Depends(get_db)):
    return crud.create_hospital(db, hospital)

@app.put("/hospitals/{hospital_id}", response_model=schemas.HospitalOut)
def update_hospital(hospital_id: int, hospital: schemas.HospitalUpdate, db: Session = Depends(get_db)):
    db_hospital = crud.update_hospital(db, hospital_id, hospital)
    if not db_hospital:
        raise HTTPException(status_code=404, detail="Hospital not found")
    return db_hospital

@app.delete("/hospitals/{hospital_id}")
def delete_hospital(hospital_id: int, db: Session = Depends(get_db)):
    success = crud.delete_hospital(db, hospital_id)
    if not success:
        raise HTTPException(status_code=404, detail="Hospital not found")
    return {"ok": True}

# --- Patient endpoints ---
@app.get("/patients", response_model=List[schemas.PatientOut])
def get_patients(hospital_id: Optional[int] = Query(None), db: Session = Depends(get_db)):
    return crud.get_patients(db, hospital_id=hospital_id)

@app.post("/patients", response_model=schemas.PatientOut)
def add_patient(patient: schemas.PatientCreate, db: Session = Depends(get_db)):
    return crud.create_patient(db, patient)

@app.put("/patients/{patient_id}", response_model=schemas.PatientOut)
def update_patient(patient_id: int, patient: schemas.PatientUpdate, db: Session = Depends(get_db)):
    db_patient = crud.update_patient(db, patient_id, patient)
    if not db_patient:
        raise HTTPException(status_code=404, detail="Patient not found")
    return db_patient

@app.delete("/patients/{patient_id}")
def delete_patient(patient_id: int, db: Session = Depends(get_db)):
    success = crud.delete_patient(db, patient_id)
    if not success:
        raise HTTPException(status_code=404, detail="Patient not found")
    return {"ok": True} 