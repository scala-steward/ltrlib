package io.github.metarank.ltrlib.booster

import io.github.metarank.ltrlib.metric.Metric
import io.github.metarank.ltrlib.model.{Dataset, Model}
import org.apache.commons.math3.linear.{Array2DRowRealMatrix, ArrayRealVector, RealMatrix, RealVector}

trait Booster[D] extends Model {
  protected var nativeLibIsClosed = false
  def save(): Array[Byte]
  def predictMat(values: Array[Double], rows: Int, cols: Int): Array[Double]
  def weights(): Array[Double]
  def close(): Unit
  def isClosed(): Boolean = nativeLibIsClosed

  def whenNotClosed[T](f: => T): T = if (!nativeLibIsClosed) f else throw new Exception("booster is already closed")

  override def predict(values: RealMatrix): ArrayRealVector = {
    val rows = values.getRowDimension
    val cols = values.getColumnDimension
    val data = new Array[Double](rows * cols)
    var row  = 0
    while (row < values.getRowDimension) {
      System.arraycopy(values.getRow(row), 0, data, row * cols, cols)
      row += 1
    }
    new ArrayRealVector(predictMat(data, rows, cols))
  }

  override def predict(values: RealVector): Double = {
    predictMat(values.toArray, 1, values.getDimension)(0)
  }

  override def eval(data: Dataset, metric: Metric): Double = {
    val yhat = for {
      group <- data.groups
    } yield {
      predictMat(group.values, group.rows, group.columns)
    }
    val y = data.groups.map(_.labels)
    metric.eval(y.toArray, yhat.toArray)
  }

}

object Booster {
  case class DatasetOptions(categoryFeatures: Array[Int], dims: Int)
  trait BoosterOptions {
    def trees: Int
    def learningRate: Double
    def ndcgCutoff: Int
    def maxDepth: Int
    def randomSeed: Int
    def earlyStopping: Option[Int]
  }

  trait BoosterFactory[D, T <: Booster[D], O <: BoosterOptions] {
    def apply(string: Array[Byte]): T
    def formatData(ds: BoosterDataset, parent: Option[D], options: O): D
    def closeData(d: D): Unit = {}
    def train(dataset: D, test: Option[D], options: O, dso: DatasetOptions): T
  }
}
