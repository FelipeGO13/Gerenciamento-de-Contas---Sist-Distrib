package SyncPrimitive;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import Conexao.ZooKeeperConnection;
import bean.Transacao;

/*
 * Algoritmos do prof para barriers, queues e locks
 * 
 */
public class BarrierQueueLock implements Watcher {

    static ZooKeeper zk = null;
    static Integer mutex;

    String root;

    BarrierQueueLock(String address) {
        if(zk == null){
            try {
                System.out.println("Starting ZK:");
                ZooKeeperConnection conexao = new ZooKeeperConnection();
   	         	zk = conexao.connect("localhost");
                mutex = new Integer(-1);
                System.out.println("Finished starting ZK: " + zk);
            } catch (Exception e) {
                System.out.println(e.toString());
                zk = null;
            }
        }
        //else mutex = new Integer(-1);
    }

    synchronized public void process(WatchedEvent event) {
        synchronized (mutex) {
            //System.out.println("Process: " + event.getType());
            mutex.notify();
        }
    }

    /**
     * Barrier
     */
    static public class Barrier extends BarrierQueueLock {
        int size;
        String name;

        /**
         * Barrier constructor
         *
         * @param address
         * @param root
         * @param size
         */
        Barrier(String address, String root, int size) {
            super(address);
            this.root = root;
            this.size = size;

            // Create barrier node
            if (zk != null) {
                try {
                    Stat s = zk.exists(root, false);
                    if (s == null) {
                        zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    }
                } catch (KeeperException e) {
                    System.out
                            .println("Keeper exception when instantiating queue: "
                                    + e.toString());
                } catch (InterruptedException e) {
                    System.out.println("Interrupted exception");
                }
            }

            // My node name
            try {
                name = new String(InetAddress.getLocalHost().getCanonicalHostName().toString());
            } catch (UnknownHostException e) {
                System.out.println(e.toString());
            }

        }

        /**
         * Join barrier
         *
         * @return
         * @throws KeeperException
         * @throws InterruptedException
         */

        boolean enter() throws KeeperException, InterruptedException{
            zk.create(root + "/" + name, new byte[0], Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL);
            while (true) {
                synchronized (mutex) {
                    List<String> list = zk.getChildren(root, true);

                    if (list.size() < size) {
                        mutex.wait();
                    } else {
                        return true;
                    }
                }
            }
        }

        /**
         * Wait until all reach barrier
         *
         * @return
         * @throws KeeperException
         * @throws InterruptedException
         */

        boolean leave() throws KeeperException, InterruptedException{
            zk.delete(root + "/" + name, 0);
            while (true) {
                synchronized (mutex) {
                    List<String> list = zk.getChildren(root, true);
                        if (list.size() > 0) {
                            mutex.wait();
                        } else {
                            return true;
                        }
                    }
                }
        }
    }

    /**
     * Producer-Consumer queue
     */
    static public class Queue extends BarrierQueueLock {

        /**
         * Constructor of producer-consumer queue
         *
         * @param address
         * @param name
         */
        public Queue(String address, String name) {
            super(address);
            this.root = name;
            // Create ZK node name
            if (zk != null) {
                try {
                    Stat s = zk.exists(root, false);
                    if (s == null) {
                        zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    }
                } catch (KeeperException e) {
                    System.out.println("Keeper exception when instantiating queue: " + e.toString());
                } catch (InterruptedException e) {
                    System.out.println("Interrupted exception");
                }
            }
        }

        /**
         * Add element to the queue.
         *
         * @param i
         * @return
         */

        boolean produce(Transacao t) throws KeeperException, InterruptedException{
            ByteBuffer b = ByteBuffer.allocate(4);
            byte[] dadosTransacao =  t.toString().getBytes();
            
            zk.create(root + "/element", dadosTransacao, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);

            return true;
        }


        /**
         * Remove first element from the queue.
         *
         * @return
         * @throws KeeperException
         * @throws InterruptedException
         */
        int consume() throws KeeperException, InterruptedException{
            int retvalue = -1;
            Stat stat = null;

            // Get the first element available
            while (true) {
                synchronized (mutex) {
                    List<String> list = zk.getChildren(root, true);
                    if (list.size() == 0) {
                        System.out.println("Going to wait");
                        mutex.wait();
                    } else {
                        Integer min = new Integer(list.get(0).substring(7));
                        System.out.println("List: "+list.toString());
                        String minString = list.get(0);
                        for(String s : list){
                            Integer tempValue = new Integer(s.substring(7));
                            //System.out.println("Temp value: " + tempValue);
                            if(tempValue < min) { 
                            	min = tempValue;
                            	minString = s;
                            }
                        }
                       System.out.println("Temporary value: " + root +"/"+ minString);
                        byte[] b = zk.getData(root +"/"+ minString,false, stat);
                        //System.out.println("b: " + Arrays.toString(b)); 	
                        zk.delete(root +"/"+ minString, 0);
                        ByteBuffer buffer = ByteBuffer.wrap(b);
                        retvalue = buffer.getInt();
                        return retvalue;
                    }
                }
            }
        }
    }

    static public class Lock extends BarrierQueueLock {
    	long wait;
	String pathName;
    	 /**
         * Constructor of lock
         *
         * @param address
         * @param name Name of the lock node
         */
        Lock(String address, String name, long waitTime) {
            super(address);
            this.root = name;
            this.wait = waitTime;
            // Create ZK node name
            if (zk != null) {
                try {
                    Stat s = zk.exists(root, false);
                    if (s == null) {
                        zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    }
                } catch (KeeperException e) {
                    System.out.println("Keeper exception when instantiating queue: " + e.toString());
                } catch (InterruptedException e) {
                    System.out.println("Interrupted exception");
                }
            }
        }
        
        boolean lock() throws KeeperException, InterruptedException{
            //Step 1
            pathName = zk.create(root + "/lock-", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println("My path name is: "+pathName);
            //Steps 2 to 5
            return testMin();
        }
        
	boolean testMin() throws KeeperException, InterruptedException{
	    while (true) {
		 Integer suffix = new Integer(pathName.substring(12));
	         //Step 2 
            	 List<String> list = zk.getChildren(root, false);
                 Integer min = new Integer(list.get(0).substring(5));
                 System.out.println("List: "+list.toString());
                 String minString = list.get(0);
                 for(String s : list){
                 	Integer tempValue = new Integer(s.substring(5));
                 	//System.out.println("Temp value: " + tempValue);
                 	if(tempValue < min)  {
                 		min = tempValue;
                 		minString = s;
                 	}
                 }
                System.out.println("Suffix: "+suffix+", min: "+min);
           	//Step 3
             	if (suffix.equals(min)) {
            		System.out.println("Lock acquired for "+minString+"!");
            		return true;
            	}
            	//Step 4
            	//Wait for the removal of the next lowest sequence number
            	Integer max = min;
            	String maxString = minString;
            	for(String s : list){
            		Integer tempValue = new Integer(s.substring(5));
            		//System.out.println("Temp value: " + tempValue);
            		if(tempValue > max && tempValue < suffix)  {
            			max = tempValue;
            			maxString = s;
            		}
            	}
            	//Exists with watch
            	Stat s = zk.exists(root+"/"+maxString, this);
            	System.out.println("Watching "+root+"/"+maxString);
            	//Step 5
            	if (s != null) {
            	    //Wait for notification
            	    break;  
            	}
	    }
            System.out.println(pathName+" is waiting for a notification!");
	    return false;
	}

        synchronized public void process(WatchedEvent event) {
            synchronized (mutex) {
            	String path = event.getPath();
            	if (event.getType() == Event.EventType.NodeDeleted) {
            		System.out.println("Notification from "+path);
			try {
			    if (testMin()) { //Step 5 (cont.) -> go to step 2 to check
				this.compute();
			    } else {
				System.out.println("Not lowest sequence number! Waiting for a new notification.");
			    }
			} catch (Exception e) {e.printStackTrace();}
            	}
            }
        }
        
        void compute() {
        	System.out.println("Lock acquired!");
    		try {
				new Thread().sleep(wait);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		//Exits, which releases the ephemeral node (Unlock operation)
    		System.out.println("Lock released!");
    		System.exit(0);
        }
    }


    public static void queueTest(Queue q, String tipo, Transacao t, int max) {

        System.out.println("Executando transações pendentes");
        int i;

        if (tipo.equals("p")) {
            System.out.println("Producer");
                try{
                    q.produce(t);
                } catch (KeeperException e){
                    e.printStackTrace();
                } catch (InterruptedException e){
			    e.printStackTrace();
                }
        } else {
            System.out.println("Consumer");

            for (i = 0; i < max; i++) {
                try{
                    int r = q.consume();
                    System.out.println("Item: " + r);
                } catch (KeeperException e){
                    i--;
                } catch (InterruptedException e){
			    e.printStackTrace();
                }
            }
        }
    }

    public static void barrierTest(String args[]) {
        Barrier b = new Barrier(args[1], "/b1", new Integer(args[2]));
        try{
            boolean flag = b.enter();
            System.out.println("Entered barrier: " + args[2]);
            if(!flag) System.out.println("Error when entering the barrier");
        } catch (KeeperException e){

        } catch (InterruptedException e){

        }

        // Generate random integer
        Random rand = new Random();
        int r = rand.nextInt(100);
        // Loop for rand iterations
        for (int i = 0; i < r; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }
        try{
            b.leave();
        } catch (KeeperException e){

        } catch (InterruptedException e){

        }
        System.out.println("Left barrier");
    }
    
    public static void lockTest(String args[]) {
    	Lock lock = new Lock(args[1],"/lock",new Long(args[2]));
        try{
        	boolean success = lock.lock();
        	if (success) {
        		lock.compute();
        	} else {
        		while(true) {
        			//Waiting for a notification
        		}
            }         
        } catch (KeeperException e){
        	e.printStackTrace();
        } catch (InterruptedException e){
        	e.printStackTrace();
        }
    }
    
}
