import json
import sys

lang = sys.argv[1]
name = sys.argv[2]

with open('i18n/' + lang + '.json') as f:
    data = json.load(f)

for item in data:
    # Simply check if it is array
    if isinstance(data[item], list) and len(data[item]) > 1:
        data[item] = data[item][1]
    if item == '':
        data[item]['name'] = name


with open('i18n/' + lang + '.json', 'w') as f:
    json.dump(data, f)