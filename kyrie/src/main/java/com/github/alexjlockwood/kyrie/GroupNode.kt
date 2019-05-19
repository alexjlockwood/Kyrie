package com.github.alexjlockwood.kyrie

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import java.util.*

/** A [Node] that holds a group of children [Node]s. */
class GroupNode private constructor(
        rotation: List<Animation<*, Float>>,
        pivotX: List<Animation<*, Float>>,
        pivotY: List<Animation<*, Float>>,
        scaleX: List<Animation<*, Float>>,
        scaleY: List<Animation<*, Float>>,
        translateX: List<Animation<*, Float>>,
        translateY: List<Animation<*, Float>>,
        private val children: List<Node>
) : BaseNode(rotation, pivotX, pivotY, scaleX, scaleY, translateX, translateY) {

    // <editor-fold desc="Layer">

    override fun toLayer(timeline: PropertyTimeline): GroupLayer {
        return GroupLayer(timeline, this)
    }

    internal class GroupLayer(timeline: PropertyTimeline, node: GroupNode) : BaseNode.BaseLayer(timeline, node) {
        private val children: ArrayList<Layer>

        init {
            val childrenNodes = node.children
            children = ArrayList(childrenNodes.size)
            var i = 0
            val size = childrenNodes.size
            while (i < size) {
                children.add(childrenNodes[i].toLayer(timeline))
                i++
            }
        }

        override fun onDraw(canvas: Canvas, parentMatrix: Matrix, viewportScale: PointF) {
            canvas.save()
            var i = 0
            val size = children.size
            while (i < size) {
                children[i].draw(canvas, parentMatrix, viewportScale)
                i++
            }
            canvas.restore()
        }

        override fun isStateful(): Boolean {
            for (i in 0 until children.size) {
                if (children[i].isStateful()) {
                    return true
                }
            }
            return false
        }

        override fun onStateChange(stateSet: IntArray): Boolean {
            var changed = false
            for (i in 0 until children.size) {
                changed = changed or children[i].onStateChange(stateSet)
            }
            return changed
        }
    }

    // </editor-fold>

    // <editor-fold desc="Builder">

    @DslMarker
    private annotation class GroupNodeMarker

    /** Builder class used to create [GroupNode]s. */
    @GroupNodeMarker
    class Builder internal constructor() : BaseNode.Builder<Builder>() {
        private val children = ArrayList<Node>()

        // Children.

        fun child(node: Node): Builder {
            children.add(node)
            return this
        }

        fun child(builder: Node.Builder<*>): Builder {
            return child(builder.build())
        }

        override fun self(): Builder {
            return this
        }

        override fun build(): GroupNode {
            return GroupNode(rotation, pivotX, pivotY, scaleX, scaleY, translateX, translateY, children)
        }
    }

    // </editor-fold>

    companion object {

        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}