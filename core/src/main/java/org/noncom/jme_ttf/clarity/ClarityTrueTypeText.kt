package org.noncom.jme_ttf.clarity


import com.atr.jme.font.TrueTypeBMP
import com.atr.jme.font.TrueTypeMesh
import com.atr.jme.font.shape.TrueTypeContainer
import com.atr.jme.font.util.AtlasListener
import com.atr.jme.font.util.StringContainer
import com.jme3.app.Application
import com.jme3.font.Rectangle
import com.jme3.material.Material
import com.jme3.material.RenderState
import com.jme3.math.ColorRGBA
import com.jme3.renderer.queue.RenderQueue
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.VertexBuffer
import com.jme3.scene.shape.Box

/** Heavily based on Adam Tryders true type text component for Lemur */

class ClarityTrueTypeText(val stringContainer: StringContainer, font: TrueTypeBMP<*>, material: Material): Node() {

    constructor(text: String, font: TrueTypeBMP<*>, material: Material): this(StringContainer(font, text), font, material)

    constructor(text: String, font: TrueTypeBMP<*>) : this(StringContainer(font, text), font, getDefaultBMPMaterial(font))

    companion object {

        val defaultColor = ColorRGBA(1f, 1f, 1f, 1f)
        val defaultOutline = ColorRGBA(0f, 0f, 0f, 1f)

        var fontDebugGeometry: Geometry? = null

        /** each font has an associated cached material, so that each new ClarityClearTypeText would get the same material to prevent
         * data leaking and disposal of the existing material texture as defined in update of the font */
        val fontToMaterial = mutableMapOf<String, Material>()

        fun getDefaultBMPMaterial(font: TrueTypeBMP<*>): Material {
            return getDefaultBMPMaterial(font, defaultColor, defaultOutline)
        }

        fun getDefaultBMPMaterial(font: TrueTypeBMP<*>, color: ColorRGBA, outline: ColorRGBA): Material {
            val key = "$font-$color-$outline"
            if(fontToMaterial.containsKey(key)) {
                return fontToMaterial[key]!!
            }
//            return Material(AClarity.getInstance().assetManager, "Common/MatDefs/Misc/Unshaded.j3md").apply {
//                additionalRenderState.isWireframe = true
//            }
            val material = if (font.outline > 0) {
                Material(AClarity.getInstance().assetManager, "Common/MatDefs/TTF/TTF_BitmapOutlined.j3md").apply {
                    setColor("Outline", outline)
                }
            } else {
                Material(AClarity.getInstance().assetManager, "Common/MatDefs/TTF/TTF_Bitmap.j3md")
            }.apply {
                setColor("Color", color)
                //println("SETTING TEXTURE")
                //setTexture("Texture", font.atlas)
                additionalRenderState.blendMode = RenderState.BlendMode.Alpha
            }
            fontToMaterial[key] = material
            return material
        }

        fun getDefaultMeshMaterial(font: TrueTypeMesh, color: ColorRGBA): Material {
            return Material(AClarity.getInstance().assetManager, "Common/MatDefs/TTF/TTF.j3md").apply {
                setColor("Color", color)
                setBoolean("useAA", AClarity.getInstance().isSupportDerivatives && font.isAA)
            }
        }

        /** To help debug the font texture you might want to visualize the font on a cube,
         * pass your app instance a node to link the cube to */
        fun initFontDebuggingGeometry(app: Application, node: Node) {
            val mesh = Box(20f, 20f, 20f)
            fontDebugGeometry = Geometry("A", mesh)
            fontDebugGeometry?.material = Material(app.assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
            node.attachChild(fontDebugGeometry)
            fontDebugGeometry?.setLocalTranslation(0f, 50f, 0f)
        }
    }

    var vertexColor = ColorRGBA(1f, 1f, 1f, 1f)

    var oldAtlasWidth = 0
    var oldAtlasHeight = 0

    var listener: AtlasListener? = null
    var fontScale = 0f

    var maxLines = 1
        set(value) {
            when {
                value >= 0 -> {
                    field = value
                    stringContainer.setMaxLines(value)
                    maxHeight = stringContainer.textBox.height
                }
                else -> maxHeight = 0f
            }
        }

    var maxHeight = 1f
        set(value) {
            field = value
            invalidate()
        }

    var font = font
        set(value) {
            if(listener != null) {
                (font as TrueTypeBMP).removeAtlasListener(listener)
                (value as TrueTypeBMP).addAtlasListener(listener)
            }
            field = value
            fontScale = font.scale
            /** possible kerning correction */
            fixKerning()
        }

    var textBox = Rectangle(0f, 0f, 0f, 0f,)

    fun setTextBox(x: Float, y: Float, width: Float, height: Float) {
        textBox = Rectangle(x, y, width, height)
        update()
    }

    val material = material

    var ttc: TrueTypeContainer? = null

    var isInitialized = false

    init {
        update()
        isInitialized = true
        /** this has to happen here in init, after the initial update(), otherwise if kept in update() it gives a weird comodification exception */
        installAtlasListener()
    }

    fun update() {
        /** possible kerning correction */
        fixKerning()
        fontScale = font.scale
        stringContainer.textBox = textBox//Rectangle(0f, 0f, AGame.current.getScreenWidth().toFloat(), AGame.current.getScreenHeight().toFloat())
        if(ttc != null) {
            ttc!!.removeFromParent()
            ttc = null
        }
        ttc = font.getFormattedText(stringContainer, material)
        ttc!!.queueBucket = RenderQueue.Bucket.Transparent
        attachChild(ttc)
        oldAtlasWidth = font.atlas.image.width
        oldAtlasHeight = font.atlas.image.height
        //installAtlasListener() /** disabled: see the comment in the init{} section */
        /** !!! NEED TO CALL THIS ON EVERY ATLAS UPDATE BECAUSE THE PREVIOUS
         *  TEXTURE GETS DESTROYED AND WILL LOOK CORRUPTED / CRASH JVM */
        ttc!!.material.setTexture("Texture", font.atlas)
        ttc!!.updateGeometry()

        /** the font debugging geometry can be enabled in AGame */
        fontDebugGeometry?.material?.setTexture("ColorMap", font.atlas)

        /** re-apply the vertex color */
        setColor(vertexColor)
    }

    fun setColor(color: ColorRGBA) {
        vertexColor = color
        getGeometries().forEach { geometry -> setVertexColor(geometry, color)}
    }

    fun getGeometries() : List<Geometry> {
        return ttc!!.geometries
    }

    private fun fixKerning() {
        if (!AClarity.getInstance().isMobile && font is TrueTypeBMP) {
            when {
                font.scaledPointSize < 17 -> stringContainer.kerning = 2
                font.scaledPointSize < 23 -> stringContainer.kerning = 1
                else -> stringContainer.kerning = 0
            }
        }
    }

    fun getKerning(): Int {
        return stringContainer.kerning
    }

    fun invalidate() {
        /** this function currently does nothing but it could serve to mark that the objects
         * needs to be regenerated */
        if(isInitialized) { update() }
    }

    fun setText(text: String) {
        if (text != stringContainer.text) {
            stringContainer.text = text
            update()
            invalidate()
        }
    }

    fun getText(): String = stringContainer.text

    fun setHAlignment(halign: StringContainer.Align) {
        stringContainer.alignment = halign
        invalidate()
    }

    fun setHAlignment(halign: String) {
        when(halign) {
            "right" -> setHAlignment(StringContainer.Align.Right)
            "left" -> setHAlignment(StringContainer.Align.Left)
            "center" -> setHAlignment(StringContainer.Align.Center)
        }
    }

    fun setVAlignment(valign: StringContainer.VAlign) {
        stringContainer.verticalAlignment = valign
        invalidate()
    }

    fun setWrapMode(mode: String) {
        when(mode) {
            "no-wrap" -> setWrapMode(StringContainer.WrapMode.NoWrap)
            "char" -> setWrapMode(StringContainer.WrapMode.Char)
            "word" -> setWrapMode(StringContainer.WrapMode.Word)
            "char-clip" -> setWrapMode(StringContainer.WrapMode.CharClip)
            "word-clip" -> setWrapMode(StringContainer.WrapMode.WordClip)
            "clip" -> setWrapMode(StringContainer.WrapMode.Clip)
        }
    }

    fun setKerning(kerning: Int) {
        stringContainer.kerning = kerning
        invalidate()
    }

    fun setWrapMode(wrapMode: StringContainer.WrapMode) {
        stringContainer.wrapMode = wrapMode
        invalidate()
    }

    fun installAtlasListener() {
        font.addAtlasListener { assetManager, oldWidth, oldHeight, newWidth, newHeight, font ->
            //println("---> ATLAS LISTENER [A]")
            if(oldWidth != newWidth || oldHeight != newHeight) {
                //println("---> ATLAS LISTENER [B]")
                font.scale = fontScale
//                ttc!!.updateGeometry() // <- NPE COZ TTC == null here coz it is = null in update()
                invalidate()
            }
//            ttc!!.material.setTexture("Texture", font.atlas) // <- NPE COZ TTC == null here coz it is = null in update()
            oldAtlasWidth = newWidth
            oldAtlasHeight = newHeight
        }
    }

    fun getTextWidth() = ttc?.textWidth ?: 0
    fun getTextHeight() = ttc?.textHeight ?: 0
    fun getBoxWidth() = ttc?.width ?: 0
    fun getBoxHeight() = ttc?.height ?: 0

    fun setVertexColor(geometry: Geometry, color: ColorRGBA) {

        geometry.mesh.setDynamic()

        //println("mat def = ${geometry.material.materialDef.assetName}, ${geometry.material.materialDef.name}")
        //println("params: ")
        //geometry.material.params.forEach { param ->
        //    println("${param.name} -> ${param.value}")
        //}

        geometry.material.setBoolean("VertexColor", true)

        val mesh = geometry.mesh
        //val mesh = args.arg(1).fromLua(Mesh::class.java)

        val colors = FloatArray(mesh.vertexCount * 4) { i ->
            //println("SETTING VERTEX $i color to $color")
            when (i % 4) {
                0 -> color.r
                1 -> color.g
                2 -> color.b
                3 -> color.a
                else -> throw IllegalStateException("This cannot happen")
            }
        }

        mesh.setBuffer(VertexBuffer.Type.Color, 4, colors)

        geometry.material.setBoolean("VertexColor", true)
    }
}