package com.gmail.br45entei.server.data;

import java.util.UUID;

/** @author Brian_Entei */
public interface DisposableUUIDData {
	
	/** @return This data's UUID */
	public UUID getUUID();
	
	/** Disposes of this data */
	public void dispose();
	
	/** Deletes this data's associated resources(e.g. files on disk) */
	public void delete();
	
}
