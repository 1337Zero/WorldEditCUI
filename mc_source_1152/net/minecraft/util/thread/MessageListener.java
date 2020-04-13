package net.minecraft.util.thread;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface MessageListener<Msg> extends AutoCloseable {
   String getName();

   void send(Msg message);

   default void close() {
   }

   default <Source> CompletableFuture<Source> ask(Function<? super MessageListener<Source>, ? extends Msg> messageProvider) {
      CompletableFuture<Source> completableFuture = new CompletableFuture();
      completableFuture.getClass();
      Msg object = messageProvider.apply(create("ask future procesor handle", completableFuture::complete));
      this.send(object);
      return completableFuture;
   }

   static <Msg> MessageListener<Msg> create(final String name, final Consumer<Msg> action) {
      return new MessageListener<Msg>() {
         public String getName() {
            return name;
         }

         public void send(Msg message) {
            action.accept(message);
         }

         public String toString() {
            return name;
         }
      };
   }
}
