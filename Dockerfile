FROM python:3.11
WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt
COPY ./backend /app/backend
CMD ["uvicorn", "backend.main:app", "--host", "0.0.0.0", "--port", "8000"] 