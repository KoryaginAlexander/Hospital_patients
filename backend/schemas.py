from pydantic import BaseModel
from typing import Optional
from datetime import date

class UserCreate(BaseModel):
    username: str
    password: str

class UserOut(BaseModel):
    id: int
    username: str
    class Config:
        orm_mode = True

class Token(BaseModel):
    access_token: str
    token_type: str

class HospitalBase(BaseModel):
    name: str
    address: str
    phone: str

class HospitalCreate(HospitalBase):
    pass

class HospitalUpdate(HospitalBase):
    pass

class HospitalOut(HospitalBase):
    id: int
    class Config:
        orm_mode = True

class PatientBase(BaseModel):
    name: str
    surname: str
    patronymic: str
    age: int
    diagnosis: str
    address: str
    phone: str
    admission_date: date
    hospital_id: int

class PatientCreate(PatientBase):
    pass

class PatientUpdate(PatientBase):
    pass

class PatientOut(PatientBase):
    id: int
    class Config:
        orm_mode = True 