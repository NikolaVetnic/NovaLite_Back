package controllers

import auth.AuthAction
import forms.BefriendsDtoForm
import models.Befriends
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import services.BefriendsService
import utils.EStatus

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class BefriendsController @Inject() (
  befriendsService: BefriendsService,
  authAction: AuthAction) (implicit ec: ExecutionContext) extends InjectedController with I18nSupport {


  lazy val logger: Logger = Logger(getClass)


  /********************
   * USER CONNECTIONS *
   ********************/
  def getConnections(): Action[AnyContent] = Action.async { implicit request =>
    befriendsService
      .all()
      .map(befriendsObjects => Ok(Json.toJson(befriendsObjects)))
  }


  def getConnectionById(id: Long): Action[AnyContent] = authAction.async { implicit request =>
    befriendsService
      .getConnectionByUserIds(request.user.id.get, id)
      .map(res => Ok(Json.obj("befriends" -> res)))
  }


  def deleteConnection(id0: Long, id1: Long): Action[AnyContent] = Action.async {
    befriendsService.delete(id0, id1).map {
      case EStatus.Success => Ok(Json.obj("status" -> ("Connection " + id0 + "-" + id1 + " deleted.")))
      case EStatus.Failure => BadRequest(Json.obj("status" -> "Connection not found."))
    }
  }


  /*******************
   * FRIEND REQUESTS *
   *******************/
  def getRequests(): Action[AnyContent] = authAction.async { implicit request =>
    befriendsService
      .getRequestsByUserId(request.user.id.get)
      .map(befriendsObjects => Ok(Json.toJson(befriendsObjects)))
  }


  def getRequestsById(id: Long): Action[AnyContent] = authAction.async { implicit request =>
    befriendsService
      .getRequestsByUserId(id)
      .map(befriendsObjects => Ok(Json.toJson(befriendsObjects)))
  }


  def getApplicants(): Action[AnyContent] = authAction.async { implicit request =>
    befriendsService
      .getApplicantsByUserId(request.user.id.get)
      .map(applicants => Ok(Json.obj("requests" -> applicants)))
  }


  def sendRequest: Action[AnyContent] = authAction.async { implicit request =>
    withFormErrorHandling(BefriendsDtoForm.create, "create failed") { befriendsDto =>

      val befriendsObject = Befriends(request.user.id.get, befriendsDto.userId1, 1)

      befriendsService.sendRequest(befriendsObject).map {
        case EStatus.Success => Created(Json.obj("request" -> befriendsObject))
        case EStatus.Failure => BadRequest(Json.obj("status" -> "Friend request not persisted."))
      }
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


  /***************
   * FRIENDSHIPS *
   ***************/
  def listFriendships(): Action[AnyContent] = authAction.async { implicit request =>
    befriendsService
      .getFriendshipsByUserId(request.user.id.get)
      .map(befriendsObjects => Ok(Json.toJson(befriendsObjects)))
  }


  def getFriendship(id: Long): Action[AnyContent] = authAction.async { implicit request =>
    befriendsService
      .exists(Befriends(request.user.id.get, id, 2))
      .map(res => Ok(Json.obj("res" -> res)))
  }


  def listFriends(): Action[AnyContent] = authAction.async { implicit request =>
    befriendsService
      .getFriendsByUserId(request.user.id.get)
      .map(friends => Ok(Json.obj("friends" -> friends)))
  }


  def acceptRequest: Action[AnyContent] = authAction.async { implicit request =>
    withFormErrorHandling(BefriendsDtoForm.create, "create failed") { befriendsDto =>

      val befriendsObject = Befriends(request.user.id.get, befriendsDto.userId1, 2)

      befriendsService.acceptRequest(befriendsObject).map {
        case EStatus.Success => Created(Json.obj("friendship" -> befriendsObject))
        case EStatus.Failure => BadRequest(Json.obj("status" -> "Friendship not persisted."))
      }
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
