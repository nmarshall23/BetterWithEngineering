package betterwithengineering.base;

import betterwithengineering.BWE;
import betterwithengineering.ThermalExpansion;
import betterwithengineering.enc.ExNihilo;
import betterwithengineering.ie.HempFix;
import betterwithengineering.ie.MechPower;
import betterwithengineering.ie.TreatedWood;
import betterwithengineering.ie.SteelRebalance;
import betterwithmods.module.Feature;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.List;

public class CompatLoader {
    public static HashMultimap<String, Class<? extends Feature>> COMPAT_CLASS = HashMultimap.create();
    public static List<Feature> features = Lists.newArrayList();

    public static void construct() {
        if(BWE.ConfigManager.immersiveEngineering.mechPower.enabled)
            COMPAT_CLASS.put("immersiveengineering", MechPower.class);
        if(BWE.ConfigManager.immersiveEngineering.overrideIndustrialHempDrops)
            COMPAT_CLASS.put("immersiveengineering", HempFix.class);
        if(BWE.ConfigManager.addTreatedWood) {
            COMPAT_CLASS.put("immersiveengineering", TreatedWood.class);
        }
        if(BWE.ConfigManager.immersiveEngineering.steel.enabled)
            COMPAT_CLASS.put("immersiveengineering", SteelRebalance.class);
        COMPAT_CLASS.put("thermalexpansion", ThermalExpansion.class);
        if(BWE.ConfigManager.thermalExpansion.hardcoreInductionSmelter)
            COMPAT_CLASS.put("thermalexpansion", ThermalExpansion.class);
        if(BWE.ConfigManager.exNihilo.supportMechPower)
            COMPAT_CLASS.put("exnihilocreatio", ExNihilo.MechPower.class);
        for (String key : COMPAT_CLASS.keySet()) {
            if (Loader.isModLoaded(key)) {
                for (Class<? extends Feature> c : COMPAT_CLASS.get(key)) {
                    try {
                        Feature f = c.newInstance();
                        if (f.hasSubscriptions()) {
                            MinecraftForge.EVENT_BUS.register(f);
                        }
                        features.add(f);
                        BWE.logger.info("Loading BWE Compat: {}", f.configName);
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void preInit(FMLPreInitializationEvent event) {
        features.forEach(c -> c.preInit(event));
    }

    public static void init(FMLInitializationEvent event) {
        features.forEach(c -> c.init(event));
    }

    public static void postInit(FMLPostInitializationEvent event) {
        features.forEach(c -> c.postInit(event));
    }

    private static boolean isLoaded(String modid) {
        return Loader.isModLoaded(modid);
    }
}
