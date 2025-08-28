package com.example.beequeen;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0016\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\u000b\u001a\u00020\u00042\u0006\u0010\f\u001a\u00020\u0004J\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00040\u000e2\u0006\u0010\u0005\u001a\u00020\u0006J\u0010\u0010\u000f\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u000e\u0010\u0010\u001a\u00020\u00112\u0006\u0010\f\u001a\u00020\u0004J\u0016\u0010\u0012\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0013\u001a\u00020\u0004J\u0016\u0010\u0014\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\nJ&\u0010\u0015\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u0011J\u0016\u0010\u001b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\u00042\u0006\u0010\u001c\u001a\u00020\u0011\u00a8\u0006\u001d"}, d2 = {"Lcom/example/beequeen/TrainingUtils;", "", "()V", "exportZip", "Ljava/io/File;", "context", "Landroid/content/Context;", "importZip", "", "uri", "Landroid/net/Uri;", "labelsFileFor", "image", "listItems", "", "outDir", "readLabel", "", "saveCaptured", "src", "saveFromUri", "saveLabeledFrame", "bmp", "Landroid/graphics/Bitmap;", "box", "Landroid/graphics/RectF;", "label", "updateLabel", "newLabel", "app_debug"})
public final class TrainingUtils {
    @org.jetbrains.annotations.NotNull()
    public static final com.example.beequeen.TrainingUtils INSTANCE = null;
    
    private TrainingUtils() {
        super();
    }
    
    private final java.io.File outDir(android.content.Context context) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.io.File> listItems(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.io.File labelsFileFor(@org.jetbrains.annotations.NotNull()
    java.io.File image) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.io.File saveFromUri(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.io.File saveCaptured(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.io.File src) {
        return null;
    }
    
    public final void importZip(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.io.File exportZip(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    public final void saveLabeledFrame(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.graphics.Bitmap bmp, @org.jetbrains.annotations.NotNull()
    android.graphics.RectF box, @org.jetbrains.annotations.NotNull()
    java.lang.String label) {
    }
    
    public final void updateLabel(@org.jetbrains.annotations.NotNull()
    java.io.File image, @org.jetbrains.annotations.NotNull()
    java.lang.String newLabel) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String readLabel(@org.jetbrains.annotations.NotNull()
    java.io.File image) {
        return null;
    }
}