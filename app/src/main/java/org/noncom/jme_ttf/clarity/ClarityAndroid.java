/*
 * $Id$
 *
 * Copyright (c) 2012-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.noncom.jme_ttf.clarity;

import com.jme3.app.Application;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.TextureKey;
import com.jme3.renderer.opengl.GL;
import com.jme3.system.android.JmeAndroidSystem;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;



import java.nio.IntBuffer;

public class ClarityAndroid extends AClarity {

    /**
     * General categories for grouping a variety of screen densities.
     * <ul>
     * <li>ldpi - Low DPI ~120dpi</li>
     * <li>mdpi - Medium DPI ~160dpi</li>
     * <li>hdpi - High DPI ~240dpi</li>
     * <li>xhdpi - Extra high DPI ~320dpi</li>
     * <li>xxhdpi - Extra extra high DPI ~480dpi</li>
     * <li>xxxhdpi - Extra extra extra high DPI ~640dpi</li>
     * </ul>
     *
     * @see <a href="https://developer.android.com/guide/practices/screens_support.html">https://developer.android.com/guide/practices/screens_support.html</a>
     *
     * @author Adam T. Ryder
     */
    public enum GeneralDensity {
        ldpi,
        mdpi,
        hdpi,
        xhdpi,
        xxhdpi,
        xxxhdpi;

        public GeneralDensity previous() {
            switch(this) {
                case ldpi:
                    return null;
                case mdpi:
                    return ldpi;
                case hdpi:
                    return mdpi;
                case xhdpi:
                    return hdpi;
                case xxhdpi:
                    return xhdpi;
                case xxxhdpi:
                    return xxhdpi;
                default:
                    return null;
            }
        }

        public GeneralDensity next() {
            switch(this) {
                case ldpi:
                    return mdpi;
                case mdpi:
                    return hdpi;
                case hdpi:
                    return xhdpi;
                case xhdpi:
                    return xxhdpi;
                case xxhdpi:
                    return xxxhdpi;
                case xxxhdpi:
                    return null;
                default:
                    return null;
            }
        }

        @Override
        public String toString() {
            switch(this) {
                case ldpi:
                    return "ldpi";
                case mdpi:
                    return "mdpi";
                case hdpi:
                    return "hdpi";
                case xhdpi:
                    return "xhdpi";
                case xxhdpi:
                    return "xxhdpi";
                case xxxhdpi:
                    return "xxxhdpi";
                default:
                    return "ldpi";
            }
        }
    }

    private GeneralDensity generalDensity;

    @Override
    protected void initPlatform(Application app) {
        String vendor = System.getProperty("java.vendor.url");
        if (vendor != null && vendor.toLowerCase().contains("android")) {
            //We're running on Android so we want to know the general
            //screen density category we're running on and how the
            //actual density relates to our base density of 160 and
            //the density category.
            isMobile = true;
            isAndroid = true;

            android.util.DisplayMetrics metrics = JmeAndroidSystem.getView().getResources().getDisplayMetrics();
            dpi = metrics.densityDpi;
            setScreenDensity(dpi);

            //

            String[] ext = android.opengl.GLES20.glGetString(GL.GL_EXTENSIONS).split(" ");
            derivativeSupported = false;
            for (String s : ext) {
                if (s.equals("GL_OES_standard_derivatives")) {
                    derivativeSupported = true;
                    break;
                }
            }
            actualDerivativeSupported = derivativeSupported;

            IntBuffer ib = BufferUtils.createIntBuffer(1);
            android.opengl.GLES20.glGetIntegerv(GL.GL_MAX_TEXTURE_SIZE, ib);
            ib.rewind();
            maxTrueTypeResolution = ib.get() < 2048 ? 1024 : 2048;

            System.out.println("LemurDynamo Running on Android\n"
                    + "Density: " + generalDensity.toString() + " @ " + Integer.toString(dpi) + "dpi"
                    + "\nDerivatives: " + Boolean.toString(derivativeSupported));
        } else {
            throw new IllegalStateException("Unknown Android Java vendor: " + vendor);
        }
    }

    protected ClarityAndroid(Application app) {
        super(app);
    }
    
    /**
     * Gets the density of the display in dots per inch. For desktop
     * systems this is defaulted to 72 regardless of actual density.
     * 
     * @return The screen density in dots per inch.
     */
    public int getScreenDensity() {
        return dpi;
    }
    
    /**
     * Sets the screen density in dots per inch. This is used for
     * calculating the density dependant sizes of fonts and textures.
     * 
     * @param densityDpi The desired density setting in dots per inch.
     */
    public void setScreenDensity(int densityDpi) {
        if (densityDpi <= 0)
            return;
        
        dpi = densityDpi;
        
        if (dpi >= 640) {
            generalDensity = GeneralDensity.xxxhdpi;
            relDpiMod = dpi / 640f;
        } else if (dpi >= 480) {
            generalDensity = GeneralDensity.xxhdpi;
            relDpiMod = dpi / 480f;
        } else if (dpi >= 320) {
            generalDensity = GeneralDensity.xhdpi;
            relDpiMod = dpi / 320f;
        } else if (dpi >= 240) {
            generalDensity = GeneralDensity.hdpi;
            relDpiMod = dpi / 240f;
        } else if (dpi >= 160) {
            generalDensity = GeneralDensity.mdpi;
            relDpiMod = dpi / 160f;
        } else {
            generalDensity = GeneralDensity.ldpi;
            if (dpi > 0) {
                relDpiMod = dpi / 120f;
            } else {
                relDpiMod = 1;
            }
        }

        //dpiMod = dpi / 160f;
        if (dpi > 0) {
            dpiMod = dpi / 160f;
        } else {
            dpiMod = 1;
        }
    }
    
    /**
     * Gets the screen density scale relative to the base density
     * of 160dpi. For a 320dpi screen this will be 2.0, for a 80dpi
     * screen this will be 0.5.
     * 
     * @return The scale of the screen density relative to the base density
     * of 160dpi.
     */
    public float getDensityScale() {
        return dpiMod;
    }
    
    /**
     * Gets a modifier used to determine the screen density relative to
     * its {@link GeneralDensity}. For MDPI devices 160 * getDensityModifier()
     * will yield the actual screen density, for HDPI devices
     * 240 * getDensityModifier() will yield the actual screen density.
     * 
     * This can be used to scale a texture loaded with
     * {@link #loadTextureDP(String, boolean, boolean)} to ensure
     * that the texture is scaled to match the screen density.
     * 
     * @return A modifier used to determine the screen density relative
     * to its {@link GeneralDensity}.
     * 
     * @see GeneralDensity
     */
    public float getDensityModifier() {
        return relDpiMod;
    }

//    protected void logBuildInfo() {
//        try {
//            java.net.URL u = Resources.getResource("lemur.build.date");
//            String build = Resources.toString(u, Charsets.UTF_8);
//            log.info("Lemur build date:" + build);
//        } catch( java.io.IOException e ) {
//            log.error( "Error reading build info", e );
//        }
//    }

    
    /**
     * Loads a texture based upon the screens general density. <p>We look
     * for the filename in subdirectories of the parent directory where
     * the subdirectory names match general density names. General densities
     * are:</p>
     * <ul>
     * <li>ldpi - Low DPI ~120dpi</li>
     * <li>mdpi - Medium DPI ~160dpi</li>
     * <li>hdpi - High DPI ~240dpi</li>
     * <li>xhdpi - Extra high DPI ~320dpi</li>
     * <li>xxhdpi - Extra extra high DPI ~480dpi</li>
     * <li>xxxhdpi - Extra extra extra high DPI ~640dpi</li>
     * </ul>
     *
     * <p>First we look in the folder matching the current general density,
     * if the texture is not found we look in the next lowest and continue
     * to do so until we find the file. If the texture is still not found
     * we try to load the texture without inserting a subdirectory and if
     * we still don't find the file we look again in general density
     * subdirectories this time looking in the next highest and continuing
     * until we find the texture. If we don't find the texture a
     * <code>RuntimeException</code>.</p>
     *
     * <p>This is all bypassed if we're not running on mobile and we just try
     * the texture at the supplied path.</p>
     *
     * <p>For example if we're running on a mobile device with a 160dpi screen
     * density and the requested texture is Textures/filename.png we first
     * attempt to find the texture Textures/mdpi/filename.png, then
     * Textures/ldpi/filename.png, then Textures/filename.png, then
     * Textures/hdpi/filename.png and so on...</p>
     *
     * @param path Path to the desired texture or the directory containing
     * all the supported general density subdirectories at least one of which
     * containing the desired filename.
     * @param repeat If false the texture will be clamped so that UV coordinates
     * outside the borders of the texture will render the nearest border texel.
     * Otherwise UV coordinates outside the textures borders will wrap around to
     * the other side of the texture.
     * @param generateMips If true mip-maps will be generated for this texture.
     *
     * @return The requested texture.
     *
     * @see GeneralDensity
     * @see <a href="https://developer.android.com/guide/practices/screens_support.html">https://developer.android.com/guide/practices/screens_support.html</a>
     *
     * @author Adam T. Ryder
     */
    public Texture loadTextureDP(String path, boolean repeat, boolean generateMips) {
//        if (!isMobile)
//            return GuiGlobals.getInstance().loadTexture(path, repeat, generateMips);

        int slash = path.lastIndexOf("/");
        StringBuilder dpPath = new StringBuilder("");
        String fileName = path;
        if (slash >= 0) {
            if (slash == path.length() - 1)
                throw new RuntimeException("Error loading texture:" + path);

            fileName = path.substring(slash + 1);
            dpPath.append(path.substring(0, slash + 1));
        }

        GeneralDensity gdpi = generalDensity;
        int sbl = dpPath.length();
        do {
            dpPath.append(gdpi.toString());
            dpPath.append("/");
            dpPath.append(fileName);

            TextureKey key = new TextureKey(dpPath.toString());
            key.setGenerateMips(generateMips);
            Texture t;
            try {
                t = assets.loadTexture(key);
            } catch (AssetNotFoundException e) {
                t = null;
            }

            if (t != null) {
                if (repeat) {
                    t.setWrap(Texture.WrapMode.Repeat);
                } else
                    t.setWrap(Texture.WrapMode.EdgeClamp);

                return t;
            }

            gdpi = gdpi.previous();
            dpPath.delete(sbl, dpPath.length());
        } while (gdpi != null);

        dpPath.append(fileName);
        TextureKey key = new TextureKey(dpPath.toString());
        key.setGenerateMips(generateMips);
        Texture t;
        try {
            t = assets.loadTexture(key);
        } catch (AssetNotFoundException e) {
            t = null;
        }

        if (t != null) {
            if (repeat) {
                t.setWrap(Texture.WrapMode.Repeat);
            } else
                t.setWrap(Texture.WrapMode.EdgeClamp);

            return t;
        }

        dpPath.delete(sbl, dpPath.length());
        gdpi = generalDensity.next();
        while (gdpi != null) {
            dpPath.append(gdpi.toString());
            dpPath.append("/");
            dpPath.append(fileName);

            key = new TextureKey(dpPath.toString());
            key.setGenerateMips(generateMips);
            t = assets.loadTexture(key);

            if (t != null) {
                if (repeat) {
                    t.setWrap(Texture.WrapMode.Repeat);
                } else
                    t.setWrap(Texture.WrapMode.EdgeClamp);

                return t;
            }

            gdpi = gdpi.next();
            dpPath.delete(sbl, dpPath.length());
        }

        throw new RuntimeException("Error loading texture:" + path);
    }
}
