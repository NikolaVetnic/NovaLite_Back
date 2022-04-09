package controllers

import forms.PostReactionForm
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, InjectedController, Request, Result}
import services.PostReactionService
import utils.EStatus
import utils.ErrorMsg.{ID_NOT_FOUND_ERROR, RELATED_DB_ENTRY_NOT_FOUND_ERROR}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class PostReactionController @Inject()(postReactionService: PostReactionService)
                                      (implicit ec: ExecutionContext) extends InjectedController with I18nSupport {

  lazy val logger: Logger = Logger(getClass)

  def list: Action[AnyContent] = Action.async { implicit request =>
    postReactionService.getAll().map(posts => Ok(Json.toJson(posts)))
  }

  def getByPostId(postId: Long): Action[AnyContent] = Action.async { implicit request =>
    postReactionService
      .getByPostId(postId)
      .map(maybePostReaction => if (maybePostReaction.isEmpty) BadRequest(ID_NOT_FOUND_ERROR) else Ok(Json.toJson(maybePostReaction)))
  }

  def getByUserIdAndPostId(userId: Long, postId: Long): Action[AnyContent] = Action.async { implicit request =>
    postReactionService
      .getByUserIdAndPostId(userId, postId)
      .map(maybePostReaction => if (maybePostReaction.isEmpty) BadRequest(ID_NOT_FOUND_ERROR) else Ok(Json.toJson(maybePostReaction)))
  }

  def create: Action[AnyContent] = Action.async { implicit request =>
    withFormErrorHandling(PostReactionForm.create, "create failed") { postReaction =>
      postReactionService.create(postReaction).map {
        case EStatus.Success => Created(Json.toJson(postReaction))
        case EStatus.Failure => BadRequest(RELATED_DB_ENTRY_NOT_FOUND_ERROR)
      }
    }
  }

  def delete(userId: Long, postId: Long): Action[AnyContent] = Action.async {
    postReactionService
      .delete(userId, postId)
      .map(_ => Ok(Json.obj("status" -> "Post reaction deleted!")))
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
