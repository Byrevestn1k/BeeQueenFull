package com.example.beequeen;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017H\u0002J\u0012\u0010\u0018\u001a\u00020\u00152\b\u0010\u0019\u001a\u0004\u0018\u00010\u001aH\u0014J\b\u0010\u001b\u001a\u00020\u0015H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcom/example/beequeen/LiveActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "btnCorrect", "Landroid/widget/Button;", "btnIncorrect", "detector", "Lorg/tensorflow/lite/task/vision/detector/ObjectDetector;", "lastBitmap", "Landroid/graphics/Bitmap;", "lastBox", "Landroid/graphics/RectF;", "overlay", "Lcom/example/beequeen/OverlayView;", "previewView", "Landroidx/camera/view/PreviewView;", "tone", "Landroid/media/ToneGenerator;", "yuvConverter", "Lcom/example/beequeen/YuvToRgbConverter;", "analyze", "", "image", "Landroidx/camera/core/ImageProxy;", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "startCamera", "app_debug"})
public final class LiveActivity extends androidx.appcompat.app.AppCompatActivity {
    private androidx.camera.view.PreviewView previewView;
    private com.example.beequeen.OverlayView overlay;
    private android.widget.Button btnCorrect;
    private android.widget.Button btnIncorrect;
    @org.jetbrains.annotations.NotNull()
    private final android.media.ToneGenerator tone = null;
    @org.jetbrains.annotations.Nullable()
    private org.tensorflow.lite.task.vision.detector.ObjectDetector detector;
    @org.jetbrains.annotations.NotNull()
    private final com.example.beequeen.YuvToRgbConverter yuvConverter = null;
    @org.jetbrains.annotations.Nullable()
    private android.graphics.Bitmap lastBitmap;
    @org.jetbrains.annotations.Nullable()
    private android.graphics.RectF lastBox;
    
    public LiveActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void startCamera() {
    }
    
    private final void analyze(androidx.camera.core.ImageProxy image) {
    }
}