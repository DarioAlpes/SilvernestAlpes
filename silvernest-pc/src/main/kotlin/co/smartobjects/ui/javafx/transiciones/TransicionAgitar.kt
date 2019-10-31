package co.smartobjects.ui.javafx.transiciones

import javafx.animation.*
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.CacheHint
import javafx.scene.Node
import javafx.util.Duration

internal class TransicionAgitar
(
        private val nodo: Node
) : Transition()
{

    private val WEB_EASE = Interpolator.SPLINE(0.25, 0.1, 0.25, 1.0)
    private val timeline: Timeline
    private var oldCache = false
    private var oldCacheHint = CacheHint.DEFAULT
    private val useCache = true
    private val translateXInicial: Double

    private val x = SimpleDoubleProperty()

    init
    {
        statusProperty().addListener { _, _, newStatus ->
            when (newStatus)
            {
                Animation.Status.RUNNING -> starting()
                else                     -> stopping()
            }
        }

        timeline =
                Timeline(
                        KeyFrame(Duration.millis(0.0), KeyValue(x, 0, WEB_EASE)),
                        KeyFrame(Duration.millis(100.0), KeyValue(x, -10, WEB_EASE)),
                        KeyFrame(Duration.millis(200.0), KeyValue(x, 10, WEB_EASE)),
                        KeyFrame(Duration.millis(300.0), KeyValue(x, -10, WEB_EASE)),
                        KeyFrame(Duration.millis(400.0), KeyValue(x, 10, WEB_EASE)),
                        KeyFrame(Duration.millis(500.0), KeyValue(x, -10, WEB_EASE))
                        )
        translateXInicial = nodo.translateX
        x.addListener { _, _, n1 -> nodo.translateX = translateXInicial + n1.toDouble() }

        cycleDuration = Duration.millis(500.0)
        delay = Duration.seconds(0.1)
    }

    private fun starting()
    {
        if (useCache)
        {
            oldCache = nodo.isCache
            oldCacheHint = nodo.cacheHint
            nodo.isCache = true
            nodo.cacheHint = CacheHint.SPEED
        }
    }

    private fun stopping()
    {
        if (useCache)
        {
            nodo.isCache = oldCache
            nodo.cacheHint = oldCacheHint
        }
    }

    override fun interpolate(d: Double)
    {
        timeline.playFrom(Duration.seconds(d))
        timeline.stop()
    }
}