package controllers

import forms.PostForm
import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import services.PostService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class PostController @Inject()(postService: PostService)
                              (implicit ec: ExecutionContext) extends InjectedController with I18nSupport {

  lazy val logger: Logger = Logger(getClass)

  def create: Action[AnyContent] = Action.async { implicit request =>
    withFormErrorHandling(PostForm.create, "create failed") { post =>
      postService
        .create(post)
        .map(post => Created(Json.toJson(post)))
    }
  }

  def update: Action[AnyContent] = Action.async { implicit request =>
    withFormErrorHandling(PostForm.create, "update failed") { post =>
      postService
        .update(post)
        .map(post => Ok(Json.toJson(post)))
    }
  }

  def list: Action[AnyContent] = Action.async { implicit request =>
    postService
      .getAll()
      .map(posts => Ok(Json.toJson(posts)))
  }

  def get(id: Long): Action[AnyContent] = Action.async { implicit request =>
    postService
      .get(id)
      .map(maybePost => Ok(Json.toJson(maybePost)))
  }

  def delete(id: Long): Action[AnyContent] = Action.async {
    postService
      .delete(id)
      .map(_ => Ok(""))
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