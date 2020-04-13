package net.minecraft.structure.pool;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.block.JigsawBlock;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureFeatures;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructurePoolBasedGenerator {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final StructurePoolRegistry REGISTRY = new StructurePoolRegistry();

   public static void addPieces(Identifier startPoolId, int size, StructurePoolBasedGenerator.PieceFactory pieceFactory, ChunkGenerator<?> chunkGenerator, StructureManager structureManager, BlockPos pos, List<StructurePiece> pieces, Random random) {
      StructureFeatures.initialize();
      new StructurePoolBasedGenerator.StructurePoolGenerator(startPoolId, size, pieceFactory, chunkGenerator, structureManager, pos, pieces, random);
   }

   static {
      REGISTRY.add(StructurePool.EMPTY);
   }

   public interface PieceFactory {
      PoolStructurePiece create(StructureManager structureManager, StructurePoolElement poolElement, BlockPos pos, int i, BlockRotation rotation, BlockBox elementBounds);
   }

   static final class StructurePoolGenerator {
      private final int maxSize;
      private final StructurePoolBasedGenerator.PieceFactory pieceFactory;
      private final ChunkGenerator<?> chunkGenerator;
      private final StructureManager structureManager;
      private final List<StructurePiece> children;
      private final Random random;
      private final Deque<StructurePoolBasedGenerator.ShapedPoolStructurePiece> structurePieces = Queues.newArrayDeque();

      public StructurePoolGenerator(Identifier startingPool, int maxSize, StructurePoolBasedGenerator.PieceFactory pieceFactory, ChunkGenerator<?> chunkGenerator, StructureManager structureManager, BlockPos blockPos, List<StructurePiece> children, Random random) {
         this.maxSize = maxSize;
         this.pieceFactory = pieceFactory;
         this.chunkGenerator = chunkGenerator;
         this.structureManager = structureManager;
         this.children = children;
         this.random = random;
         BlockRotation blockRotation = BlockRotation.random(random);
         StructurePool structurePool = StructurePoolBasedGenerator.REGISTRY.get(startingPool);
         StructurePoolElement structurePoolElement = structurePool.getRandomElement(random);
         PoolStructurePiece poolStructurePiece = pieceFactory.create(structureManager, structurePoolElement, blockPos, structurePoolElement.method_19308(), blockRotation, structurePoolElement.getBoundingBox(structureManager, blockPos, blockRotation));
         BlockBox blockBox = poolStructurePiece.getBoundingBox();
         int i = (blockBox.maxX + blockBox.minX) / 2;
         int j = (blockBox.maxZ + blockBox.minZ) / 2;
         int k = chunkGenerator.method_20402(i, j, Heightmap.Type.WORLD_SURFACE_WG);
         poolStructurePiece.translate(0, k - (blockBox.minY + poolStructurePiece.getGroundLevelDelta()), 0);
         children.add(poolStructurePiece);
         if (maxSize > 0) {
            int l = true;
            Box box = new Box((double)(i - 80), (double)(k - 80), (double)(j - 80), (double)(i + 80 + 1), (double)(k + 80 + 1), (double)(j + 80 + 1));
            this.structurePieces.addLast(new StructurePoolBasedGenerator.ShapedPoolStructurePiece(poolStructurePiece, new AtomicReference(VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(box), VoxelShapes.cuboid(Box.from(blockBox)), BooleanBiFunction.ONLY_FIRST)), k + 80, 0));

            while(!this.structurePieces.isEmpty()) {
               StructurePoolBasedGenerator.ShapedPoolStructurePiece shapedPoolStructurePiece = (StructurePoolBasedGenerator.ShapedPoolStructurePiece)this.structurePieces.removeFirst();
               this.generatePiece(shapedPoolStructurePiece.piece, shapedPoolStructurePiece.pieceShape, shapedPoolStructurePiece.minY, shapedPoolStructurePiece.currentSize);
            }

         }
      }

      private void generatePiece(PoolStructurePiece piece, AtomicReference<VoxelShape> pieceShape, int minY, int currentSize) {
         StructurePoolElement structurePoolElement = piece.getPoolElement();
         BlockPos blockPos = piece.getPos();
         BlockRotation blockRotation = piece.getRotation();
         StructurePool.Projection projection = structurePoolElement.getProjection();
         boolean bl = projection == StructurePool.Projection.RIGID;
         AtomicReference<VoxelShape> atomicReference = new AtomicReference();
         BlockBox blockBox = piece.getBoundingBox();
         int i = blockBox.minY;
         Iterator var13 = structurePoolElement.getStructureBlockInfos(this.structureManager, blockPos, blockRotation, this.random).iterator();

         while(true) {
            while(true) {
               label90:
               while(var13.hasNext()) {
                  Structure.StructureBlockInfo structureBlockInfo = (Structure.StructureBlockInfo)var13.next();
                  Direction direction = (Direction)structureBlockInfo.state.get(JigsawBlock.FACING);
                  BlockPos blockPos2 = structureBlockInfo.pos;
                  BlockPos blockPos3 = blockPos2.offset(direction);
                  int j = blockPos2.getY() - i;
                  int k = -1;
                  StructurePool structurePool = StructurePoolBasedGenerator.REGISTRY.get(new Identifier(structureBlockInfo.tag.getString("target_pool")));
                  StructurePool structurePool2 = StructurePoolBasedGenerator.REGISTRY.get(structurePool.getTerminatorsId());
                  if (structurePool != StructurePool.INVALID && (structurePool.getElementCount() != 0 || structurePool == StructurePool.EMPTY)) {
                     boolean bl2 = blockBox.contains(blockPos3);
                     AtomicReference atomicReference3;
                     int m;
                     if (bl2) {
                        atomicReference3 = atomicReference;
                        m = i;
                        if (atomicReference.get() == null) {
                           atomicReference.set(VoxelShapes.cuboid(Box.from(blockBox)));
                        }
                     } else {
                        atomicReference3 = pieceShape;
                        m = minY;
                     }

                     List<StructurePoolElement> list = Lists.newArrayList();
                     if (currentSize != this.maxSize) {
                        list.addAll(structurePool.getElementIndicesInRandomOrder(this.random));
                     }

                     list.addAll(structurePool2.getElementIndicesInRandomOrder(this.random));
                     Iterator var26 = list.iterator();

                     while(var26.hasNext()) {
                        StructurePoolElement structurePoolElement2 = (StructurePoolElement)var26.next();
                        if (structurePoolElement2 == EmptyPoolElement.INSTANCE) {
                           break;
                        }

                        Iterator var28 = BlockRotation.randomRotationOrder(this.random).iterator();

                        label117:
                        while(var28.hasNext()) {
                           BlockRotation blockRotation2 = (BlockRotation)var28.next();
                           List<Structure.StructureBlockInfo> list2 = structurePoolElement2.getStructureBlockInfos(this.structureManager, BlockPos.ORIGIN, blockRotation2, this.random);
                           BlockBox blockBox2 = structurePoolElement2.getBoundingBox(this.structureManager, BlockPos.ORIGIN, blockRotation2);
                           int o;
                           if (blockBox2.getBlockCountY() > 16) {
                              o = 0;
                           } else {
                              o = list2.stream().mapToInt((structureBlockInfox) -> {
                                 if (!blockBox2.contains(structureBlockInfox.pos.offset((Direction)structureBlockInfox.state.get(JigsawBlock.FACING)))) {
                                    return 0;
                                 } else {
                                    Identifier identifier = new Identifier(structureBlockInfox.tag.getString("target_pool"));
                                    StructurePool structurePool = StructurePoolBasedGenerator.REGISTRY.get(identifier);
                                    StructurePool structurePool2 = StructurePoolBasedGenerator.REGISTRY.get(structurePool.getTerminatorsId());
                                    return Math.max(structurePool.method_19309(this.structureManager), structurePool2.method_19309(this.structureManager));
                                 }
                              }).max().orElse(0);
                           }

                           Iterator var33 = list2.iterator();

                           StructurePool.Projection projection2;
                           boolean bl3;
                           int q;
                           int r;
                           int t;
                           BlockBox blockBox4;
                           BlockPos blockPos6;
                           int w;
                           do {
                              Structure.StructureBlockInfo structureBlockInfo2;
                              do {
                                 if (!var33.hasNext()) {
                                    continue label117;
                                 }

                                 structureBlockInfo2 = (Structure.StructureBlockInfo)var33.next();
                              } while(!JigsawBlock.attachmentMatches(structureBlockInfo, structureBlockInfo2));

                              BlockPos blockPos4 = structureBlockInfo2.pos;
                              BlockPos blockPos5 = new BlockPos(blockPos3.getX() - blockPos4.getX(), blockPos3.getY() - blockPos4.getY(), blockPos3.getZ() - blockPos4.getZ());
                              BlockBox blockBox3 = structurePoolElement2.getBoundingBox(this.structureManager, blockPos5, blockRotation2);
                              int p = blockBox3.minY;
                              projection2 = structurePoolElement2.getProjection();
                              bl3 = projection2 == StructurePool.Projection.RIGID;
                              q = blockPos4.getY();
                              r = j - q + ((Direction)structureBlockInfo.state.get(JigsawBlock.FACING)).getOffsetY();
                              if (bl && bl3) {
                                 t = i + r;
                              } else {
                                 if (k == -1) {
                                    k = this.chunkGenerator.method_20402(blockPos2.getX(), blockPos2.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                                 }

                                 t = k - q;
                              }

                              int u = t - p;
                              blockBox4 = blockBox3.translated(0, u, 0);
                              blockPos6 = blockPos5.add(0, u, 0);
                              if (o > 0) {
                                 w = Math.max(o + 1, blockBox4.maxY - blockBox4.minY);
                                 blockBox4.maxY = blockBox4.minY + w;
                              }
                           } while(VoxelShapes.matchesAnywhere((VoxelShape)atomicReference3.get(), VoxelShapes.cuboid(Box.from(blockBox4).contract(0.25D)), BooleanBiFunction.ONLY_SECOND));

                           atomicReference3.set(VoxelShapes.combine((VoxelShape)atomicReference3.get(), VoxelShapes.cuboid(Box.from(blockBox4)), BooleanBiFunction.ONLY_FIRST));
                           w = piece.getGroundLevelDelta();
                           int y;
                           if (bl3) {
                              y = w - r;
                           } else {
                              y = structurePoolElement2.method_19308();
                           }

                           PoolStructurePiece poolStructurePiece = this.pieceFactory.create(this.structureManager, structurePoolElement2, blockPos6, y, blockRotation2, blockBox4);
                           int ab;
                           if (bl) {
                              ab = i + j;
                           } else if (bl3) {
                              ab = t + q;
                           } else {
                              if (k == -1) {
                                 k = this.chunkGenerator.method_20402(blockPos2.getX(), blockPos2.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                              }

                              ab = k + r / 2;
                           }

                           piece.addJunction(new JigsawJunction(blockPos3.getX(), ab - j + w, blockPos3.getZ(), r, projection2));
                           poolStructurePiece.addJunction(new JigsawJunction(blockPos2.getX(), ab - q + y, blockPos2.getZ(), -r, projection));
                           this.children.add(poolStructurePiece);
                           if (currentSize + 1 <= this.maxSize) {
                              this.structurePieces.addLast(new StructurePoolBasedGenerator.ShapedPoolStructurePiece(poolStructurePiece, atomicReference3, m, currentSize + 1));
                           }
                           continue label90;
                        }
                     }
                  } else {
                     StructurePoolBasedGenerator.LOGGER.warn("Empty or none existent pool: {}", structureBlockInfo.tag.getString("target_pool"));
                  }
               }

               return;
            }
         }
      }
   }

   static final class ShapedPoolStructurePiece {
      private final PoolStructurePiece piece;
      private final AtomicReference<VoxelShape> pieceShape;
      private final int minY;
      private final int currentSize;

      private ShapedPoolStructurePiece(PoolStructurePiece piece, AtomicReference<VoxelShape> pieceShape, int minY, int currentSize) {
         this.piece = piece;
         this.pieceShape = pieceShape;
         this.minY = minY;
         this.currentSize = currentSize;
      }
   }
}
