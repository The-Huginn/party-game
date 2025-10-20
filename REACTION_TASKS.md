# Reaction Tasks Feature

This document describes the new reaction tasks feature that adds random, fast-paced challenges to the game.

## Overview

Reaction tasks are a new category of tasks that appear randomly during gameplay and challenge all players to react quickly. When a reaction task appears:

1. A beep sound plays immediately when the task starts
2. Players see the task instruction (e.g., "Stand up! Last one to stand drinks")
3. A countdown timer gives players a few seconds to react
4. Another beep sounds when time expires
5. The last player to complete the action drinks

## Implementation Details

### Changes Made

1. **Frontend Timer Enhancement** (`frontend/src/routes/game/task/TimerComponent.svelte`)
   - Modified `startTimer()` function to play a beep sound when the timer starts
   - This creates the "GO!" signal for reaction tasks
   - The existing end-of-timer beep creates the "TIME'S UP!" signal

2. **New Task Category** (`population/tasks/reaction-tasks.json`)
   - Created a new "Reaction Tasks" category
   - Added 4 different reaction tasks:
     - Stand up (5 seconds)
     - Touch your nose (4 seconds)
     - Raise your hand (3 seconds)
     - Point at the ceiling (4 seconds)
   - Each task includes English and Slovak translations

### Task Configuration

All reaction tasks use the following settings:
- **Type**: `ALL` - All players participate
- **Repeat**: `NEVER` - Each task appears only once per game
- **Frequency**: `1` - Each task appears once in the shuffled deck
- **Timer**: `{timer_X_0}` format where X is the duration in seconds, 0 is the delay (autostart)
- **Price**: 1 drink for the last player to react

## How to Add These Tasks to Your Game

### Prerequisites
- Python 3 installed
- Backend services running (see main README.md)
- Python dependencies installed: `pip install -r population/requirements.txt`

### Steps to Populate Tasks

1. **Ensure the backend is running:**
   ```bash
   cd backend/task-game
   mvn clean quarkus:dev
   ```

2. **Run the population script:**
   ```bash
   cd population
   python populate.py
   ```

   This script will:
   - Create the "Reaction Tasks" category
   - Add all 4 reaction tasks to the database
   - Set up English and Slovak translations

3. **Start a new game:**
   - The reaction tasks will now be included in the shuffled task deck
   - They will appear at random points during gameplay

## How Reaction Tasks Work in Gameplay

1. **Task Appears**: Players see the reaction task on screen with a timer
2. **First Beep**: Timer starts automatically and plays a beep (this is the signal to GO!)
3. **Countdown**: Timer counts down from X seconds
4. **Players React**: All players must perform the action (stand up, touch nose, etc.)
5. **Second Beep**: Timer expires and plays another beep (time's up!)
6. **Consequence**: The last player to have completed the action drinks 1 drink

## Customization

You can add more reaction tasks by editing `population/tasks/reaction-tasks.json`:

```json
{
    "payload": {
        "repeat": "NEVER",
        "type": "ALL",
        "frequency": 1,
        "task": {
            "locale": "en",
            "content": "Your action here! Last one to do it drinks {timer_4_0}"
        },
        "price": {
            "enabled": true,
            "price": 1
        }
    },
    "translations": [
        {
            "locale": "sk",
            "translation": "Your Slovak translation {timer_4_0}"
        }
    ]
}
```

**Timer Format**: `{timer_DURATION_DELAY}`
- `DURATION`: How many seconds the timer runs (e.g., 3, 4, 5)
- `DELAY`: Delay before timer starts (use 0 for immediate autostart)

## Technical Notes

- Tasks are shuffled at game start, so reaction tasks appear at unpredictable times
- The beep sound (`beep.mp4`) is reused for both timer start and end
- Timer durations can be adjusted per task (recommended: 3-5 seconds for quick reactions)
- All players must participate, making these tasks perfect for group engagement

## Future Enhancements

Possible improvements:
- Add different sound effects for start vs. end beeps
- Create variable-delay tasks that start after 1-3 seconds for surprise factor
- Add more varied reaction tasks with different difficulties
- Implement reaction time tracking to determine exact last player
