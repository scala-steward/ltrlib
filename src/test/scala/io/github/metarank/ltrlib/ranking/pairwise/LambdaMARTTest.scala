package io.github.metarank.ltrlib.ranking.pairwise

import io.github.metarank.ltrlib.dataset.LetorDataset
import io.github.metarank.ltrlib.booster.Booster.BoosterOptions
import io.github.metarank.ltrlib.booster.{LightGBMBooster, XGBoostBooster}
import io.github.metarank.ltrlib.metric.MSE
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LambdaMARTTest extends AnyFlatSpec with Matchers {
  it should "train on letor: lightgbm" in {
    val lm      = LambdaMART(LetorDataset.train, BoosterOptions(), LightGBMBooster(_, _))
    val booster = lm.fit()
    val mse     = lm.eval(booster, LetorDataset.train, MSE)
    mse should be > 0.95
  }

  it should "train on letor: xgboost" in {
    val lm      = LambdaMART(LetorDataset.train, BoosterOptions(), XGBoostBooster(_, _))
    val booster = lm.fit()
    val mse     = lm.eval(booster, LetorDataset.train, MSE)
    mse should be > 0.95
  }
}