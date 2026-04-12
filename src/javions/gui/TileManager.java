package javions.gui;

import javions.Preconditions;
import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * manage the tile view
 *  @author Ramy Hatimy (356771)
 *  @author Nada Eddahir (356653)
 */
public class TileManager {
    private static final int MAX_MEMORY_CACHE_SIZE = 100;
    private final String tileServer;
    private final LinkedHashMap<TileId,Image> cacheMemory = new LinkedHashMap<>(128, 0.75F,true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<TileId, Image> eldest) {
            return size() > MAX_MEMORY_CACHE_SIZE;
        }
    };
    private final Path pathCacheDisk;

    /**
     * represents an OSM tile manager. Its role is to get the tiles from a
     * tile server and store them in a memory cache and in a disk cache.
     * @param pathCacheDisk the path to the cache disk
     * @param tileServer server from where the tile are extracted
     * @throws NullPointerException if one of the two arguments is null
     */
    public TileManager(Path pathCacheDisk ,String tileServer){
        this.tileServer = Objects.requireNonNull(tileServer);
        this.pathCacheDisk = Objects.requireNonNull(pathCacheDisk);
    }

    /**
     * represents the identity of an OSM tile
     * @param zoom depicts the zoom of the tile
     * @param x the position x of the tile
     * @param y the position y of the tile
     */
    public record TileId(int zoom, int x, int y){
        private static final int ZOOM_SUP = 19;
        private static final int ZOOM_INF = 6;

        public TileId{
            Preconditions.checkArgument(isValid(zoom,x,y));
        }

        public static boolean isValid(int zoom, int x, int y){
            if (!(ZOOM_INF <= zoom && zoom <= ZOOM_SUP)) {
                return false;
            }
            int maxCoordinate = 1 << zoom;
            return (0<=x) && (x<maxCoordinate) && (0<=y) && (y<maxCoordinate);
        }
    }

    /**
     * return the image of the tile pass as parameter
     * @param tile the tile ID of the image we want to return
     * @return the corresponding image of the tile
     */
    public Image imageForTileAt(TileId tile) throws IOException {
        if(cacheMemory.containsKey(tile)){
            return cacheMemory.get(tile);
        } else if (doesFileExist(pathCacheDisk,tile)){
            try(InputStream file = new FileInputStream(tilePath(pathCacheDisk,tile).toFile())){
                cacheMemory.put(tile,new Image(file));
                return cacheMemory.get(tile);
            }
        }else{
            URL u = new URL(
                    "https://"+tileServer+"/"+tile.zoom()+"/"+tile.x()+"/"+tile.y()+".png");
            URLConnection c = u.openConnection();
            c.setRequestProperty("User-Agent", "Javions");
            try(InputStream i = c.getInputStream()){
                byte[] stream = i.readAllBytes();
                //here we get the parent of the file pathCacheDik/zoom/x
                Files.createDirectories(tilePath(pathCacheDisk,tile).getParent());
                //here I create the file that contains the tile
                Files.createFile(tilePath(pathCacheDisk,tile));
                try(OutputStream cacheDisk = new FileOutputStream(tilePath(pathCacheDisk,tile).toFile())){
                    cacheDisk.write(stream);
                    InputStream streamImage = new ByteArrayInputStream(stream);
                    cacheMemory.put(tile,new Image(streamImage));
                    return cacheMemory.get(tile);
                }
            }
        }
    }
    private boolean doesFileExist(Path pathCacheDisk,TileId tile){
        return Files.exists(tilePath(pathCacheDisk,tile));
    }
    private Path tilePath(Path pathCacheDisk,TileId tile){
        return pathCacheDisk.resolve(Paths.get
                (String.valueOf(tile.zoom()), String.valueOf(tile.x()), tile.y()+".png"));
    }
}
