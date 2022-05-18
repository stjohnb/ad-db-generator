package net.bstjohn.ad.generator.snapshots

import net.bstjohn.ad.generator.format.groups.Groups
import net.bstjohn.ad.generator.format.users.Users

case class DbSnapshot(
  users: Users,
  groups: Groups
)
