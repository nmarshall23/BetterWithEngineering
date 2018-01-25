package betterwithengineering.ie;

import betterwithengineering.BWE;
import betterwithmods.BWMod;
import betterwithmods.common.BWMBlocks;
import betterwithmods.common.BWMRecipes;
import betterwithmods.common.blocks.BlockUrn;
import betterwithmods.common.blocks.mechanical.BlockCookingPot;
import betterwithmods.common.items.ItemMaterial;
import betterwithmods.common.registry.bulk.manager.CrucibleManager;
import betterwithmods.module.Feature;
import betterwithmods.module.ModuleLoader;
import betterwithmods.module.gameplay.*;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockArcFurnace;
import blusunrize.immersiveengineering.common.util.RotationUtil;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.List;

public class SteelRebalance extends Feature {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        //Remove Recipes
        BWMRecipes.removeRecipe(new ItemStack(IEContent.itemMetal, 1, 8));
        BWMRecipes.removeRecipe(new ItemStack(IEContent.itemMetal, 1, 28));

        BWMRecipes.removeRecipe(new ResourceLocation(ImmersiveEngineering.MODID, "metal_storage/steel_block"));
        BWMRecipes.removeRecipe(new ResourceLocation(ImmersiveEngineering.MODID, "metal_storage/steel_slab_back"));
        BWMRecipes.removeRecipe(new ResourceLocation(ImmersiveEngineering.MODID, "metal_storage/steel_slab"));
        BWMRecipes.addRecipe(new ShapedOreRecipe(new ResourceLocation(BWMod.MODID, "steel_slabs"),
                new ItemStack(IEContent.blockStorageSlabs, 1, 8), "BBB", 'B', new ItemStack(BWMBlocks.STEEL_BLOCK)));
        BWMRecipes.addRecipe(new ShapelessOreRecipe(new ResourceLocation(BWMod.MODID, "steel_block_convert"),
                new ItemStack(BWMBlocks.STEEL_BLOCK), new ItemStack(IEContent.blockStorage, 1, 8)));

    }

    public void hackMultiblocks() {
        RotationUtil.permittedRotation.add(state -> !(state.getBlock() instanceof BlockCookingPot));
        MultiblockArcFurnace.instance.getStructureManual()[0][0][2] = BlockCookingPot.getStack(BlockCookingPot.EnumType.CRUCIBLE);
        MultiblockHandler.getMultiblocks().removeIf(mb -> mb instanceof MultiblockArcFurnace);
        MultiblockHandler.registerMultiblock(BWMMultiBlockArcFurnance.instance);
    }

    @Override
    public void init(FMLInitializationEvent event) {
//        hackMultiblocks();
        //Remove Recipes
        ArcFurnaceRecipe.removeRecipes(new ItemStack(IEContent.itemMetal, 1, 8));
        int ratio = BWE.ConfigManager.immersiveEngineering.steel.ratio;
        if (ModuleLoader.isFeatureEnabled(HarderSteelRecipe.class)) {
            ArcFurnaceRecipe.addRecipe(ItemMaterial.getMaterial(ItemMaterial.EnumMaterial.INGOT_STEEL, ratio), new IngredientStack("ingotIron", ratio), BlockUrn.getStack(BlockUrn.EnumType.EMPTY, 1), 512, 100,
                    new IngredientStack("dustCoke", ratio), BlockUrn.getStack(BlockUrn.EnumType.FULL, 1), ItemMaterial.getMaterial(ItemMaterial.EnumMaterial.SOUL_FLUX, ratio));
        } else {
            ArcFurnaceRecipe.addRecipe(ItemMaterial.getMaterial(ItemMaterial.EnumMaterial.INGOT_STEEL, ratio), new IngredientStack("ingotIron", ratio), BlockUrn.getStack(BlockUrn.EnumType.EMPTY, 1), 512, 100,
                    new IngredientStack("dustCoke", ratio), BlockUrn.getStack(BlockUrn.EnumType.FULL, 1));
        }


        //Add Recipes
        MillRecipes.addMillRecipe(new ItemStack(IEContent.itemMaterial, 1, 17), new String[]{"fuelCoke"});
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        //OreDictionary
        removeAllOredict("ingotSteel");
        OreDictionary.registerOre("ingotSteel", ItemMaterial.getMaterial(ItemMaterial.EnumMaterial.INGOT_STEEL));

        removeAllOredict("blockSteel");
        OreDictionary.registerOre("blockSteel", new ItemStack(BWMBlocks.STEEL_BLOCK));

        CrucibleManager.getInstance().removeRecipe(new ItemStack(IEContent.itemMetal, 1, 8), ItemStack.EMPTY);
        FurnaceRecipes.instance().getSmeltingList().remove(new ItemStack(IEContent.itemMetal, 1, 17));


    }

    private static List<NonNullList<ItemStack>> OREDICT_CONTENTS = Lists.newArrayList(), OREDICT_CONTENTS_UN = Lists.newArrayList();

    static {
        OREDICT_CONTENTS = ReflectionHelper.getPrivateValue(OreDictionary.class, null, "idToStack");
        OREDICT_CONTENTS_UN = ReflectionHelper.getPrivateValue(OreDictionary.class, null, "idToStackUn");
    }

    public static void removeAllOredict(String ore) {
        int oreId = OreDictionary.getOreID(ore);
        OREDICT_CONTENTS.get(oreId).clear();
    }

    public static boolean removeOredict(String ore, ItemStack stack) {
        ItemStack result = ItemStack.EMPTY;
        for (ItemStack itemStack : OreDictionary.getOres(ore)) {
            if (OreDictionary.itemMatches(stack, itemStack, false)) {
                result = itemStack;
                break;
            }
        }
        if (!result.isEmpty()) {
            int oreId = OreDictionary.getOreID(ore);
            OREDICT_CONTENTS.get(oreId).remove(result);
            return true;
        }
        return false;
    }


}