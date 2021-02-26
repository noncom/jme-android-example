package org.noncom.jme_ttf.clarity;


import com.atr.jme.font.TrueTypeBMP;
import com.atr.jme.font.TrueTypeMesh;
import com.atr.jme.font.asset.TrueTypeKeyBMP;
import com.atr.jme.font.asset.TrueTypeKeyMesh;
import com.atr.jme.font.asset.TrueTypeLoader;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.texture.Texture;

/**
 */
public abstract class AClarity {

    private static AClarity instance;

    protected AssetManager assets;
    protected RenderManager renderManager;

    protected float dpiMod;
    protected float relDpiMod;
    protected int dpi;
    public boolean isMobile;
    public boolean isAndroid;
    protected boolean actualDerivativeSupported;
    protected boolean derivativeSupported = true;
    public int maxTrueTypeResolution = 2048;
    public boolean lockTrueType = false;
    public boolean strongTrueType = false;

    protected String trueTypePreload = "";
//            "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
//            + "abcdefghijklmnopqrstuvwxyz"
//            + "0123456789!@#$%^&*()-_+=*/"
//            + "\\:;\"<>,.?{}[]|`~'";

    public static AClarity getInstance() {
        return instance;
    }

    protected abstract void initPlatform(Application app);

    protected AClarity(Application app) {
        instance = this;
        this.assets = app.getAssetManager();
        this.renderManager = app.getRenderManager();
        assets.registerLoader(TrueTypeLoader.class, "ttf");
        trueTypePreload = (String) assets.loadAsset("texts/ttf_preload/russian.txt");

        //Check to see if we're running on Android and, if so
        //Determine the screen's density and if standard
        //derivatives is supported by the GLES implementation.
        initPlatform(app);
    }

    public AssetManager getAssetManager() {
        return assets;
    }

    public RenderManager getRenderManager() { return renderManager; }

    /**
     * Sets whether or not to support GLSL derivatives. This is used to
     * render pseudo anti-aliasing in GradientBackgroundComponents if
     * supported. If GradientBackgroundComponents have artifacts consider
     * setting this to false. This can be set to true if and only if
     * the graphics device actually supports it.
     *
     * @param support True to support derivatives otherwise false.
     */
    public void setSupportDerivatives(boolean support) {
        if (!actualDerivativeSupported)
            return;
        derivativeSupported = support;
    }

    /**
     * Whether or not GLSL derivative support is enabled. This is
     * used by GradientBackgroundComponents to render pseudo
     * anti-aliasing.
     *
     * @return True if derivative support has been enabled.
     *
     * @see #setSupportDerivatives(boolean)
     */
    public boolean isSupportDerivatives() {
        return derivativeSupported;
    }

    /**
     * Sets the characters to pre-load into a
     * <code>TrueTypeFont</code> atlas when loading
     * <code>TrueTypeFont</code>s.
     *
     * @param characters The characters to pre-load.
     */
    public void setTrueTypePreloadCharacters(String characters) {
        trueTypePreload = characters;
    }

    /**
     * Gets the characters to pre-load into a
     * <code>TrueTypeFont</code> atlas when loading
     * <code>TrueTypeFont</code>s.
     *
     * @return A <code>String</code> containing the characters
     * to pre-load.
     */
    public String getTrueTypePreloadCharacters() {
        return trueTypePreload;
    }

    /**
     * Sets if true type fonts should have their texture atlas locked
     * after pre-loading characters. Locking the atlas prevents the
     * font from adding new characters to the atlas.
     *
     * @param lock True to lock the atlas otherwise false.
     *
     * @see #setTrueTypePreloadCharacters(String)
     */
    public void setLockTrueType(boolean lock) {
        lockTrueType = lock;
    }

    /**
     * Whether or not true type font have their atlas locked after
     * pre-loading characters. Locking the atlas prevents the font
     * from adding new characters to the atlas.
     *
     * @return True if true type fonts lock their atlas.
     *
     * @see #setLockTrueType(boolean)
     * @see #setTrueTypePreloadCharacters(String)
     */
    public boolean getLockTrueType() {
        return lockTrueType;
    }

    /**
     * Sets if loaded true type fonts should be referenced with strong
     * or weak references.
     *
     * @param keep True to use strong references, otherwise false.
     */
    public void keepTrueType(boolean keep) {
        strongTrueType = keep;
    }

    /**
     * Whether or not true type fonts should use strong or weak
     * references.
     *
     * @return True if strong references should be used otherwise
     * false.
     *
     * @see #keepTrueType(boolean)
     */
    public boolean isKeepTrueType() {
        return strongTrueType;
    }


    /**
     * Loads a true type font scaling the point size based upon the org.game.example.system's screen
     * density.
     *
     * @param path The path to the desired font.
     * @param style The style to use such as <code>com.atr.jme.font.util.Style.Plain</code>.
     * @param pointSize The desired point size.
     * @param outline The size of the font's outline.
     * @return The loaded font.
     *
     * @see com.atr.jme.font.util.Style
     *
     * @author Adam T. Ryder
     */
    public TrueTypeBMP loadFontDP(String path, com.atr.jme.font.util.Style style, int pointSize, int outline) {
        return loadFont(path, style, pointSize, outline, dpi);
    }

    /**
     * Loads a scalable true type font scaling the point size based upon the
     * org.game.example.system's screen density.
     *
     * @param path The path to the desired font.
     * @param style The style to use such as <code>com.atr.jme.font.util.Style.Plain</code>.
     * @param pointSize The desired point size.
     * @return The loaded font.
     *
     * @see com.atr.jme.font.util.Style
     *
     * @author Adam T. Ryder
     */
    public TrueTypeMesh loadMeshFontDP(String path, com.atr.jme.font.util.Style style, int pointSize, int outline) {
        return loadMeshFont(path, style, pointSize, dpi);
    }




    /**
     * Loads a true type font without modifying the point size based on screen density.
     *
     * @param path The path to the desired font.
     * @param style The style to use such as <code>com.atr.jme.font.util.Style.Plain</code>.
     * @param pointSize The desired point size.
     * @param outline The size of the font's outline.
     * @return The loaded font.
     *
     * @see com.atr.jme.font.util.Style
     *
     * @author Adam T. Ryder
     */
    public TrueTypeBMP loadFont(String path, com.atr.jme.font.util.Style style, int pointSize, int outline) {
        return loadFont(path, style, pointSize, outline, 72);
    }

    /**
     * Loads a true type font without modifying the point size based on supplied density.
     *
     * @param path The path to the desired font.
     * @param style The style to use such as <code>com.atr.jme.font.util.Style.Plain</code>.
     * @param pointSize The desired point size.
     * @return The loaded font.
     *
     * @see com.atr.jme.font.util.Style
     *
     * @author Adam T. Ryder
     */
    public TrueTypeBMP loadFont(String path, com.atr.jme.font.util.Style style, int pointSize, int outline, int dpi) {
        float scale = 1;
        int actualSize = pointSize;
        if (!isMobile) {
            if (pointSize < 32) {
                actualSize = (int)Math.floor(pointSize / 0.73f);
                scale = pointSize / (float)actualSize;
            } else if (pointSize < 53) {
                actualSize = (int)Math.floor(pointSize / 0.84f);
                scale = pointSize / (float)actualSize;
            }
        }

        System.out.println("LOADING TTF");
        TrueTypeBMP ttf = (TrueTypeBMP)assets.loadAsset(
                new TrueTypeKeyBMP(path, style, actualSize, outline, dpi, !strongTrueType,
                        trueTypePreload, maxTrueTypeResolution));
        ttf.setScale(scale);
        ttf.lock(lockTrueType);

        return ttf;
    }

    /**
     * Loads a scalable true type font without modifying the point size based on screen density.
     *
     * @param path The path to the desired font.
     * @param style The style to use such as <code>com.atr.jme.font.util.Style.Plain</code>.
     * @param pointSize The desired point size.
     * @return The loaded font.
     *
     * @see com.atr.jme.font.util.Style
     *
     * @author Adam T. Ryder
     */
    public TrueTypeMesh loadMeshFont(String path, com.atr.jme.font.util.Style style, int pointSize) {
        return loadMeshFont(path, style, pointSize, 72);
    }

    /**
     * Loads a scalable true type font without modifying point size based on supplied density.
     *
     * @param path The path to the desired font.
     * @param style The style to use such as <code>com.atr.jme.font.util.Style.Plain</code>.
     * @param pointSize The desired point size.
     * @return The loaded font.
     *
     * @see com.atr.jme.font.util.Style
     *
     * @author Adam T. Ryder
     */
    public TrueTypeMesh loadMeshFont(String path, com.atr.jme.font.util.Style style, int pointSize, int dpi) {


        TrueTypeMesh ttf = (TrueTypeMesh)assets.loadAsset(
                new TrueTypeKeyMesh(path, style, pointSize, dpi, !strongTrueType,
                        trueTypePreload));
        ttf.setScale(1);

        //ttf.getGlyphs(trueTypePreload);
        ttf.lock(lockTrueType);

        return ttf;
    }

    public com.atr.jme.font.util.Style fontStyle(String style) {
        switch(style.toLowerCase()) {
            case "plain":
                return com.atr.jme.font.util.Style.Plain;
            case "bold":
                return com.atr.jme.font.util.Style.Bold;
            case "italic":
                return com.atr.jme.font.util.Style.Italic;
            case "bolditalic":
                return com.atr.jme.font.util.Style.BoldItalic;
            case "italicbold":
                return com.atr.jme.font.util.Style.BoldItalic;
            default:
                return com.atr.jme.font.util.Style.Plain;
        }
    }

    public Material loadMaterial(String path) {
        if (path.toLowerCase().endsWith(".j3m"))
            return assets.loadMaterial(path);
        return new Material(assets, path);
    }

    /**
     * Scales a value by the screens density on mobile devices.
     *
     * @param value The value to scale.
     * @return The value scaled by the screens density.
     *
     * @author Adam T. Ryder
     */
    public float dp(float value) {
        return value * dpiMod;
    }

    /**
     * Scales a value by the screens density on mobile devices.
     *
     * @param value The value to scale.
     * @return The value scaled by the screens density.
     *
     * @author Adam T. Ryder
     */
    public double dp(double value) {
        return value * dpiMod;
    }

    /**
     * Scales a <code>Vector3f</code> by the screens density on
     * mobile devices. The original <code>Vector3f</code> is not
     * modified.
     *
     * @param value The <code>Vector3f</code> to scale.
     * @return A new <code>Vector3f</code> scaled by the screens density.
     *
     * @author Adam T. Ryder
     */
    public Vector3f dp(Vector3f value) {
        return value.mult(dpiMod);
    }

    public int dpInt(int value) {
        return Math.round(value * dpiMod);
    }

    /**
     * Scales a <code>Vector3f</code> by the screens density on
     * mobile devices. The original <code>Vector3f</code> will be
     * modified.
     *
     * @param value The <code>Vector3f</code> to scale.
     * @return The <code>Vector3f</code> scaled by the screens density.
     *
     * @author Adam T. Ryder
     */
    public Vector3f dpLocal(Vector3f value) {
        return value.multLocal(dpiMod);
    }

    /**
     * Scales a <code>Float</code> by the screens density
     * relative to it's base general density. In example if the
     * general density is hdpi(240dpi base) this will scale the
     * value by (actual screen density / 240). This is best
     * used for scaling textures loaded with
     * {@link #loadTextureDP(String, boolean, boolean)}.
     *
     * @param value The value to scale.
     * @return The value scaled by the screens density.
     *
     * @author Adam T. Ryder
     */
    public float dpRel(float value) {
        return value * relDpiMod;
    }

    /**
     * Scales a <code>Double</code> by the screens density
     * relative to it's base general density. In example if the
     * general density is hdpi(240dpi base) this will scale the
     * value by (actual screen density / 240). This is best
     * used for scaling textures loaded with
     * {@link #loadTextureDP(String, boolean, boolean)}.
     *
     * @param value The value to scale.
     * @return The value scaled by the screens density.
     *
     * @author Adam T. Ryder
     */
    public double dpRel(double value) {
        return value * relDpiMod;
    }

    /**
     * Scales a <code>Vector3f</code> by the screens density
     * relative to it's base general density. In example if the
     * general density is hdpi(240dpi base) this will scale the
     * value by (actual screen density / 240). This is best
     * used for scaling textures loaded with
     * {@link #loadTextureDP(String, boolean, boolean)}.
     *
     * @param value The <code>Vector3f</code> to scale.
     * @return The <code>Vector3f</code> scaled by the screens density.
     *
     * @author Adam T. Ryder
     */
    public Vector3f dpRel(Vector3f value) {
        return value.mult(relDpiMod);
    }

    /**
     * Scales a <code>Vector3f</code> by the screens density
     * relative to it's base general density. In example if the
     * general density is hdpi(240dpi base) this will scale the
     * value by (actual screen density / 240). This will modify
     * the supplied Vector3f. This is best used for scaling textures
     * loaded with
     * {@link #loadTextureDP(String, boolean, boolean)}.
     *
     * @param value The <code>Vector3f</code> to scale.
     * @return The <code>Vector3f</code> scaled by the screens density.
     *
     * @author Adam T. Ryder
     */
    public Vector3f dpeRelLocal(Vector3f value) {
        return value.multLocal(relDpiMod);
    }

    public abstract Texture loadTextureDP(String path, boolean repeat, boolean generateMips);
}
