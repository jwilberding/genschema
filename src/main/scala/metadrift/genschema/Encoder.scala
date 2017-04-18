package metadrift.genschema

import java.time.ZonedDateTime
import java.util.{Date, UUID}

import reflect.runtime.universe.TypeTag
import io.circe.{Json, JsonObject}
import shapeless._
import io.circe.syntax._
import shapeless.labelled.FieldType
import cats.instances.list.catsStdInstancesForList

trait Encoder[T] {
  def toJsonSchema(objOpt: Option[JsonObject] = None): Encoder.Modifier
}

// define serialisation of "primitive" types
object EncoderImplicits {

  val numberSchema: JsonObject =
    JsonObject.from(List("type" -> "number".asJson))

  implicit def dateEncoder: Encoder[Date] =
    (objOpt: Option[JsonObject]) =>
      Encoder.Required(
        JsonObject.from(
          List("type" -> "string".asJson, "format" -> "date".asJson)))

  implicit def zonedDateTimeEncoder: Encoder[ZonedDateTime] =
    (objOpt: Option[JsonObject]) =>
      Encoder.Required(
        JsonObject.from(
          List("type" -> "string".asJson, "format" -> "date".asJson)))

  implicit def boolEncoder: Encoder[Boolean] =
    (objOpt: Option[JsonObject]) =>
      Encoder.Required(JsonObject.from(List("type" -> "boolean".asJson)))

  implicit def stringEncoder: Encoder[String] =
    (objOpt: Option[JsonObject]) =>
      Encoder.Required(JsonObject.from(List("type" -> "string".asJson)))

  implicit def intEncoder: Encoder[Int] =
    (objOpt: Option[JsonObject]) => Encoder.Required(numberSchema)

  implicit def longEncoder: Encoder[Long] =
    (objOpt: Option[JsonObject]) => Encoder.Required(numberSchema)

  implicit def doubleEncoder: Encoder[Double] =
    (objOpt: Option[JsonObject]) => Encoder.Required(numberSchema)

  implicit def uuidEncoder: Encoder[UUID] =
    (objOpt: Option[JsonObject]) =>
      Encoder.Required(JsonObject.from(List(
        "type" -> "string".asJson,
        "pattern" -> "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$".asJson)))

  implicit def arrayEncoder[T](
      implicit st: Lazy[Encoder[T]]): Encoder[Array[T]] =
    (objOpt: Option[JsonObject]) =>
      Encoder.Required(
        JsonObject.from(List("type" -> "array".asJson,
                             "items" -> st.value.toJsonSchema().json.asJson)))

  implicit def listEncoder[T](
      implicit st: Lazy[Encoder[T]]): Encoder[List[T]] =
    (objOpt: Option[JsonObject]) =>
      Encoder.Required(
        JsonObject.from(List("type" -> "array".asJson,
                             "items" -> st.value.toJsonSchema().json.asJson)))

  implicit def seqEncoder[T](implicit st: Lazy[Encoder[T]]): Encoder[Seq[T]] =
    (objOpt: Option[JsonObject]) =>
      Encoder.Required(
        JsonObject.from(List("type" -> "array".asJson,
                             "items" -> st.value.toJsonSchema().json.asJson)))

  implicit def optionEncoder[T](
      implicit st: Lazy[Encoder[T]]): Encoder[Option[T]] =
    (objOpt: Option[JsonObject]) =>
      st.value.toJsonSchema(objOpt) match {
        case Encoder.Required(json) => Encoder.Optional(json)
        case element: Encoder.Optional => element
    }
  implicit def mapEncoder[T](
      implicit st: Lazy[Encoder[T]]): Encoder[Map[String, T]] =
    (objOpt: Option[JsonObject]) => {
      val valueSchema = st.value.toJsonSchema(objOpt).json.asJson
      Encoder.Required(
        JsonObject.from(List("type" -> "object".asJson,
                             "additionalProperties" -> valueSchema)))
    }

}

object Encoder {
  sealed abstract trait Modifier {
    val json: JsonObject
  }
  case class Required(json: JsonObject) extends Modifier
  case class Optional(json: JsonObject) extends Modifier

  val initialObject: JsonObject =
    JsonObject.from(
      List("type" -> "object".asJson,
           "properties" -> Map.empty[String, Json].asJson,
           "additionalProperties" -> false.asJson,
           "required" -> List.empty[String].asJson))

  val initialCoproductMap: JsonObject =
    JsonObject.from(List("oneOf" -> List.empty[Json].asJson))

  def apply[T](implicit st: Lazy[Encoder[T]]): Encoder[T] = st.value

  private def adjust(obj0: JsonObject, key: String)(fun: Json => Json) = {
    val value = obj0(key).get
    val obj1 = obj0.remove(key)
    obj1.add(key, fun(value))
  }

  private def updateRequired(obj: JsonObject, fieldName: String) = {
    adjust(obj, "required") { vals =>
      (fieldName.asJson +: vals.asArray.get).asJson
    }
  }

  private def updateProperties(obj: JsonObject,
                               fieldName: String,
                               value: Json) = {
    adjust(obj, "properties") { vals =>
      vals.asObject.get.add(fieldName, value).asJson
    }
  }
  private def updateOneOf(obj: JsonObject, schema: Json) = {
    adjust(obj, "oneOf") { vals =>
      (schema +: vals.asArray.get).asJson
    }
  }

  implicit def deriveHNil: Encoder[HNil] =
    (objOpt: Option[JsonObject]) => Required(objOpt.get)

  implicit def deriveHCons[K <: Symbol, V, T <: HList](
      implicit key: Witness.Aux[K],
      scv: Lazy[Encoder[V]],
      sct: Lazy[Encoder[T]]): Encoder[FieldType[K, V] :: T] =
    (objOpt: Option[JsonObject]) => {
      val obj0 = objOpt.getOrElse(initialObject)
      val obj1 = scv.value.toJsonSchema() match {
        case Required(json) =>
          updateProperties(updateRequired(obj0, key.value.name),
                           key.value.name,
                           json.asJson)
        case Optional(json) =>
          updateProperties(obj0, key.value.name, json.asJson)
      }
      sct.value.toJsonSchema(Some(obj1))
    }

  implicit def deriveCNil: Encoder[CNil] =
    (objOpt: Option[JsonObject]) => Required(objOpt.get)

  implicit def deriveCCons[K <: Symbol, V, T <: Coproduct](
      implicit key: Witness.Aux[K],
      scv: Lazy[Encoder[V]],
      sct: Lazy[Encoder[T]],
      ev: TypeTag[V]): Encoder[FieldType[K, V] :+: T] =
    (objOpt: Option[JsonObject]) => {
      val obj0 = objOpt.getOrElse(initialCoproductMap)
      val obj1 = updateOneOf(obj0, scv.value.toJsonSchema().json.asJson)
      sct.value.toJsonSchema(Some(obj1))
    }

  implicit def deriveInstance[F, G](implicit gen: LabelledGeneric.Aux[F, G],
                                    sg: Lazy[Encoder[G]]): Encoder[F] =
    (objOpt: Option[JsonObject]) => sg.value.toJsonSchema(objOpt)
}
