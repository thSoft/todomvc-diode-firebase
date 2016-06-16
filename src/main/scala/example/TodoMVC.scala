package example

import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._

@JSExport("TodoMVC")
object TodoMVC extends JSApp {

  val baseUrl = BaseUrl(dom.window.location.href.takeWhile(_ != '#'))

  val routerConfig: RouterConfig[TodoFilter] = RouterConfigDsl[TodoFilter].buildConfig { dsl =>
    import dsl._

    val todoConnection = AppCircuit.connect(_.todos)

    /* how the application renders the list given a filter */
    def filterRoute(s: TodoFilter): Rule = staticRoute("#/" + s.link, s) ~> renderR(router => todoConnection(p => TodoList(p, s, router)))

    val filterRoutes: Rule = TodoFilter.values.map(filterRoute).reduce(_ | _)

    /* build a final RouterConfig with a default page */
    filterRoutes.notFound(redirectToPage(TodoFilter.All)(Redirect.Replace))
  }

  /** The router is itself a React component, which at this point is not mounted (U-suffix) */
  val router: ReactComponentU[Unit, Resolution[TodoFilter], Any, TopNode] =
    Router(baseUrl, routerConfig.logToConsole)()

  @JSExport
  override def main(): Unit = {
    AppCircuit.dispatch(Init)
    ReactDOM.render(router, dom.document.getElementsByClassName("todoapp")(0))
  }
}
