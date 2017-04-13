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
package talendant.genschema

import java.util.UUID

import io.circe.generic.semiauto
import io.circe.syntax._
import org.scalatest.FunSuite
import talendant.genschema
import talendant.genschema.EncoderImplicits._
import talendant.genschema.GenSchema.createSchema

case class TypeOne(param1: Int)
case class TypeTwo(param1: Int, param2: Long)

case class TypeThreeParamOne(param1: String)
case class TypeThree(param1: TypeThreeParamOne, param2: Double)

case class TypeFour(param1: Int, param2: Option[Int])

case class TypeFive[T](param1: T, param2: String)

case class TypeSix(param1: UUID)

sealed abstract trait Foo
case class Bar(p: String) extends Foo
case class Baz(z: String) extends Foo

class GenSchemaTest extends FunSuite {
  implicit val typeOneEncoder = Encoder[TypeOne]
  implicit val typeTwoEncoder = genschema.Encoder[TypeTwo]
  implicit val typeThreeParamOneEncoder = genschema.Encoder[TypeThreeParamOne]
  implicit val typeThreeEncoder = genschema.Encoder[TypeThree]
  implicit val typeFourEncoder = genschema.Encoder[TypeFour]
  implicit val typeFiveEncoder = genschema.Encoder[TypeFive[Int]]
  implicit val typeFiveStringEncoder = genschema.Encoder[TypeFive[String]]
  implicit val typeSixEncoder = genschema.Encoder[TypeSix]
  implicit val fooEncoder = genschema.Encoder[Foo]

  implicit val typeOneJsonEncoder = semiauto.deriveEncoder[TypeOne]
  implicit val typeTwoJsonEncoder = semiauto.deriveEncoder[TypeTwo]
  implicit val typeThreeParamOneJsonEncoder =
    semiauto.deriveEncoder[TypeThreeParamOne]
  implicit val typeThreeJsonEncoder = semiauto.deriveEncoder[TypeThree]
  implicit val typeFourJsonEncoder = semiauto.deriveEncoder[TypeFour]
  implicit val typeFiveJsonEncoder = semiauto.deriveEncoder[TypeFive[Int]]
  implicit val typeFiveStringJsonEncoder =
    semiauto.deriveEncoder[TypeFive[String]]
  implicit val typeSixJsonEncoder = semiauto.deriveEncoder[TypeSix]
  implicit val fooJsonEncoder = semiauto.deriveEncoder[Foo]
  implicit val bazJsonEncoder = semiauto.deriveEncoder[Baz]
  implicit val barJsonEncoder = semiauto.deriveEncoder[Bar]

  test("justAnInt") {
    val good = 10.asJson
    val bad = "foo".asJson
    TestSupport.validateSchema(good, createSchema[Int]())

    intercept[RuntimeException] {
      TestSupport.validateSchema(bad, createSchema[Int]())
    }
  }

  test("typeOne") {
    val good = TypeOne(10).asJson
    val bad = TypeTwo(1, 2L).asJson
    TestSupport.validateSchema(good, createSchema[TypeOne]())

    intercept[RuntimeException] {
      TestSupport.validateSchema(bad, createSchema[TypeOne]())
    }
  }

  test("typeTwo") {
    val good = TypeTwo(1, 2L).asJson
    val bad = TypeOne(10).asJson

    TestSupport.validateSchema(good, createSchema[TypeTwo]())
    intercept[RuntimeException] {
      TestSupport.validateSchema(bad, createSchema[TypeTwo]())
    }
  }

  test("typeThree") {
    val good = TypeThree(TypeThreeParamOne("foobar"), 2.0).asJson
    val bad = TypeOne(10).asJson
    TestSupport.validateSchema(good, createSchema[TypeThree]())
    intercept[RuntimeException] {
      TestSupport.validateSchema(bad, createSchema[TypeThree]())
    }
  }

  test("typeFour") {
    val good1 = TypeFour(1, Some(2)).asJson
    val good2 = TypeFour(2, None).asJson
    val bad = TypeThreeParamOne("foo").asJson

    TestSupport.validateSchema(good1, createSchema[TypeFour]())
    TestSupport.validateSchema(good2, createSchema[TypeFour]())
    intercept[RuntimeException] {
      TestSupport.validateSchema(bad, createSchema[TypeFour]())
    }
  }

  test("typeFive") {
    val good1 = TypeFive[Int](3, "foo").asJson
    val good2 = TypeFive[String]("boo", "baz").asJson
    val bad = TypeOne(1).asJson

    TestSupport.validateSchema(good1, createSchema[TypeFive[Int]]())
    TestSupport.validateSchema(good2, createSchema[TypeFive[String]]())

    intercept[RuntimeException] {
      TestSupport.validateSchema(bad, createSchema[TypeFive[Int]]())
    }
  }

  test("typeSix") {
    val good = TypeSix(UUID.randomUUID()).asJson
    val bad = TypeOne(1).asJson

    TestSupport.validateSchema(good, createSchema[TypeSix]())
    intercept[RuntimeException] {
      TestSupport.validateSchema(bad, createSchema[TypeSix]())
    }

  }

  test("sumTypes") {
    val good1 = Baz("e").asJson
    val good2 = Bar("k").asJson
    val bad = TypeOne(1).asJson

    TestSupport.validateSchema(good1, createSchema[Foo]())
    TestSupport.validateSchema(good2, createSchema[Foo]())
    intercept[RuntimeException] {
      TestSupport.validateSchema(bad, createSchema[Foo]())
    }

  }
  test("enumTypes") {
    val good1 = Baz("e").asJson
    val good2 = Bar("k").asJson
    val bad = TypeOne(1).asJson

    TestSupport.validateSchema(good1, createSchema[Foo]())
    TestSupport.validateSchema(good2, createSchema[Foo]())
    intercept[RuntimeException] {
      TestSupport.validateSchema(bad, createSchema[Foo]())
    }

  }

  test("collections1") {
    val good = Array(1, 2, 3, 4).asJson
    val bad = Array("string", "booz").asJson

    TestSupport.validateSchema(good, createSchema[Array[Int]]())
    intercept[RuntimeException] {
      TestSupport.validateSchema(bad, createSchema[Array[Int]]())
    }
  }

  test("collections2") {
    val good = List(1, 2, 3, 4).asJson
    val bad = List("one", "two").asJson

    TestSupport.validateSchema(good, createSchema[List[Int]]())
    intercept[RuntimeException] {
      TestSupport.validateSchema(bad, createSchema[List[Int]]())
    }
  }

  test("collections3") {
    val good = Seq(1, 2, 3, 4).asJson
    val bad = Seq("one", "two").asJson

    TestSupport.validateSchema(good, createSchema[Seq[Int]]())
    intercept[RuntimeException] {
      TestSupport.validateSchema(bad, createSchema[Seq[Int]]())
    }
  }
}
