package example

import java.util.UUID
import scala.scalajs.js
import diode.Action
import upickle.Js
import upickle.default.readJs
import upickle.Js
import scala.concurrent.Promise
import upickle.default.writeJs

// Define our application model
case class AppModel(todos: Todos)

case class TodoId(id: String)

case class Todos(entries: Map[TodoId, Todo])

case class Todo(title: String, isCompleted: Boolean) {
  def toJson: js.Any = upickle.json.writeJs(writeJs[Todo](this)).asInstanceOf[js.Any]
}

object Todo {
  def fromJson(json: js.Any): Todo = {
    readJs[Todo](upickle.json.readJs(json))
  }
}

sealed abstract class TodoFilter(val link: String, val title: String, val accepts: Todo => Boolean)

object TodoFilter {

  object All extends TodoFilter("", "All", _ => true)

  object Active extends TodoFilter("active", "Active", !_.isCompleted)

  object Completed extends TodoFilter("completed", "Completed", _.isCompleted)

  val values = List[TodoFilter](All, Active, Completed)
}

// define actions
case object Init extends Action

case class Add(todo: Todo) extends Action

case class Added(id: TodoId, todo: Todo) extends Action

case class Update(id: TodoId, todo: Todo) extends Action

case class Updated(id: TodoId, todo: Todo) extends Action

case class Delete(id: TodoId) extends Action

case class Deleted(id: TodoId) extends Action

case class Toggle(id: TodoId) extends Action

case object ToggleAll extends Action

case object DeleteCompleted extends Action

case class SelectFilter(filter: TodoFilter) extends Action