package quickcheck

import org.scalacheck._
import Arbitrary._
import Gen._
import Prop._

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {

  lazy val genHeap: Gen[H] = oneOf(
    const(empty),
    for {
      node <- arbitrary[Int]
      heap <- oneOf(const(empty), genHeap)
    } yield insert(node, heap)
  )
  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

  property("gen1") = forAll { (h: H) =>
    val m = if (isEmpty(h)) 0 else findMin(h)
    findMin(insert(m, h)) == m
  }
  property("min1") = forAll { a: Int =>
    val h = insert(a, empty)
    findMin(h) == a
  }
  property("min2") = forAll { (a: Int, b: Int) =>
    val h = insert(a, empty)
    val z = insert(b, h)
    if (a < b)
      findMin(z) == a
    else findMin(z) == b
  }

  property("mindel1") = forAll { a: String =>
    val h = insert(a, empty)
    deleteMin(h) == empty
  }

  def helperSequence(h: H): Seq[Int] = {
    if (h == empty) Seq()
    else {
      val x = findMin(h)
      Seq(x) ++ helperSequence(deleteMin(h))
    }
  }

  def compareHeaps(a: H, b: H): Boolean =
    if (helperSequence(a) == helperSequence(b)) true else false

  property("sequence") = forAll { (h: H) =>
    val mins = helperSequence(h)
    mins.sortWith(_ < _) == mins
  }

  property("comp") = forAll { (a: H, b: H) =>
    compareHeaps(meld(a, b), meld(b, a))
  }

  property("comp") = forAll { (a: H, b: H, z: Int) =>
    val az = insert(z, a)
    val bz = insert(z, b)
    compareHeaps(meld(b, az), meld(a, bz))
  }
}
