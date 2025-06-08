from sqlalchemy.orm import Session
from . import models, schemas, auth
from typing import List, Optional

def get_user_by_username(db: Session, username: str) -> Optional[models.User]:
    return db.query(models.User).filter(models.User.username == username).first()

def create_user(db: Session, user: schemas.UserCreate) -> models.User:
    hashed_password = auth.get_password_hash(user.password)
    db_user = models.User(username=user.username, hashed_password=hashed_password)
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

def authenticate_user(db: Session, username: str, password: str) -> Optional[models.User]:
    user = get_user_by_username(db, username)
    if not user or not auth.verify_password(password, user.hashed_password):
        return None
    return user

# --- Hospital CRUD ---
def get_hospitals(db: Session) -> List[models.Hospital]:
    return db.query(models.Hospital).all()

def get_hospital(db: Session, hospital_id: int) -> Optional[models.Hospital]:
    return db.query(models.Hospital).filter(models.Hospital.id == hospital_id).first()

def create_hospital(db: Session, hospital: schemas.HospitalCreate) -> models.Hospital:
    db_hospital = models.Hospital(**hospital.dict())
    db.add(db_hospital)
    db.commit()
    db.refresh(db_hospital)
    return db_hospital

def update_hospital(db: Session, hospital_id: int, hospital: schemas.HospitalUpdate) -> Optional[models.Hospital]:
    db_hospital = get_hospital(db, hospital_id)
    if not db_hospital:
        return None
    for key, value in hospital.dict().items():
        setattr(db_hospital, key, value)
    db.commit()
    db.refresh(db_hospital)
    return db_hospital

def delete_hospital(db: Session, hospital_id: int) -> bool:
    db_hospital = get_hospital(db, hospital_id)
    if not db_hospital:
        return False
    db.delete(db_hospital)
    db.commit()
    return True

# --- Patient CRUD ---
def get_patients(db: Session, hospital_id: Optional[int] = None) -> List[models.Patient]:
    query = db.query(models.Patient)
    if hospital_id is not None:
        query = query.filter(models.Patient.hospital_id == hospital_id)
    return query.all()

def get_patient(db: Session, patient_id: int) -> Optional[models.Patient]:
    return db.query(models.Patient).filter(models.Patient.id == patient_id).first()

def create_patient(db: Session, patient: schemas.PatientCreate) -> models.Patient:
    db_patient = models.Patient(**patient.dict())
    db.add(db_patient)
    db.commit()
    db.refresh(db_patient)
    return db_patient

def update_patient(db: Session, patient_id: int, patient: schemas.PatientUpdate) -> Optional[models.Patient]:
    db_patient = get_patient(db, patient_id)
    if not db_patient:
        return None
    for key, value in patient.dict().items():
        setattr(db_patient, key, value)
    db.commit()
    db.refresh(db_patient)
    return db_patient

def delete_patient(db: Session, patient_id: int) -> bool:
    db_patient = get_patient(db, patient_id)
    if not db_patient:
        return False
    db.delete(db_patient)
    db.commit()
    return True 