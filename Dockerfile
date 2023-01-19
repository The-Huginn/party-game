FROM python:3.11-slim-buster

WORKDIR /python-docker

COPY requirements.txt requirements.txt
RUN pip3 install -r requirements.txt

COPY endpoints endpoints
COPY entities entities
COPY services services
COPY static static
COPY templates templates
COPY main.py main.py

ENV FLASK_APP main.py

CMD ["python3", "-m", "flask", "run", "--host=0.0.0.0"]