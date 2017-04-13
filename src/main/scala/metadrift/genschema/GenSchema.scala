/**
  *  Copyright 2014 Coursera Inc.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
package metadrift.genschema

import scala.reflect.runtime.universe.TypeTag
import io.circe.syntax._
import io.circe.{Json, JsonObject}
import cats.instances.list.catsStdInstancesForList

/**
  * GenSchema lets you take any Scala type and create JSON Schema out of it
  *
  * @example
  * {{{
  *      // Pass the type as a type parameter
  *      case class MyType(...)
  *
  *      GenSchema.createSchema[MyType]()
  *
  *
  * }}}
  */
object GenSchema {

  private def getInitialSchemaMap[T](id: Option[String] = None,
                                     description: Option[String],
                                     title: Option[String] = None)(
      implicit st: Encoder[T],
      ev: TypeTag[T]): JsonObject = {
    JsonObject.from(
      List(
        Some(
          "title" -> title
            .getOrElse(ev.tpe.typeSymbol.name.decodedName.toString)
            .asJson),
        Some("$schema" -> "http://json-schema.org/draft-04/schema#".asJson),
        id.map(("id" -> _.asJson)),
        description.map(("description" -> _.asJson))
      ).flatten)
  }

  def createSchema[T](id: Option[String] = None,
                      description: Option[String] = None,
                      title: Option[String] = None)(
      implicit encoder: Encoder[T],
      ev: TypeTag[T]): Json = {
    JsonObject
      .from(
        getInitialSchemaMap(id, description, title).toList ++
          encoder.toJsonSchema().json.toList)
      .asJson
  }

  def createSchemaString[T](id: Option[String] = None,
                            description: Option[String] = None,
                            title: Option[String] = None)(
      implicit encoder: Encoder[T],
      ev: TypeTag[T]): String = {
    createSchema(id, description, title).spaces2
  }
}
