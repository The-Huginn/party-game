# Structure of json files

## Required parameters for file
* "title" - The title displayed when selecting categories
* "description" - The description of tasks
* "tasks" - array of task objects

## Required parameters for task
* "task" - String description of task what to do. Can contain special values, which are processed before posting with following format: \<parameter>, few current options\
  
  *  \<timer> - Displays the number representation of parameter "timer".
  *  \<n> - where n is an integer from total number of players - 1. This randomly selects another player, who is not on turn.

## Optional parameters
| **name** | **Description** | **Default value** |
| :------- | :-------------: | ----------------: |
| template | You can specify, which template from templates/ directory is used. | "single-simple.html" |
| repeat | "Always" - task will repeat multiple times per game. "Once Per Player" - the task will appear for every player and then removed. Can be combined with **frequency** | "Never" |
| frequency | You can set how many times it should occur per repeat. If **repeat** is set to "Never" after the specified amount the task will no longer occur | 1 |
| price | The punishment for not completing the task | 1 |
| message | Allows you to override the default message used in template for punishment message | - |
| timer | When present a timer is added to the template. You specify the number of seconds for execution | 30 |


## Example file
```json
{
    "tasks": [
        {
            "task": "Your first repeating task.",
            "repeat": false,
            "frequency": 2
        },
        {
            "task": "Timer task for <timer> seconds with custom punishment and template.",
            "timer": 10,
            "price": 2,
            "message": "You were bad, now drink",
            "template": "all-simple.html"
        }
    ]
}
```