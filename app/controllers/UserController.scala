package controllers

import forms.UserForm
import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import services.UserService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class UserController @Inject()(userService: UserService)
                              (implicit ec: ExecutionContext) extends InjectedController with I18nSupport {

  lazy val logger: Logger = Logger(getClass)

  def create: Action[AnyContent] = Action.async { implicit request =>
    withFormErrorHandling(UserForm.create, "create failed") { user =>
      userService
        .create(user)
        .map(user => Created(Json.toJson(user)))
    }
  }

  def update: Action[AnyContent] = Action.async { implicit request =>
    withFormErrorHandling(UserForm.create, "update failed") { user =>
      userService
        .update(user)
        .map(user => Ok(Json.toJson(user)))
    }
  }

  def list: Action[AnyContent] = Action.async { implicit request =>
    userService
      .getAll()
      .map(users => Ok(Json.toJson(users)))
  }

  def get(id: Long): Action[AnyContent] = Action.async { implicit request =>
    userService
      .get(id)
      .map(maybeUser => Ok(Json.toJson(maybeUser)))
  }

  def delete(id: Long): Action[AnyContent] = Action.async {
    userService
      .delete(id)
      .map(_ => Ok(""))
  }

  private def withFormErrorHandling[A](form: Form[A], onFailureMessage: String)
                                      (block: A => Future[Result])
                                      (implicit request: Request[AnyContent]): Future[Result] = {
    form.bindFromRequest.fold(
      errors => {
        Future.successful(BadRequest(errors.errorsAsJson))
      }, {
        model => {
          Try(block(model)) match {
            case Failure(e) => {
              logger.error(onFailureMessage, e)
              Future.successful(InternalServerError)
            }
            case Success(eventualResult) => eventualResult.recover {
              case e =>
                logger.error(onFailureMessage, e)
                InternalServerError
            }
          }
        }
      }
    )
  }
}