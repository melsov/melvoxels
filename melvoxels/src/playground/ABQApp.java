package playground;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

class Producer
        implements Runnable
{
    private BlockingQueue<String> drop;
    List<String> messages = Arrays.asList(
            "Mares eat oats",
            "Does eat oats",
            "Little lambs eat ivy",
            "Wouldn't you eat ivy too?");

    public Producer(BlockingQueue<String> d) {
        this.drop = d;
    }

    public void run()
    {
        try
        {
            for (String s : messages) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                drop.put(s);
                ABQApp.PrintThread();
            }
            drop.put("DONE");
        }
        catch (InterruptedException intEx)
        {
            System.out.println("Interrupted! " +
                    "Last one out, turn out the lights!");
        }
    }
}

class Consumer
        implements Runnable
{
    private BlockingQueue<String> drop;
    public Consumer(BlockingQueue<String> d) {
        this.drop = d;
    }

    public void run()
    {
        try
        {
            String msg = null;
//            while (!((msg = drop.take()).equals("DONE")))
            while (true) {
                if (msg == null) {
                    msg = drop.poll(500, TimeUnit.MILLISECONDS);
                }
                if (msg != null) {
                    System.out.println(msg);
                    if (((msg).equals("DONE"))) {
                        break;
                    }
                } else {
                    System.out.println("twas null");

                }
                ABQApp.PrintThread();
                msg = null;
            }
        }
        catch (InterruptedException intEx)
        {
            System.out.println("Interrupted! " +
                    "Last one out, turn out the lights!");
        }
    }
}

public class ABQApp
{
    public static void main(String[] args)
    {
        PrintThread();
        BlockingQueue<String> drop = new ArrayBlockingQueue(1, true);
        (new Thread(new Producer(drop))).start();
        (new Thread(new Consumer(drop))).start();
    }

    public static void PrintThread() {
        System.out.println(Thread.currentThread().getName());
    }
}
