package pro.komaru.tridot.client.gfx.particle.behavior.component;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import pro.komaru.tridot.client.gfx.trail.TrailPointBuilder;
import pro.komaru.tridot.client.render.RenderBuilder;

import javax.annotation.Nullable;

public class TrailParticleBehaviorComponent extends ParticleBehaviorComponent implements RenderBuilder.TrailSegmentSprite{
    public TrailPointBuilder trailPointBuilder = TrailPointBuilder.create(10);

    public float st;
    public float mt;
    public float et;

    public float r;
    public float g;
    public float b;
    public float a;

    public float[] hsv1 = new float[3], hsv2 = new float[3];

    @Nullable public TextureAtlasSprite headSegmentSprite;
    @Nullable public TextureAtlasSprite tailSegmentSprite;
    @Nullable public TextureAtlasSprite[] segmentSprites;
    public boolean hasSegmentOverrides;

    @Override
    @Nullable
    public TextureAtlasSprite getSprite(int segmentIndex, int segmentCount){
        if(headSegmentSprite != null && segmentIndex == segmentCount - 1) return headSegmentSprite;
        if(tailSegmentSprite != null && segmentIndex == 0) return tailSegmentSprite;
        if(segmentSprites != null && segmentSprites.length > 0) return segmentSprites[Math.min(segmentIndex, segmentSprites.length - 1)];
        return null;
    }

    @Override
    public boolean flipV(int segmentIndex, int segmentCount){
        return headSegmentSprite != null && segmentIndex == segmentCount - 1;
    }
}
