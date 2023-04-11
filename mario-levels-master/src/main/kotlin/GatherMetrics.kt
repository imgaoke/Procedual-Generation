import levelGenerators.sample.GapLevelGenerator
import metrics.AbstractMetric
import metrics.JumpMetric
import metrics.LinearityMetric
import metrics.MetricsEvaluator
import metrics.MyMetricJava

// Runs an evaluation of metrics on a given generator
fun main() {
    val levelGenerator = levelGenerators.test.LevelGenerator()
    val metrics = listOf<AbstractMetric>(
        LinearityMetric(),
        // TODO: Change this to your custom metric
        MyMetricJava()
    )

    val metricsEvaluator = MetricsEvaluator(levelGenerator, metrics)

    metricsEvaluator.generateCSV("src/main/python/data/metrics.csv", 100)
}