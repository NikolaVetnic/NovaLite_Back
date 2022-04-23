package controllers

import auth.AuthAction
import forms.{CommentInputDtoForm, CommentReactionForm, CommentUpdateDtoForm}
import models.{Comment, CommentInsertDto}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, InjectedController, Request, Result}
import services.{CommentReactionService, CommentService, PostService}
import utils.EStatus

import javax.inject.Inject
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

class CommentController @Inject()(
  commentService: CommentService,
  commentReactionService: CommentReactionService,
  authAction: AuthAction) (implicit ec: ExecutionContext) extends InjectedController with I18nSupport {


  lazy val logger: Logger = Logger(getClass)


  def getCommentsByPostId(id: Long): Action[AnyContent] = authAction.async { implicit request =>
    commentService
      .getByPostId(id)
      .map(comments => Ok(Json.obj("comments" -> comments)))
  }


  def createComment: Action[AnyContent] = authAction.async { implicit request =>
    withFormErrorHandling(CommentInputDtoForm.create, "create failed") { commentDto =>

      val commentInsertDtoObject = CommentInsertDto(commentDto.content, request.user.id.get, commentDto.postId)
      println("AAAA : " + commentInsertDtoObject)
      commentService.create(commentInsertDtoObject).map {
        case EStatus.Success =>
          // FIXME: concurrency?
          Created(Json.obj("comment" -> (Await.result(commentService.getAll(), Duration.Inf).sortBy(_.id).last)))
        case EStatus.Failure =>
          BadRequest(Json.obj("status" -> "Post not persisted."))
      }
    }
  }


  def createCommentReaction: Action[AnyContent] = Action.async { implicit request =>
    withFormErrorHandling(CommentReactionForm.create, "create failed") { commentReaction =>
      commentReactionService.create(commentReaction).map {
        case EStatus.Success =>
          Created(Json.obj("commentReaction" -> commentReaction))
        case EStatus.Failure =>
          BadRequest(Json.obj("status" -> ("At least one of User " + commentReaction.userId + ", Comment " + commentReaction.commentId + ", Reaction " + commentReaction.reactionId + " not found.")))
      }
    }
  }


  def updateComment: Action[AnyContent] = authAction.async { implicit request =>
    withFormErrorHandling(CommentUpdateDtoForm.create, "update failed") { commentUpdateDto =>

      val updatedComment = Comment(commentUpdateDto.id, commentUpdateDto.content, null, commentUpdateDto.ownerId, commentUpdateDto.postId)

      commentService.update(updatedComment).map {
        case EStatus.Success =>
          Ok(Json.obj("comment" -> Await.result(commentService.get(commentUpdateDto.id.get), Duration.Inf).get))
        case EStatus.Failure =>
          BadRequest(Json.obj("status" -> ("Comment " + commentUpdateDto.id.get + " not found or not owned by User " + commentUpdateDto.ownerId + ".")))
      }
    }
  }


  def deleteComment(id: Long): Action[AnyContent] = authAction.async { implicit request =>
    commentService
      .delete(id)
      .map {
        case EStatus.Success =>
          Ok(Json.obj("status" -> ("Comment " + id + " of User " + request.user.id.get + " deleted!")))
        case EStatus.Failure =>
          BadRequest(Json.obj("status" -> ("Comment " + request.user.id.get + "-" + id + " not found.")))
      }
  }


  def deleteCommentReaction(id: Long): Action[AnyContent] = authAction.async { implicit request =>
    commentReactionService
      .delete(request.user.id.get, id)
      .map {
        case EStatus.Success =>
          Ok(Json.obj("status" -> ("Comment reaction of User " + request.user.id.get + " on comment " + id + " deleted!")))
        case EStatus.Failure =>
          BadRequest(Json.obj("status" -> ("Comment reaction " + request.user.id.get + "-" + id + " not found.")))
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
