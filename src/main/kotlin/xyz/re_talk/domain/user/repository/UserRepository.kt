package xyz.re_talk.domain.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import xyz.re_talk.domain.user.entity.User

@Repository
interface UserRepository : JpaRepository<User, Long>