package controllers

import forms.{BefriendsForm, UserForm}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import services.{BefriendsService, UserService}
import utils.EStatus
import utils.ErrorMsg.{ID_NOT_FOUND_ERROR, ID_NOT_FOUND_OR_USERNAME_FOUND_ERROR, OBJECT_NOT_CREATED_ERROR, UPDATED_OBJECT_NOT_FOUND_ERROR}

import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class UserController @Inject()(userService: UserService, befriendsService: BefriendsService)
                              (implicit ec: ExecutionContext) extends InjectedController with I18nSupport {

  lazy val logger: Logger = Logger(getClass)

  def create: Action[AnyContent] = Action.async { implicit request =>
    withFormErrorHandling(UserForm.create, "create failed") { user =>
      userService.create(user).map {
        case EStatus.Success => Created(Json.toJson(user))
        case EStatus.Failure => BadRequest(OBJECT_NOT_CREATED_ERROR)
      }
    }
  }

  def update: Action[AnyContent] = Action.async { implicit request =>
    withFormErrorHandling(UserForm.create, "update failed") { user =>
      userService.update(user).map {
        case EStatus.Success => {
          val updatedUser = Await.result(userService.get(user.id.get), Duration(10, TimeUnit.SECONDS))
          if (updatedUser == null) BadRequest(UPDATED_OBJECT_NOT_FOUND_ERROR) else Ok(Json.toJson(updatedUser))
        }
        case EStatus.Failure => BadRequest(ID_NOT_FOUND_OR_USERNAME_FOUND_ERROR)
      }
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
      .map(maybeUser => if (maybeUser.isEmpty) BadRequest(ID_NOT_FOUND_ERROR) else Ok(Json.toJson(maybeUser)))
  }

  def delete(id: Long): Action[AnyContent] = Action.async {
    userService
      .delete(id)
      .map(_ => Ok(Json.obj("status" -> "User deleted!")))
  }

  def sendRequest: Action[AnyContent] = Action.async { implicit request =>
    withFormErrorHandling(BefriendsForm.create, "create failed") { befriends =>
      befriendsService.request(befriends).map {
        case EStatus.Success => Created(Json.toJson(befriends))
        case EStatus.Failure => BadRequest(OBJECT_NOT_CREATED_ERROR)
      }
    }
  }

  def acceptRequest: Action[AnyContent] = Action.async { implicit request =>
    withFormErrorHandling(BefriendsForm.create, "create failed") { befriends =>
      befriendsService.accept(befriends).map {
        case EStatus.Success => Created(Json.toJson(befriends))
        case EStatus.Failure => BadRequest(OBJECT_NOT_CREATED_ERROR)
      }
    }
  }

  def deleteRequest(userId0: Long, userId1: Long): Action[AnyContent] = Action.async {
    befriendsService
      .delete(userId0, userId1)
      .map(_ => Ok(Json.obj("status" -> "Friend request deleted!")))
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