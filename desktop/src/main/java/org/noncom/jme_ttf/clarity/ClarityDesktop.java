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
import com.jme3.asset.TextureKey;
import com.jme3.renderer.Caps;
import com.jme3.texture.Texture;

/**
 *  A utility class that sets up some default global behavior for
 *  the default GUI elements and provides some common access to
 *  things like the AssetManager.
 *
 *  <p>When initialized, AClarity will keep a reference to the
 *  AssetManager for use in creating materials, loading fonts, and so
 *  on.  It will also:
 *  <ul>
 *  <li>Setup the KeyInterceptState for allowing edit fields to intercept
 *      key events ahead of the regular input processing.</li>
 *  <li>Initialize InputMapper to provide advanced controller input processing.</li>
 *  <li>Setup the MouseAppState to provide default mouse listener and picking
 *      support for registered pick roots.</li>
 *  <li>Setup the FocusManagerState that keeps track of the currently
 *      focused component and makes sure transition methods are properly called.</li>
 *  <li>Setup the default styles.</li>
 *  <li>Sets up the layer based geometry comparators for the default app
 *      viewport.</li>
 *  </ul>
 *
 *  <p>For applications that wish to customize the behavior of AClarity,
 *  it is possible to set a custom subclass instead of initializing the
 *  default implementation.  Examples of reasons do do this might include
 *  using custom materials instead of the default JME materials or otherwise
 *  customizing the initialization setup.</p>
 *
 *  @author    Paul Speed
 */
public class ClarityDesktop extends AClarity {

    @Override
    protected void initPlatform(Application app) {
        //We're not running on Android so everything should be
        //easy going from here on out :)
        isMobile = false;
        isAndroid = false;
        dpiMod = 1;
        //dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        dpi = 72;
        relDpiMod = 1;
        derivativeSupported = app.getRenderer().getCaps().contains(Caps.GLSL110);
        actualDerivativeSupported = derivativeSupported;
        String os = System.getProperty("os.name");
        System.out.println("LemurDynamo Running on " + os + "\n"
                + "Derivatives: " + Boolean.toString(derivativeSupported)
                + "\nDPI: " + Float.toString(dpi));
    }

    protected ClarityDesktop(Application app) {
        super(app);
    }

    /**
     * The default implementation for Desktop simply loads the texture as usual
     * @param path
     * @param repeat
     * @param generateMips
     * @return
     */
    @Override
    public Texture loadTextureDP(String path, boolean repeat, boolean generateMips) {
        return loadTexture(path, repeat, generateMips);
    }

    public Texture loadTexture( String path, boolean repeat, boolean generateMips ) {
        TextureKey key = new TextureKey(path);
        key.setGenerateMips(generateMips);

        Texture t = assets.loadTexture(key);
        if( t == null ) {
            throw new RuntimeException("Error loading texture:" + path);
        }

        if( repeat ) {
            t.setWrap(Texture.WrapMode.Repeat);
        } else {
            // JME has deprecated Clamp and defaults to EdgeClamp.
            // I think the WrapMode.EdgeClamp javadoc is totally bonkers, though.
            t.setWrap(Texture.WrapMode.EdgeClamp);
        }

        return t;
    }
}
