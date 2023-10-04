import ScreenSaver
import Cocoa
import KotlinLogo

class KotlinLogosView : ScreenSaverView {

    let kotlinScreenSaverView = KotlinScreenSaverViewKt.create()

    override init?(frame: NSRect, isPreview: Bool) {
        super.init(frame: frame, isPreview: isPreview)
        let b = Bundle(for: type(of: self))
        kotlinScreenSaverView.doInit(screenSaverView: self, isPreview: isPreview, bundle: b)
    }
    
    @available(*, unavailable)
    required init?(coder decoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func draw(_ rect: NSRect) {
        super.draw(rect)
        
        var r = rect
        kotlinScreenSaverView.draw(rect: &r)
    }
    
    override func animateOneFrame() {
        super.animateOneFrame()
        kotlinScreenSaverView.animateOneFrame()
    }
    
    lazy var sheetController: NSWindowController = ConfigKt.prefController()

    override var hasConfigureSheet: Bool {
        return true
    }
    
    override var configureSheet: NSWindow? {
        return sheetController.window
    }
}
