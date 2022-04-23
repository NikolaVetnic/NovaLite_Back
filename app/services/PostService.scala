package services

import dao.{CommentDao, PostDao, PostReactionDao}
import models.{Post, PostDisplayDto, PostInsertDto}
import utils.EStatus
import utils.EStatus.EStatus

import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class PostService @Inject()(
  dao: PostDao,
  userService: UserService,
  befriendsService: BefriendsService,
  postReactionDao: PostReactionDao,
  commentDao: CommentDao)(implicit ex: ExecutionContext) {


  /**********
   * EXISTS *
   **********/
  def exists(id: Long): Future[Boolean] = {
    dao.exists(id)
  }


  /*******
   * GET *
   *******/
  def getAll(): Future[Seq[Post]] = {
    dao.all()
  }

  def getByFriendIds(id: Long): Future[Seq[PostDisplayDto]] = {
    // TODO: write a concurrent version of this method
    val friends = Await.result(befriendsService.getFriendsByUserId(id), Duration.Inf);
    val friendIds = friends.map(_.id);
    val posts = friendIds.flatMap(id => Await.result(dao.getByOwnerId(id.get), Duration.Inf));

    val res = posts.map {
      post => {
        val numLikes = Await.result(postReactionDao.getLikesByPostId(post.id.get), Duration.Inf).size
        PostDisplayDto(
          post.id,
          post.title,
          post.content,
          post.dateTime.toLocalDateTime,
          friends.find(_.id.get == post.ownerId).get,
          numLikes)
      }
    }

    Future { res }
  }


  def get(id: Long): Future[Option[Post]] = {
    dao.get(id)
  }


  def getByOwnerId(id: Long): Future[Seq[PostDisplayDto]] = {
    // TODO: write a concurrent version of this method
    val owner = Await.result(userService.get(id), Duration.Inf);
    val posts = Await.result(dao.getByOwnerId(id), Duration.Inf);

    val res = posts.map {
      post => {
        val numLikes = Await.result(postReactionDao.getLikesByPostId(post.id.get), Duration.Inf).size
        PostDisplayDto(post.id, post.title, post.content, post.dateTime.toLocalDateTime, owner.get, numLikes)
      }
    }

    Future { res }
  }


  def getByIdAndOwnerId(id: Long, ownerId: Long): Future[Seq[Post]] = {
    dao.getByOwnerId(ownerId)
  }


  def getByTitleContentAndOwnerId(title: String, content: String, ownerId: Long): Future[Seq[Post]] = {
    dao.getByTitleContentAndOwnerId(title, content, ownerId)
  }


  /********
   * POST *
   ********/
  def create(postDto: PostInsertDto): Future[EStatus] = {
    userService.existsId(postDto.ownerId).map {
      case true =>
        dao.insert(postDto)
        EStatus.Success
      case false =>
        EStatus.Failure
    }
  }


  def update(post: Post): Future[EStatus] = {
    dao.existsAndOwnedBy(post.id.get, post.ownerId).map {
      case true =>
        dao.update(post)
        EStatus.Success
      case false =>
        EStatus.Failure
    }
  }


  def delete(id: Long): Future[EStatus] = {

    dao.exists(id).map {
      case true =>
        // TODO: write a concurrent version of this method
        Await.result(commentDao.getByPostId(id), Duration.Inf).map(c => commentDao.delete(c.id.get))
        Await.result(postReactionDao.getByPostId(id), Duration.Inf).map(p => postReactionDao.delete(p.userId, p.postId))
        dao.delete(id)

        EStatus.Success
      case false =>
        EStatus.Failure
    }
  }


  def deleteByIdAndOwnerId(id: Long, ownerId: Long): Future[EStatus] = {
    dao.existsAndOwnedBy(id, ownerId).map {
      case true =>
        dao.deleteByIdAndOwnerId(id, ownerId)
        EStatus.Success
      case false =>
        EStatus.Failure
    }
  }
}
