package com.example.beequeen;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\u0018\u00002\u00020\u0001B\u001b\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006J(\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u000b2\u0006\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 H\u0002J\u0010\u0010!\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001bH\u0014J\u0016\u0010\"\u001a\u00020\u00192\u0006\u0010\u001c\u001a\u00020\u000b2\u0006\u0010#\u001a\u00020\fJ\u0016\u0010$\u001a\u00020\u00192\u0006\u0010\u001c\u001a\u00020\u000b2\u0006\u0010%\u001a\u00020\u000eJ\u0016\u0010&\u001a\u00020\u00192\u0006\u0010\u0010\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\fJ\u0014\u0010\'\u001a\u00020\u00192\f\u0010(\u001a\b\u0012\u0004\u0012\u00020\u00150\u0014R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\f0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u000e0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006)"}, d2 = {"Lcom/example/beequeen/OverlayView;", "Landroid/view/View;", "context", "Landroid/content/Context;", "attrs", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "boxPaint", "Landroid/graphics/Paint;", "classColors", "", "", "", "enabledClasses", "", "imageHeight", "imageWidth", "queenSmoother", "Lcom/example/beequeen/QueenEmaSmoother;", "results", "", "Lcom/example/beequeen/DetectorHelper$DetectionResult;", "textBgPaint", "textPaint", "drawLabel", "", "canvas", "Landroid/graphics/Canvas;", "label", "score", "", "rect", "Landroid/graphics/RectF;", "onDraw", "setClassColor", "color", "setClassEnabled", "enabled", "setFrameInfo", "setResults", "detectionResults", "app_debug"})
public final class OverlayView extends android.view.View {
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint boxPaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint textPaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint textBgPaint = null;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.example.beequeen.DetectorHelper.DetectionResult> results;
    private int imageWidth = 0;
    private int imageHeight = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, java.lang.Boolean> enabledClasses = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, java.lang.Integer> classColors = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.beequeen.QueenEmaSmoother queenSmoother = null;
    
    @kotlin.jvm.JvmOverloads()
    public OverlayView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs) {
        super(null);
    }
    
    public final void setFrameInfo(int imageWidth, int imageHeight) {
    }
    
    public final void setClassEnabled(@org.jetbrains.annotations.NotNull()
    java.lang.String label, boolean enabled) {
    }
    
    public final void setClassColor(@org.jetbrains.annotations.NotNull()
    java.lang.String label, int color) {
    }
    
    public final void setResults(@org.jetbrains.annotations.NotNull()
    java.util.List<com.example.beequeen.DetectorHelper.DetectionResult> detectionResults) {
    }
    
    @java.lang.Override()
    protected void onDraw(@org.jetbrains.annotations.NotNull()
    android.graphics.Canvas canvas) {
    }
    
    private final void drawLabel(android.graphics.Canvas canvas, java.lang.String label, float score, android.graphics.RectF rect) {
    }
    
    @kotlin.jvm.JvmOverloads()
    public OverlayView(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
}