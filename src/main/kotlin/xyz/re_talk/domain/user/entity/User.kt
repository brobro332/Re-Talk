package xyz.re_talk.domain.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import xyz.re_talk.global.common.entity.BaseEntity

@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val providerId: String,

    @Column(nullable = false)
    val provider: String = "KAKAO",

    @Column(nullable = false)
    var nickname: String,

    var profileImage: String? = null,
): BaseEntity()