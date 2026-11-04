# knowledgeBoost

A small Android app that helps you remember what you are learning, one question at a time.

Pick a category, then a topic, and go through its questions in file order. Answer from memory, then tap **Show answer** to check the short answer, the explanation and, for some questions, a code example. **Previous** and **Next** move between questions without grading anything. The app remembers where you stopped in each topic.

After **Show answer**, grade yourself:

- **Knew it** hides the question from its topic for 7 days, then 30, 90, 180 and 360 days, and then every 360 days. The card says how many days the next **Knew it** hides it for. A hidden question comes back on its day and waits for you, even after a long break.
- **Missed** puts the question on the **Missed** list and starts its steps again from 7 days. It stays available in its topic.
- **Remove** hides the question everywhere. The **Removed** screen lets you restore it exactly as it was.

More:

- Each topic shows how many questions are available now, hidden as known, and on the Missed list
- The **Missed** list shows every missed question with its category and topic, also the ones hidden for now. Go through them with the same card, or **Clear** them from the list; a question leaves the list only when you clear it or remove it
- Dark mode, fully offline: no account, no network, no notifications, all data stays on the phone and is not copied to a cloud backup or a new phone

## Adding a topic

Every topic is one JSON file in `app/src/main/assets/topics/`. The file name is the topic's id, for example `redis.json`. The app ships with four sample topics (AWS, Redis, Kotlin and Python); replace them or add your own.

```json
{
  "category": "Databases",
  "title": "Redis",
  "questions": [
    {
      "id": "redis-event-loop",
      "question": "Why does Redis use an event loop?",
      "answer": "One thread multiplexes many client sockets, so commands run one at a time without locks.",
      "explanation": "Redis keeps data in memory, so a command is usually faster than the network round trip ...",
      "code": [
        "while (!stop) {",
        "    events = epoll_wait(fd, ...);",
        "    for (e in events) handle(e);",
        "}"
      ]
    }
  ]
}
```

- `category`, `title` and `questions` are required. Topics with the same `category` are listed together; categories and topics are sorted by name.
- Every question needs a non-blank `id`, `question`, `answer` and `explanation`. `code` is optional: one string per line, shown in a monospace font that scrolls sideways. Use `""` for an empty line.
- The file name and every question `id` are lower-case slugs such as `redis-event-loop`: letters, digits and single hyphens.
- A question `id` is the question's permanent key and must be unique across **all** topic files. Never change it once the app is installed, or its progress is lost.
- Text can be corrected at any time, and questions can be added or reordered; progress stays.
- Deleting a question hides it; its progress comes back if the same `id` is added again.

Then:

1. Run `./gradlew --offline :app:testDebugUnitTest`. The tests check every topic file; a file that breaks a rule is skipped by the app, so fix what the tests report.
2. Bump `versionCode` in `app/build.gradle.kts`.
3. Build with `./gradlew --offline :app:assembleDebug` and install with `adb install -r app/build/outputs/apk/debug/app-debug.apk`. Installing over the old version keeps your progress.

## Testing the day steps

Debug builds accept a clock offset in minutes over adb, so the 7- to 360-day steps can be checked without waiting:

```
adb shell am broadcast -f 0x20 -n com.krzyzowski.knowledgeboost/.debug.DebugCommandReceiver \
  -a com.krzyzowski.knowledgeboost.debug.SET_TIME_OFFSET --ei minutes 10080
```

`10080` moves the app's clock 7 days ahead; `0` resets it. Release builds do not contain this receiver.

## Future ideas

- A daily-question mode: one question a day from the topics you choose
- Statistics per topic
- Search across all questions
- In-app topic import from a file

Built with Kotlin and Jetpack Compose, Android 8.0+.
