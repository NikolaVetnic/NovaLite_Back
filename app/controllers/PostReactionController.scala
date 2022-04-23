package controllers

import auth.AuthAction
import forms.PostReactionForm
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import services.PostReactionService
import utils.EStatus

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


class PostReactionController @Inject()(postReactionService: PostReactionService, authAction: AuthAction)
                                      (implicit ec: ExecutionContext) extends InjectedController with I18nSupport {


  lazy val logger: Logger = Logger(getClass)


  def listAll: Action[AnyContent] = Action.async { implicit request =>
    postReactionService.getAll().map(posts => Ok(Json.toJson(posts)))
  }


  def list: Action[AnyContent] = authAction.async { implicit request =>
    postReactionService.getByUserId(request.user.id.get).map(posts => Ok(Json.toJson(posts)))
  }


  def getByPostId(postId: Long): Action[AnyContent] = authAction.async { implicit request =>
    postReactionService
      .getByUserIdAndPostId(request.user.id.get, postId)
      .map(preact =>
        if (preact.isEmpty)
          BadRequest(Json.obj("status" -> ("Post " + postId + " not found or no reaction by User " + request.user.id.get + ".")))
        else
          Ok(Json.obj("postReaction" -> preact)))
  }


  def getLikesByPostId(postId: Long): Action[AnyContent] = authAction.async { implicit request =>
    postReactionService
      .getLikesByUserIdAndPostId(request.user.id.get, postId)
      .map(preact => Ok(Json.obj("likes" -> preact)))
  }


  def create: Action[AnyContent] = Action.async { implicit request =>
    withFormErrorHandling(PostReactionForm.create, "create failed") { postReaction =>
      postReactionService.create(postReaction).map {
        case EStatus.Success =>
          Created(Json.obj("postReaction" -> postReaction))
        case EStatus.Failure =>
          BadRequest(Json.obj("status" -> ("At least one of User " + postReaction.userId + ", Post " + postReaction.postId + ", Reaction " + postReaction.reactionId + " not found.")))
      }
    }
  }


  def delete(userId: Long, postId: Long): Action[AnyContent] = Action.async {
    postReactionService
      .delete(userId, postId)
      .map {
        case EStatus.Success =>
          Ok(Json.obj("status" -> ("Post reaction of User " + userId + " on post " + postId + " deleted!")))
        case EStatus.Failure =>
          BadRequest(Json.obj("status" -> ("Post reaction " + userId + "-" + postId + " not found.")))
      }
  }


  def deleteOwn(postId: Long): Action[AnyContent] = authAction.async { implicit request =>
    postReactionService
      .delete(request.user.id.get, postId)
      .map {
        case EStatus.Success =>
          Ok(Json.obj("status" -> ("Post reaction of User " + request.user.id.get + " on post " + postId + " deleted!")))
        case EStatus.Failure =>
          BadRequest(Json.obj("status" -> ("Post reaction " + request.user.id.get + "-" + postId + " not found.")))
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
