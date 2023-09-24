import requests, json, glob, time
from pathlib import Path

# api_url = "https://game.thehuginn.com/api/task/"
api_url = "http://localhost:8082/"
ignore = ['template']

# Delete request made to /clearAll
total_requests = 1

def make_request(url, json=None, data=None):
    global total_requests
    total_requests = total_requests + 1
    
    response = requests.post(url, json=json, data=data)
    if response.status_code != 200:
        print(f'[ERROR] - status [{response.status_code}] url[{url}] json[{json}] data[{data}]')
    return response.json()

start = time.time()

requests.delete(api_url + '/task-mode/clearAll')

tasks = [Path(x).stem for x in glob.glob('tasks/*.json')]
for task in tasks:
    if task in ignore:
        continue

    f = open(f'tasks/{task}.json', 'r')
    data = json.loads(f.read())

    if 'category' in data:
        id = make_request(api_url + '/category', json=data['category']['payload'])['id']
        for translation in data['category']['translations']:
            make_request(api_url + '/category/translation/' + str(id) + '/' + translation['locale'], json=translation)
    else:
        id = 0

    for task in data['tasks']:
        final_url = api_url + '/task/category/' + str(id)

        task_id = make_request(final_url, json=task['payload'])['id']

        for translation in task['translations']:
            make_request(api_url + '/task/' + str(task_id) + '/' + translation['locale'], data=translation['translation'].encode())

end = time.time()

print(f'Total number of requests: {total_requests} done in {end - start} seconds')