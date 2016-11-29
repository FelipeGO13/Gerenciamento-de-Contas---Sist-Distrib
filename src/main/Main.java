package main;

import SyncPrimitive.LeaderElection;

public class Main {

	/*
	 * Ideia Leader Election - Ao rodar o prog, as infos são guardadas em znodes
	 * diferentes, mas só 1 é o líder definindo a leitura e escrita de infos e
	 * sendo responsavel por replicar as infos para os outros znodes. se este
	 * znode "morre" um novo deve assumir a responsabilidade de leitura, escrita
	 * e replicacao
	 */
	public static void main(String[] args) {
		

		String leaderAddress = "localhost";
		
		
		LeaderElection leader = new LeaderElection(leaderAddress);
		leader.leaderElection(leaderAddress);

	
	}

}
