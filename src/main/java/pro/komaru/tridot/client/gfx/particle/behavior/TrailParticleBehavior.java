package pro.komaru.tridot.client.gfx.particle.behavior;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.*;
import net.minecraft.util.*;
import pro.komaru.tridot.client.gfx.*;
import pro.komaru.tridot.client.gfx.particle.*;
import pro.komaru.tridot.client.gfx.particle.behavior.component.*;
import pro.komaru.tridot.client.gfx.particle.data.*;
import pro.komaru.tridot.client.gfx.trail.*;
import pro.komaru.tridot.client.render.*;
import pro.komaru.tridot.util.*;
import pro.komaru.tridot.util.phys.*;

import javax.annotation.*;
import java.util.*;
import java.util.function.*;

public class TrailParticleBehavior extends ParticleBehavior implements ICustomBehaviorParticleRender{
    public ColorParticleData colorData;
    public GenericParticleData transparencyData;
    public boolean secondColor;
    public int trailSize;
    Function<Float, Float> widthFunc;
    @Nullable public List<ResourceLocation> segmentTextures;
    @Nullable public ResourceLocation headSegmentTexture;
    public float headSegmentLength = 0F;
    @Nullable public ResourceLocation tailSegmentTexture;

    public TrailParticleBehavior(ColorParticleData colorData, GenericParticleData transparencyData, boolean secondColor, int trailSize, Function<Float, Float> widthFunc, SpinParticleData xSpinData, SpinParticleData ySpinData, SpinParticleData zSpinData, float xOffset, float yOffset, float zOffset, boolean firstSide, boolean secondSide, boolean camera, boolean xRotCam, boolean yRotCam){
        this(colorData, transparencyData, secondColor, trailSize, widthFunc, xSpinData, ySpinData, zSpinData, xOffset, yOffset, zOffset, firstSide, secondSide, camera, xRotCam, yRotCam, null, null, 0F, null);
    }

    public TrailParticleBehavior(ColorParticleData colorData, GenericParticleData transparencyData, boolean secondColor, int trailSize, Function<Float, Float> widthFunc, SpinParticleData xSpinData, SpinParticleData ySpinData, SpinParticleData zSpinData, float xOffset, float yOffset, float zOffset, boolean firstSide, boolean secondSide, boolean camera, boolean xRotCam, boolean yRotCam, @Nullable List<ResourceLocation> segmentTextures, @Nullable ResourceLocation headSegmentTexture, float headSegmentLength, @Nullable ResourceLocation tailSegmentTexture){
        super(xSpinData, ySpinData, zSpinData, xOffset, yOffset, zOffset, firstSide, secondSide, camera, xRotCam, yRotCam);
        this.colorData = colorData;
        this.transparencyData = transparencyData;
        this.secondColor = secondColor;
        this.trailSize = trailSize;
        this.widthFunc = widthFunc;
        this.segmentTextures = segmentTextures != null ? new ArrayList<>(segmentTextures) : null;
        this.headSegmentTexture = headSegmentTexture;
        this.headSegmentLength = headSegmentLength;
        this.tailSegmentTexture = tailSegmentTexture;
    }

    public TrailParticleBehavior copy(){
        return new TrailParticleBehavior(
            colorData, transparencyData, secondColor, trailSize, widthFunc,
            xSpinData, ySpinData, zSpinData, xOffset, yOffset, zOffset,
            firstSide, secondSide, camera, xRotCam, yRotCam,
            segmentTextures != null ? new ArrayList<>(segmentTextures) : null,
            headSegmentTexture,
            headSegmentLength,
            tailSegmentTexture
        );
    }

    public static TrailParticleBehaviorBuilder create(){
        return new TrailParticleBehaviorBuilder(0, 0, 0);
    }

    public static TrailParticleBehaviorBuilder create(float xOffset, float yOffset, float zOffset){
        return new TrailParticleBehaviorBuilder((float)Math.toRadians(xOffset), (float)Math.toRadians(yOffset), (float)Math.toRadians(zOffset));
    }

    public TrailParticleBehaviorComponent getComponent(){
        return new TrailParticleBehaviorComponent();
    }

    public TrailParticleBehaviorComponent getTrailComponent(GenericParticle particle){
        if(particle.behaviorComponent instanceof TrailParticleBehaviorComponent behaviorComponent){
            return behaviorComponent;
        }

        return getComponent();
    }

    @Override
    public void init(GenericParticle particle){
        super.init(particle);
        TrailParticleBehaviorComponent component = getTrailComponent(particle);
        component.trailPointBuilder = TrailPointBuilder.create(trailSize);

        float r1 = GenericParticle.pickRandomValue(colorData.r1, colorData.rr11, colorData.rr12);
        float g1 = GenericParticle.pickRandomValue(colorData.g1, colorData.rg11, colorData.rg12);
        float b1 = GenericParticle.pickRandomValue(colorData.b1, colorData.rb11, colorData.rb12);
        float r2 = GenericParticle.pickRandomValue(colorData.r2, colorData.rr21, colorData.rr22);
        float g2 = GenericParticle.pickRandomValue(colorData.g2, colorData.rg21, colorData.rg22);
        float b2 = GenericParticle.pickRandomValue(colorData.b2, colorData.rb21, colorData.rb21);

        component.st = GenericParticle.pickRandomValue(transparencyData.startingValue, transparencyData.rs1, transparencyData.rs2);
        component.mt = GenericParticle.pickRandomValue(transparencyData.middleValue, transparencyData.rm1, transparencyData.rm2);
        component.et = GenericParticle.pickRandomValue(transparencyData.endingValue, transparencyData.re1, transparencyData.re2);

        Col.RGBtoHSV(r1, g1, b1, component.hsv1);
        Col.RGBtoHSV(r2, g2, b2, component.hsv2);
        initSegmentSprites(component);
    }

    protected void initSegmentSprites(TrailParticleBehaviorComponent component){
        component.headSegmentSprite = null;
        component.tailSegmentSprite = null;
        component.segmentSprites = null;
        boolean hasList = segmentTextures != null && !segmentTextures.isEmpty();
        if(headSegmentTexture == null && tailSegmentTexture == null && !hasList){
            component.hasSegmentOverrides = false;
            return;
        }

        AbstractTexture atlasTex = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_PARTICLES);
        if(!(atlasTex instanceof TextureAtlas atlas)){
            component.hasSegmentOverrides = false;
            return;
        }

        if(headSegmentTexture != null) component.headSegmentSprite = atlas.getSprite(headSegmentTexture);
        if(tailSegmentTexture != null) component.tailSegmentSprite = atlas.getSprite(tailSegmentTexture);
        if(hasList){
            component.segmentSprites = new TextureAtlasSprite[segmentTextures.size()];
            for(int i = 0; i < segmentTextures.size(); i++){
                ResourceLocation tex = segmentTextures.get(i);
                component.segmentSprites[i] = tex != null ? atlas.getSprite(tex) : null;
            }
        }

        component.hasSegmentOverrides = component.headSegmentSprite != null || component.tailSegmentSprite != null || component.segmentSprites != null;
    }

    public void pickColor(GenericParticle particle, float coeff){
        TrailParticleBehaviorComponent component = getTrailComponent(particle);
        float[] hsv1 = component.hsv1, hsv2 = component.hsv2;
        var col = Col.HSVtoRGB(hsv1[0], hsv1[1] * 100f, hsv1[2] * 100f);
        var col2 = Col.HSVtoRGB(hsv2[0], hsv2[1] * 100f, hsv2[2] * 100f);
        col.lerp(col2,coeff);
        setColor(particle, col.r,col.g,col.b);
    }

    public void setColor(GenericParticle particle, float r, float g, float b){
        TrailParticleBehaviorComponent component = getTrailComponent(particle);
        component.r = r;
        component.g = g;
        component.b = b;
    }

    @Override
    public void updateTraits(GenericParticle particle){
        TrailParticleBehaviorComponent component = getTrailComponent(particle);
        component.trailPointBuilder.add(Vec3f.from(particle.getPosition()));
        component.trailPointBuilder.tick();

        pickColor(particle, colorData.colorCurveEasing.apply(colorData.getProgress(particle.age, particle.lifetime)));
        component.a = transparencyData.getValue(particle.age, particle.lifetime, component.st, component.mt, component.et);
    }

    @Override
    public void updateRenderTraits(GenericParticle particle, float partialTicks){
        TrailParticleBehaviorComponent component = getTrailComponent(particle);
        pickColor(particle, colorData.colorCurveEasing.apply(colorData.getProgress(particle.age, particle.lifetime)));
        component.a = transparencyData.getValue(particle.age, particle.lifetime, component.st, component.mt, component.et);
    }

    @Override
    public void render(GenericParticle particle, VertexConsumer vertexConsumer, Camera renderInfo, float partialTicks){
        if(particle.shouldRenderTraits) updateRenderTraits(particle, partialTicks);
        TridotParticles.addBehaviorParticleList(particle, this);
    }

    @Override
    public void render(GenericParticle particle, PoseStack poseStack, MultiBufferSource buffer, float partialTicks){
        TrailParticleBehaviorComponent component = getTrailComponent(particle);
        List<TrailPoint> trail = new ArrayList<>(component.trailPointBuilder.points());
        if(trail.size() > 2 && particle.getAge() > component.trailPointBuilder.trailLength.get()){
            TrailPoint position = trail.get(0);
            TrailPoint nextPosition = trail.get(1);
            float x = Mth.lerp(partialTicks, position.getPosition().x, nextPosition.getPosition().x);
            float y = Mth.lerp(partialTicks, position.getPosition().y, nextPosition.getPosition().y);
            float z = Mth.lerp(partialTicks, position.getPosition().z, nextPosition.getPosition().z);
            trail.set(0, new TrailPoint(new Vec3f(x, y, z)));
        }

        float x = (float)(Mth.lerp(partialTicks, particle.xo, particle.x));
        float y = (float)(Mth.lerp(partialTicks, particle.yo, particle.y));
        float z = (float)(Mth.lerp(partialTicks, particle.zo, particle.z));
        if(!trail.isEmpty()){
            trail.set(trail.size() - 1, new TrailPoint(new Vec3f(x, y, z)));
        }

        RenderBuilder builder = RenderBuilder.create().setRenderType(particle.renderType).setSided(firstSide, secondSide)
        .setUV(particle.getU0(), particle.getV0(), particle.getU1(), particle.getV1())
        .setColorRaw(particle.getRed(), particle.getGreen(), particle.getBlue())
        .setAlpha(particle.getAlpha())
        .setLight(particle.getLightColor(partialTicks));
        if(secondColor){
            builder.setSecondColorRaw(component.r, component.g, component.b)
            .setSecondAlpha(component.a);
        }

        if(widthFunc == null) widthFunc = (f) -> f;

        if(component.headSegmentSprite != null && trail.size() >= 2){
            float headLen = headSegmentLength > 0f ? headSegmentLength : Math.max(0.01f, widthFunc.apply(1f) * (particle.getSize() / 2f));
            applyHeadSegmentSplit(trail, headLen);
        }

        builder.renderTrail(poseStack, trail, (f) -> widthFunc.apply(f) * (particle.getSize() / 2f), component.hasSegmentOverrides ? component : null);
    }

    private static final float MIN_SEGMENT_LENGTH = 1e-5f;
    private static final float SPLIT_EPSILON = 1e-3f;
    private static void applyHeadSegmentSplit(List<TrailPoint> trail, float targetLen){
        if(trail.size() < 2 || targetLen <= 0f) return;
        float accumulated = 0f;
        for(int i = trail.size() - 2; i >= 0; i--){
            Vec3f pPrev = trail.get(i).getPosition();
            Vec3f pNext = trail.get(i + 1).getPosition();
            float dx = pNext.x - pPrev.x;
            float dy = pNext.y - pPrev.y;
            float dz = pNext.z - pPrev.z;
            float segLen = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
            if(accumulated + segLen >= targetLen && segLen > MIN_SEGMENT_LENGTH){
                float need = targetLen - accumulated;
                float t = Mth.clamp(need / segLen, 0f, 1f);
                boolean snapToNext = t <= SPLIT_EPSILON;
                boolean snapToPrev = t >= 1f - SPLIT_EPSILON;
                int keepIndex = snapToNext ? i + 1 : i;

                while(trail.size() - 1 > keepIndex + 1){
                    trail.remove(trail.size() - 2);
                }

                if(!snapToNext && !snapToPrev){
                    Vec3f neck = new Vec3f(
                        Mth.lerp(t, pNext.x, pPrev.x),
                        Mth.lerp(t, pNext.y, pPrev.y),
                        Mth.lerp(t, pNext.z, pPrev.z)
                    );

                    trail.add(trail.size() - 1, new TrailPoint(neck));
                }

                return;
            }

            accumulated += segLen;
        }
    }
}