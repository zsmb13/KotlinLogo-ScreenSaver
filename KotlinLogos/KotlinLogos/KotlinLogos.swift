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
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) { // 1 second from now
                NSApplication.shared.terminate(nil)

                // Alternative impl
                // self.removeFromSuperview()
            }
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
