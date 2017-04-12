package talendant.genschema

import scala.collection.JavaConverters._
import com.github.fge.jsonschema.core.report.ProcessingMessage
import com.github.fge.jsonschema.main.JsonSchemaFactory
import io.circe.{Json, Printer}
import com.fasterxml.jackson.databind.ObjectMapper

object TestSupport {
  val printer = Printer.spaces2.copy(dropNullKeys = true)

  def validateSchema(json: Json, jsonSchema: Json): Unit = {
    val mapper = new ObjectMapper
    val schema = mapper.readTree(printer.pretty(jsonSchema))
    val instance = mapper.readTree(printer.pretty(json))

    val validator = JsonSchemaFactory.byDefault().getValidator

    val processingReport = validator.validate(schema, instance)

    if (!processingReport.isSuccess) {
      throw (new RuntimeException(s"""schema: ${jsonSchema.spaces2}
      | instance: ${json.spaces2}
      | messages: ${processingReport.asScala
                                       .map { message: ProcessingMessage =>
                                         message.toString
                                       }
                                       .mkString(",")}
      """.stripMargin))
    }
  }
}
