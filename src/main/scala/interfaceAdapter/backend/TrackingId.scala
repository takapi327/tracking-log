package interfaceAdapter.backend

import java.util.UUID

trait TrackingId {
  lazy val generateUUID = UUID.randomUUID
}
