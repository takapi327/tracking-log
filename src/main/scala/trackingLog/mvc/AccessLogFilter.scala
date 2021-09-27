package trackingLog.mvc

import javax.inject.Inject

import scala.concurrent.{Future, ExecutionContext}

import play.api.libs.json.Json
import play.api.mvc._
import play.api.http.Status._
import play.api.{Configuration, Environment}

import akka.stream.Materializer
import org.uaparser.scala.Parser

class AccessLogFilter @Inject()(
  implicit val mat:  Materializer,
  implicit val ex:   ExecutionContext,
  implicit val conf: Configuration,
  implicit val env:  Environment
) extends Filter with TrackingLogging {

  private lazy val IGNORE_PREFIX_LIST = conf.getOptional[Seq[String]]("access.path.ignore").getOrElse(Seq.empty[String])
  private lazy val MOBILE_UA_REGEX    = "(iPhone|webOS|iPod|Android|BlackBelly|mobile|SAMSUNG|IEMobile|OperaMobi)".r.unanchored

  def apply(invocation: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    val start = System.currentTimeMillis()
    invocation(rh).map(result => {
      accessLog(start, result.header.status)(rh)
      result
    }) recover {case error => {
      accessLog(start, INTERNAL_SERVER_ERROR)(rh)
      throw error
    }}
  }

  private def accessLog(start: Long, status: Int)(implicit rh: RequestHeader) = {
    if (!IGNORE_PREFIX_LIST.exists(rh.path.startsWith)) {
      val elapsedTime   = System.currentTimeMillis - start
      val client        = rh.headers.get("User-Agent").map(Parser.default.parse)
      val remoteAddress = rh.headers.get("X-Real-Ip").flatMap(_.split(",").map(_.trim).headOption) orElse
        rh.headers.get("X-Forwarded-For").flatMap(_.split(",").map(_.trim).headOption) getOrElse
        rh.remoteAddress

      val data = Json.obj(
        "rid"                 -> rh.id,
        "host"                -> rh.host,
        "version"             -> rh.version,
        "method"              -> rh.method,
        "Status"              -> status,
        "path"                -> rh.path,
        "uri"                 -> rh.uri,
        "uuid"                -> rh.cookies.get("_ga").map(_.value),
        "session_id"          -> rh.cookies.get("_gid").map(_.value),
        "uid"                 -> rh.cookies.get("_uid").map(_.value),
        "geoid_current"       -> rh.cookies.get("_lcid").map(_.value),
        "geoid_interested"    -> rh.cookies.get("_liid").map(_.value),
        "remote_address"      -> remoteAddress,
        "client_os"           -> client.map(_.os.family),
        "client_os_version"   -> client.map(v => Seq(v.os.family, v.os.minor.getOrElse(0), v.os.patch.getOrElse(0), v.os.patchMinor.getOrElse(0)).mkString(".")),
        "client_device"       -> client.map(_.device.family),
        "client_device_brand" -> client.map(_.device.brand),
        "client_device_model" -> client.map(_.device.model),
        "client_ua"           -> client.map(_.userAgent.family),
        "client_ua_version"   -> client.map(v => Seq(v.userAgent.major.getOrElse(0), v.userAgent.minor.getOrElse(0), v.userAgent.patch.getOrElse(0)).mkString(".")),
        "client_is_mobile"    -> rh.headers.get("User-Agent").map(MOBILE_UA_REGEX.findFirstIn(_).isDefined),
        "user_agent"          -> rh.headers.get("User-Agent"),
        "referer"             -> rh.headers.get("Referer"),
        "elapsed_time"        -> elapsedTime,
        "headers"             -> rh.headers.headers.map(v => Json.obj(v._1 -> v._2)),
        "cookies"             -> Seq.empty[String],
      )
      trackingLogger.info(data.toString())
    }
  }
}