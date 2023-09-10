url='https://game.thehuginn.com/api/task'
# url='http://localhost:8082'

curl -X 'POST' \
  "${url}/task" \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "type": "SINGLE",
  "repeat": "ALWAYS",
  "frequency": 1,
  "price": {
    "enabled": true,
    "price": 0
  },
  "task": {
    "locale": "en",
    "content": "{player_c} talk to {player_1} for {timer_10}"
  }
}'

curl -X 'POST' \
  "${url}/task" \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "type": "SINGLE",
  "repeat": "ALWAYS",
  "frequency": 1,
  "price": {
    "enabled": true,
    "price": 0
  },
  "task": {
    "locale": "en",
    "content": "{player_c} dance for {timer_10}"
  }
}'

curl -X 'POST' \
  "${url}/task" \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "type": "SINGLE",
  "repeat": "ALWAYS",
  "frequency": 1,
  "price": {
    "enabled": true,
    "price": 0
  },
  "task": {
    "locale": "en",
    "content": "{player_c} look outside"
  }
}'

curl -X 'POST' \
  "${url}/task" \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "type": "SINGLE",
  "repeat": "ALWAYS",
  "frequency": 1,
  "price": {
    "enabled": true,
    "price": 0
  },
  "task": {
    "locale": "en",
    "content": "{player_c} dance for {timer_10_3}"
  }
}'

curl -X 'POST' \
  "${url}/task" \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "type": "SINGLE",
  "repeat": "ALWAYS",
  "frequency": 1,
  "price": {
    "enabled": true,
    "price": 0
  },
  "task": {
    "locale": "en",
    "content": "{player_c} sing for {timer_10_-1}"
  }
}'

curl -X 'POST' \
  "${url}/task" \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "type": "DUO",
  "repeat": "ALWAYS",
  "frequency": 1,
  "price": {
    "enabled": true,
    "price": 0
  },
  "task": {
    "locale": "en",
    "content": "Smile at each other for {timer_10_-1}"
  }
}'

curl -X 'POST' \
  "${url}/task/3/sk" \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{player_c} pozri sa von'

curl -X 'POST' \
  "${url}/task/5/sk" \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{player_c} spievaj {timer_10_-1}'

curl -X 'POST' \
  "${url}/task/6/sk" \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d 'Usmievajte sa jeden na druheho na {timer_10_-1}'
