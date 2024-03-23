package net.nick6464.flyingislands.structure;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.StructureType;
import net.nick6464.flyingislands.FlyingIslands;
import net.nick6464.flyingislands.world.gen.structure.IslandStructure;

public class ModGenerators {

    public static StructurePieceType ISLAND_GEN;
    public static StructureType<IslandStructure> ISLAND_STRUCTURE;

    private static StructurePieceType registerPiece(StructurePieceType.Simple type) {
        return Registry.register(Registries.STRUCTURE_PIECE,
                Identifier.of(FlyingIslands.MOD_ID, "island"),
                type);
    }

    public static void registerGenerators() {
        Identifier ISLAND_ID = Identifier.of(FlyingIslands.MOD_ID, "island");

        ISLAND_GEN = registerPiece(IslandGenerator::new);
        RegistryKey.of(RegistryKeys.STRUCTURE, ISLAND_ID);
        ISLAND_STRUCTURE = Registry.register(Registries.STRUCTURE_TYPE,
                ISLAND_ID,
                () -> IslandStructure.CODEC);
    }
}
