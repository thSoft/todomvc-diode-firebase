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
  def initialModel = AppModel(Todos(Seq()))

  override val actionHandler = composeHandlers(
    new TodoHandler(zoomRW(_.todos)((m, v) => m.copy(todos = v)).zoomRW(_.todoList)((m, v) => m.copy(todoList = v)), this)
  )
}

class TodoHandler[M](modelRW: ModelRW[M, Seq[Todo]], dispatcher: Dispatcher) extends ActionHandler(modelRW) {

  //
  def updateOne(id: TodoId)(f: TodoInfo => TodoInfo): Seq[Todo] =
    value.map {
      case Todo(`id`, info) => Todo(id, f(info))
      case other => other
    }

  def toggle(todo: Todo): Todo = {
    todo.copy(info = todo.info.copy(isCompleted = !todo.info.isCompleted))
  }

  def updateRemote(todo: Todo): Future[Action] = {
    firebase.child(todo.id.id).set(todo.info.toJson).toFuture.map(_ => NoAction)
  }

  def removeRemote(id: TodoId): Future[Action] = {
    firebase.child(id.id).remove().toFuture.map(_ => NoAction)
  }

  val firebase = new Firebase("https://thsoft.firebaseio.com/todos")

  override def handle = {
    case Init =>
      effectOnly(Effect(Future {
        firebase.on("child_added", (snapshot: FirebaseDataSnapshot, previousKey: js.UndefOr[String]) =>
          dispatcher.dispatch(Added(Todo(TodoId(snapshot.key()), TodoInfo.fromJson(snapshot.`val`()))))
        )
        firebase.on("child_removed", (snapshot: FirebaseDataSnapshot, previousKey: js.UndefOr[String]) =>
          dispatcher.dispatch(Deleted(TodoId(snapshot.key())))
        )
        firebase.on("child_changed", (snapshot: FirebaseDataSnapshot, previousKey: js.UndefOr[String]) =>
          dispatcher.dispatch(Updated(Todo(TodoId(snapshot.key()), TodoInfo.fromJson(snapshot.`val`()))))
        )
        NoAction
      }))
    case Add(title) =>
      effectOnly(Effect({
        val promise = Promise[Action]
        firebase.push(TodoInfo(title, false).toJson, (value: js.Any) => {
          promise.success(NoAction)
          ()
        })
        promise.future
      }))
    case Added(todo) =>
      updated(value :+ todo)
    case Update(todo) =>
      effectOnly(Effect(updateRemote(todo)))
    case Updated(todo) =>
      updated(updateOne(todo.id)(_ => todo.info))
    case Toggle(id) =>
      effectOnly(Effect(
        Future.traverse(value)(todo => {
          if (todo.id == id) {
            updateRemote(toggle(todo))
          } else {
            Future()
          }
        }).map(_ => NoAction)
      ))
    case ToggleAll =>
      effectOnly(Effect(
          Future.traverse(value)(todo => {
            updateRemote(toggle(todo))
          }).map(_ => NoAction)
          ))
    case Delete(id) =>
      effectOnly(Effect(
        removeRemote(id)
      ))
    case Deleted(id) =>
      updated(value.filterNot(_.id == id))
    case DeleteCompleted =>
      effectOnly(Effect(
        Future.traverse(value)(todo => {
          if (todo.info.isCompleted) {
            removeRemote(todo.id)
          } else {
            Future()
          }
        }).map(_ => NoAction)
      ))
  }
}
