package controllers

import auth.{AuthAction, AuthService}
import forms.{BefriendsForm, UserForm}
import models.Befriends
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

class UserController @Inject()(
  userService: UserService,
  befriendsService: BefriendsService,
  authService: AuthService,
  authAction: AuthAction) (implicit ec: ExecutionContext) extends InjectedController with I18nSupport {


  lazy val logger: Logger = Logger(getClass)


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
    // FIXME: this is a quick and dirty fix, needs concurrency
    userService.getByUsername2(username).exists(_.password == password)
  }


  def getSelf() = authAction { implicit request =>
    // FIXME: this is not done as per instructions but it works
    Ok(Json.obj("currentUser" -> request.user))
  }


  def create: Action[AnyContent] = Action.async { implicit request =>
    withFormErrorHandling(UserForm.create, "create failed") { user =>
      userService.create(user).map {
        case EStatus.Success => Created(Json.toJson(user))
        case EStatus.Failure => BadRequest(OBJECT_NOT_CREATED_ERROR)
      }
    }
  }


  def updateSelf: Action[AnyContent] = Action.async { implicit request =>
    // TODO: get currentUser and update it using a DTO
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


  def listAll: Action[AnyContent] = Action.async { implicit request =>
    userService
      .getAll()
      .map(users => Ok(Json.toJson(users)))
  }


  def delete(id: Long): Action[AnyContent] = Action.async {
    userService
      .delete(id)
      .map(_ => Ok(Json.obj("status" -> "User deleted!")))
  }


  def deleteSelf(): Action[AnyContent] = authAction.async { implicit request =>
    userService
      .delete(request.user.id.get)
      .map(_ => Ok(Json.obj("status" -> "User deleted!")))
  }


  def listConnections(): Action[AnyContent] = Action.async { implicit request =>
    befriendsService
      .all()
      .map(befriendsObjects => Ok(Json.toJson(befriendsObjects)))
  }


  def listRequests(): Action[AnyContent] = authAction.async { implicit request =>
    befriendsService
      .getRequestsByUserId(request.user.id.get)
      .map(befriendsObjects => Ok(Json.toJson(befriendsObjects)))
  }


  def listFriendships(): Action[AnyContent] = authAction.async { implicit request =>
    befriendsService
      .getFriendshipsByUserId(request.user.id.get)
      .map(befriendsObjects => Ok(Json.toJson(befriendsObjects)))
  }


  def sendRequest: Action[AnyContent] = Action.async { implicit request =>
    // TODO: modify so that it uses currentUser as one ID
    withFormErrorHandling(BefriendsForm.create, "create failed") { befriends =>
      befriendsService.request(befriends).map {
        case EStatus.Success => Created(Json.toJson(Befriends(befriends.userId0, befriends.userId1, 1)))    // quick hack
        case EStatus.Failure => BadRequest(OBJECT_NOT_CREATED_ERROR)
      }
    }
  }


  def acceptRequest: Action[AnyContent] = Action.async { implicit request =>
    // TODO: modify so that it uses currentUser as one ID
    withFormErrorHandling(BefriendsForm.create, "create failed") { befriends =>
      befriendsService.accept(befriends).map {
        case EStatus.Success => Created(Json.toJson(Befriends(befriends.userId0, befriends.userId1, 2)))    // quick hack
        case EStatus.Failure => BadRequest(OBJECT_NOT_CREATED_ERROR)
      }
    }
  }


  def deleteRequest(userId0: Long, userId1: Long): Action[AnyContent] = Action.async {
    // TODO: modify so that it uses currentUser as one ID
    befriendsService
      .deleteRequest(userId0, userId1)
      .map(_ => Ok(Json.obj("status" -> "Friend request deleted!")))
  }


  def deleteFriendship(userId0: Long, userId1: Long): Action[AnyContent] = Action.async {
    // TODO: modify so that it uses currentUser as one ID
    befriendsService
      .deleteFriendship(userId0, userId1)
      .map(_ => Ok(Json.obj("status" -> "Friendship deleted!")))
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