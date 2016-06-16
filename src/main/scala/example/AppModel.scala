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

case class Todos(todoList: Seq[Todo])

case class Todo(id: TodoId, info: TodoInfo)

case class TodoInfo(title: String, isCompleted: Boolean) {
  def toJson: js.Any = upickle.json.writeJs(writeJs[TodoInfo](this)).asInstanceOf[js.Any]
}

object TodoInfo {
  def fromJson(json: js.Any): TodoInfo = {
    readJs[TodoInfo](upickle.json.readJs(json))
  }
}

sealed abstract class TodoFilter(val link: String, val title: String, val accepts: Todo => Boolean)

object TodoFilter {

  object All extends TodoFilter("", "All", _ => true)

  object Active extends TodoFilter("active", "Active", !_.info.isCompleted)

  object Completed extends TodoFilter("completed", "Completed", _.info.isCompleted)

  val values = List[TodoFilter](All, Active, Completed)
}

// define actions
case object Init extends Action

case class Add(title: String) extends Action

case class Added(todo: Todo) extends Action

case class Update(todo: Todo) extends Action

case class Updated(todo: Todo) extends Action

case class Toggle(id: TodoId) extends Action

case object ToggleAll extends Action

case class Delete(id: TodoId) extends Action

case class Deleted(id: TodoId) extends Action

case object DeleteCompleted extends Action

case class SelectFilter(filter: TodoFilter) extends Action