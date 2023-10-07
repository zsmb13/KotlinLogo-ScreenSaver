import ScreenSaver
import Cocoa
import KotlinLogo

class KotlinLogosView : ScreenSaverView {
    let kotlinScreenSaverView = KotlinScreenSaverViewKt.create()

    override init?(frame: NSRect, isPreview: Bool) {
        super.init(frame: frame, isPreview: isPreview)
        kotlinScreenSaverView.doInit(screenSaverView: self, isPreview: isPreview)
    }

    required init?(coder decoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func draw(_ rect: NSRect) {
        super.draw(rect)
        var mutableRect = rect
        kotlinScreenSaverView.draw(rect: &mutableRect)
    }

    override func animateOneFrame() {
        super.animateOneFrame()
        kotlinScreenSaverView.animateOneFrame()
    }
    
    override var hasConfigureSheet: Bool { kotlinScreenSaverView.configureSheet != nil }
    override var configureSheet: NSWindow? { kotlinScreenSaverView.configureSheet }
}
