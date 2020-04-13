package net.minecraft.entity.ai.pathing;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkCache;

public class PathNodeNavigator {
   private final PathMinHeap minHeap = new PathMinHeap();
   private final Set<PathNode> field_59 = Sets.newHashSet();
   private final PathNode[] successors = new PathNode[32];
   private final int range;
   private final PathNodeMaker pathNodeMaker;

   public PathNodeNavigator(PathNodeMaker pathNodeMaker, int range) {
      this.pathNodeMaker = pathNodeMaker;
      this.range = range;
   }

   @Nullable
   public Path findPathToAny(ChunkCache world, MobEntity mob, Set<BlockPos> positions, float followRange, int distance, float rangeMultiplier) {
      this.minHeap.clear();
      this.pathNodeMaker.init(world, mob);
      PathNode pathNode = this.pathNodeMaker.getStart();
      Map<TargetPathNode, BlockPos> map = (Map)positions.stream().collect(Collectors.toMap((blockPos) -> {
         return this.pathNodeMaker.getNode((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
      }, Function.identity()));
      Path path = this.findPathToAny(pathNode, map, followRange, distance, rangeMultiplier);
      this.pathNodeMaker.clear();
      return path;
   }

   @Nullable
   private Path findPathToAny(PathNode startNode, Map<TargetPathNode, BlockPos> positions, float followRange, int distance, float rangeMultiplier) {
      Set<TargetPathNode> set = positions.keySet();
      startNode.penalizedPathLength = 0.0F;
      startNode.distanceToNearestTarget = this.calculateDistances(startNode, set);
      startNode.heapWeight = startNode.distanceToNearestTarget;
      this.minHeap.clear();
      this.field_59.clear();
      this.minHeap.push(startNode);
      int i = 0;
      int j = (int)((float)this.range * rangeMultiplier);

      while(!this.minHeap.isEmpty()) {
         ++i;
         if (i >= j) {
            break;
         }

         PathNode pathNode = this.minHeap.pop();
         pathNode.visited = true;
         set.stream().filter((targetPathNode) -> {
            return pathNode.getManhattanDistance((PathNode)targetPathNode) <= (float)distance;
         }).forEach(TargetPathNode::markReached);
         if (set.stream().anyMatch(TargetPathNode::isReached)) {
            break;
         }

         if (pathNode.getDistance(startNode) < followRange) {
            int k = this.pathNodeMaker.getSuccessors(this.successors, pathNode);

            for(int l = 0; l < k; ++l) {
               PathNode pathNode2 = this.successors[l];
               float f = pathNode.getDistance(pathNode2);
               pathNode2.pathLength = pathNode.pathLength + f;
               float g = pathNode.penalizedPathLength + f + pathNode2.penalty;
               if (pathNode2.pathLength < followRange && (!pathNode2.isInHeap() || g < pathNode2.penalizedPathLength)) {
                  pathNode2.previous = pathNode;
                  pathNode2.penalizedPathLength = g;
                  pathNode2.distanceToNearestTarget = this.calculateDistances(pathNode2, set) * 1.5F;
                  if (pathNode2.isInHeap()) {
                     this.minHeap.setNodeWeight(pathNode2, pathNode2.penalizedPathLength + pathNode2.distanceToNearestTarget);
                  } else {
                     pathNode2.heapWeight = pathNode2.penalizedPathLength + pathNode2.distanceToNearestTarget;
                     this.minHeap.push(pathNode2);
                  }
               }
            }
         }
      }

      Stream stream2;
      if (set.stream().anyMatch(TargetPathNode::isReached)) {
         stream2 = set.stream().filter(TargetPathNode::isReached).map((targetPathNode) -> {
            return this.createPath(targetPathNode.getNearestNode(), (BlockPos)positions.get(targetPathNode), true);
         }).sorted(Comparator.comparingInt(Path::getLength));
      } else {
         stream2 = set.stream().map((targetPathNode) -> {
            return this.createPath(targetPathNode.getNearestNode(), (BlockPos)positions.get(targetPathNode), false);
         }).sorted(Comparator.comparingDouble(Path::getManhattanDistanceFromTarget).thenComparingInt(Path::getLength));
      }

      Optional<Path> optional = stream2.findFirst();
      if (!optional.isPresent()) {
         return null;
      } else {
         Path path = (Path)optional.get();
         return path;
      }
   }

   private float calculateDistances(PathNode node, Set<TargetPathNode> targets) {
      float f = Float.MAX_VALUE;

      float g;
      for(Iterator var4 = targets.iterator(); var4.hasNext(); f = Math.min(g, f)) {
         TargetPathNode targetPathNode = (TargetPathNode)var4.next();
         g = node.getDistance(targetPathNode);
         targetPathNode.updateNearestNode(g, node);
      }

      return f;
   }

   private Path createPath(PathNode endNode, BlockPos target, boolean reachesTarget) {
      List<PathNode> list = Lists.newArrayList();
      PathNode pathNode = endNode;
      list.add(0, endNode);

      while(pathNode.previous != null) {
         pathNode = pathNode.previous;
         list.add(0, pathNode);
      }

      return new Path(list, target, reachesTarget);
   }
}
