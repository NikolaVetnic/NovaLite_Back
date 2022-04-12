package controllers

import auth.AuthAction
import forms.{PostDtoForm, PostForm}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import services.PostService
import utils.EStatus
import utils.ErrorMsg.UPDATED_OBJECT_NOT_FOUND_ERROR

import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class PostController @Inject()(postService: PostService, authAction: AuthAction)
                              (implicit ec: ExecutionContext) extends InjectedController with I18nSupport {

  lazy val logger: Logger = Logger(getClass)

  def create: Action[AnyContent] = Action.async { implicit request =>
    // TODO: input user ID not from URL but from currentUser
    withFormErrorHandling(PostDtoForm.create, "create failed") { postDto =>
      postService.create(postDto).map {
        case EStatus.Success => {

          val allPosts = Await.result(
            postService.getByTitleContentAndOwnerId(postDto.title, postDto.content, postDto.ownerId),
            Duration(10, TimeUnit.SECONDS))

          if (allPosts == null)
            BadRequest(UPDATED_OBJECT_NOT_FOUND_ERROR)
          else
            Ok(Json.toJson(allPosts(allPosts.size - 1)))
        }
        case EStatus.Failure => BadRequest("User with ID " + postDto.ownerId + " not found.")
      }
    }
  }

  def update: Action[AnyContent] = Action.async { implicit request =>
    // TODO: update only if post by currentUser
    withFormErrorHandling(PostForm.create, "update failed") { post =>
      postService.update(post).map {
        case EStatus.Success => Ok(Json.toJson(post))
        case EStatus.Failure => BadRequest("Post with ID " + post.id + " not found.")
      }
    }
  }

  def listAll: Action[AnyContent] = Action.async { implicit request =>
    postService
      .getAll()
      .map(posts => Ok(Json.toJson(posts)))
  }

  def list(): Action[AnyContent] = authAction.async { implicit request =>
    postService
      .getByOwnerId(request.user.id.get)
      .map(posts => Ok(Json.toJson(posts)))
  }

  def get(id: Long): Action[AnyContent] = Action.async { implicit request =>
    // TODO: check only posts by currentUser
    postService
      .get(id)
      .map(maybePost => Ok(Json.toJson(maybePost)))
  }

  def delete(id: Long): Action[AnyContent] = Action.async {
    // TODO: delete only if post by currentUser
    postService
      .delete(id)
      .map(_ => Ok(Json.obj("status" -> "Post deleted!")))
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