package controllers

import auth.{AuthAction, AuthService}
import dao.UserDao
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(
  cc: ControllerComponents,
  userDao: UserDao,
  authAction: AuthAction,
  authService: AuthService) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def test() = authAction { implicit request =>
    // FIXME: this is not done as per instructions but it works
    Ok("Logged In : " + request.user.username)
  }

  def login(username: String, pass: String) = Action { implicit request: Request[AnyContent] =>
    if (isValidLogin(username, pass)) {
      val token = authService.generateToken(username)

      Ok(Json.obj("currentUser" -> username, "jwt" -> token))
    } else {
      // we should redirect to login page
      Unauthorized(Json.obj("currentUser" -> "none")).withNewSession
    }
  }

  private def isValidLogin(username: String, password: String): Boolean = {
    userDao.getByUsername2(username).exists(_.password == password)
  }
}
