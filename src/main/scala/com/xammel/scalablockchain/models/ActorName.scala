package com.xammel.scalablockchain.models

trait ActorName {
  lazy val actorName: String = this.getClass.getName
    .split("\\.")
    .last
    .replaceAll("\\$", "")
    .toLowerCase
}
