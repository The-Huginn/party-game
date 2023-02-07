FROM python:3.11-slim-buster

LABEL description="Core of web application using gunicorn with workers of flask application."

ENV APP_DIR /app
WORKDIR ${APP_DIR}

ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

COPY app/ ${APP_DIR}

ENV FLASK_APP main.py

COPY requirements.txt requirements.txt
RUN pip3 install --upgrade pip && \
    pip3 install -r requirements.txt

RUN groupadd -g 1000 -r app && \
    useradd -u 1000 -r -g app -m -s /sbin/nologin -c "App user" app && \
    chown -R app:app ${APP_DIR} && \
    chmod -R 755 ${APP_DIR}

USER app

ENTRYPOINT [ "gunicorn", "--config", "gunicorn.conf.py", "main:app" ]