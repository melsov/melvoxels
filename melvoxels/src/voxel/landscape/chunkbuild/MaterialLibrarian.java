package voxel.landscape.chunkbuild;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;

import java.util.HashMap;

/**
 * Created by didyouloseyourdog on 8/13/14.
 */
public class MaterialLibrarian
{
    private Material blockMaterial;
    private Material blockMaterialAnimated;
    private Material blockMaterialTranslucentAnimated;
    private HashMap<ColorRGBA, Material> wireFrameMaterials = new HashMap<ColorRGBA, Material>(12);
    private HashMap<ColorRGBA, Material> solidMaterials = new HashMap<ColorRGBA, Material>(12);
    private Material vertexColorMaterial;

    private AssetManager assetManager;

    public MaterialLibrarian(AssetManager _assetManager) {
        assetManager = _assetManager;
    }

    public Material getBlockMaterial() {
        if (blockMaterial == null) blockMaterial = makeTexMapMaterial();
        return blockMaterial;
    }
    public Material getBlockMaterialAnimated() {
        if (blockMaterialAnimated == null) blockMaterialAnimated = makeTexMapMaterial();
        return blockMaterialAnimated;
    }
    public Material getBlockMaterialTranslucentAnimated() {
        if (blockMaterialTranslucentAnimated == null) blockMaterialTranslucentAnimated = makeTranslucentAnimatedTexMapMaterial();
        return blockMaterialTranslucentAnimated;
    }
    public Material getVertexColorMaterial() {
        if (vertexColorMaterial == null) vertexColorMaterial = wireFrameMaterialVertexColor();
        return vertexColorMaterial;
    }

    private Material makeTexMapMaterial() {
        Material mat = new Material(assetManager, "MatDefs/BlockTexNuevo.j3md");
        Texture blockTex = assetManager.loadTexture("Textures/dog_64d_.jpg");
//        Texture blockTex = assetManager.loadTexture("Textures/dog_4096d.jpg");
        blockTex.setMagFilter(Texture.MagFilter.Nearest);
        blockTex.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("ColorMap", blockTex);
        return mat;
    }
    private Material makeTranslucentAnimatedTexMapMaterial() {
        Material mat = new Material(assetManager, "MatDefs/BlockTexTranslucentAnimated.j3md");
        Texture blockTex = assetManager.loadTexture("Textures/dog_64d_.png");
        blockTex.setMagFilter(Texture.MagFilter.Nearest);
        blockTex.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("ColorMap", blockTex);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
//        mat.getAdditionalRenderState().setDepthTest(false);
        return mat;
    }
    public Material wireFrameMaterialWithColor(ColorRGBA color) {
        Material wireMaterial = wireFrameMaterials.get(color);
        if (wireMaterial != null) {
            return wireMaterial;
        }

        wireMaterial = new Material(assetManager, "/Common/MatDefs/Misc/Unshaded.j3md");
        wireMaterial.setColor("Color", color);
        wireMaterial.getAdditionalRenderState().setWireframe(true);
        wireFrameMaterials.put(color, wireMaterial);
        return wireMaterial;
    }
    public Material solidMaterialWithColor(ColorRGBA color) {
        Material solidMaterial = solidMaterials.get(color);
        if (solidMaterial != null) {
            return solidMaterial;
        }
        solidMaterial = new Material(assetManager, "/Common/MatDefs/Misc/Unshaded.j3md");
        solidMaterial.setColor("Color", color);
        solidMaterials.put(color, solidMaterial);
        return solidMaterial;
    }
    private Material wireFrameMaterialVertexColor() {
        Material wireMaterial = new Material(assetManager, "/Common/MatDefs/Misc/Unshaded.j3md");
        wireMaterial.setBoolean("VertexColor", true);
        wireMaterial.getAdditionalRenderState().setWireframe(true);
        return wireMaterial;
    }
}
