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
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
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
        Ok(Json.obj("user" -> username, "jwt" -> authService.generateToken(username)))
      case false =>
        Unauthorized(Json.obj("user" -> "none"))
    }
  }


  def getSelf() = authAction { implicit request =>
    Ok(Json.obj("user" -> request.user))
  }


  def create: Action[AnyContent] = Action.async { implicit request =>
    withFormErrorHandling(UserForm.create, "create failed") { user =>
      userService.create(user).map {
        case EStatus.Success =>
          // FIXME: concurrency?
          Created(Json.obj("user" -> Await.result(userService.getAll(), Duration.Inf).sortBy(_.id).last))
        case EStatus.Failure =>
          BadRequest(Json.obj("status" -> "User not persisted."))
      }
    }
  }


  def updateSelf: Action[AnyContent] = authAction.async { implicit request =>
    withFormErrorHandling(UserDtoForm.create, "update failed") { userDto =>

      val updatedUser = User(
        request.user.id, request.user.username, userDto.firstName, userDto.lastName,
        request.user.password, userDto.imgUrl, request.user.roleId)

      userService.update(updatedUser).map {
        case EStatus.Success =>
          Ok(Json.obj("user" -> (updatedUser)))
        case EStatus.Failure =>
          BadRequest(Json.obj("status" -> "User not found."))
      }
    }
  }


  def listAll: Action[AnyContent] = Action.async { implicit request =>
    userService
      .getAll()
      .map(users => Ok(Json.obj("users" -> users)))
  }


  def delete(id: Long): Action[AnyContent] = Action.async {
    userService
      .delete(id)
      .map {
        case EStatus.Success => Ok(Json.obj("status" -> ("User " + id + "deleted.")))
        case EStatus.Failure => BadRequest(Json.obj("status" -> "User not found."))
      }
  }


  def deleteSelf(): Action[AnyContent] = authAction.async { implicit request =>
    userService
      .delete(request.user.id.get)
      .map {
        case EStatus.Success => Ok(Json.obj("status" -> ("User " + request.user.id.get + "deleted.")))
        case EStatus.Failure => BadRequest(Json.obj("status" -> "User not found."))
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
        case EStatus.Success => Created(Json.obj("request" -> befriendsObject))
        case EStatus.Failure => BadRequest(Json.obj("status" -> "Friend request not persisted."))
      }
    }
  }


  def acceptRequest: Action[AnyContent] = authAction.async { implicit request =>
    withFormErrorHandling(BefriendsDtoForm.create, "create failed") { befriendsDto =>

      val befriendsObject = Befriends(request.user.id.get, befriendsDto.userId1, 2)

      befriendsService.accept(befriendsObject).map {
        case EStatus.Success => Created(Json.obj("friendship" -> befriendsObject))
        case EStatus.Failure => BadRequest(Json.obj("status" -> "Friendship not persisted."))
      }
    }
  }


  def deleteConnection(id0: Long, id1: Long): Action[AnyContent] = Action.async {
    befriendsService.delete(id0, id1).map {
      case EStatus.Success => Ok(Json.obj("status" -> ("Connection " + id0 + "-" + id1 + " deleted.")))
      case EStatus.Failure => BadRequest(Json.obj("status" -> "Connection not found."))
    }
  }


  def deleteRequest(id1: Long): Action[AnyContent] = authAction.async { implicit request =>
    befriendsService
      .deleteRequest(request.user.id.get, id1)
      .map {
        case EStatus.Success => Ok(Json.obj("status" -> ("Friend request " + request.user.id.get + "-" + id1 + " deleted.")))
        case EStatus.Failure => BadRequest(Json.obj("status" -> "Friend request not found."))
      }
  }


  def deleteFriendship(id1: Long): Action[AnyContent] = authAction.async { implicit request =>
    befriendsService
      .deleteFriendship(request.user.id.get, id1)
      .map {
        case EStatus.Success => Ok(Json.obj("status" -> ("Friendship " + request.user.id.get + "-" + id1 + " deleted.")))
        case EStatus.Failure => BadRequest(Json.obj("status" -> "Friendship not found."))
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