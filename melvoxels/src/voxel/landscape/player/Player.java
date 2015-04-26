package voxel.landscape.player;

import com.jme3.export.Savable;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.shape.Box;
import voxel.landscape.BlockType;
import voxel.landscape.Chunk;
import voxel.landscape.VoxelLandscape;
import voxel.landscape.coord.*;
import voxel.landscape.map.TerrainMap;

import java.util.ArrayList;
import java.util.List;

public class Player 
{
    private TerrainMap terrainMap;
    private Audio audio;
    private VoxelLandscape app;
    private PlayerControl playerControl = new PlayerControl();
    private Geometry blockCursor;
    Geometry[] blockStepCursorsDEBUG;
    private Node playerNode;
    private Node headNode;

    public final Coord3 spawn = new Coord3(0,0,0);

    private static float height = 1.5f;
    private static float halfWidthXZ = .40f;
    private static final Vector3f blockEdgeOffset = new Vector3f(.5f, .5f, .5f);
    private boolean thirdPerson = true;

    private static int AUTO_MOVE = 0;
    private static int FLY_MODE = 1;
    private static final float NORMAL_MOVE_SPEED = 6f;
    private static final float FLY_MODE_MOVE_SPEED = 15f;
	private static float MOVE_SPEED = NORMAL_MOVE_SPEED;
    private Vector3f inputVelocity = Vector3f.ZERO.clone();
    private float jumpVelocity = 0f;
    private static final float JUMP_VELOCITY= 11f;
    private static final float REAL_GRAVITY = 10f;
    private static float gravity = REAL_GRAVITY;
    private boolean grounded = false;
    private boolean jumping = false;
    private boolean headBump = false;
    private int blockInHandType = BlockType.DIRT.ordinal();
    public static final int REACHABLE_BLOCK_RADIUS = 12;
    private static final int REACHABLE_BLOCK_RADIUS_SQUARED = REACHABLE_BLOCK_RADIUS * REACHABLE_BLOCK_RADIUS;

    private boolean debugPlaceCoord = false;

//TODO: add fly cam mode...
    private ActionListener userInputListener = new ActionListener() {
    	public void onAction(String name, boolean keyPressed, float tpf) {
    		if (name.equals("Break") && !keyPressed) {
    			handleBreakBlock();
    		}
    		else if (name.equals("Place") && !keyPressed) {
    			handlePlaceBlock();
    		} else if (name.equals("GoHome") && !keyPressed) {
                teleportHome();
            }
            else if (name.equals("Up") && !keyPressed) {
                debugPlaceCoord = !debugPlaceCoord;
            }
            else if (name.equals("Down") && !keyPressed) {
                nextBlockInHandType();
            }
            else if (name.equals("Right") && !keyPressed) {
                blockInHandType = BlockType.LANTERN.ordinal();
            }
            else if (name.equals("Left") && !keyPressed) {
                toggleFlyMode();
            }
            else if (name.equals("UpArrow") && !keyPressed) {
                app.nudgeInfoCamXZ(Direction.ZPOS);
            }
            else if (name.equals("DownArrow") && !keyPressed) {
                app.nudgeInfoCamXZ(Direction.ZNEG);
            }
            else if (name.equals("RightArrow") && !keyPressed) {
                app.nudgeInfoCamXZ(Direction.XNEG);
            }
            else if (name.equals("LeftArrow") && !keyPressed) {
                app.nudgeInfoCamXZ(Direction.XPOS);
            }
            else if (name.equals("Inventory") && !keyPressed) {
            }
            else if (name.equals("ToggleInfoView") && !keyPressed) {
                app.toggleInfoViewAxis(); //
            }
            else if (name.equals("ToggleInfoViewDistance") && !keyPressed) {
                app.toggleInfoViewDistance();
            }
            else if (name.equals("DebugBlock") && !keyPressed) {
                printBlockCursorInfo();
            }
    	}
    };
    private void printBlockCursorInfo() {
        Coord3 global = Coord3.FromVector3f(blockCursor.getLocalTranslation());
        B.bugln(terrainMap.getBlockInfoString(global));
    }
    private void moveNextChunk(int dir) {
        Coord3 unit = Direction.DirectionCoordForDirection(dir);
        Coord3 loc = Coord3.FromVector3f(playerNode.getLocalTranslation());
        teleportTo(Chunk.ToWorldPosition(Chunk.ToChunkPosition(loc).add(unit)));
    }
    public void teleportTo(Coord3 global) {
        playerNode.setLocalTranslation(global.toVector3());
    }
    private void teleportHome() {
        playerNode.setLocalTranslation(new Vector3f(spawn.x,terrainMap.GetMaxY(spawn.x, spawn.z, false) + 4, spawn.z));
    }
    private void toggleFlyMode() {
        FLY_MODE = FLY_MODE == 1 ? 0 : 1;
        doSettingsForFlyMode();
    }
    private void doSettingsForFlyMode() {
        if (FLY_MODE==1) {
            gravity = 0f;
            MOVE_SPEED = FLY_MODE_MOVE_SPEED;
        } else {
            gravity = REAL_GRAVITY;
            MOVE_SPEED = NORMAL_MOVE_SPEED;
        }

    }
    private void nextBlockInHandType() {
        blockInHandType = BlockType.NextPlaceableBlockFrom(blockInHandType);
    }
    //TOOD: create a toggle between first and third person camera (hide/reveal player geometry)
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float keyPressed, float tpf) {

            if (FLY_MODE != 1 && name.equals("jump") && grounded && !jumping && jumpVelocity < .01  ) {
                jumping = true;
                jumpVelocity = JUMP_VELOCITY;
            }

            Vector3f move = Vector3f.ZERO.clone();
            if (name.equals("moveForward") ) {
                move.z = MOVE_SPEED;
            }
            else if (name.equals("moveBackward") ) {
                move.z = -MOVE_SPEED;
            }
            if (name.equals("moveRight") ) {
                move.x = -MOVE_SPEED;
            }
            else if (name.equals("moveLeft") ) {
                move.x = MOVE_SPEED;
            }
            if (name.equals("moveUp") ) {
                move.y = MOVE_SPEED;
            }
            else if (name.equals("moveDown") ) {
                move.y = -MOVE_SPEED;
            }

            /*
            TODO: resolve jump, move direction weirdness (look at Unity character controllers)
             */
            float velY = move.y;
            move.y = 0;
            Quaternion camro = headNode.getLocalRotation().clone(); // cam.getRotation();
            turnInputToCamera(camro.mult(move.clone()));
            inputVelocity.y = velY;
        }
    };
    private void turnInputToCamera(Vector3f camV) {
        inputVelocity.x = camV.x;
        inputVelocity.z = camV.z;
    }
    private void resetInputVelocity() {
        inputVelocity.x = 0f;
        inputVelocity.y = 0f;
        inputVelocity.z = 0f;
    }
    private void rotatePlayerHead() {
        Vector2f mouse = app.getInputManager().getCursorPosition();
        if (mouse == null) return;

        Vector2f halfScreen = app.getScreenDims().multy(.5f).toVector2f();

        mouse = mouse.subtract(halfScreen);
        Quaternion roll = new Quaternion();
        roll.fromAngleAxis( 2f * FastMath.PI * -mouse.x /(halfScreen.x ) , new Vector3f(0,1,0) );
        Quaternion rollY = new Quaternion();
        rollY.fromAngleAxis( FastMath.PI * -mouse.y /(halfScreen.y * 2.0f) , new Vector3f(1,0,0) );
        roll = roll.mult(rollY);

        headNode.setLocalRotation(roll);
    }
    private void movePlayer(float tpf) {
        Vector3f pos = playerNode.getWorldTranslation().clone(); // playerNode.getLocalTranslation().clone();
        if (AUTO_MOVE == 1) {
            inputVelocity.x = MOVE_SPEED;
        }
        Vector3f scaledV = inputVelocity.clone();
        if ((grounded && jumpVelocity < .01) || headBump) {
            jumpVelocity = 0f;
        } else {
            scaledV.y += jumpVelocity;
            jumpVelocity -= gravity * tpf;
        }
        scaledV = scaledV.mult(tpf);
        scaledV = checkCollisions(pos, scaledV); //*

        playerNode.move(scaledV);

        if(!jumping) {
            resetInputVelocity();
        }
    }

    private Vector3f checkCollisions(final Vector3f curLoc, final Vector3f proposedMove) {
        //proposedMove is the amount the player should move based on user input and gravity (and getting pushed by something?)
        //make a new vec3f "proposedLoc" by adding proposed move to curLoc
        //also add blockEdgeOffset (.5, .5, .5) so that we get the correct integer when looking up blocks.
        //blockEdgeOffset will be subtracted again before returning
        Vector3f proposedLoc = curLoc.add(proposedMove).add(blockEdgeOffset);

        // a List<Integer> 'unimpededDirections' for use later when finding ground. We need to know
        // which directions are not blocked, in case we need to check whether the player's toe catches
        // a block that's one 'next-door' (in x or z)
        List<Integer> unimpededDirections = new ArrayList<Integer>(6);

        /* check for side walls */
        // Go through all of the XZ directions in a for loop
        // get the xzUnitVec is a 'unit vector' for the direction in question: example for XPOS: (1, 0, 0). for ZNEG (0, 0, -1)
        // xzPlayerEdge is the unit vector * player Half Width: example for XPOS (.4, 0, 0). ZNEG (0, 0, -.4)
        // xzEdgePos is the proposedLoc + xzPlayerEdge: if proposed loc is (3.61, 0, 0) then -> (for XPOS) -> (4.01, 0, 0)
        // Coord3 edge is the integer version of xzEdgePos. example (4, 0, 0). Use this last to look up the corresponding
        // block in terrainMap
        Coord3 edge;
        Vector3f xzUnitVec, xzPlayerEdge, xzEdgePos;
        for (int i = 0; i < Direction.DirectionXZVector3fs.length; ++i) {
            xzUnitVec = Direction.DirectionXZVector3fs[i].clone();
            xzPlayerEdge = xzUnitVec.mult(halfWidthXZ);
            xzEdgePos = proposedLoc.add(xzPlayerEdge);
            edge = Coord3.FromVector3fAdjustNegative(xzEdgePos);
            boolean found_solid = BlockType.IsSolid(terrainMap.lookupBlock(edge));
            if (!found_solid){ // if no solid block at feet, check head level
                edge.y++;
                found_solid = BlockType.IsSolid(terrainMap.lookupBlock(edge));
            }
            /* if still no side wall, add to the unimpeded list */
            if (!found_solid) {
                unimpededDirections.add(i);
                continue;
            }
            /* if side wall, nudge the proposed loc away from the side wall */
            Vector3f corner = edge.toVector3();
            if (xzPlayerEdge.x < 0 || xzPlayerEdge.z < 0) {
                corner = corner.add(Direction.UNIT_XZ.clone()); //doesn't matter which dir since the other dir will be zeroed out.
            }
            proposedLoc = proposedLoc.add(corner.subtract(xzEdgePos).mult(Direction.DirectionXZVector3fsAbsValues[i]));
        }
        /* check grounded, if not jumping */
        Coord3 pco = null;
        boolean gotGround = false;
        if (jumpVelocity < 0.001) {
            pco = Coord3.FromVector3fAdjustNegative(proposedLoc.subtract(0, .1f, 0));
            if (BlockType.IsSolid(terrainMap.lookupBlock(pco))) {
                gotGround = true;
                proposedLoc.y = (pco.y + 1f) + (jumping ? .11f : .1f);
            }

            /*
            if no ground directly underneath player, check if player's 'toes' are supported by ground.
            in other words, player collision cube overlaps to an adjacent block and we find ground underneath that block.
            but only look for ground in unimpeded directions b/c our toes should not enter a solid block
            */
            if (!gotGround) for (Integer i : unimpededDirections) {
                Vector3f xzunitdir = Direction.DirectionXZVector3fs[i];
                Vector3f xzdir = xzunitdir.mult(halfWidthXZ);
                pco = Coord3.FromVector3fAdjustNegative(proposedLoc.subtract(0, .1f, 0).add(xzdir));
                if (BlockType.IsSolid(terrainMap.lookupBlock(pco))) {
                    gotGround = true;
                    proposedLoc.y = (pco.y + 1f) + (jumping ? .11f : .1f);
                    break;
                }
            }
            grounded = gotGround;
            if (!gotGround) jumping = false;
        }

        // check for head bump
        boolean gotHeadBump = false;
        if (jumpVelocity > 0) {
//            float headRoom = -playerBodyOffset.y + .7f;
            float headRoom = height + .1f; // + .7f;
            pco = Coord3.FromVector3fAdjustNegative(proposedLoc.add(0, headRoom, 0));
            if (BlockType.IsSolid(terrainMap.lookupBlock(pco))){
                proposedLoc.y = pco.y - headRoom - .1f;
                jumpVelocity = 0;
                gotHeadBump = true;
            }
            if (!gotHeadBump) for (Integer i : unimpededDirections) {
                Vector3f xzunitdir = Direction.DirectionXZVector3fs[i];
                Vector3f xzdir = xzunitdir.mult(halfWidthXZ);
                pco = Coord3.FromVector3fAdjustNegative(proposedLoc.add(0, headRoom, 0).add(xzdir));
                if (BlockType.IsSolid(terrainMap.lookupBlock(pco))){
                    proposedLoc.y = pco.y - headRoom - .1f;
                    jumpVelocity = 0;
                    gotHeadBump = true;
                    break;
                }
            }
        }
        headBump = gotHeadBump;
        return proposedLoc.subtract(curLoc).subtract(blockEdgeOffset);
    }

    public Player(TerrainMap _terrainMap, Camera _camera, Audio _audio, VoxelLandscape _app, Node _overlayNode, Node _terrainNode) {
        doSettingsForFlyMode();
    	terrainMap = _terrainMap;
    	audio = _audio;
    	app = _app;

    	initBlockCursor();
        _overlayNode.attachChild(blockCursor);
    	initPlayerGeom(_camera, _terrainNode);

        if (_camera != null)
            adjustCameraFrustum(_camera);

        teleportHome();
    }

    public Node getPlayerNode() {
        if (playerNode == null) {
            playerNode = new Node("player_node");
        }
        return playerNode;
    }
    public Node getCamNode() { return playerControl.getNode(); }
    public Node getHeadNode() { return headNode; }
    public ActionListener getUserInputListener() { return userInputListener; }
    public AnalogListener getAnalogListener() { return analogListener; }

    private void moveBlockCursor() {
        Vector3f pos = stepThroughBlocksUntilHitSolid(false);
    	if (pos == null) pos = Vector3f.NEGATIVE_INFINITY.clone();
    	pos = VektorUtil.Floor(pos);
        blockCursor.setLocalTranslation(pos);
    }
    private void handleBreakBlock() {
    	Vector3f vhit = stepThroughBlocksUntilHitSolid(false);
    	if (vhit == null) return;
    	Coord3 hitC =Coord3.FromVector3f( vhit  );
    	if (hitC == null) return;
        audio.playBreakCompleteSound();
        terrainMap.SetBlockAndRecompute(BlockType.AIR.ordinal(), hitC);
    }
    private void handlePlaceBlock() {
    	Vector3f vhit = stepThroughBlocksUntilHitSolid(true);
    	if (vhit == null) return;
    	Coord3 placeCo = Coord3.FromVector3f( vhit);
    	if (placeCo == null) return;
    	audio.playBreakCompleteSound();
    	terrainMap.SetBlockAndRecompute(blockInHandType, placeCo);
    }
    private Vector3f stepThroughBlocksUntilHitSolid(boolean wantPlaceBlock) {
        return stepThroughBlocksUntilHit(playerControl.getLocation(), playerControl.getDirection(), wantPlaceBlock);
    }
	/*
	 * updating JMonkey geometry bounds and checking collisions are computationally expensive...
	 * So, instead, rely completely on block look-ups for placing/breaking/colliding
	 */
    private Vector3f stepThroughBlocksUntilHit(Vector3f start, Vector3f direction, boolean wantPlaceBlock) {
    	int block;
    	start = start.add(new Vector3f(.5f,.5f,.5f)); //BECAUSE VOXELS ARE CENTERED AROUND WHOLE NUMBER COORDS...
        Coord3 startCoord = Coord3.FromVector3f(start);
    	Coord3 hit = null;
    	Vector3f hitV = start.clone();
        MutableInteger hitFaceDirectionIn = wantPlaceBlock ? new MutableInteger() : null;
        //TODO: make a plane geom for cursor. it always rotates acc. to hitFaceDirectionIn (which is now never null, etc.)
        int distanceFromPlayerSquared = 0;

        int TEST_iter_safety = 0;
        // TODO: verify: w/o iter_safety does this (somehow) cause an infinite loop?
        while(distanceFromPlayerSquared < REACHABLE_BLOCK_RADIUS_SQUARED && TEST_iter_safety++ < 20) {
            hitV = VektorUtil.EscapePositionOnUnitGrid(hitV, direction, hitFaceDirectionIn);
            hit = Coord3.FromVector3f(VektorUtil.SubtractOneFromNegativeComponents(hitV));
            distanceFromPlayerSquared = hit.minus(startCoord).magnitudeSquared();

            block = terrainMap.lookupBlock(hit);

            if (wantPlaceBlock && BlockType.AcceptsPlaceBlock(block)) {
                Coord3 placeRelCoord = Direction.DirectionCoordForDirection(Direction.OppositeDirection(hitFaceDirectionIn.integer));
                return Coord3.FromVector3fAdjustNegative(hitV.add(placeRelCoord.toVector3())).toVector3();
            } else if (BlockType.IsBreakAble(block)) {
                return VektorUtil.SubtractOneFromNegativeComponents(hitV); // hitV;
            }
        }
    	return null;
    }
    protected void initBlockCursor() {
      Box box = new Box(.505f, .505f, .505f);
      blockCursor = new Geometry("block_cursor", box);
      Material mark_mat = app.wireFrameMaterialWithColor(ColorRGBA.Black); 
      blockCursor.setMaterial(mark_mat);
    }
    private void initDebugBlockStepCursors() {
        blockStepCursorsDEBUG = new Geometry[20];
        for(int i = 0; i < blockStepCursorsDEBUG.length; ++i) {
            blockStepCursorsDEBUG[i] = new Geometry("block-step-cursor", new Box(.15f, .65f, .15f));
            blockStepCursorsDEBUG[i].setMaterial(app.wireFrameMaterialWithColor(ColorRGBA.Red));
        }
    }
    private Geometry makePlayerGeometry() {
        Box box = new Box(Vector3f.ZERO, new Vector3f(halfWidthXZ*2f,height,halfWidthXZ*2f));
        Geometry playerGeom = new Geometry("player_geom", box);
    	playerGeom.setMaterial(app.wireFrameMaterialWithColor(ColorRGBA.Blue));
        return playerGeom;
    }
    private Geometry makeSmallBox(ColorRGBA color) {
        Box box = new Box(Vector3f.ZERO, new Vector3f(.1f,.1f,.1f));
        Geometry playerGeom = new Geometry("small_geom", box);
        playerGeom.setMaterial(app.wireFrameMaterialWithColor(color));
        return playerGeom;
    }
    private void debugMoveHeadNodeUp() {
        headNode.setLocalTranslation(-3, 5, -3);
    }

    private void initPlayerGeom(Camera _cam, Node _terrainNode)
    {
    	playerNode = getPlayerNode();
        headNode = new Node("head_node");
        playerNode.attachChild(headNode);

        //DEBUG: SHOW GEOMETRY
        Geometry playerGeom = makePlayerGeometry();
        playerNode.attachChild(playerGeom);
        playerGeom.setLocalTranslation(-halfWidthXZ, 0f, -halfWidthXZ);
        playerNode.attachChild(makeSmallBox(ColorRGBA.Orange));

        CameraNode camNode;
        if (_cam != null) {
            camNode = new CameraNode("cam_node", _cam);
            headNode.attachChild(camNode);

            //This mode means that camera copies the movements of the target:
            camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
            camNode.lookAt(headNode.getLocalTranslation(), Vector3f.UNIT_Y.clone());
            camNode.addControl(playerControl);

            //TEST
            headNode.setLocalTranslation(0f, height, 0f);
            headNode.attachChild(makeSmallBox(ColorRGBA.Magenta));

        } else {
            _terrainNode.addControl(playerControl);
        }
    	playerNode.setLocalTranslation(new Vector3f(0,50,0));
    }

    public void adjustCameraFrustum(Camera _camera) {
        Coord2 dims = app.getScreenDims();
        float aspect = dims.y/(float)dims.x;
        float near = .12f; ;
        float right = near * .75f ;
        float left = -right ;
        float top = right* aspect;
        float bottom = -top;
        float far = 1000.0f;
        _camera.setFrustum(near, far, left, right, top, bottom);
    }

    public class PlayerControl extends AbstractControl implements Cloneable, Savable
    {
    	private float timeSinceUpdate = 0;
    	private static final float TIME_PER_UPDATE = .15f;

        private Node getNode() { return (Node) getSpatial(); }
        private Vector3f getLocation() { return getSpatial().getWorldTranslation().clone(); }
        private Vector3f getDirection() { return getSpatial().getWorldRotation().clone().mult(Vector3f.UNIT_Z).normalize(); }
		@Override
		protected void controlUpdate(float tpf) {
			timeSinceUpdate += tpf;
			if (timeSinceUpdate > TIME_PER_UPDATE) {
				timeSinceUpdate = 0;
				moveBlockCursor();

			}
            rotatePlayerHead();
			movePlayer(tpf);
		}
    	
		@Override
		protected void controlRender(RenderManager arg0, ViewPort arg1) {
		}
    }

}
