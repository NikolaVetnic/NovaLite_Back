package auth

import com.auth0.jwk.UrlJwkProvider
import dao.UserDao
import pdi.jwt._
import play.api.Configuration

import java.time.Clock
import java.util.Base64
import javax.inject.Inject
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

class AuthService @Inject()(config: Configuration, userDao: UserDao) {


  implicit val clock: Clock = Clock.systemUTC


  // A regex that defines the JWT pattern and allows us to
  // extract the header, claims and signature
  private val jwtRegex = """(.+?)\.(.+?)\.(.+?)""".r
  private val userIdRegex = new Regex("""\((?<id>\d.*?)\)""", "id");


  // Your Auth0 domain, read from configuration
  private def domain = config.get[String]("auth0.domain")
  private def audience = config.get[String]("auth0.audience")
  private def issuer = s"https://$domain/"


//  def generateToken(username: String): String = {
//
//    val user = userDao.getByUsername2(username).get
//
//    // enkodiranje
//    val payload = Json.obj("id" -> user.id, "username" -> user.username)
//    val jwt = JWT.encode("secret-key", payload)
//
//    println("Token :: " + jwt)
//    println("Decoded :: " + JWT.decode(jwt, Some("secret-key")))
//
//    jwt
//  }



//  def generateToken(username: String): String = {
//
//    val user = userDao.getByUsername2(username).get
//    val claim = JwtClaim(issuer = Some("ovdeNekiRandomString"), subject = Some(user.id.toString)).expiresIn(3600 * 24)
//    val token = Jwt.encode(claim, "pk",  JwtAlgorithm.HS256)
//
//    val t = (splitToken andThen decodeElements)(token).get
//
//    println(JwtJson.parseHeader(t._1).toJson)
//    println(t._2)
//    println(t._3)
//
//    val tt = validateJwt(token)
//
//    println("validated : " + tt)
//
//    t._1
//  }



  val secretKey = "pk"
  val alg = JwtAlgorithm.HS256;



  def generateToken(username: String): String = {

    val user = userDao.getByUsername2(username).get

    // enkodiranje
    val claim = JwtClaim(issuer = Some("ovdeNekiRandomString"), subject = Some(user.id.toString)).expiresIn(3600 * 24)
    val token = Jwt.encode(claim, secretKey,  alg)

    // dekodiranje
    val decodedToken = Jwt.decode(token, "pk", Seq(alg))
    val userIdFromToken = getUserIdFromJwt(token)

    //

    val jwtTokenPayload = token.split('.')(1)
    val jwtSig = token.split('.')(2)
    val expirationDate = new String(Base64.getDecoder.decode(jwtTokenPayload))
    println("EXP DATE :: " + expirationDate)

    //

    Jwt.isValid(token, secretKey, Seq(alg))
    val vvv = validateJwt(token).get.toJson

    // test
    println("Token : " + token)
    println("Decoded Token : " + decodedToken.get.toJson)
    println("User ID from Token : " + userIdFromToken)
    println("Sig : " + jwtSig)
    println("Token is valid : " + Jwt.isValid(token, secretKey, Seq(alg)))
    println("Token is valid : " + Jwt.isValid(vvv, secretKey, Seq(alg)))
    println("Token is valid : " + token)
    println("Token is valid : " + vvv)

    token
  }


  def getUserIdFromJwt(token: String) = {
    val result = userIdRegex.findFirstMatchIn(decodeElements(splitToken(token)).get._2.toString).get;
    result.group("id").toLong
  }





  // Validates a JWT and potentially returns the claims if the token was successfully parsed
  def validateJwt(token: String): Try[JwtClaim] = for {
//    jwk <- getJwk(token)                                                        // Get the secret key for this token
    claims <- Jwt.decode(token, secretKey, Seq(alg))  // Decode the token using the secret key
//    _ <- validateClaims(claims)                                                 // validate the data stored inside the token
  } yield claims

//  // Gets the JWK from the JWKS endpoint using the jwks-rsa library
//  private val getJwk = (token: String) =>
//    (splitToken andThen decodeElements) (token) flatMap {
//      case (header, _, _) =>
//        val jwtHeader = JwtJson.parseHeader(header)                             // extract the header
//        val jwkProvider = new UrlJwkProvider(s"https://$domain")
//
//        // Use jwkProvider to load the JWKS data and return the JWK
//        jwtHeader.keyId.map { k =>
//          Try(jwkProvider.get(k))
//        } getOrElse Failure(new Exception("Unable to retrieve kid"))
//    }


  // Splits a JWT into it's 3 component parts
  private val splitToken = (jwt: String) => jwt match {
    case jwtRegex(header, body, sig) => Success((header, body, sig))
    case _ => Failure(new Exception("Token does not match the correct pattern"))
  }


  // As the header and claims data are base64-encoded, this function
  // decodes those elements
  private val decodeElements = (data: Try[(String, String, String)]) => data map {
    case (header, body, sig) => (JwtBase64.decodeString(header), JwtBase64.decodeString(body), sig)
  }


//  // Validates the claims inside the token. isValid checks the issuedAt, expiresAt,
//  // issuer and audience fields.
//  private val validateClaims = (claims: JwtClaim) =>
//    if (claims.isValid(issuer, audience)) {
//      Success(claims)
//    } else {
//      Failure(new Exception("The JWT did not pass validation"))
//    }
}