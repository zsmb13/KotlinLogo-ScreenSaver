import ScreenSaver
import KotlinLogo

class KotlinLogosView: ScreenSaverView {
    var kotlinScreenSaverView: KotlinScreenSaverView!

    override init?(frame: NSRect, isPreview: Bool) {
        super.init(frame: frame, isPreview: isPreview)
        animationTimeInterval = 1 / 60.0
        kotlinScreenSaverView = KotlinScreenSaverViewKt.create(screenSaverView: self, isPreview: isPreview)
        DistributedNotificationCenter.default.addObserver(
            self,
            selector: #selector(KotlinLogosView.willStop(_:)),
            name: Notification.Name("com.apple.screensaver.willstop"),
            object: nil
        )
    }

    @objc func willStop(_ aNotification: Notification) {
        if (!isPreview) {
            // Alternative impl
            // self.removeFromSuperview()
            NSApplication.shared.terminate(nil)
        }
    }

    required init?(coder decoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func animateOneFrame() {
        super.animateOneFrame()
        kotlinScreenSaverView.animateOneFrame()
    }

    override func startAnimation() {
        super.startAnimation()
        kotlinScreenSaverView.startAnimation()
    }

    override var hasConfigureSheet: Bool {
        kotlinScreenSaverView.configureSheet != nil
    }
    override var configureSheet: NSWindow? {
        kotlinScreenSaverView.configureSheet
    }
}
