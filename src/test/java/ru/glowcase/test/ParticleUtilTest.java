package ru.glowcase.test;

import org.bukkit.Particle;
import ru.glowcase.effect.ParticleUtil;

public class ParticleUtilTest {

    public static void run() {
        System.out.println("Running ParticleUtilTest...");

        // Test 1: Exact lookup
        Particle p1 = ParticleUtil.getParticle("FLAME", Particle.REDSTONE);
        if (p1 != Particle.FLAME) {
            throw new AssertionError("Expected FLAME but got: " + p1);
        }

        // Test 2: Fallback on unknown
        Particle p2 = ParticleUtil.getParticle("NON_EXISTENT_PARTICLE_XYZ", Particle.HEART);
        if (p2 != Particle.HEART) {
            throw new AssertionError("Expected fallback HEART but got: " + p2);
        }

        // Test 3: Null / empty check
        Particle p3 = ParticleUtil.getParticle(null, Particle.FIREWORKS_SPARK);
        if (p3 != Particle.FIREWORKS_SPARK) {
            throw new AssertionError("Expected fallback FIREWORKS_SPARK but got: " + p3);
        }

        System.out.println("  [PASSED] ParticleUtilTest (Lookup & fallback safety)");
    }
}
