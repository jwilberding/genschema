# GenSchema

## Generate JSON Schema from Scala classes

Features

* Generates JSON Schema from classes
* Supports common types
    * Seq, Lists, Arrays, etc.
    * Options
    * java.util.Date
    * String, Boolean, Int, Long, Double
    * java.util.UUID
* Expandable via creating implicit encoders

## Installation
Add this line to your `build.sbt`:

        libraryDependencies += "talendant" %% "genschema" % "0.1.0"

## Usage

With a type parameter

        import talendant.genschema.Encoder
        import talendant.genschema.EncoderImplicits._

        case class MyType(myValue: Int)
        implicit myTypeEncoder: Encoder[MyType]

        GenSchema.createSchema[MyType]

## Custom Encoders

You can create custom encoders very simply. Just be aware that if you
create a custom encoder for the schema you probably need to create a
custom in whatever json generation library you use. Otherwise, they
wont match and the schema wont validate.

     case class MyType(myValue: Int)

     implicit def dateEncoder: Encoder[MyType] =
        (objOpt: Option[JsonObject]) =>
          Encoder.Required(JsonObject.from(List("type" -> "string".asJson)))

# Acknolegements

This is a mostly ground up reimplementation of the work by Coursera
https://github.com/coursera/autoschema and Saul Hernandez's work at
http://github.com/sauldhernandez/autoschema. This makes a lot of
fundamental improvements including moving the base encoder to
shapeless and there by allowing plugability in the encoder in a
natural manner.
