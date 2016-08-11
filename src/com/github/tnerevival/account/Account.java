package com.github.tnerevival.account;

import com.github.tnerevival.TNE;
import com.github.tnerevival.serializable.SerializableItemStack;
import com.github.tnerevival.utils.AccountUtils;

import java.io.Serializable;
import java.util.*;

public class Account implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * A HashMap of this account's balances from every world that the player has visited.
	 */
	private Map<String, Double> balances = new HashMap<>();
	
	/**
	 * A HashMap of this account's banks from every world that the player has visited.
	 */
	private Map<String, Bank> banks = new HashMap<>();
	
	private Map<String, CreditsEntry> credits = new HashMap<>();
	
	private Map<String, Integer> commands = new HashMap<>();
	

	private List<SerializableItemStack> overflow = new ArrayList<SerializableItemStack>();
	
	private String joined;
	
	/**
	 * The account number for this account.
	 * This number is unique to the account.
	 */
	private int accountNumber = 0;
	
	private UUID uid;
	
	/**
	 * The account's balance of in-game virtual currency.
	 */
	private double balance;
	
	/**
	 * The status of this account in String form.
	 */
	//TODO: Make use of account statuses
	private AccountStatus status;
	
	private String pin;
	
	public Account(UUID uid) {
		this(uid, TNE.instance.manager.accounts.size() + 1);
	}
	
	public Account(UUID uid, int accountNumber) {
		this.uid = uid;
		this.joined = new String(TNE.instance.dateFormat.format(new Date()));
		this.accountNumber = accountNumber;
		this.status = AccountStatus.NORMAL;
		this.pin = "TNENOSTRINGVALUE";
		setBalance(TNE.instance.defaultWorld, 0.0);
	}
	
	public String balancesToString() {
		Iterator<Map.Entry<String, Double>> balanceIterator = balances.entrySet().iterator();
		
		int count = 0;
		String toReturn = "";
		while(balanceIterator.hasNext()) {
			Map.Entry<String, Double> balanceEntry = balanceIterator.next();
			if(count > 0) {
				toReturn += ":";
			}
			toReturn += balanceEntry.getKey() + "," + balanceEntry.getValue();
			count++;
		}
		return toReturn;
	}
	
	public void balancesFromString(String from) {
		String[] b = from.split("\\:");
		
		for(String s : b) {
			String[] balance = s.split("\\,");
			balances.put(balance[0], Double.valueOf(balance[1]));
		}
	}

	public String commandsToString() {
		Iterator<Map.Entry<String, Integer>> commandsIterator = commands.entrySet().iterator();
    StringBuilder builder = new StringBuilder();

    while(commandsIterator.hasNext()) {
      Map.Entry<String, Integer> commandEntry = commandsIterator.next();

      if(builder.length() > 0) {
        builder.append(",");
      }
      builder.append(commandEntry.getKey() + "=" + commandEntry.getValue());
    }
    return builder.toString();
	}

	public void commandsFromString(String value) {
	  String[] values = value.split(",");

    for(String s : values) {
      String[] data = s.split("=");
      commands.put(data[0], Integer.valueOf(data[1]));
    }
  }

	public String creditsToString() {
	  Iterator<Map.Entry<String, CreditsEntry>> creditsIterator = credits.entrySet().iterator();
    StringBuilder builder = new StringBuilder();

    while(creditsIterator.hasNext()) {
      Map.Entry<String, CreditsEntry> creditsEntry = creditsIterator.next();
      if(builder.length() > 0) {
        builder.append(",");
      }
      builder.append(creditsEntry.getKey() + "=" + creditsEntry.getValue().toString());
    }

    return builder.toString();
  }

  public void creditsFromString(String value) {
    String[] values = value.split(",");

    for(String s : values) {
      String[] data = s.split("=");
      credits.put(data[0], CreditsEntry.fromString(data[1]));
    }
  }
	
	/*
	 * Inventory Time Credits
	 */
	public Map<String, Long> getTimes(String inventory) {
		if(credits.get(inventory) != null) {
			return credits.get(inventory).getCredits();
		}
		return new HashMap<String, Long>();
	}
	
	public void addTime(String world, String inventory, Long time) {
		setTime(world, inventory, getTimeLeft(world, inventory) + time);
	}
	
	public Long getTimeLeft(String world, String inventory) {
		if(credits.get(inventory) != null) {
			return credits.get(inventory).getRemaining(world);
		}
		return 0L;
	}
	
	public void setTime(String world, String inventory, long time) {
		Map<String, Long> inventoryCredits = (credits.get(inventory) != null) ? credits.get(inventory).getCredits() : new HashMap<String, Long>();
		inventoryCredits.put(world, time);
		credits.put(inventory, new CreditsEntry(inventoryCredits));
	}
	
	/*
	 * Command Credits
	 */
	public void addCredit(String command) {
		if(commands.containsKey(command)) {
			commands.put(command, commands.get(command) + 1);
		}
	}
	
	public void removeCredit(String command) {
		if(commands.containsKey(command)) {
			commands.put(command, commands.get(command) - 1);
		}
	}
	
	public Boolean hasCredit(String command) {
		return (commands.containsKey(command) && commands.get(command) > 0);
	}
	
	public Map<String, Integer> getCredits() {
		return commands;
	}
	
	/*
	 * MISC Methods/Getters & Setters
	 */
	public String overflowToString() {
		if(!overflow.isEmpty()) {
			String toReturn = "";
			
			int count = 0;
			for(SerializableItemStack item : overflow) {
				if(count != 0) {
					toReturn += "*";
				}
				toReturn += item.toString();
				count++;
			}
			return toReturn;
		}
		return "TNENOSTRINGVALUE";
	}

	/**
	 * @return the accountNumber
	 */
	public int getAccountNumber() {
		return accountNumber;
	}

	/**
	 * @param accountNumber the accountNumber to set
	 */
	public void setAccountNumber(int accountNumber) {
		this.accountNumber = accountNumber;
	}

	public UUID getUid() {
		return uid;
	}

	public void setUid(UUID uid) {
		this.uid = uid;
	}

	/**
	 * @return the joined
	 */
	public String getJoined() {
		return joined;
	}

	/**
	 * @param joined the joined to set
	 */
	public void setJoined(String joined) {
		this.joined = joined;
	}

	/**
	 * @return the balance
	 */
	public double getBalance() {
		return balance;
	}

	/**
	 * @return the status
	 */
	public AccountStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = AccountStatus.fromName(status);
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public Map<String, Double> getBalances() {
		return balances;
	}

	public void setBalances(HashMap<String, Double> balances) {
		this.balances = balances;
	}
	
	public Double getBalance(String world) {
		return balances.get(world);
	}
	
	public void setBalance(String world, Double balance) {
		this.balances.put(world, AccountUtils.round(balance));
	}

	public Map<String, Bank> getBanks() {
		return banks;
	}

	public void setBanks(HashMap<String, Bank> banks) {
		this.banks = banks;
	}
	
	public void setBank(String world, Bank bank) {
		this.banks.put(world, bank);
	}
	
	public Bank getBank(String world) {
		return this.banks.get(world);
	}

	public List<SerializableItemStack> getOverflow() {
		return overflow;
	}

	public void setOverflow(List<SerializableItemStack> overflow) {
		this.overflow = overflow;
	}
}