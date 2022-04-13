package controllers

import auth.AuthAction
import forms.{PostInputDtoForm, PostUpdateDtoForm}
import models.{Post, PostInsertDto}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import services.PostService
import utils.EStatus

import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


class PostController @Inject()(postService: PostService, authAction: AuthAction)
                              (implicit ec: ExecutionContext) extends InjectedController with I18nSupport {


  lazy val logger: Logger = Logger(getClass)


  def create: Action[AnyContent] = authAction.async { implicit request =>
    withFormErrorHandling(PostInputDtoForm.create, "create failed") { postDto =>

      val postInsertDtoObject = PostInsertDto(postDto.title, postDto.content, request.user.id.get)

      postService.create(postInsertDtoObject).map {
        case EStatus.Success =>
          // FIXME: concurrency?
          Created(Json.obj("post" -> (Await.result(postService.getAll(), Duration.Inf).sortBy(_.id).last)))
        case EStatus.Failure =>
          BadRequest(Json.obj("status" -> "Post not persisted."))
      }
    }
  }


  def update: Action[AnyContent] = authAction.async { implicit request =>
    withFormErrorHandling(PostUpdateDtoForm.create, "update failed") { postUpdateDto =>

      val post = Post(postUpdateDto.id, postUpdateDto.title, postUpdateDto.content, null, request.user.id.get)

      postService.update(post).map {
        case EStatus.Success =>
          Ok(Json.obj("post" -> Await.result(postService.get(post.id.get), Duration.Inf).get))
        case EStatus.Failure =>
          BadRequest(Json.obj("status" -> ("Post " + post.id.get + " not found or not owned by User " + post.ownerId + ".")))
      }
    }
  }


  def listAll: Action[AnyContent] = Action.async { implicit request =>
    postService
      .getAll()
      .map(posts => Ok(Json.obj("posts" -> posts)))
  }


  def list(): Action[AnyContent] = authAction.async { implicit request =>
    postService
      .getByOwnerId(request.user.id.get)
      .map(posts => Ok(Json.obj("posts" -> posts)))
  }


  def get(id: Long): Action[AnyContent] = authAction.async { implicit request =>
    postService
      .getByIdAndOwnerId(id, request.user.id.get)
      .map(post =>
        if (!post.isEmpty)
          Ok(Json.obj("post" -> post))
        else
          BadRequest(Json.obj("status" -> ("Post " + id + " not found or not owned by User " + request.user.id.get + "."))))
  }


  def delete(id: Long): Action[AnyContent] = Action.async { implicit request =>
    postService.delete(id).map {
      case EStatus.Success =>
        Ok(Json.obj("status" -> ("Post " + id + " deleted!")))
      case EStatus.Failure =>
        BadRequest(Json.obj("status" -> ("Post " + id + " not found.")))
    }
  }


  def deleteOwnPost(id: Long): Action[AnyContent] = authAction.async { implicit request =>
    postService.deleteByIdAndOwnerId(id, request.user.id.get).map {
        case EStatus.Success =>
          Ok(Json.obj("status" -> ("Post " + id + " deleted!")))
        case EStatus.Failure =>
          BadRequest(Json.obj("status" -> ("Post " + id + " not found or not owned by User " + request.user.id.get + ".")))
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