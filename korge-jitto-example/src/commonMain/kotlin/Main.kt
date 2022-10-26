import com.soywiz.korge.*
import com.soywiz.korge.jitto.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*

suspend fun main() = Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {
    val sceneContainer = sceneContainer()

    sceneContainer.changeTo({ MyScene() })
}

class MyScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        val jitto = JittoView().xy(256, 256).addTo(this)
        while (true) {
            jitto.interpolateTo(
                Jitto(
                    rightHand = 10.degrees,
                    leftHand = 14.degrees,
                    leftLeg = +1.0,
                    rightLeg = -1.0,
                    leftEyeDist = 0.0,
                    leftEyeAngle = 0.degrees,
                    rightEyeDist = 0.0,
                    rightEyeAngle = 0.degrees,
                    rotation = 45.degrees
                )
            )
            jitto.interpolateTo(
                Jitto(
                    rightHand = -10.degrees,
                    leftHand = -14.degrees,
                    leftLeg = -1.0,
                    rightLeg = +1.0,
                )
            )
        }
    }
}
