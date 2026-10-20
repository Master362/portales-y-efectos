package com.dedportals.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class GreenSparkParticle extends TextureSheetParticle {
    protected GreenSparkParticle(ClientLevel level, double x, double y, double z,
                                 double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.lifetime = 20 + this.random.nextInt(25);
        this.quadSize = 0.08F + this.random.nextFloat() * 0.1F;
        this.gravity = -0.01F;
        this.friction = 0.96F;
        this.hasPhysics = false;
        this.setColor(0.6F + this.random.nextFloat() * 0.3F, 1.0F, 0.7F);
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = 1.0F - (float) this.age / (float) this.lifetime;
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.quadSize * (1.0F - 0.6F * ((this.age + partialTick) / this.lifetime));
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z, double xd, double yd, double zd) {
            return new GreenSparkParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
