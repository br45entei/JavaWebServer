package com.gmail.br45entei.server.data;

import com.gmail.br45entei.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/** @author Brian_Entei */
public class Credential {
	
	private String	username;
	private String	password;
	
	public Credential() {
		this.username = "";
		this.password = "";
	}
	
	public Credential(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public final String getUsername() {
		return this.username;
	}
	
	public final Credential setUsername(String username) {
		this.username = username;
		return this;
	}
	
	public final String getPassword() {
		return this.password;
	}
	
	public final Credential setPassword(String password) {
		this.password = password;
		return this;
	}
	
	public final boolean hasPassword() {
		return this.password != null && !this.password.trim().isEmpty();
	}
	
	public final boolean matches(Credential other) {
		if(other == null) {
			return false;
		}
		if(other == this) {
			return true;
		}
		if(this.username == null || this.password == null) {
			return false;
		}
		return this.username.equalsIgnoreCase(other.username) && this.password.equalsIgnoreCase(other.password);
	}
	
	public final boolean saveToConfig(ConfigurationSection mem) {
		if(mem == null || this.username == null || this.username.trim().isEmpty()) {
			return false;
		}
		mem.set(this.username, this.password);
		return true;
	}
	
	public static final List<Credential> loadFromConfig(ConfigurationSection mem, String key) {
		if(mem == null || key == null || key.trim().isEmpty()) {
			return null;
		}
		return loadFromConfig(mem.getConfigurationSection(key));
	}
	
	public static final List<Credential> loadFromConfig(ConfigurationSection mem) {
		if(mem == null) {
			return null;
		}
		List<Credential> creds = new ArrayList<>();
		for(String key : mem.getKeys(false)) {
			if(key.trim().isEmpty()) {
				continue;
			}
			Credential cred = new Credential(key, mem.getString(key));
			if(cred.hasPassword()) {
				creds.add(cred);
			}
		}
		return creds;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.password == null) ? 0 : this.password.hashCode());
		result = prime * result + ((this.username == null) ? 0 : this.username.toLowerCase().hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		Credential other = (Credential) obj;
		if(this.password == null) {
			if(other.password != null) return false;
		} else if(!this.password.equals(other.password)) return false;
		if(this.username == null) {
			if(other.username != null) return false;
		} else if(!this.username.equalsIgnoreCase(other.username)) return false;
		return true;
	}
	
}
