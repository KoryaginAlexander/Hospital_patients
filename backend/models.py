from sqlalchemy import Column, Integer, String, ForeignKey, Date
from sqlalchemy.orm import relationship
from .database import Base

class Hospital(Base):
    __tablename__ = "hospitals"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, index=True)
    address = Column(String)
    phone = Column(String)
    patients = relationship("Patient", back_populates="hospital")

class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True)
    hashed_password = Column(String)

class Patient(Base):
    __tablename__ = "patients"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String)
    surname = Column(String)
    patronymic = Column(String)
    age = Column(Integer)
    diagnosis = Column(String)
    address = Column(String)
    phone = Column(String)
    admission_date = Column(Date)
    hospital_id = Column(Integer, ForeignKey("hospitals.id"))
    hospital = relationship("Hospital", back_populates="patients") 