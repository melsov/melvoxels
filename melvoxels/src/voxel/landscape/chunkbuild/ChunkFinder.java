package voxel.landscape.chunkbuild;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import voxel.landscape.Chunk;
import voxel.landscape.collection.ColumnMap;
import voxel.landscape.coord.Box2;
import voxel.landscape.coord.Coord2;
import voxel.landscape.coord.Coord3;
import voxel.landscape.coord.VektorUtil;
import voxel.landscape.map.TerrainMap;
import voxel.landscape.settings.BuildSettings;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static com.jme3.math.FastMath.PI;

/**
 * Created by didyouloseyourdog on 7/31/14.
 */
public class ChunkFinder {

    private static int LOOK_RADIUS = (int) (BuildSettings.ADD_COLUMN_RADIUS * .75f);
    private static List<Vector3f> LookDirections = new ArrayList<Vector3f>();

    private static int SizeLookDirections;
    private static List<Coord3> OrthoCoordIncrements2D = new ArrayList<Coord3>();
    private static List<Coord3> DiagonalCoordIncrements2D = new ArrayList<Coord3>();

    private static List<Coord3> SurroundingCoordIncrements3D = new ArrayList<Coord3>();
    private static List<Coord3> SurroundingCoordIncrements2D = new ArrayList<Coord3>();
    private static List<Coord3> OrthoCoordIncrements3D = new ArrayList<Coord3>();
    private static List<Coord3> DiagonalCoordIncrements3D = new ArrayList<Coord3>();
    private static List<Coord3> TestColumnCoords = new ArrayList<Coord3>();

    public static Vector3f DebugPosition = new Vector3f();

    static {
        SetupLookDirections();
        SetupOrthoCoordLook2D();
        SetupDiagonalCoordLook2D();
        SetupSurroundingCoordLook3D();
        SetupSurroundingCoordLook2D();
        SetupOrthoCoordLook3D();
        SetupDiagonalCoordLook3D();
        SetupTestColumnCoords();
    }
    private static final int TestColumnsXDim = 2;
    private static final int TestColumnsXStart = -1;
    private static final int TestColumnsZDim = 2;
    private static final int TestColumnsZStart = -1;
    private static Box2 testColumns; // = new Square(new Coord2(-7, 3), new Coord2(4, 4));
    public static Box2 GetTestColumns() {
        if (testColumns == null) {
            testColumns = new Box2(new Coord2(TestColumnsXStart, TestColumnsZStart), new Coord2(TestColumnsXDim, TestColumnsZDim));
        }
        return testColumns;
    }
    private static void SetupTestColumnCoords() {
        for(int x = GetTestColumns().start.x; x < GetTestColumns().extent().x; ++x) {
            for(int z = GetTestColumns().start.getZ(); z < GetTestColumns().extent().getZ(); ++z) {
                TestColumnCoords.add(new Coord3(x, 0, z));
            }
        }
    }
    private static void SetupLookDirections() {
        Quaternion q = Quaternion.IDENTITY.clone();
        LookDirections.add(Vector3f.UNIT_Z.clone());
        Vector3f forward = Vector3f.UNIT_Z.clone();

        float turn_divisions = 12f;
        for(int j= (int) turn_divisions; j> 0 ; --j) {
            Quaternion turn = q.clone().mult(CreateFromAxisAngle(1,0,0, PI/2f *(j/turn_divisions)));
            float quarter_divisions = 12f;
            for (int i = 1; i < quarter_divisions; ++i) {
                Quaternion turnd = turn.clone().mult(CreateFromAxisAngle(0,1,0,PI/(2f)*(i/quarter_divisions)));

                Vector3f look = turnd.clone().mult(Vector3f.UNIT_Z.clone());
                Vector3f otherWay = look.clone();
                otherWay.x *= -1;
                Vector3f otherWayDown = look.clone();
                otherWayDown.y *= -1;
                Vector3f otherOtherWayDown = otherWay.clone();
                otherOtherWayDown.y *= -1;

                LookDirections.add(otherWayDown);
                LookDirections.add(otherOtherWayDown);
                LookDirections.add(look);
                LookDirections.add(otherWay);

            }
        }
        List<Vector3f> lookBack = new ArrayList<Vector3f>();
        for(Vector3f v : LookDirections) {
            Vector3f vb = v.clone();
            vb.z *=-1;
            lookBack.add(vb);
        }
        LookDirections.addAll(lookBack);
        SizeLookDirections = LookDirections.size();
    }

    private static void SetupOrthoCoordLook2D() {
        for(int forward=0; forward < LOOK_RADIUS; ++forward) {
            Coord3 forwardCoord = new Coord3(0,0,forward);
            OrthoCoordIncrements2D.add(forwardCoord.clone());

            for(int lateral=1; lateral<=forward; ++lateral) {
                Coord3 forwardRight = forwardCoord.clone();
                forwardRight.x = lateral;
                OrthoCoordIncrements2D.add(forwardRight);
                Coord3 forwardLeft = forwardCoord.clone();
                forwardLeft.x = -lateral;
                OrthoCoordIncrements2D.add(forwardLeft);
            }
        }
    }
    private static void SetupDiagonalCoordLook2D() {
        for(int forward=0; forward < LOOK_RADIUS; ++forward) {
            Coord3 diagCoord = new Coord3(forward,0,forward);
            DiagonalCoordIncrements2D.add(diagCoord.clone());

            for(int lateral=1; lateral<=forward; ++lateral) {
                Coord3 diagRight = diagCoord.clone();
                diagRight.x -= lateral;
                DiagonalCoordIncrements2D.add(diagRight);
                Coord3 diagLeft = diagCoord.clone();
                diagLeft.y -= lateral; //NOTE: y stands in for z in Coord2
                DiagonalCoordIncrements2D.add(diagLeft);
            }
        }
    }

    private static void SetupSurroundingCoordLook2D() {
        Coord3 currentCoord = Coord3.Zero.clone();
        SurroundingCoordIncrements3D.add(currentCoord);
        for(int x=3;x>-3;x--) {
            for(int z=3;z>-3;z--) {
                int y = 0;
                if (x != 0 && z != 0) {
                    SurroundingCoordIncrements3D.add(new Coord3(x,y,z));
                }
            }
        }
    }
    private static void SetupSurroundingCoordLook3D() {
        Coord3 currentCoord = Coord3.Zero.clone();
        SurroundingCoordIncrements3D.add(currentCoord);
        for(int x=3;x>-3;x--) {
            for(int y=3;y>-3;y--){
                for(int z=3;z>-3;z--) {
                    if (x != 0 && y != 0 && z != 0) {
                        SurroundingCoordIncrements3D.add(new Coord3(x,y,z));
                    }
                }
            }
        }
    }
    private static void SetupOrthoCoordLook3D() {

        for(int forward=0; forward < LOOK_RADIUS; ++forward) {
            Coord3 forwardCoord = new Coord3(0,0,forward);
            OrthoCoordIncrements3D.add(forwardCoord.clone());

            for(int lateral=1; lateral<=forward + 1; ++lateral) {

                Coord3 downOne = forwardCoord.clone();
                downOne.y -= lateral;
                OrthoCoordIncrements3D.add(downOne);
                Coord3 upOne = forwardCoord.clone();
                upOne.y += lateral;
                OrthoCoordIncrements3D.add(upOne);

                Coord3 forwardRight = forwardCoord.clone();
                forwardRight.x += lateral;
                OrthoCoordIncrements3D.add(forwardRight);

                downOne = forwardRight.clone();
                downOne.y -= lateral;
                OrthoCoordIncrements3D.add(downOne);
                upOne = forwardRight.clone();
                upOne.y += lateral;
                OrthoCoordIncrements3D.add(upOne);

                Coord3 forwardLeft = forwardCoord.clone();
                forwardLeft.x += -lateral;
                OrthoCoordIncrements3D.add(forwardLeft);

                downOne = forwardLeft.clone();
                downOne.y -= lateral;
                OrthoCoordIncrements3D.add(downOne);
                upOne = forwardLeft.clone();
                upOne.y += lateral;
                OrthoCoordIncrements3D.add(upOne);
            }
        }
    }
    private static void SetupDiagonalCoordLook3D() {
        for(int forward=0; forward < LOOK_RADIUS; ++forward) {
            Coord3 diagCoord = new Coord3(forward ,0,forward);
            DiagonalCoordIncrements3D.add(diagCoord.clone());

            for(int lateral=1; lateral<=forward; ++lateral) {

                Coord3 downOne = diagCoord.clone();
                downOne.y -= lateral;
                DiagonalCoordIncrements3D.add(downOne);
                Coord3 upOne = diagCoord.clone();
                upOne.y += lateral;
                DiagonalCoordIncrements3D.add(upOne);

                Coord3 diagRight = diagCoord.clone();
                diagRight.x -= lateral;
                DiagonalCoordIncrements3D.add(diagRight);

                downOne = diagRight.clone();
                downOne.y -= lateral;
                DiagonalCoordIncrements3D.add(downOne);
                upOne = diagRight.clone();
                upOne.y += lateral;
                DiagonalCoordIncrements3D.add(upOne);

                Coord3 diagLeft = diagCoord.clone();
                diagLeft.z -= lateral;
                DiagonalCoordIncrements3D.add(diagLeft);

                downOne = diagLeft.clone();
                downOne.y -= lateral;
                DiagonalCoordIncrements3D.add(downOne);
                upOne = diagLeft.clone();
                upOne.y += lateral;
                DiagonalCoordIncrements3D.add(upOne);
            }
        }
    }

    public static Vector3f GetLookDirectionAtIndex(int index) {
        if (index < SizeLookDirections) return null;
        return LookDirections.get(index);
    }

    private static Quaternion CreateFromAxisAngle(double xx, double yy, double zz,  double a){
        // Here we calculate the sin( theta / 2 ) once for optimization
        float result = (float) Math.sin(a / 2.0);

        // Calculate the x, y and z of the quaternion
        float x = (float) (xx * result);
        float y = (float) (yy * result);
        float z = (float) (zz * result);

        // Calculate the w value by cos( theta / 2 )
        float w = (float) Math.cos(a / 2.0);

        Quaternion q = new Quaternion();
        q.set(x,y,z,w);
        return q.normalizeLocal();
    }

    public static Coord3 ClosestReadyToBuildChunk(Camera cam, TerrainMap terrainMap, ColumnMap columnMap) {
        return ClosestChunk(cam, terrainMap, columnMap);
    }

    public static final boolean UseTestColumns = false;
    private static int TestColumnIndex = 0;

//TODO: improve—"eh-hem"—the closest column finding shape: more of a square around player at first, less based on cam direction

    public static Coord3 ClosestEmptyColumn(Camera cam, TerrainMap terrainMap, ColumnMap columnMap, boolean limitToAddRadius) {
        if (UseTestColumns) {
            if (TestColumnIndex < TestColumnCoords.size()) {
                return TestColumnCoords.get(TestColumnIndex++);
            }
            return new Coord3(0);
        }
        Coord3 result = ClosestColumn(cam, terrainMap, columnMap); // ***** WANT
        if (limitToAddRadius && result != null && !BuildSettings.ChunkCoordWithinAddRadius(cam.getLocation(), result)) return null;
        return result;
    }

    private static Coord3 SignCoordXZ(Vector3f direction) {
        Coord3 sign =  Coord3.FromVector3f(VektorUtil.Sign(direction));
        sign = sign.signNonZero();
        sign.y = 1;
        return sign;
    }

    private static Coord3 ClosestChunk(Camera cam, TerrainMap terrainMap, ColumnMap columnMap) {
        Vector3f camDir = cam.getDirection().clone();
        Coord3 result = ClosestChunk(cam, terrainMap, columnMap, camDir.clone());
        if (result == null) {
            camDir.x *= -1;
            result = ClosestChunk(cam, terrainMap,columnMap,camDir.clone());
        }
        return result;
    }

    private static Coord3 ClosestChunk(Camera cam, TerrainMap terrainMap, ColumnMap columnMap, Vector3f camDir) {
        Coord3 camChunkCoord = Chunk.ToChunkPosition(Coord3.FromVector3f(cam.getLocation()));
        if (!columnMap.IsBuilt(camChunkCoord.x, camChunkCoord.z)) return null; //whoa! more important things to do right now
        camChunkCoord.y = Math.min(camChunkCoord.y, terrainMap.getMaxChunkCoordY() - 1);

        Chunk result=null;
        Coord3 nextPos;
        for(Coord3 surroundingCoord : SurroundingCoordIncrements3D) {
            nextPos = surroundingCoord.add(camChunkCoord);
            result = terrainMap.GetChunk(nextPos);
            if (ChunkIsReady(columnMap, result, nextPos)){
                return nextPos;
            }
        }

        Coord3 lookCo = UnbuiltChunkInDirection(terrainMap, columnMap, camDir, cam.getLocation(), 8);
        if (lookCo != null) return lookCo;

        Coord3 directionSignXZ = SignCoordXZ(camDir);
        List<Coord3> nudgeChunkPosCoords = DiagonalCoordIncrements3D;
        boolean isOrthoAndXGreater = false;
        if (Math.abs(camDir.z) > .65f || Math.abs(camDir.x) > .65f) { //ORTHO LOOK DIRECTION?
            Vector3f absCamDir = VektorUtil.Abs(camDir);
            isOrthoAndXGreater = absCamDir.x > absCamDir.z;
            nudgeChunkPosCoords = OrthoCoordIncrements3D;
        }

        Coord3 nudge;
        for (int i=0; i< nudgeChunkPosCoords.size(); ++i) {
            nudge = nudgeChunkPosCoords.get(i).clone();
            if (isOrthoAndXGreater) {
                int x = nudge.x;
                nudge.x = nudge.z;
                nudge.z = x;
            }
            nudge = nudge.multy(directionSignXZ);
            nextPos = nudge.add(camChunkCoord);

            result = terrainMap.GetChunk(nextPos);
            if (ChunkIsReady(columnMap, result, nextPos)){
                return nextPos;
            }
        }

        return null;
    }

    private static Coord3 UnbuiltChunkInDirection(TerrainMap terrainMap, ColumnMap columnMap, Vector3f lookDirection, Vector3f camPosOrig, int depth ) {
        Vector3f camPos = camPosOrig.clone();
        Chunk searchChunk = null;
        Coord3 chunkCoord = null;
        Vector3f toCorner, escapeVector, nextPosV;
        for(int j=0;j< depth;++j) {
            toCorner = GetChunkCornerVector(lookDirection, camPos).subtract(camPos);
            escapeVector = VektorUtil.RelativeEscapeVectorXZ(toCorner, lookDirection, 1.05f);
            nextPosV = camPos.add(escapeVector);

            chunkCoord = Chunk.ToChunkPosition(Coord3.FromVector3f(nextPosV));
            searchChunk = terrainMap.GetChunk(chunkCoord);
            if (ChunkIsReady(columnMap, searchChunk, chunkCoord)) return chunkCoord;
            camPos = nextPosV;
        }
        return null;
    }

    private static Coord3 ClosestColumn(Camera cam, TerrainMap terrainMap, ColumnMap columnMap) {
        Vector3f camDir = cam.getDirection().clone();
        Coord3 result = ClosestColumn(cam, terrainMap, columnMap, camDir.clone());
        if (result == null) {
            result = ClosestColumn(cam, terrainMap, columnMap, VektorUtil.SwapXZ(camDir));
        }
        if (result == null) {
            camDir = camDir.mult(new Vector3f(-1f, 1f, -1f));
            result = ClosestColumn(cam, terrainMap, columnMap, camDir.clone());
        }
        if (result == null) {
            result = ClosestColumn(cam, terrainMap, columnMap, VektorUtil.SwapXZ(camDir));
        }
        return result;
    }

    private static Coord3 ClosestColumn(Camera cam, TerrainMap terrainMap, ColumnMap columnMap, Vector3f camDir) {
        Coord3 camChunkCoord = Chunk.ToChunkPosition(Coord3.FromVector3f(cam.getLocation()));
        camChunkCoord.y = Math.min(camChunkCoord.y, terrainMap.getMaxChunkCoordY() - 1);

        /* Look below player a little */
        Coord3 searchCo, lookBelow, nextPos;

        for(Coord3 surroundingCoord : SurroundingCoordIncrements2D) {
            nextPos = surroundingCoord.add(camChunkCoord);
            searchCo = FindColumn(columnMap, terrainMap, nextPos.clone());
            if (searchCo != null) return searchCo;
        }

        Coord3 directionSignXZ = SignCoordXZ(camDir);

        List<Coord3> nudgeChunkPosCoords = DiagonalCoordIncrements2D;
        boolean isOrthoAndXGreater = false;
        if (Math.abs(camDir.z) > .65f || Math.abs(camDir.x) > .65f) { //ORTHO LOOK DIRECTION?
            Vector3f absCamDir = VektorUtil.Abs(camDir);
            isOrthoAndXGreater = absCamDir.x > absCamDir.z;
            nudgeChunkPosCoords = OrthoCoordIncrements2D;
        }

        Coord3 nudge;
        for (int i=0; i< nudgeChunkPosCoords.size(); ++i) {
            nudge = nudgeChunkPosCoords.get(i).clone();
            if (isOrthoAndXGreater) {
                int x = nudge.x;
                nudge.x = nudge.z;
                nudge.z = x;
            }
            nudge = nudge.multy(directionSignXZ);
            nextPos = nudge.add(camChunkCoord);
            searchCo = FindColumn(columnMap, terrainMap, nextPos.clone());
            if (searchCo != null) return searchCo;
        }

        return null;
    }
    private static Vector3f GetChunkCornerVector(Vector3f direction, Vector3f position) {
        Vector3f corner = position.add(GetPointedAtCorner(direction, Chunk.XLENGTH));
        return Chunk.ToChunkPosition(Coord3.FromVector3f(corner)).toVector3();
    }
    private static Vector3f GetPointedAtCorner(Vector3f direction, float cubeUnitLength) {
        return VektorUtil.OneIfPos(direction).mult(cubeUnitLength);
    }

    private static Coord3 FindColumn(ColumnMap columnMap, TerrainMap map,  Coord3 look) {
        if (columnMap.HasNotBeenStarted(look.x, look.z)) return look;
        if (map.columnHasHiddenChunk(look)) {
            return look;
        }
        return null;
    }

    private static boolean ChunkIsReady(ColumnMap columnMap, Chunk result, Coord3 foundCoord) {
        return columnMap.IsBuilt(foundCoord.x, foundCoord.z) && result != null &&
            !result.getHasEverStartedBuilding() && !result.getIsAllAir();
}

    //DEBUG
    public static BufferedImage bufferedImageTest() {
        BufferedImage buff;
        buff = new BufferedImage(800,800, BufferedImage.TYPE_INT_ARGB);

        Vector2f center = new Vector2f(400,400);
        float count = LookDirections.size();
        int c = 0;
        float blue = 0;
        for (Vector3f v : LookDirections) {
            Vector2f off = center.add(new Vector2f(v.x, v.y).mult(200*v.z));
            drawSmallDot(buff,(int) off.x, (int) off.y, new Color(1f - blue,0,blue));
            drawSmallDot(buff,(int) off.x, (int) off.y, new Color(1,0,0));
            blue = c++/count; // (int)(blue + (c++/count)) % 245;
        }

//        Vector3f camDir = new Vector3f(-.9f, -1f, .5f);
//        drawDirectionLooks(buff, camDir);
//        camDir = new Vector3f(.6f, -1f, -.5f);
//        drawDirectionLooks(buff, camDir);
//        camDir = new Vector3f(.7f, 1f, 1f);
//        drawDirectionLooks(buff, camDir);
//        camDir = new Vector3f(-.8f, -1f, -1f);
//        drawDirectionLooks(buff, camDir);
        return buff;
    }

    private static void drawDirectionLooks(BufferedImage buff, Vector3f camDir) {
        Coord3 dirXZ3 = SignCoordXZ(camDir.clone());
        int blue = 0;
        Coord3 cent3 = new Coord3(400,0,400);
        Vector3f absCamDir = VektorUtil.Abs(camDir);

        for (int i=0; i< OrthoCoordIncrements3D.size(); ++i) {
            Coord3 cc = OrthoCoordIncrements3D.get(i).clone();
            if (absCamDir.x > absCamDir.z) {
                int x = cc.x;
                cc.x = cc.z;
                cc.z = x;
            }
            cc.z *= dirXZ3.z;
            cc.x *= dirXZ3.x;
            Coord3 added = cent3.add(cc.multy(40));
            added.x += cc.y*2;
            added.z += cc.y*2;
            drawDot(buff, added.x, added.z, new Color(255 -blue, (int) (122 - blue *.3),blue));
            blue = (blue + 1) % 255;
//            nudge = nudge.multy(dirXZ);
//            nextPos = nudge.add(camChunkCoord);
//            foundPos = FindChunkBelow(columnMap, terrainMap, result, nextPos, 1);
//            if (foundPos != null) return foundPos;
        }

//        for (Coord3 c : OrthoCoordIncrements3D) {
//            Coord3 cc = c.clone();
//
//            if (absCamDir.x > absCamDir.z) {
//                int x = cc.x;
//                cc.x = cc.z;
//                cc.z = x;
//                cc.x *= dirXZ3.x;
//            } else {
//                cc.z *= dirXZ3.z;
//                cc.x *= dirXZ3.x;
//            }
//
//            Coord3 added = cent3.add(cc.multy(40));
//            added.x += cc.y*2;
//            added.z += cc.y*2;
//            drawDot(buff, added.x, added.z, new Color(255 -blue, (int) (122 - blue *.3),blue));
//            blue = (blue + 1) % 255;
//        }
//        for (Coord2 cc : OrthoCoordIncrements2D) {

//            if (camDir.x > camDir.z) {
//                int x = cc.x;
//                cc.x = cc.y;
//                cc.y = x;
//                cc.y *= dirXZ.x;
//            } else {
//                cc.y *= dirXZ.y;
//            }
//            Coord2 added = cent.add(cc.multy(dirXZ).multy(40));
//            drawDot(buff, added.x, added.y, new Color(55,66,222));
//        }
//        for (Coord2 cc : DiagonalCoordIncrements2D) {
//
//            if (absCamDir.x > absCamDir.z) {
//                int x = cc.x;
//                cc.x = cc.y;
//                cc.y = x;
//                cc.y *= dirXZ.x;
//            } else {
//                cc.y *= dirXZ.y;
//            }
//            Coord2 added = cent.add(cc.multy(dirXZ).multy(40));
//            drawDot(buff, added.x, added.y, new Color(255 -blue, (int) (122 - blue *.5),blue));
//            blue = (blue + 5) % 255;
//        }
    }

    private static void drawSmallDot(BufferedImage buff, int x, int y, Color color) {
        float count = 10f;
        for(int i=0; i< count; ++i) {
//            float times = i/count;
//            int xp = (int) (6*times*Math.cos(2f*Math.PI*(i/count))) + x;
//            int yp = (int) (6*times*Math.sin(2f*Math.PI*(i/count))) + y;
//            buff.setRGB(xp,yp, color.getRGB());
            int xp = (int) (3*Math.cos(2f*Math.PI*(i/count))) + x;
            int yp = (int) (3*Math.sin(2f*Math.PI*(i/count))) + y;
            buff.setRGB(xp,yp, color.getRGB());
        }
    }

    private static void drawDot(BufferedImage buff, int x, int y, Color color) {
        float count = 300f;
        for(int i=0; i< count; ++i) {
//            float times = i/count;
//            int xp = (int) (6*times*Math.cos(2f*Math.PI*(i/count))) + x;
//            int yp = (int) (6*times*Math.sin(2f*Math.PI*(i/count))) + y;
//            buff.setRGB(xp,yp, color.getRGB());
            int xp = (int) (10*Math.cos(2f*Math.PI*(i/count))) + x;
            int yp = (int) (10*Math.sin(2f*Math.PI*(i/count))) + y;
            buff.setRGB(xp,yp, color.getRGB());
        }
    }

    private static void setDebugPosition(Coord3 pos, boolean columnPosition) {
        if (columnPosition) pos = Chunk.ToWorldPosition(pos);
        setDebugPosition(pos.toVector3(), columnPosition);
    }

    private static void setDebugPosition(Vector3f pos, boolean columnPosition) {
        if (columnPosition) DebugPosition = pos;
    }


}
