package controllers

import auth.{AuthAction, AuthService}
import forms.{BefriendsDtoForm, UserDtoForm, UserForm}
import models.{Befriends, User}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, _}
import services.{BefriendsService, UserService}
import utils.EStatus

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class UserController @Inject()(
  userService: UserService,
  befriendsService: BefriendsService,
  authService: AuthService,
  authAction: AuthAction) (implicit ec: ExecutionContext) extends InjectedController with I18nSupport {


  lazy val logger: Logger = Logger(getClass)


  def login(username: String, password: String) = Action.async { implicit request: Request[AnyContent] =>
    userService.isValidLogin(username, password).map {
      case true =>
        Ok(Json.obj("currentUser" -> username, "jwt" -> authService.generateToken(username)))
      case false =>
        Unauthorized(Json.obj("currentUser" -> "none"))
    }
  }


  def getSelf() = authAction { implicit request =>
    Ok(Json.obj("currentUser" -> request.user))
  }


  def create: Action[AnyContent] = Action.async { implicit request =>
    withFormErrorHandling(UserForm.create, "create failed") { user =>
      userService.create(user).map {
        case EStatus.Success => Created(Json.toJson(user))
        case EStatus.Failure => BadRequest(Json.obj("error" -> "User not persisted."))
      }
    }
  }


  def updateSelf: Action[AnyContent] = authAction.async { implicit request =>
    withFormErrorHandling(UserDtoForm.create, "update failed") { userDto =>

      val updatedUser = User(
        request.user.id, request.user.username, userDto.firstName, userDto.lastName,
        request.user.password, userDto.imgUrl, request.user.roleId)

      userService.update(updatedUser).map {
        case EStatus.Success => Ok(Json.toJson(updatedUser))
        case EStatus.Failure => BadRequest(Json.obj("error" -> "User not found."))
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
      .map {
        case EStatus.Success => Ok(Json.obj("status" -> "User deleted."))
        case EStatus.Failure => BadRequest(Json.obj("error" -> "User not found."))
      }
  }


  def deleteSelf(): Action[AnyContent] = authAction.async { implicit request =>
    userService
      .delete(request.user.id.get)
      .map {
        case EStatus.Success => Ok(Json.obj("status" -> "User deleted."))
        case EStatus.Failure => BadRequest(Json.obj("error" -> "User not found."))
      }
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


  def sendRequest: Action[AnyContent] = authAction.async { implicit request =>
    withFormErrorHandling(BefriendsDtoForm.create, "create failed") { befriendsDto =>

      val befriendsObject = Befriends(request.user.id.get, befriendsDto.userId1, 1)

      befriendsService.request(befriendsObject).map {
        case EStatus.Success => Created(Json.toJson(befriendsObject))
        case EStatus.Failure => BadRequest(Json.obj("error" -> "Friend request not persisted."))
      }
    }
  }


  def acceptRequest: Action[AnyContent] = authAction.async { implicit request =>
    withFormErrorHandling(BefriendsDtoForm.create, "create failed") { befriendsDto =>

      val befriendsObject = Befriends(request.user.id.get, befriendsDto.userId1, 2)

      befriendsService.accept(befriendsObject).map {
        case EStatus.Success => Created(Json.toJson(befriendsObject))
        case EStatus.Failure => BadRequest(Json.obj("error" -> "Friendship not persisted."))
      }
    }
  }


  def deleteConnection(id0: Long, id1: Long): Action[AnyContent] = Action.async {
    befriendsService.delete(id0, id1).map {
      case EStatus.Success => Ok(Json.obj("status" -> "Connection deleted."))
      case EStatus.Failure => BadRequest(Json.obj("error" -> "Connection not found."))
    }
  }


  def deleteRequest(id1: Long): Action[AnyContent] = authAction.async { implicit request =>
    befriendsService
      .deleteRequest(request.user.id.get, id1)
      .map {
        case EStatus.Success => Ok(Json.obj("status" -> "Friend request deleted."))
        case EStatus.Failure => BadRequest(Json.obj("error" -> "Friend request not found."))
      }
  }


  def deleteFriendship(id1: Long): Action[AnyContent] = authAction.async { implicit request =>
    befriendsService
      .deleteFriendship(request.user.id.get, id1)
      .map {
        case EStatus.Success => Ok(Json.obj("status" -> "Friendship deleted."))
        case EStatus.Failure => BadRequest(Json.obj("error" -> "Friendship not found."))
      }
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