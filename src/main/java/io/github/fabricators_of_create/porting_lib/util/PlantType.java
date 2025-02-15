package io.github.fabricators_of_create.porting_lib.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class PlantType {
	private static final Pattern INVALID_CHARACTERS = Pattern.compile("[^a-z_]"); //Only a-z and _ are allowed, meaning names must be lower case. And use _ to separate words.
	private static final Map<String, PlantType> VALUES = new ConcurrentHashMap<>();

	public static final PlantType PLAINS = get("plains");
	public static final PlantType DESERT = get("desert");
	public static final PlantType BEACH = get("beach");
	public static final PlantType CAVE = get("cave");
	public static final PlantType WATER = get("water");
	public static final PlantType NETHER = get("nether");
	public static final PlantType CROP = get("crop");
	private final String name;

	private PlantType(String name) {
		this.name = name;
	}

	/**
	 * Getting a custom {@link PlantType}, or an existing one if it has the same name as that one. Your plant should implement {@link IPlantable}
	 * and return this custom type in {@link IPlantable#getPlantType(BlockGetter, BlockPos)}.
	 *
	 * <p>If your new plant grows on blocks like any one of them above, never create a new {@link PlantType}.
	 * This Type is only functioning in
	 * {@link Block#canSustainPlant(BlockState, BlockGetter, BlockPos, Direction, IPlantable)},
	 * which you are supposed to override this function in your new block and create a new plant type to grow on that block.
	 * <p>
	 * This method can be called during parallel loading
	 *
	 * @param name the name of the type of plant, you had better follow the style above
	 * @return the acquired {@link PlantType}, a new one if not found.
	 */
	public static PlantType get(String name) {
		return VALUES.computeIfAbsent(name, e ->
		{
			if (INVALID_CHARACTERS.matcher(e).find())
				throw new IllegalArgumentException("PlantType.get() called with invalid name: " + name);
			return new PlantType(e);
		});
	}

	public String getName() {
		return name;
	}
}

