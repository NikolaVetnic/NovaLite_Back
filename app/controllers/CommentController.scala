package controllers

import auth.AuthAction
import forms.{CommentInputDtoForm, CommentReactionForm, CommentUpdateDtoForm}
import models.{Comment, CommentInsertDto}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import services.{CommentReactionService, CommentService}
import utils.EStatus

import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
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
      commentService.create(CommentInsertDto(commentDto.content, request.user.id.get, commentDto.postId)).map {
        comment => Created(Json.obj("comment" -> comment))
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
      commentService.update(Comment(commentUpdateDto.id, commentUpdateDto.content, null, commentUpdateDto.ownerId, commentUpdateDto.postId)).map {
        comment => Ok(Json.obj("comment" -> comment))
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
