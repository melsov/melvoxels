package voxel.landscape.debug.debugutil;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import voxel.landscape.player.B;

/**
 * Created by didyouloseyourdog on 7/27/14.
 */
public class GUIInfo extends AbstractControl {

    private Node guiNode;
    private BitmapFont guiFont;
    private BitmapText hudText;
    private float count=0;
    private int timeDebug = 0;
    private static final float PAUSE = 1f;

    public GUIInfo(Node _guiNode, BitmapFont _guiFont) {
        guiNode = _guiNode;
        guiFont = _guiFont;

        hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize());
        hudText.setColor(ColorRGBA.White);
        guiNode.attachChild(hudText);
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (count > PAUSE) {
            memoryInfo();
            count = 0;
        }
        count += tpf;

    }
    private void memoryInfo() {
        AddInfo(MemoryStats.MemoryInfo());
    }
    private void AddInfo(String info)
    {
        if(timeDebug++ % 10 == 0) B.bugln(info);
        hudText.setText(info);             // the text
        hudText.setLocalTranslation(300, hudText.getLineHeight() + 50, 0); // position
    }

    @Override
    protected void controlRender(RenderManager renderManager, ViewPort viewPort) {

    }
}
