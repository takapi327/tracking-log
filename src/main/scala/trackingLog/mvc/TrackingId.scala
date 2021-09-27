package trackingLog.mvc

import java.util.UUID

trait TrackingId {
  lazy val generateUUID = UUID.randomUUID
}
