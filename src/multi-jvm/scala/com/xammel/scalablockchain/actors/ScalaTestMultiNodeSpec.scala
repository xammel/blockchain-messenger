package com.xammel.scalablockchain.actors

import akka.remote.testkit.{MultiNodeSpec, MultiNodeSpecCallbacks}
import akka.testkit.ImplicitSender
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

abstract class ScalaTestMultiNodeSpec
    extends MultiNodeSpec(NodeMultiNodeConfig)
    with ImplicitSender
    with MultiNodeSpecCallbacks
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def initialParticipants = roles.size

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()
}
