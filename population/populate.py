import requests, json

# api_url = "https://game.thehuginn.com/api/task/"
api_url = "http://localhost:8082/"

categories_file = open('categories.json', 'r')
categories = json.loads(categories_file.read())

tasks_file = open('tasks.json', 'r')
tasks = json.loads(tasks_file.read())

for category in categories:
    response = requests.post(api_url + '/category', json=category['payload'])
    id = response.json()['id']

    for translation in category['translations']:
        requests.post(api_url + '/category/translation/' + str(id) + '/' + translation['locale'], json=translation)

for task in tasks:
    final_url = api_url + '/task'
    if 'category' in task:
        final_url = final_url + '/category/' + str(task['category'])

    response = requests.post(final_url, json=task['payload'])
    id = response.json()['id']

    for translation in task['translations']:
        requests.post(api_url + '/task/' + str(id) + '/' + translation['locale'], data=translation['translation'].encode())