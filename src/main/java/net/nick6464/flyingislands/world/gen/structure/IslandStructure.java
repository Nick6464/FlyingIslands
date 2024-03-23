package net.nick6464.flyingislands.world.gen.structure;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import net.nick6464.flyingislands.structure.IslandGenerator;
import net.nick6464.flyingislands.structure.ModGenerators;

public class IslandStructure
        extends Structure {
    public static final Codec<IslandStructure> CODEC = createCodec(IslandStructure::new);

    public IslandStructure(Structure.Config config) {
        super(config);
    }

    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
        return getStructurePosition(context, Heightmap.Type.WORLD_SURFACE_WG, (collector) -> {
            addPieces(collector, context);
        });
    }

    private static void addPieces(StructurePiecesCollector collector, Structure.Context context) {
        collector.addPiece(new IslandGenerator(context.random(), context.chunkPos().getStartX(), context.chunkPos().getStartZ()));
    }

    public StructureType<?> getType() {
        return ModGenerators.ISLAND_STRUCTURE;
    }
}

