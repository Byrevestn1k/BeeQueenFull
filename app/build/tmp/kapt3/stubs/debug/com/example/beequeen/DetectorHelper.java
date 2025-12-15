package com.example.beequeen;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010%\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001:\u0001&B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\b2\u0006\u0010\u0014\u001a\u00020\u0015J\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00050\bJ\u0018\u0010\u0017\u001a\u00020\u000b2\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u0019H\u0002J \u0010\u001b\u001a\n \u001d*\u0004\u0018\u00010\u001c0\u001c2\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u001e\u001a\u00020\u0005H\u0002J$\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00130\b2\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00130\b2\u0006\u0010!\u001a\u00020\u000bH\u0002J\u0016\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020\u00052\u0006\u0010%\u001a\u00020\u000bR\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00050\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\rX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\rX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\'"}, d2 = {"Lcom/example/beequeen/DetectorHelper;", "", "context", "Landroid/content/Context;", "modelPath", "", "(Landroid/content/Context;Ljava/lang/String;)V", "classNames", "", "classThresholds", "", "", "inputSize", "", "numChannels", "numPreds", "tflite", "Lorg/tensorflow/lite/Interpreter;", "detect", "Lcom/example/beequeen/DetectorHelper$DetectionResult;", "bitmap", "Landroid/graphics/Bitmap;", "getClassNames", "iou", "a", "Landroid/graphics/RectF;", "b", "loadModelFile", "Ljava/nio/MappedByteBuffer;", "kotlin.jvm.PlatformType", "path", "nms", "src", "iouThresh", "setThreshold", "", "label", "value01", "DetectionResult", "app_debug"})
public final class DetectorHelper {
    private final int inputSize = 640;
    private final int numPreds = 8400;
    private final int numChannels = 8;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> classNames = null;
    @org.jetbrains.annotations.NotNull()
    private final org.tensorflow.lite.Interpreter tflite = null;
    
    /**
     * ПОРОГИ, КЕРОВАНІ З UI
     */
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, java.lang.Float> classThresholds = null;
    
    public DetectorHelper(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String modelPath) {
        super();
    }
    
    public final void setThreshold(@org.jetbrains.annotations.NotNull()
    java.lang.String label, float value01) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getClassNames() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.beequeen.DetectorHelper.DetectionResult> detect(@org.jetbrains.annotations.NotNull()
    android.graphics.Bitmap bitmap) {
        return null;
    }
    
    private final java.util.List<com.example.beequeen.DetectorHelper.DetectionResult> nms(java.util.List<com.example.beequeen.DetectorHelper.DetectionResult> src, float iouThresh) {
        return null;
    }
    
    private final float iou(android.graphics.RectF a, android.graphics.RectF b) {
        return 0.0F;
    }
    
    private final java.nio.MappedByteBuffer loadModelFile(android.content.Context context, java.lang.String path) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0007H\u00c6\u0003J\'\u0010\u0012\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\u0013\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0016\u001a\u00020\u0017H\u00d6\u0001J\t\u0010\u0018\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u0019"}, d2 = {"Lcom/example/beequeen/DetectorHelper$DetectionResult;", "", "label", "", "score", "", "box", "Landroid/graphics/RectF;", "(Ljava/lang/String;FLandroid/graphics/RectF;)V", "getBox", "()Landroid/graphics/RectF;", "getLabel", "()Ljava/lang/String;", "getScore", "()F", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class DetectionResult {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String label = null;
        private final float score = 0.0F;
        @org.jetbrains.annotations.NotNull()
        private final android.graphics.RectF box = null;
        
        public DetectionResult(@org.jetbrains.annotations.NotNull()
        java.lang.String label, float score, @org.jetbrains.annotations.NotNull()
        android.graphics.RectF box) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getLabel() {
            return null;
        }
        
        public final float getScore() {
            return 0.0F;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.graphics.RectF getBox() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        public final float component2() {
            return 0.0F;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.graphics.RectF component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.beequeen.DetectorHelper.DetectionResult copy(@org.jetbrains.annotations.NotNull()
        java.lang.String label, float score, @org.jetbrains.annotations.NotNull()
        android.graphics.RectF box) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}