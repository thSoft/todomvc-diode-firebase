package example

import diode.react.ModelProxy
import diode.Action
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.ext.KeyCode

object TodoList {

  case class Props(proxy: ModelProxy[Todos], currentFilter: TodoFilter, ctl: RouterCtl[TodoFilter])

  case class State(editing: Option[TodoId])

  class Backend($: BackendScope[Props, State]) {
    def mounted(props: Props) = Callback {}

    def handleNewTodoKeyDown(dispatch: Action => Callback)(e: ReactKeyboardEventI): Option[Callback] = {
      val title = e.target.value.trim
      if (e.nativeEvent.keyCode == KeyCode.Enter && title.nonEmpty) {
        Some(Callback(e.target.value = "") >> dispatch(Add(Todo(title, false))))
      } else {
        None
      }
    }

    def editingDone(): Callback =
      $.modState(_.copy(editing = None))

    val startEditing: TodoId => Callback =
      id => $.modState(_.copy(editing = Some(id)))

    def render(p: Props, s: State) = {
      val proxy = p.proxy()
      val dispatch = (action: Action) => p.proxy.dispatch(action)
      val todos = proxy.entries
      val filteredTodos = todos.filter(entry => p.currentFilter.accepts.apply(entry._2))
      val activeCount = todos.count(entry => TodoFilter.Active.accepts.apply(entry._2))
      val completedCount = todos.size - activeCount

      <.div(
        <.h1("todos"),
        <.header(
          ^.className := "header",
          <.input(
            ^.className := "new-todo",
            ^.placeholder := "What needs to be done?",
            ^.onKeyDown ==>? handleNewTodoKeyDown(dispatch),
            ^.autoFocus := true
          )
        ),
        todos.nonEmpty ?= todoList(dispatch, s.editing, filteredTodos, activeCount),
        todos.nonEmpty ?= footer(p, dispatch, p.currentFilter, activeCount, completedCount)
      )
    }

    def todoList(dispatch: Action => Callback, editing: Option[TodoId], todos: Map[TodoId, Todo], activeCount: Int) =
      <.section(
        ^.className := "main",
        <.input.checkbox(
          ^.className := "toggle-all",
          ^.checked := activeCount == 0,
          ^.onChange ==> { e: ReactEventI => dispatch(ToggleAll) }
        ),
        <.ul(
          ^.className := "todo-list",
          todos.toSeq.map((entry) => {
            val id = entry._1
            val todo = entry._2
            TodoView(TodoView.Props(
                onToggle = dispatch(Toggle(id)),
                onDelete = dispatch(Delete(id)),
                onStartEditing = startEditing(id),
                onUpdateTitle = title => dispatch(Update(id, todo.copy(title = title))) >> editingDone(),
                onCancelEditing = editingDone(),
                id = id,
                todo = todo,
                isEditing = editing.contains(id)
            ))
          }
          )
        )
      )

    def footer(p: Props, dispatch: Action => Callback, currentFilter: TodoFilter, activeCount: Int, completedCount: Int): ReactElement =
      Footer(Footer.Props(
        filterLink = p.ctl.link,
        onSelectFilter = f => dispatch(SelectFilter(f)),
        onClearCompleted = dispatch(DeleteCompleted),
        currentFilter = currentFilter,
        activeCount = activeCount,
        completedCount = completedCount
      ))
  }

  private val component = ReactComponentB[Props]("TodoList")
    .initialState_P(p => State(None))
    .renderBackend[Backend]
      .componentDidMount(scope => scope.backend.mounted(scope.props))
    .build

  def apply(proxy: ModelProxy[Todos], currentFilter: TodoFilter, ctl: RouterCtl[TodoFilter]) = component(Props(proxy, currentFilter, ctl))
}
