import ScreenSaver
import Cocoa
import logodemo

class KotlinLogosView : ScreenSaverView {
    let kotlinScreenSaverView = MainKt.create()

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
    
    // Note to self, needs restart of Sys Preferences to take effect
    override var hasConfigureSheet: Bool {
        return false
    }
    
    override var configureSheet: NSWindow? {
        return nil
    }
}
