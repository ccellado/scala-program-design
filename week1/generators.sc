trait Generator[+T] {
  self => // an alias for ”this”
  def generate: T
  def map[S](f: T => S): Generator[S] =
    new Generator[S] {
      def generate = f(self.generate)
    }
  def flatMap[S](f: T => Generator[S]): Generator[S] =
    new Generator[S] {
      def generate = f(self.generate).generate
    }
}
def single[T](x: T): Generator[T] =
  new Generator[T] {
    def generate = x
  }

def choose(lo: Int, hi: Int): Generator[Int] =
  for (x <- integers) yield lo + x % (hi - lo)

def oneOf[T](xs: T*): Generator[T] =
  for (idx <- choose(0, xs.length)) yield xs(idx)

val integers = new Generator[Int] {
  val rand = new java.util.Random
  def generate = rand.nextInt()
}

val booleans = for (x <- integers) yield x > 0

def pairs[T, U](t: Generator[T], u: Generator[U]) =
  for {
    x <- t
    y <- u
  } yield (x, y)

def lists: Generator[List[Int]] =
  for {
    isEmpty <- booleans
    list <- if (isEmpty) emptyLists else nonEmptyLists
  } yield list

def emptyLists = single(Nil)

def nonEmptyLists =
  for {
    head <- integers
    tail <- lists
  } yield head :: tail

trait Tree
case class Inner(left: Tree, right: Tree) extends Tree
case class Leaf(x: Int) extends Tree

def trees: Generator[Tree] =
  for {
    isLeaf <- booleans
    tree <- if (isLeaf) treesInner else treesLeaf
  } yield tree

def treesLeaf: Generator[Leaf] =
  for {
    x <- integers
  } yield Leaf(x)

def treesInner =
  for {
    x <- trees
    y <- trees
  } yield Inner(x, y)

println(trees.generate)
