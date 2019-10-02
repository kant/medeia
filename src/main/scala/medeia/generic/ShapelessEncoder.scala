package medeia.generic

import medeia.encoder.BsonEncoder
import org.mongodb.scala.bson.BsonDocument
import shapeless.{:+:, ::, CNil, Coproduct, HList, HNil, Lazy, Witness}
import shapeless.labelled.FieldType

trait ShapelessEncoder[Base, H] {
  def encode(a: H): BsonDocument
}

object ShapelessEncoder {
  implicit def hnilEncoder[Base]: ShapelessEncoder[Base, HNil] =
    _ => BsonDocument()

  implicit def hlistObjectEncoder[Base, K <: Symbol, H, T <: HList](
      implicit
      witness: Witness.Aux[K],
      hEncoder: Lazy[BsonEncoder[H]],
      tEncoder: ShapelessEncoder[Base, T],
      options: GenericDerivationOptions[Base] = GenericDerivationOptions[Base]()
  ): ShapelessEncoder[Base, FieldType[K, H] :: T] = {
    val fieldName: String = options.transformKeys(witness.value.name)
    hlist =>
      {
        val head = hEncoder.value.encode(hlist.head)
        val tail: BsonDocument = tEncoder.encode(hlist.tail)
        tail.append(fieldName, head)
      }
  }

  implicit def cnilEncoder[Base]: ShapelessEncoder[Base, CNil] = ???

  implicit def coproductEncoder[Base, K <: Symbol, H, T <: Coproduct](
      implicit
      witness: Witness.Aux[K],
      hInstance: Lazy[BsonEncoder[H]],
      tInstance: ShapelessEncoder[Base, T]
  ): ShapelessEncoder[Base, FieldType[K, H] :+: T] = ???
}
