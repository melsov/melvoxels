package voxel.landscape.chunkbuild;

import voxel.landscape.VoxelLandscape;
import voxel.landscape.collection.ColumnMap;
import voxel.landscape.map.TerrainMap;
import voxel.landscape.map.light.ChunkSunLightComputer;
import voxel.landscape.noise.TerrainDataProvider;

import static java.lang.Thread.sleep;

public class AsyncGenerateColumnData extends ResponsiveRunnable
{
	private int x,z;
	private TerrainMap terrainMap;
	private ColumnMap columnMap;
    private TerrainDataProvider dataProvider = new TerrainDataProvider(VoxelLandscape.WorldSettings.seed);
	public AsyncGenerateColumnData(final TerrainMap _terrainMap, final ColumnMap _columnMap, int xx, int zz) {
		columnMap = _columnMap;
		x = xx; z = zz;
		terrainMap = _terrainMap;
	}
	@Override
	public void doRun() {
        try {
            sleep(200); //temp...
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        terrainMap.generateSurface(x, z, dataProvider, null);
		ChunkSunLightComputer.ComputeRays(terrainMap, x, z);
		ChunkSunLightComputer.Scatter(terrainMap, columnMap, x, z); //TEST WANT
	}
	
	public int getX() { return x; }
	public int getZ() { return z; }
	
}