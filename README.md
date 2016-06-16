# TodoMVC Diode React + Firebase Example

The familiar TodoMVC application implemented in Diode React with Firebase persistence. Adapted from
[TodoMVC Diode React](https://github.com/ochrons/diode/tree/master/examples/todomvc).

## Usage

```
git clone https://github.com/ochrons/diode.git
cd diode
sbt publishLocal
cd ..
git clone https://github.com/thSoft/firebase-scalajs
cd firebase-scalajs
sbt publishLocal
cd ..
git clone https://github.com/thSoft/todomvc-diode-firebase
cd todomvc-diode-firebase
sbt ~fastOptJS
```
1. Wait for update/compilation to complete (you'll see `1. Waiting for source changes... (press enter to interrupt)`)
1. Navigate to [http://localhost:12345/target/scala-2.11/classes/index.html](http://localhost:12345/target/scala-2.11/classes/index.html)
1. Press `Enter` in console to quit
