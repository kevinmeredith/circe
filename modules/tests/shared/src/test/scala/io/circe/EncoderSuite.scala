package io.circe

import cats.laws.discipline.ContravariantTests
import io.circe.syntax._
import io.circe.tests.CirceSuite

class EncoderSuite extends CirceSuite {
  checkLaws("Encoder[Int]", ContravariantTests[Encoder].contravariant[Int, Int, Int])

  "mapJson" should "transform encoded output" in forAll { (m: Map[String, Int], k: String, v: Int) =>
    val newEncoder = Encoder[Map[String, Int]].mapJson(
      _.withObject(obj => Json.fromJsonObject(obj.add(k, v.asJson)))
    )

    assert(Decoder[Map[String, Int]].apply(newEncoder(m).hcursor) === Right(m.updated(k, v)))
  }

  "Encoder[Enumeration]" should "write Scala Enumerations" in {
    object WeekDay extends Enumeration {
      type WeekDay = Value
      val Mon, Tue, Wed, Thu, Fri, Sat, Sun = Value
    }

    implicit val encoder = Encoder.enumEncoder(WeekDay)
    val json = WeekDay.Fri.asJson
    val decoder = Decoder.enumDecoder(WeekDay)
    assert(decoder.apply(json.hcursor) == Right(WeekDay.Fri))
  }

  "encodeSet" should "match sequence encoders" in forAll { (xs: Set[Int]) =>
    assert(Encoder.encodeSet[Int].apply(xs) === Encoder[Seq[Int]].apply(xs.toSeq))
  }

  "encodeList" should "match sequence encoders" in forAll { (xs: List[Int]) =>
    assert(Encoder.encodeList[Int].apply(xs) === Encoder[Seq[Int]].apply(xs))
  }

  "encodeVector" should "match sequence encoders" in forAll { (xs: Vector[Int]) =>
    assert(Encoder.encodeVector[Int].apply(xs) === Encoder[Seq[Int]].apply(xs))
  }

  "encodeFloat" should "match string representation" in forAll { x: Float =>
    // All Float values should be encoded in a way that match the original value.
    assert(Encoder[Float].apply(x).toString.toFloat === x)

    // For floats which are NOT represented with scientific notation,
    // the JSON representaton should match Float.toString
    // This should catch cases where 1.2f would previously be encoded
    // as 1.2000000476837158 due to the use of .toDouble
    if (!x.toString.toLowerCase.contains('e')) {
      assert(Encoder[Float].apply(x).toString === x.toString)
    }
  }
}
