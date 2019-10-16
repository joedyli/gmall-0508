package com.atguigu.gmall.item.test;

import java.util.concurrent.*;

public class CallableTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

//        FutureTask futureTask = new FutureTask<>(new MyCallable());
//        new Thread(futureTask).start();
//        System.out.println(futureTask.get()); // get方法会阻塞线程

        ExecutorService executorService = Executors.newFixedThreadPool(3);
//        Future future = executorService.submit(new MyCallable());
//        System.out.println(future.get());// 阻塞

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {  // 查询sku
            return "hello ";
        });


        CompletableFuture<String> future1 = future.thenApplyAsync(t -> {
            System.out.println("上一个任务的返回结果：" + t); // 查询spu
            return t + "world!";
        });

        CompletableFuture<String> future2 = future.thenApplyAsync(t -> {
            System.out.println("上一个任务的返回结果：" + t); // 查询品牌
            return t + " CompletableFuture";
        });

        future1.thenCombineAsync(future2, (t, u)->{
            System.out.println("t: " + t);
            System.out.println("u: " + u);
            return "合并任务";
        });

        CompletableFuture<Void> future3 = CompletableFuture.allOf(future1, future2);
        future3.get();
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");



//        return itemVO;

//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("初始化了一个completableFuture对象");// 查询sku
//            int i = 1/0;
//            return "hello ";
//        }).whenComplete((t, u) -> {
//
//        }).exceptionally(t -> {
//            System.out.println("t: " + t);
//            return "你出现异常了！！";
//        }).handleAsync((t, u) -> {
//            System.out.println("handle t: " + t);
//            System.out.println("handle u: " + u);
//            return "handler......";
//        });
    }
}

class MyCallable implements Callable{

    @Override
    public Object call() throws Exception {
        System.out.println("你好，我是" + Thread.currentThread().getName());
        return "hello call!";
    }
}
