/*
 * AE2 Crystal Science - RainbowColoring (1.12.2)
 * Port of ae2lt 1.21.1 com.moakiee.ae2lt.recipe.RainbowPigmeeColoring.
 *
 * 彩虹猪咪的染色引擎：把一件物品推进到 MC 16 色的下一色，绝不无中生有。
 * 1.12.2 适配点：
 *   - 皮革染色：DataComponents.DYED_COLOR → ItemArmor 的 display.color NBT
 *   - 盾牌底色：1.12.2 无此特性，不移植
 *   - 颜色族识别：注册名模式（ae2lt 唯一途径）之外，增加 metadata 颜色族
 *     （方块 BlockState 含 EnumDyeColor 类型的 "color" 属性，羊毛/玻璃/混凝土等）
 *   - 无色基底：从已注册合成配方反推（n 件 + 1 染料 → n 件同色成品）
 * 1.12.2 无配方热重载，识别结果首次使用时惰性构建一次即可。
 */
package pigtech.recipe;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RainbowColoring {

    private static final EnumDyeColor[] COLORS = EnumDyeColor.values();

    /** 矿物词典染料名（按 EnumDyeColor 元数据序；1.12.2 的"淡灰"注册名是 silver） */
    private static final String[] DYE_ORE_NAMES = {
            "dyeWhite", "dyeOrange", "dyeMagenta", "dyeLightBlue",
            "dyeYellow", "dyeLime", "dyePink", "dyeGray",
            "dyeLightGray", "dyeCyan", "dyePurple", "dyeBlue",
            "dyeBrown", "dyeGreen", "dyeRed", "dyeBlack"};

    /** 长名字先匹配，避免 light_blue/silver 被 blue/gray 抢先截断（对齐源码 NAME_ORDER） */
    private static final EnumDyeColor[] NAME_ORDER = Arrays.stream(COLORS)
            .sorted(Comparator.comparingInt((EnumDyeColor color) -> color.getName().length()).reversed())
            .toArray(EnumDyeColor[]::new);

    private RainbowColoring() {
    }

    /** 一组完整的 16 色变体；color 为当前色，null 表示无色基底 */
    private static final class Variant {
        final ItemStack[] items;
        final EnumDyeColor color;

        Variant(ItemStack[] items, EnumDyeColor color) {
            this.items = items;
            this.color = color;
        }
    }

    // ==================== 对外入口 ====================

    /** 推进到下一色；不可染色时返回 EMPTY */
    public static ItemStack next(ItemStack input) {
        if (input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (isDyeableLeather(input)) {
            ItemArmor armor = (ItemArmor) input.getItem();
            // 对齐源码：未染过色从白色开始；混合色从最接近的 MC 色继续循环
            EnumDyeColor target = armor.hasColor(input)
                    ? nextColor(nearestColor(armor.getColor(input)))
                    : EnumDyeColor.WHITE;
            ItemStack result = input.copy();
            result.setCount(1);
            armor.setColor(result, target.getColorValue());
            return result;
        }
        Variant variant = resolve(input);
        if (variant == null) {
            return ItemStack.EMPTY;
        }
        EnumDyeColor target = variant.color == null ? EnumDyeColor.WHITE : nextColor(variant.color);
        return withVariant(input, variant.items[target.getMetadata()]);
    }

    // ==================== 颜色数学（纯搬运） ====================

    private static EnumDyeColor nextColor(EnumDyeColor color) {
        return EnumDyeColor.byMetadata((color.getMetadata() + 1) % COLORS.length);
    }

    private static EnumDyeColor nearestColor(int rgb) {
        EnumDyeColor nearest = EnumDyeColor.WHITE;
        int distance = Integer.MAX_VALUE;
        for (EnumDyeColor color : COLORS) {
            int candidate = color.getColorValue();
            int red = ((rgb >> 16) & 255) - ((candidate >> 16) & 255);
            int green = ((rgb >> 8) & 255) - ((candidate >> 8) & 255);
            int blue = (rgb & 255) - (candidate & 255);
            int squared = red * red + green * green + blue * blue;
            if (squared < distance) {
                nearest = color;
                distance = squared;
            }
        }
        return nearest;
    }

    private static boolean isDyeableLeather(ItemStack stack) {
        return stack.getItem() instanceof ItemArmor
                && ((ItemArmor) stack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER;
    }

    /** 换到变体族里的另一件物品，保留数量以外的全部 NBT（名称、附魔、容器内容） */
    private static ItemStack withVariant(ItemStack input, ItemStack variantStack) {
        ItemStack result = variantStack.copy();
        result.setCount(1);
        if (input.hasTagCompound()) {
            result.setTagCompound(input.getTagCompound().copy());
        }
        return result;
    }

    // ==================== 变体族识别 ====================

    private static volatile Map<Item, Variant> uncolored;

    /** 注册名族的查询缓存（含否定结果）；合成匹配每键变化都会调用，必须便宜 */
    private static final Map<Item, Variant> ID_FAMILY_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Variant NO_FAMILY = new Variant(new ItemStack[0], null);

    /** 完整解析：有色（metadata 族 / 注册名族）→ 无色基底（配方反推） */
    @Nullable
    private static Variant resolve(ItemStack stack) {
        Variant colored = resolveColored(stack);
        if (colored != null) {
            return colored;
        }
        return uncoloredVariants().get(stack.getItem());
    }

    /** 仅识别"已经是某种颜色"的物品；无色基底不在此列（配方反推有先后依赖） */
    @Nullable
    private static Variant resolveColored(ItemStack stack) {
        Variant meta = metaFamily(stack);
        if (meta != null) {
            return meta;
        }
        Variant cached = ID_FAMILY_CACHE.computeIfAbsent(stack.getItem(), item -> {
            Variant found = idFamily(item);
            return found != null ? found : NO_FAMILY;
        });
        return cached == NO_FAMILY ? null : cached;
    }

    /**
     * metadata 颜色族：方块的 BlockState 含 EnumDyeColor 类型的 "color" 属性，
     * 物品损伤值即颜色元数据（羊毛、地毯、染色玻璃/板、染色粘土、混凝土(粉)、床）。
     */
    @Nullable
    private static Variant metaFamily(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemBlock)) {
            return null;
        }
        Block block = ((ItemBlock) stack.getItem()).getBlock();
        IProperty<?> property = block.getBlockState().getProperty("color");
        if (!(property instanceof PropertyEnum) || property.getValueClass() != EnumDyeColor.class) {
            return null;
        }
        EnumDyeColor color = EnumDyeColor.byMetadata(stack.getMetadata() & 15);
        ItemStack[] items = new ItemStack[COLORS.length];
        for (EnumDyeColor c : COLORS) {
            items[c.getMetadata()] = new ItemStack(stack.getItem(), 1, c.getMetadata());
        }
        return new Variant(items, color);
    }

    /** 注册名颜色族：路径中嵌入颜色名，且 16 色变体全部存在（潜影盒、模组彩色线缆等） */
    @Nullable
    private static Variant idFamily(Item item) {
        ResourceLocation id = item.getRegistryName();
        if (id == null) {
            return null;
        }
        String path = id.getPath();
        for (EnumDyeColor color : NAME_ORDER) {
            // 1.12.2 的 SILVER 注册名是 silver，但源码系模组使用 light_gray——两个名字都要认
            for (String name : idNames(color)) {
                for (int start = path.indexOf(name); start >= 0; start = path.indexOf(name, start + 1)) {
                    int end = start + name.length();
                    if ((start == 0 || path.charAt(start - 1) == '_' || path.charAt(start - 1) == '/')
                            && (end == path.length() || path.charAt(end) == '_' || path.charAt(end) == '/')) {
                        Variant family = family(id.getNamespace(), path.substring(0, start), path.substring(end), color);
                        if (family != null) {
                            return family;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private static Variant family(String namespace, String prefix, String suffix, EnumDyeColor current) {
        ItemStack[] items = new ItemStack[COLORS.length];
        for (EnumDyeColor color : COLORS) {
            Item found = null;
            for (String name : idNames(color)) {
                found = ForgeRegistries.ITEMS.getValue(new ResourceLocation(namespace, prefix + name + suffix));
                if (found != null && found != net.minecraft.init.Items.AIR) {
                    break;
                }
                found = null;
            }
            if (found == null) {
                return null;
            }
            items[color.getMetadata()] = new ItemStack(found);
        }
        return new Variant(items, current);
    }

    /** 颜色在注册名里的候选写法：silver 与 light_gray 互为别名 */
    private static String[] idNames(EnumDyeColor color) {
        return color == EnumDyeColor.SILVER ? new String[]{"silver", "light_gray"} : new String[]{color.getName()};
    }

    // ==================== 无色基底：配方反推 ====================

    private static Map<Item, Variant> uncoloredVariants() {
        Map<Item, Variant> result = uncolored;
        if (result == null) {
            synchronized (RainbowColoring.class) {
                result = uncolored;
                if (result == null) {
                    uncolored = result = discoverUncolored();
                }
            }
        }
        return result;
    }

    /**
     * 扫描全部有序/无序合成配方，识别模式：n 件相同基底 + 1 份对应染料 → n 件同色成品。
     * 一种基底能染成多种不同物品时视为歧义，不自动选择（对齐源码）。
     */
    private static Map<Item, Variant> discoverUncolored() {
        Map<Item, Variant> result = new HashMap<>();
        Set<Item> ambiguous = new HashSet<>();
        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            if (recipe.isDynamic() || recipe instanceof RecipeRainbowPigmeeCycle) {
                continue;
            }
            if (!(recipe instanceof ShapedRecipes || recipe instanceof ShapelessRecipes)) {
                continue;
            }
            ItemStack output = recipe.getRecipeOutput();
            if (output.isEmpty() || output.hasTagCompound()) {
                continue;
            }
            Variant colored = resolveColored(output);
            if (colored == null || colored.color == null) {
                continue;
            }

            int dyes = 0;
            List<Ingredient> materials = new ArrayList<>();
            for (Ingredient ingredient : recipe.getIngredients()) {
                ItemStack[] options = ingredient.getMatchingStacks();
                if (options.length == 0) {
                    continue;
                }
                boolean allDye = true;
                for (ItemStack option : options) {
                    if (!isDye(option, colored.color)) {
                        allDye = false;
                        break;
                    }
                }
                if (allDye) {
                    dyes++;
                } else {
                    materials.add(ingredient);
                }
            }
            if (dyes != 1 || materials.isEmpty() || output.getCount() != materials.size()) {
                continue;
            }

            // 每个材料槽必须接受同一件物品：n 件输入恰好换 n 件成品
            for (ItemStack candidate : materials.get(0).getMatchingStacks()) {
                if (candidate.isEmpty() || resolveColored(candidate) != null) {
                    continue;
                }
                boolean acceptedByAll = true;
                for (Ingredient material : materials) {
                    if (!material.apply(candidate)) {
                        acceptedByAll = false;
                        break;
                    }
                }
                if (acceptedByAll) {
                    addUncolored(result, ambiguous, candidate.getItem(),
                            new Variant(colored.items, null));
                }
            }
        }
        ambiguous.forEach(result::remove);
        return java.util.Collections.unmodifiableMap(result);
    }

    private static boolean isDye(ItemStack stack, EnumDyeColor color) {
        if (stack.getItem() == net.minecraft.init.Items.DYE) {
            return EnumDyeColor.byDyeDamage(stack.getMetadata()) == color;
        }
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            if (DYE_ORE_NAMES[color.getMetadata()].equals(OreDictionary.getOreName(oreId))) {
                return true;
            }
        }
        return false;
    }

    private static void addUncolored(Map<Item, Variant> result, Set<Item> ambiguous, Item item, Variant family) {
        Variant existing = result.putIfAbsent(item, family);
        if (existing != null && !sameFamily(existing.items, family.items)) {
            ambiguous.add(item);
        }
    }

    private static boolean sameFamily(ItemStack[] a, ItemStack[] b) {
        for (int i = 0; i < a.length; i++) {
            if (!ItemStack.areItemsEqual(a[i], b[i])) {
                return false;
            }
        }
        return true;
    }
}
