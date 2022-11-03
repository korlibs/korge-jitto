package com.soywiz.korge.jitto

import com.soywiz.klock.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korim.paint.*
import com.soywiz.korim.vector.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.geom.interpolate
import com.soywiz.korma.geom.vector.*
import com.soywiz.korma.interpolation.*

data class Jitto(
    var leftHand: Angle = (0).degrees,
    var rightHand: Angle = 0.degrees,
    var leftLeg: Double = -1.0,
    var rightLeg: Double = +1.0,
    var rotation: Angle = 0.degrees,
    val leftEyeDist: Double = 0.75,
    val leftEyeAngle: Angle = 90.degrees,
    val rightEyeDist: Double = 0.75,
    val rightEyeAngle: Angle = 90.degrees,
    //var rotation: Angle = -45.degrees,
) : Interpolable<Jitto> {
    override fun interpolateWith(ratio: Double, other: Jitto): Jitto = interpolate(this, other, ratio)

    companion object {
        fun interpolate(a: Jitto, b: Jitto, ratio: Double): Jitto {
            return Jitto(
                leftHand = ratio.interpolate(a.leftHand, b.leftHand),
                rightHand = ratio.interpolate(a.rightHand, b.rightHand),
                leftLeg = ratio.interpolate(a.leftLeg, b.leftLeg),
                rightLeg = ratio.interpolate(a.rightLeg, b.rightLeg),
                rotation = ratio.interpolate(a.rotation, b.rotation),
                leftEyeDist = ratio.interpolate(a.leftEyeDist, b.leftEyeDist),
                leftEyeAngle = ratio.interpolate(a.leftEyeAngle, b.leftEyeAngle),
                rightEyeDist = ratio.interpolate(a.rightEyeDist, b.rightEyeDist),
                rightEyeAngle = ratio.interpolate(a.rightEyeAngle, b.rightEyeAngle),
            )
        }
    }
}

class JittoView(shapeSide: Double = 512.0) : Container() {
    var drawShadow = true
    var drawBorder = true
    var drawColor = true

    var shapeSide: Double = shapeSide
        set(value) {
            field = value
            refresh()
        }

    val shapeScale: Double get() = shapeSide / 512.0

    //val graphics = graphics(renderer = GraphicsRenderer.GPU)
    val graphics = graphics(renderer = GraphicsRenderer.SYSTEM)

    var model = Jitto()
        set(value) {
            field = value
            refresh()
        }

    suspend fun interpolateTo(other: Jitto) {
        val start = this.model
        tween(time = .3.seconds) {
            model = Jitto.interpolate(start, other, it)
        }
    }

    init {
        refresh()
    }

    fun refresh() {
        graphics.rotation = model.rotation
        graphics.updateShape { build(this) }
    }

    fun build(shape: ShapeBuilder) {
        val shapeScale = this.shapeScale
        if (drawShadow) shape.render(Layer.BACKGROUND, Colors.BLACK, if (drawBorder) 120.0 else 64.0, Point(0, 20), shapeScale)
        if (drawBorder) shape.render(Layer.BORDER, Colors.WHITE, 120.0, Point(0, 0), shapeScale)
        if (drawColor) shape.render(Layer.FILL, Colors["#47009C"], 60.0, Point(0, 0), shapeScale)
    }

    enum class Layer {
        BACKGROUND,
        BORDER,
        FILL
    }

    fun ShapeBuilder.render(layer: Layer, color: RGBA, thickness: Double, displacement: IPoint, scale: Double = 1.0) {
        stroke(Stroke(color, thickness = thickness * scale, startCap = LineCap.ROUND, endCap = LineCap.ROUND)) {
            val leftArmPoint = Point.fromPolar((-180).degrees + model.leftHand, 220.0)
            val rightArmPoint = Point.fromPolar((0).degrees - model.rightHand, 220.0)
            val leftLegPoint = Point.fromPolar(90.degrees - 38.degrees * model.leftLeg, 250.0)
            val rightLegPoint = Point.fromPolar(90.degrees - 38.degrees * model.rightLeg, 250.0)
            keepTransform {
                scale(scale)
                translate(displacement.x, displacement.y)

                line(0.0, 0.0, rightArmPoint.x, rightArmPoint.y)
                line(0.0, 0.0, leftArmPoint.x, leftArmPoint.y)
                line(0.0, 0.0, leftLegPoint.x, leftLegPoint.y)
                line(0.0, 0.0, rightLegPoint.x, rightLegPoint.y)
                //line(0, 0, (-160 * leftLeg).toIntRound(), 200)
                //line(0, 0, (-160 * rightLeg).toIntRound(), 200)
            }
        }

        val leftEye = Point(-100, -150) - Point(0.0, model.leftHand.sine * 40.0)
        val rightEye = Point(+100, -150) - Point(0.0, model.rightHand.sine * 40.0)
        val eyeSize = 72.0
        val pupilSize = 32.0

        val eyeLeftPos = Point.fromPolar(model.leftEyeAngle, model.leftEyeDist * (eyeSize - pupilSize))
        val eyeRightPos = Point.fromPolar(model.rightEyeAngle, model.rightEyeDist * (eyeSize - pupilSize))

        if (layer == Layer.BORDER) {
            fill(color) {
                keepTransform {
                    scale(scale)
                    translate(displacement.x, displacement.y)
                    ellipseCenter(leftEye.x, leftEye.y, eyeSize, eyeSize)
                    ellipseCenter(rightEye.x, rightEye.y, eyeSize, eyeSize)
                }
            }
        }
        if (layer == Layer.FILL) {
            fill(color) {
                keepTransform {
                    scale(scale)
                    translate(displacement.x, displacement.y)
                    ellipseCenter(leftEye.x + eyeLeftPos.x, leftEye.y + eyeLeftPos.y, pupilSize)
                    ellipseCenter(rightEye.x + eyeRightPos.x, rightEye.y + eyeRightPos.y, pupilSize)
                }
            }
        }
        stroke(Stroke(color, thickness = thickness * scale * 0.6, startCap = LineCap.ROUND, endCap = LineCap.ROUND)) {
            keepTransform {
                scale(scale)
                translate(displacement.x, displacement.y)
                ellipseCenter(leftEye.x, leftEye.y, eyeSize)
                ellipseCenter(rightEye.x, rightEye.y, eyeSize)
            }
        }
    }

    fun VectorBuilder.ellipseCenter(x: Double, y: Double, rw: Double, rh: Double = rw) {
        ellipse(x - rw, y - rh, rw * 2, rh * 2)
    }
}

object JittoViewExample {
    suspend fun runInContainer(container: Container) {
        val jitto = JittoView(100.0).xy(256, 256).addTo(container)
        while (true) {
            container.tween(jitto::x[jitto.x + 10.0], jitto::model[Jitto(
                rightHand = +10.degrees,
                leftHand = +14.degrees,
                leftLeg = +1.0,
                leftEyeAngle = 0.degrees,
                rightLeg = -1.0,
                rightEyeAngle = 0.degrees,
            )], time = 0.5.seconds)

            container.tween(jitto::x[jitto.x + 10.0], jitto::model[Jitto(
                rightHand = 10.degrees,
                leftHand = 14.degrees,
                leftLeg = +1.0,
                rightLeg = -1.0,
                leftEyeDist = 0.0,
                leftEyeAngle = 0.degrees,
                rightEyeDist = 0.0,
                rightEyeAngle = 0.degrees,
                rotation = 45.degrees
            )], time = 0.5.seconds)

            container.tween(jitto::x[jitto.x + 10.0], jitto::model[Jitto(
                rightHand = -10.degrees,
                leftHand = -14.degrees,
                leftLeg = -1.0,
                rightLeg = +1.0,
            )], time = 0.5.seconds)
        }
    }
}
