package voxel.landscape;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import com.jme3.ui.Picture;
import com.jme3.util.BufferUtils;
import com.jme3.util.SkyFactory;
import voxel.landscape.chunkbuild.MaterialLibrarian;
import voxel.landscape.collection.ColumnMap;
import voxel.landscape.coord.Axis;
import voxel.landscape.coord.Coord2;
import voxel.landscape.coord.Coord3;
import voxel.landscape.coord.Direction;
import voxel.landscape.debug.DebugGeometry;
import voxel.landscape.debug.debugmesh.DebugChart;
import voxel.landscape.debug.debugmesh.DebugChart.DebugShapeType;
import voxel.landscape.debug.debugmesh.IDebugGet3D;
import voxel.landscape.debug.debugutil.GUIInfo;
import voxel.landscape.jmonrenderutil.WireProcessor;
import voxel.landscape.map.TerrainMap;
import voxel.landscape.player.Audio;
import voxel.landscape.player.B;
import voxel.landscape.player.Player;
import voxel.landscape.settings.WorldSettings;

import java.awt.*;
import java.awt.image.BufferedImage;


// TODO: Separate world builder and game logic, everything else...
public class VoxelLandscape extends SimpleApplication
{
    public static final WorldSettings WorldSettings = new WorldSettings(-21234);
    public static boolean FULL_SCREEN = false;

	public static boolean USE_TEXTURE_MAP = false, DEBUG_INFO_ON = false, ADD_CHUNKS_DYNAMICALLY = true, COMPILE_CHUNK_DATA_ASYNC = false,
            CULLING_ON = false, BUILD_INITIAL_CHUNKS = true, DONT_BUILD_CHUNK_MESHES = true, SHOW_COLUMN_DEBUG_QUADS = false, FORCE_WIRE_FRAME = false,
            BUILD_STRUCTURES = true, DO_USE_TEST_GEOMETRY = true, SHOULD_BUILD_CHUNK_MESH_ASYNC = true, READ_CHUNKS_FROM_FILE = false;
    public static boolean TEST_BLOCK_FACE_MESH_BUILDING = true;

    private WorldGenerator worldGenerator;
	private TerrainMap terrainMap;
	private ColumnMap columnMap = new ColumnMap();
    public ColumnMap getColumnMap() { return columnMap; }
	private Player player;

    private Material debugColumnMat;
	private Node worldNode = new Node("world_node");
	private Node overlayNode = new Node("overlay_node");

    private static float GameTime = 0f;
	private static Coord2 screenDims;
    private WireProcessor wireProcessor;
    
    public static boolean TESTING_DEBUGGING_ON = true;
    private static void setupTestStateVariables()
    {
        if (TESTING_DEBUGGING_ON) {
        	USE_TEXTURE_MAP = true;
            DEBUG_INFO_ON = false;
            ADD_CHUNKS_DYNAMICALLY = true;
            COMPILE_CHUNK_DATA_ASYNC = true;
            DO_USE_TEST_GEOMETRY = false;
            CULLING_ON = false;
            BUILD_INITIAL_CHUNKS = false;
            DONT_BUILD_CHUNK_MESHES = false;
            SHOW_COLUMN_DEBUG_QUADS = false;
            TEST_BLOCK_FACE_MESH_BUILDING = true;
            FORCE_WIRE_FRAME = true;
            BUILD_STRUCTURES = true;
            READ_CHUNKS_FROM_FILE = false;
        } else {
            USE_TEXTURE_MAP = true;
            DEBUG_INFO_ON = false;
            ADD_CHUNKS_DYNAMICALLY = true;
            COMPILE_CHUNK_DATA_ASYNC = true;
            DO_USE_TEST_GEOMETRY = false;
            CULLING_ON = false;
            BUILD_INITIAL_CHUNKS = false;
            DONT_BUILD_CHUNK_MESHES = false;
            SHOW_COLUMN_DEBUG_QUADS = false;
            TEST_BLOCK_FACE_MESH_BUILDING = true;
            FORCE_WIRE_FRAME = true;
            BUILD_STRUCTURES = true;
            READ_CHUNKS_FROM_FILE = false;
        }
    }

    public WorldGenerator getWorldGenerator() { return worldGenerator; }

    public Coord2 getScreenDims() { return screenDims; }


	/* ***************************
	 * *** Main Update loop ******
	   ****************************/
    @Override
    public void simpleUpdate(float tpf) {
        GameTime += tpf;
        terrainMap.mapUpdate(tpf);
        if (!ADD_CHUNKS_DYNAMICALLY) return;
        if (!COMPILE_CHUNK_DATA_ASYNC) return;

        worldGenerator.update(tpf);
        DebugGeometry.Update(tpf);
    }

    //<editor-fold desc="Simple Init and main">
    /*******************************************
     * Do initialization related stuff here ****
     *******************************************/
    @Override
    public void simpleInitApp() 
    {
        terrainMap = new TerrainMap(this);
        BufferUtils.setTrackDirectMemoryEnabled(true);
        setupTestStateVariables();
        if (FORCE_WIRE_FRAME) {
            wireProcessor = new WireProcessor(assetManager);
            viewPort.addProcessor(wireProcessor);
        }

        rootNode.attachChild(worldNode);

        worldGenerator = new WorldGenerator(worldNode, cam, terrainMap, columnMap, assetManager);

        inputManager.setCursorVisible(false);

    	flyCam.setEnabled(false);

    	rootNode.attachChild(overlayNode);

        // consider: detaching cam from root node
        // attach to a 'gameNode' (child of rootNode)
        // thus, separate game view from debug view
        player = new Player(terrainMap, cam, new Audio(assetManager, rootNode), this, overlayNode, rootNode);
        rootNode.attachChild(player.getPlayerNode());

        attachCoordinateGrid(1,1);

        setupSkyTexture();
        setupInfoView();
        setupPlayerDebugHat();

        setupInputs();
        initDebugGeometry();

    	initCrossHairs();

        setDisplayFps(true);
        setDisplayStatView(true);

        guiNode.addControl(new GUIInfo(guiNode, assetManager.loadFont("Interface/Fonts/Console.fnt")));
    }

	/*******************************
	 * Program starts here... ******
     *******************************/
    public static void main(String[] args) {
        VoxelLandscape app = new VoxelLandscape();
        ScreenSettings(app, FULL_SCREEN); //<--- call new method here
        app.start(); // start the game
    }
    private static void ScreenSettings(VoxelLandscape app, boolean fullScreen) {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode[] modes = device.getDisplayModes();
        int max = 0;
        int SCREEN_MODE=0; // note: there are usually several, let's pick the first
        //find max dims
        for (int i=0;i<modes.length;i++){
        	DisplayMode mode = modes[i];
        	if (mode.getWidth() * mode.getHeight() > max) {
        		max = mode.getWidth() * mode.getHeight();
        		SCREEN_MODE = i; 
        	}
        }
        AppSettings settings = new AppSettings(true);
        float scale_screen = fullScreen ? 1f : .5f;
        screenDims = new Coord2((int)(modes[SCREEN_MODE].getWidth() * scale_screen ),(int)(modes[SCREEN_MODE].getHeight() * scale_screen ));
        settings.setResolution(screenDims.x, screenDims.y);
        settings.setFrequency(modes[SCREEN_MODE].getRefreshRate());
        settings.setBitsPerPixel(modes[SCREEN_MODE].getBitDepth());
        settings.setFrameRate(60);
        settings.setVSync(true);
        settings.setDepthBits(16);
        settings.setSamples(0);
        settings.setRenderer(AppSettings.LWJGL_OPENGL2);
        if (fullScreen) {
            settings.setFullscreen(device.isFullScreenSupported());
        }
        app.setSettings(settings);
        app.setShowSettings(false);
    }
    @Override
    public void destroy() {
        super.destroy();
        worldGenerator.killThreadPools();
    }
    //</editor-fold>

    //<editor-fold desc="INPUT AND SET-UP METHODS">
    /*
     * Mouse inputs
     */
    private void setupInputs() {
        inputManager.addMapping("Break", new KeyTrigger(KeyInput.KEY_T), new MouseButtonTrigger(MouseInput.BUTTON_LEFT) );
        inputManager.addMapping("Place", new KeyTrigger(KeyInput.KEY_G), new MouseButtonTrigger(MouseInput.BUTTON_RIGHT) );
        inputManager.addMapping("GoHome", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping("UpArrow", new KeyTrigger(keyInput.KEY_UP));
        inputManager.addMapping("DownArrow", new KeyTrigger(keyInput.KEY_DOWN));
        inputManager.addMapping("RightArrow", new KeyTrigger(keyInput.KEY_RIGHT));
        inputManager.addMapping("LeftArrow", new KeyTrigger(keyInput.KEY_LEFT));
        inputManager.addMapping("Inventory", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping("ToggleInfoView", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("ToggleInfoViewDistance", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addMapping("DebugBlock", new KeyTrigger(KeyInput.KEY_B));
        inputManager.addMapping("DebugChunk", new KeyTrigger(KeyInput.KEY_C));
        inputManager.addListener(player.getUserInputListener(), 
        		"Break", "Place", "GoHome", "Up", "Down", "Right", "Left",
                "UpArrow", "DownArrow", "RightArrow", "LeftArrow", "Inventory", 
                "ToggleInfoView", "ToggleInfoViewDistance", "DebugBlock", "DebugChunk");

        inputManager.addMapping("moveForward",  new KeyTrigger(keyInput.KEY_W));
        inputManager.addMapping("moveBackward",  new KeyTrigger(keyInput.KEY_S));
        inputManager.addMapping("moveRight",  new KeyTrigger(keyInput.KEY_D));
        inputManager.addMapping("moveLeft",  new KeyTrigger(keyInput.KEY_A));
        inputManager.addMapping("moveUp",  new KeyTrigger(keyInput.KEY_Q));
        inputManager.addMapping("moveDown",  new KeyTrigger(keyInput.KEY_Z));
        inputManager.addMapping("jump",  new KeyTrigger(keyInput.KEY_SPACE));
//        inputManager.addMapping("lmb", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
//        inputManager.addMapping("rmb", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addListener(player.getAnalogListener(),
                "moveForward", "moveBackward", "moveRight", "moveLeft", "moveDown", "moveUp", "jump",
                "lmb", "rmb");
        
        // To detect key releases also give WASD QE to the input listener
        inputManager.addListener(player.getUserInputListener(), "moveForward", "moveBackward", "moveRight", "moveLeft", "moveDown", "moveUp", "jump",
                "lmb", "rmb");
    }

    private ActionListener utilityInputListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("WireFrame") && !keyPressed) {
                if (wireProcessor != null) {
                    wireProcessor.isWireFrameOn = !wireProcessor.isWireFrameOn;
                }
            }
        }
    };

    private void initDebugGeometry() {
        rootNode.attachChild(DebugGeometry.debugNode);
        DebugGeometry.debugNode.attachChild(DebugGeometry.addChunkNode);
        DebugGeometry.debugNode.attachChild(DebugGeometry.removeChunkNode);

        DebugGeometry.materialLibrarian = new MaterialLibrarian(assetManager);

        inputManager.addMapping("WireFrame", new KeyTrigger(KeyInput.KEY_V) );
        inputManager.addListener(utilityInputListener, "WireFrame");
    }

    /** A centred plus sign to help the player aim. */
    private void initCrossHairs() {
        setDisplayStatView(false);
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
                settings.getWidth() / 2 - ch.getLineWidth()/2, settings.getHeight() / 2 + ch.getLineHeight()/2, 0);
        guiNode.attachChild(ch);
    }

    //</editor-fold>

    //<editor-fold desc="DEBUG AND HELPER METHODS">
    //region Debug and helper
    private void attachCoordinateGrid(int maxX, int maxZ) {
        for (int x = 0; x < maxX; x++) {
            for (int z = 0; z < maxZ; ++z) {
                attachCoordinateAxes(new Vector3f(x * Chunk.XLENGTH, 0, z * Chunk.ZLENGTH));
            }
        }
    }

    private void attachCoordinateAxes(Vector3f pos){
        B.bugln("X AXIS = RED. Z AXIS = BLUE");
        Arrow arrow = new Arrow(Vector3f.UNIT_X.clone().mult(16f));
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Red).setLocalTranslation(pos);

        Vector3f yArrowV = Vector3f.UNIT_Y.clone().mult(16f);
        arrow = new Arrow(yArrowV);
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Green).setLocalTranslation(pos);

        arrow = new Arrow(yArrowV);
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Yellow).setLocalTranslation(pos.add(yArrowV));

        arrow = new Arrow(yArrowV);
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Pink).setLocalTranslation(pos.add(yArrowV.mult(2f)));

        arrow = new Arrow(yArrowV);
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Magenta).setLocalTranslation(pos.add(yArrowV.mult(3f)));

        arrow = new Arrow(Vector3f.UNIT_Z.clone().mult(16f));
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Blue).setLocalTranslation(pos);
    }
    private Geometry putShape(Mesh shape, ColorRGBA color){
        Geometry g = new Geometry("coordinate axis", shape);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", color);
        g.setMaterial(mat);
        rootNode.attachChild(g);
        return g;
    }
/*
     * Debug and helper methods
     */
	private void addDebugGeometry()
	{
		Coord3 min = Coord3.Zero;
		Coord3 max = Coord3.One;
		
		DebugChart debugChart = new DebugChart(min, max);
//		Geometry terrainHeights = debugChart.makeHeightMapVertsUVs(DebugShapeType.QUAD, 0f, wireFrameMaterialWithColor(ColorRGBA.Red), new IDebugGet2D() {
//			public float getAValue(int x, int z) {
//				return terrainMap.GetMaxY(x, z);
//			}
//		});
//		debugNode.attachChild(terrainHeights);
//		Geometry terrainSunHeight = debugChart.makeHeightMapVertsUVs(DebugShapeType.QUAD, 0f, wireFrameMaterialWithColor(ColorRGBA.Yellow), new IDebugGet2D() {
//			public float getAValue(int x, int z) {
//				return terrainMap.GetSunLightmap().GetSunHeight(x, z);
//			}
//		});
//		debugNode.attachChild(terrainSunHeight);
		Geometry terrainLight = debugChart.makeTerrainInfoVertsUVs3D(DebugShapeType.QUAD, 0f, wireFrameMaterialVertexColor(), new IDebugGet3D() {
			public float getAValue(int x, int y, int z) {
				return terrainMap.GetSunLightmap().GetLight(x, y, z);
			}
		});
		rootNode.attachChild(terrainLight); // make a 'debug node?'
	}
    private Vector3f fakeCamLocation() {
        float theta = FastMath.PI * (GameTime/2f);
        float radius = (GameTime/.25f);
        return new Vector3f(radius * FastMath.cos(theta), 0, radius * FastMath.sin(theta));
    }

//    public Material makeTexMapMaterial() {
//        return materialLibrarian.getBlockMaterial();
//    }

	public Material wireFrameMaterialWithColor(ColorRGBA color) {
        return worldGenerator.materialLibrarian.wireFrameMaterialWithColor(color);
	}

	private Material wireFrameMaterialVertexColor() {
        return worldGenerator.materialLibrarian.getVertexColorMaterial();
	}


    private void setupSkyTexture() {
        Texture2D skyTex = TexFromBufferedImage(OnePixelBufferedImage(new Color(.4f,.8f,1f,1f)));
        rootNode.attachChild(SkyFactory.createSky(assetManager, skyTex, true));
    }
    private static BufferedImage OnePixelBufferedImage(Color color) {
        BufferedImage image = new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0 ; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight() ; ++y) {
                image.setRGB(x, y, color.getRGB() );
            }
        }
        return image;
    }
	private static Texture2D TexFromBufferedImage(BufferedImage bim) {
		AWTLoader awtl = new AWTLoader();
		Image im = awtl.load(bim, false);
		return new Texture2D(im);
	}

	private void showImageOnScreen(BufferedImage bim) {
		Texture2D tex = TexFromBufferedImage(bim);
		tex.setMagFilter(Texture.MagFilter.Nearest);
		tex.setWrap(Texture.WrapMode.Repeat);
    	
    	Picture pic = new Picture("Pic from BufferedImage");
    	pic.setTexture(assetManager, tex, false);
    	pic.setWidth(200);
    	pic.setHeight(200);
    	pic.setPosition(0f, 100f);
    	guiNode.attachChild(pic);
	}
    private void setupPlayerDebugHat() {
        float hatZLength = 20f;
        Mesh hat = new Box(new Vector3f(-.25f,-.25f, 0), new Vector3f(.25f, .25f, hatZLength ));
        Geometry g = new Geometry("hat", hat);
        g.setMaterial(wireFrameMaterialWithColor(ColorRGBA.Red));
        player.getHeadNode().attachChild(g);
        g.setLocalTranslation(0,20f,0);
    }
    private void setupZPositiveMarker() {
        float markerLength = 20f;
        Mesh hat = new Box(new Vector3f(-.25f,-.25f, 0), new Vector3f(.25f, .25f, markerLength ));
        Geometry g = new Geometry("zpos_marker", hat);
        g.setMaterial(wireFrameMaterialWithColor(ColorRGBA.Blue));
        rootNode.attachChild(g);
        g.setLocalTranslation(0,100f,0);
    }
    private void setupXPositiveMarker() {
        float markerLength = 20f;
        Mesh hat = new Box(new Vector3f(0,-.25f,-.25f), new Vector3f(markerLength, .25f, .25f));
        Geometry g = new Geometry("xpos_marker", hat);
        g.setMaterial(wireFrameMaterialWithColor(ColorRGBA.Orange));
        rootNode.attachChild(g);
        g.setLocalTranslation(0,100f,0);
    }
    private void setupInfoView() {
        Camera cam2 = cam.clone();
        cam2.setViewPort(.6f, 1f, 0f, .4f);

        CameraNode camNode = new CameraNode("cam2_node", cam2);
        player.getPlayerNode().attachChild(camNode);

        setInfoCamPosition(); //offset from player

        ViewPort viewPort2 = renderManager.createMainView("Info_view_port", cam2);
        viewPort2.setClearFlags(true, true, true);
        viewPort2.setBackgroundColor(ColorRGBA.Black);
        viewPort2.attachScene(rootNode);
    }
    private static int INFO_AXIS = Axis.Y;
    public void toggleInfoViewAxis() {
        INFO_AXIS = Axis.NextAxis(INFO_AXIS);
        setInfoCamPosition();
    }
    private static float[] infoDistances = new float[] { 6f, 12f, 120f, 300f };
    private static int infoDistanceIndex = 0;
    public void toggleInfoViewDistance() {
        infoDistanceIndex = (++infoDistanceIndex) % infoDistances.length;
        setInfoCamPosition();
    }
    private void setInfoCamPosition() {
        float distance = infoDistances[infoDistanceIndex];
        Vector3f ploc = player.getPlayerNode().getLocalTranslation();
        CameraNode infoCamNode =  (CameraNode) player.getPlayerNode().getChild("cam2_node");
        float halfHeight = 1f;
        if (INFO_AXIS == Axis.X) {
            infoCamNode.setLocalTranslation(0, distance, 0);
            infoCamNode.lookAt(ploc.clone(), Vector3f.UNIT_Y.clone());
        } else if (INFO_AXIS == Axis.Y) {
            infoCamNode.setLocalTranslation(0, halfHeight, -distance);
            infoCamNode.lookAt(ploc.clone(), Vector3f.UNIT_Z.clone());
        } else {
            infoCamNode.setLocalTranslation(-distance, halfHeight, 0);
            infoCamNode.lookAt(ploc.clone(), Vector3f.UNIT_X.clone());
        }
    }
    public void nudgeInfoCamXZ(int direction) {
        Vector3f nudge = Direction.DirectionVector3fs[direction].mult(Chunk.XLENGTH);
        CameraNode infoCamNode = (CameraNode) player.getPlayerNode().getChild("cam2_node");
        Vector3f current = infoCamNode.getLocalTranslation();
        infoCamNode.setLocalTranslation(current.add(nudge));
    }
    private Material getDebugColumnMat() {
        if (debugColumnMat == null) debugColumnMat = wireFrameMaterialWithColor(ColorRGBA.Orange);
        return debugColumnMat;
    }
    public void attachDebugBuiltColumnAtChunkCoord(Coord3 chunkCo) {
        if (!SHOW_COLUMN_DEBUG_QUADS) return;
        chunkCo.y = 0;
        Mesh col = new Box(Vector3f.ZERO.clone(), Vector3f.UNIT_XYZ.clone().mult(new Vector3f((float)Chunk.XLENGTH,1f,(float)Chunk.ZLENGTH)));
        Vector3f pos = Chunk.ToWorldPosition(chunkCo).toVector3();
        Geometry g = new Geometry("col_mesh",col);
        g.setMaterial(getDebugColumnMat());
        rootNode.attachChild(g);
        g.setLocalTranslation(pos);
    }

    //endregion
    //</editor-fold>

    //<editor-fold desc="UNUSED">


    //</editor-fold>




}
