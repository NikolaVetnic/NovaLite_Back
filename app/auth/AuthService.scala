package auth

import dao.UserDao
import pdi.jwt._

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

class AuthService @Inject()(userDao: UserDao) {


  implicit val clock: Clock = Clock.systemUTC


  // A regex that defines the JWT pattern and allows us to extract the header, claims and signature
  private val jwtRegex = """(.+?)\.(.+?)\.(.+?)""".r
  private val userIdRegex = new Regex("""\((?<id>\d.*?)\)""", "id");


  val secretKey = "secretKey"
  val alg = JwtAlgorithm.HS256;
  val issuedBy = "issuedBy"


  def generateToken(username: String): String = {
    // FIXME: concurrency needed
    val user = Await.result(userDao.getByUsername(username), Duration.Inf).get

    val claim = JwtClaim(
      issuer = Some(issuedBy),
      subject = Some(user.id.toString)).expiresIn(3600 * 24)

    val token = "Bearer " + Jwt.encode(claim, secretKey,  alg)

    token
  }


  def decodeToken(token: String) = Jwt.decode(token, secretKey, Seq(alg))


  def getTokenPayload(token: String) = token.split('.')(1)


  def getTokenSignature(token: String) = token.split('.')(2)


  def isTokenValid(token: String) = Jwt.isValid(token, secretKey, Seq(alg))


  def getUserFromJwt(token: String) = {
    // FIXME: concurrency needed
    val id = userIdRegex.findFirstMatchIn(decodeElements(splitToken(token)).get._2.toString).get.group("id").toLong
    Await.result(userDao.get(id), Duration.Inf).get
  }


  // Validates a JWT and potentially returns the claims if the token was successfully parsed
  def validateJwt(token: String): Try[JwtClaim] = for {
    claims <- Jwt.decode(token, secretKey, Seq(alg))                            // Decode the token using the secret key
    _ <- validateClaims(claims)                                                 // validate the data stored inside the token
  } yield claims


  // Validates the claims inside the token. isValid checks the issuedAt, expiresAt, issuer and audience fields.
  private val validateClaims = (claims: JwtClaim) =>
    if (claims.isValid(issuer = issuedBy)) {
      Success(claims)
    } else {
      Failure(new Exception("The JWT did not pass validation"))
    }


  // Splits a JWT into it's 3 component parts
  private val splitToken = (jwt: String) => jwt match {
    case jwtRegex(header, body, sig) => Success((header, body, sig))
    case _ => Failure(new Exception("Token does not match the correct pattern"))
  }


  // As the header and claims data are base64-encoded, this function decodes those elements
  private val decodeElements = (data: Try[(String, String, String)]) => data map {
    case (header, body, sig) => (JwtBase64.decodeString(header), JwtBase64.decodeString(body), sig)
  }
}