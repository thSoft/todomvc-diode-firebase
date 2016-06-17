package example

import scala.scalajs.js
import diode._
import diode.react.ReactConnector
import hu.thsoft.firebase.Firebase
import hu.thsoft.firebase.FirebaseDataSnapshot
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import upickle.default.readJs
import upickle.Js
import scala.concurrent.Promise
import upickle.default.writeJs

/**
  * AppCircuit provides the actual instance of the `AppModel` and all the action
  * handlers we need. Everything else comes from the `Circuit`
  */
object AppCircuit extends Circuit[AppModel] with ReactConnector[AppModel] {
  // define initial value for the application model
  def initialModel = AppModel(Todos(Map()))

  override val actionHandler = composeHandlers(
    new TodoHandler(zoomRW(_.todos)((m, v) => m.copy(todos = v)).zoomRW(_.entries)((m, v) => m.copy(entries = v)), this)
  )
}

class TodoHandler[M](modelRW: ModelRW[M, Map[TodoId, Todo]], dispatcher: Dispatcher) extends ActionHandler(modelRW) {

  //
  def updateOne(id: TodoId)(f: Todo => Todo): Map[TodoId, Todo] =
    value.map {
      case (`id`, info) => (id, f(info))
      case other => other
    }

  def toggle(todo: Todo): Todo = {
    todo.copy(isCompleted = !todo.isCompleted)
  }

  def updateRemote(id: TodoId, todo: Todo): Future[Action] = {
    firebase.child(id.id).set(todo.toJson).toFuture.map(_ => NoAction)
  }

  def removeRemote(id: TodoId): Future[Action] = {
    firebase.child(id.id).remove().toFuture.map(_ => NoAction)
  }

  val firebase = new Firebase("https://thsoft.firebaseio.com/todos")

  override def handle = {
    case Init =>
      effectOnly(Effect(Future {
        firebase.on("child_added", (snapshot: FirebaseDataSnapshot, previousKey: js.UndefOr[String]) =>
          dispatcher.dispatch(Added(TodoId(snapshot.key()), Todo.fromJson(snapshot.`val`())))
        )
        firebase.on("child_removed", (snapshot: FirebaseDataSnapshot, previousKey: js.UndefOr[String]) =>
          dispatcher.dispatch(Deleted(TodoId(snapshot.key())))
        )
        firebase.on("child_changed", (snapshot: FirebaseDataSnapshot, previousKey: js.UndefOr[String]) =>
          dispatcher.dispatch(Updated(TodoId(snapshot.key()), Todo.fromJson(snapshot.`val`())))
        )
        NoAction
      }))
    case Add(info) =>
      effectOnly(Effect({
        val promise = Promise[Action]
        firebase.push(info.toJson, (value: js.Any) => {
          promise.success(NoAction)
          ()
        })
        promise.future
      }))
    case Added(id, todo) =>
      updated(value + ((id, todo)))
    case Update(id, todo) =>
      effectOnly(Effect(updateRemote(id, todo)))
    case Updated(id, todo) =>
      updated(updateOne(id)(_ => todo))
    case Delete(id) =>
      effectOnly(Effect(
        removeRemote(id)
      ))
    case Deleted(id) =>
      updated(value.filterNot(_._1 == id))
    case Toggle(id) =>
      effectOnly(Effect(
        Future.traverse(value)(entry => {
          val entryId = entry._1
          val todo = entry._2
          if (entryId == id) {
            updateRemote(id, toggle(todo))
          } else {
            Future()
          }
        }).map(_ => NoAction)
      ))
    case ToggleAll =>
      effectOnly(Effect(
          Future.traverse(value)(entry => {
            val id = entry._1
            val todo = entry._2
            updateRemote(id, toggle(todo))
          }).map(_ => NoAction)
          ))
    case DeleteCompleted =>
      effectOnly(Effect(
        Future.traverse(value)(entry => {
          val id = entry._1
          val todo = entry._2
          if (todo.isCompleted) {
            removeRemote(id)
          } else {
            Future()
          }
        }).map(_ => NoAction)
      ))
  }
}
