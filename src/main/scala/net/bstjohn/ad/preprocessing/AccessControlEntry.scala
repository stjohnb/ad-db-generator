package net.bstjohn.ad.preprocessing

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import net.bstjohn.ad.generator.format.ace.RightName
import net.bstjohn.ad.generator.snapshots.DbSnapshot

case class AccessControlEntry(
  sourceType: AccessControlEntryEntityType,
  sourceId: String,
  rightName: RightName,
  targetType: AccessControlEntryEntityType,
  targetId: String,
)


object AccessControlEntry {

  implicit val AccessControlEntryDecoder: Decoder[AccessControlEntry] = deriveDecoder[AccessControlEntry]
  implicit val AccessControlEntryEncoder: Encoder[AccessControlEntry] = deriveEncoder[AccessControlEntry]

  def from(snapshot: DbSnapshot): Seq[AccessControlEntry] = {
    val computerAces = snapshot.computers.data.flatMap { computer =>
      computer.Aces.map { ace =>
        AccessControlEntry(
          AccessControlEntryEntityType(ace.PrincipalType),
          ace.PrincipalSID,
          ace.RightName,
          AccessControlEntryEntityType.Computer,
          computer.ObjectIdentifier
        )
      }
    }

    val containerAces = snapshot.containers.data.flatMap { container =>
      container.Aces.map { ace =>
        AccessControlEntry(
          AccessControlEntryEntityType(ace.PrincipalType),
          ace.PrincipalSID,
          ace.RightName,
          AccessControlEntryEntityType.Container,
          container.ObjectIdentifier
        )
      }
    }

    val domainAces = snapshot.domains.data.flatMap { domain =>
      domain.Aces.map { ace =>
        AccessControlEntry(
          AccessControlEntryEntityType(ace.PrincipalType),
          ace.PrincipalSID,
          ace.RightName,
          AccessControlEntryEntityType.Domain,
          domain.ObjectIdentifier
        )
      }
    }

    val gpoAces = snapshot.gpos.data.flatMap{ gpo =>
      gpo.Aces.map { ace =>
        AccessControlEntry(
          AccessControlEntryEntityType(ace.PrincipalType),
          ace.PrincipalSID,
          ace.RightName,
          AccessControlEntryEntityType.Gpo,
          gpo.ObjectIdentifier
        )
      }
    }

    val groupAces = snapshot.groups.data.flatMap{ group =>
      group.Aces.map { ace =>
        AccessControlEntry(
          AccessControlEntryEntityType(ace.PrincipalType),
          ace.PrincipalSID,
          ace.RightName,
          AccessControlEntryEntityType.Group,
          group.ObjectIdentifier.value
        )
      }
    }

    val ouAces = snapshot.ous.data.flatMap{ ou =>
      ou.Aces.map { ace =>
        AccessControlEntry(
          AccessControlEntryEntityType(ace.PrincipalType),
          ace.PrincipalSID,
          ace.RightName,
          AccessControlEntryEntityType.Ou,
          ou.ObjectIdentifier
        )
      }
    }

    val userAces = snapshot.users.data.flatMap{ user =>
      user.Aces.map { ace =>
        AccessControlEntry(
          AccessControlEntryEntityType(ace.PrincipalType),
          ace.PrincipalSID,
          ace.RightName,
          AccessControlEntryEntityType.User,
          user.ObjectIdentifier.value
        )
      }
    }

    computerAces ++
      containerAces ++
      domainAces ++
      gpoAces ++
      groupAces ++
      ouAces ++
      userAces
  }
}