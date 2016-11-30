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
import bean.Cliente;
import bean.Transacao;
import controle.ControleTransacao;

/*
 * Algoritmos do prof para barriers, queues e locks
 * 
 */
public class BarrierQueueLock implements Watcher {

	static ZooKeeper zk = null;
	static Integer mutex;

	String root;

	BarrierQueueLock(String address) {
		if (zk == null) {
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
		// else mutex = new Integer(-1);
	}

	synchronized public void process(WatchedEvent event) {
		synchronized (mutex) {
			// System.out.println("Process: " + event.getType());
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
		public Barrier(String address, String root, int size, String leaderPath) {
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
					System.out.println("Keeper exception when instantiating queue: " + e.toString());
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

		boolean enter(Transacao t) throws KeeperException, InterruptedException {
			byte[] dadosTransacao = t.toString().getBytes();
			zk.create(root + "/" + name, dadosTransacao, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
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

		boolean leave() throws KeeperException, InterruptedException {
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
		public Queue(String address, String name, String leaderPath) {
			super(address);
			this.root = name;
			// Create ZK node name
			if (zk != null) {
				try {
					Stat s = zk.exists(root, false);
					if (s == null) {
						byte[] b = new byte[1];
						b = ByteBuffer.allocate(4).putInt(0).array();
						zk.create(root, b, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
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

		boolean produce(Transacao t) throws KeeperException, InterruptedException {
			Stat stat = null;

			byte[] b = zk.getData("/filaTransacao", false, stat);
			ByteBuffer buffer = ByteBuffer.wrap(b);
			System.out.println(b.toString());
			int numTransacoes = buffer.getInt();
			numTransacoes++;
			b = ByteBuffer.allocate(4).putInt(numTransacoes).array();
			zk.setData("/filaTransacao", b, -1);
			byte[] dadosTransacao = t.toString().getBytes();
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
		void consume() throws KeeperException, InterruptedException {
			Stat stat = null;
			ControleTransacao controleTransacao = null;
			Transacao t = null;
			// Get the first element available
			while (true) {
				synchronized (mutex) {
					List<String> list = zk.getChildren(root, true);
					if (list.size() == 0) {
						System.out.println("Going to wait");
						mutex.wait();
					} else {
						Integer min = new Integer(list.get(0).substring(7));
						System.out.println("List: " + list.toString());
						String minString = list.get(0);
						for (String s : list) {
							Integer tempValue = new Integer(s.substring(7));
							if (tempValue < min) {
								min = tempValue;
								minString = s;
							}
						}
						try {
							controleTransacao = new ControleTransacao();

							System.out.println("Temporary value: " + root + "/" + minString);

							byte[] b = zk.getData(root + "/" + minString, false, stat);

							String dados = new String(b, "UTF-8");
							String[] dadosTransacao = dados.split(",");
							System.out.println("Processando dados da transação... ");

							int codigo = Integer.parseInt(dadosTransacao[0]);
							String cpfCliente = dadosTransacao[1];
							Cliente c = new Cliente();
							c.setCpf(cpfCliente);
							int operacao = Integer.parseInt(dadosTransacao[2]);
							double valor = Double.parseDouble(dadosTransacao[3]);
							String data = dadosTransacao[4];

							String descricao = dadosTransacao[5];

							t = new Transacao(codigo, descricao, operacao, valor, c, data);
							controleTransacao.processarTransacao(t);

							System.out.println("Transação: " + codigo + " processada com sucesso!");
							zk.delete(root + "/" + minString, 0);
						} catch (Exception e) {
							e.printStackTrace();
						}

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
		 * @param name
		 *            Name of the lock node
		 */
		public Lock(String address, String name, long waitTime, String leaderPath) {
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

		boolean lock(Transacao t) throws KeeperException, InterruptedException {
			// Step 1
			pathName = zk.create(root + "/lock-", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			System.out.println("My path name is: " + pathName);
			// Steps 2 to 5
			return testMin();
		}

		boolean testMin() throws KeeperException, InterruptedException {
			while (true) {
				Integer suffix = new Integer(pathName.substring(17));
				// Step 2
				List<String> list = zk.getChildren(root, false);
				Integer min = new Integer(list.get(0).substring(5));
				System.out.println("List: " + list.toString());
				String minString = list.get(0);
				for (String s : list) {
					Integer tempValue = new Integer(s.substring(5));
					// System.out.println("Temp value: " + tempValue);
					if (tempValue < min) {
						min = tempValue;
						minString = s;
					}
				}
				System.out.println("Suffix: " + suffix + ", min: " + min);
				// Step 3
				if (suffix.equals(min)) {
					System.out.println("Lock acquired for " + minString + "!");
					return true;
				}
				// Step 4
				// Wait for the removal of the next lowest sequence number
				Integer max = min;
				String maxString = minString;
				for (String s : list) {
					Integer tempValue = new Integer(s.substring(5));
					if (tempValue > max && tempValue < suffix) {
						max = tempValue;
						maxString = s;
					}
				}
				// Exists with watch
				Stat s = zk.exists(root + "/" + maxString, this);
				System.out.println("Watching " + root + "/" + maxString);
				// Step 5
				if (s != null) {
					// Wait for notification
					break;
				}
			}
			System.out.println(pathName + " is waiting for a notification!");
			return false;
		}

		synchronized public void process(WatchedEvent event, Transacao t) {
			synchronized (mutex) {
				String path = event.getPath();
				if (event.getType() == Event.EventType.NodeDeleted) {
					System.out.println("Notification from " + path);
					try {
						if (testMin()) { // Step 5 (cont.) -> go to step 2 to
											// check
							this.compute(t);
						} else {
							System.out.println("Not lowest sequence number! Waiting for a new notification.");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		void compute(Transacao t) {
			System.out.println("Lock acquired!");
			try {
				ControleTransacao controleTransacao = new ControleTransacao();
				controleTransacao.processarTransacao(t);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Exits, which releases the ephemeral node (Unlock operation)
			System.out.println("Lock released!");
			System.exit(0);
		}
	}

	public static void queueTest(Queue q, String tipo, Transacao t, int max) {

		System.out.println("Executando transações pendentes");
		int i;

		if (tipo.equals("p")) {
			System.out.println("Producer");
			try {
				q.produce(t);
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Consumer");

			for (i = 0; i < max; i++) {
				try {
					q.consume();

				} catch (KeeperException e) {
					i--;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void barrierTest(BarrierQueueLock p, Barrier b, Transacao t) {
		
		try {
			boolean flag = b.enter(t);
			System.out.println("Entered barrier: " + t.getCodigo());
			if (!flag)
				System.out.println("Error when entering the barrier");
		} catch (KeeperException e) {

		} catch (InterruptedException e) {

		}

		if(p.getClass().equals(Lock.class))
			p.lockTest((Lock) p, t);
		else 
			p.queueTest((Queue) p, "p", t, 0);
		try {
			b.leave();
		} catch (KeeperException e) {

		} catch (InterruptedException e) {

		}
		System.out.println("Left barrier");
	}

	public static void lockTest(Lock lock, Transacao t) {
		
		try {
			boolean success = lock.lock(t);
			if (success) {
				lock.compute(t);
			} else {
				while (true) {
					
				}
			}
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
